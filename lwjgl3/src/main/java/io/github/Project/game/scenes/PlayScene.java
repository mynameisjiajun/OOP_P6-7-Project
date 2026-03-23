package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.managers.CollisionManager;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.scenes.Scene;

import io.github.Project.game.collisionstrategies.RocketCollisionStrategy;
import io.github.Project.game.collisionstrategies.SatelliteCollisionStrategy;
import io.github.Project.game.collisionstrategies.SpaceStationCollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
import io.github.Project.game.entities.Fuelbar;
import io.github.Project.game.entities.Ground;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.arrow;
import io.github.Project.game.entities.healthbar;
import io.github.Project.game.factory.GameObjectFactory;
import io.github.Project.game.movementstrategy.RocketMovementStrategy;

import java.util.List;

/**
 * Main gameplay scene.
 *
 * Mechanics:
 *  - Fly up into the space zone where 20 debris float freely.
 *  - Debris that has been floating > 20 s turns "hot" and drifts toward
 *    the space station (station warning flashes red/orange).
 *  - Fly the rocket's bowl into debris to attach it (max 3 at once).
 *  - Press E to drop attached debris back into free-float.
 *  - Descend below ATMOSPHERE_THRESHOLD (800 m) with attached debris —
 *    they burn up and are counted as cleared.
 *  - Land on the EarthStation pad to refuel and heal.
 *  - Clear 15 debris to win. Station health reaching 0 = game over.
 *
 * Factory usage: all entity creation goes through GameObjectFactory.
 * LibGDX tools used: MathUtils (random/trig), Array (debris lists),
 *   OrthographicCamera, ShapeRenderer, BitmapFont, GlyphLayout.
 */
public class PlayScene extends Scene {

    // ── Zone boundaries ────────────────────────────────────────────────
    private static final float EARTH_ZONE_END      = 2500f;
    private static final float SPACE_ZONE_START    = 4000f;
    private static final float ATMOSPHERE_THRESHOLD= 800f;
    private static final float CLOUD_BAND_MIN      = 900f;
    private static final float CLOUD_BAND_MAX      = 1800f;

    // ── World geometry ─────────────────────────────────────────────────
    private static final int   TILE_SIZE           = 256;
    private static final float WORLD_HALF_WIDTH    = 1200f;

    // ── Space station placement ────────────────────────────────────────
    private static final float STATION_X  = -150f;
    private static final float STATION_Y  = 4500f;
    private static final float STATION_W  = 300f;
    private static final float STATION_H  = 163f;

    // ── Earth station (landing pad) ────────────────────────────────────
    private static final float EARTH_PAD_W = 150f;

    // ── Debris system ──────────────────────────────────────────────────
    private static final int   MAX_DEBRIS_COUNT    = 20;
    private static final int   MAX_BOWL_CAPACITY   = 3;
    private static final int   WIN_DEBRIS_COUNT    = 15;
    private static final float DEBRIS_SPAWN_INTERVAL = 8f;
    private static final float STATION_DAMAGE      = 0.5f;
    private static final float HOT_DEBRIS_SPEED    = 40f;
    private static final float STATION_WARN_RADIUS = 600f;

    // ── Atmosphere burn effect ─────────────────────────────────────────
    private static final float ATMOSPHERE_BURN_DURATION = 0.6f;
    private static final float BURN_SIZE            = 90f;

    // ── Explosion flash ────────────────────────────────────────────────
    private static final float EXPLOSION_DURATION  = 0.35f;
    private static final float EXPLOSION_SIZE      = 80f;

    // ── Gameplay tuning ────────────────────────────────────────────────
    private static final float DAMAGE_COOLDOWN     = 1.5f;
    private static final float FUEL_DRAIN_RATE     = 0.03f;

    // ── Background colours ─────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.40f, 0.70f, 0.95f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Agency building display sizes [mission, dish, vab, tanks, tower]
    private static final float[] AGENCY_W = { 400f, 100f, 120f, 200f,  60f };
    private static final float[] AGENCY_H = { 200f, 125f, 240f, 160f, 370f };
    private static final int AGENCY_VARIANTS  = 5;
    private static final int AGENCY_INSTANCES = 12;

    // ── Cameras ────────────────────────────────────────────────────────
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Ground / sky textures ──────────────────────────────────────────
    private Texture grassTopTex;
    private Texture dirtTex;

    // ── Space agency buildings ─────────────────────────────────────────
    private Texture[] agencyTextures;   // 5 variants
    private float[]   agencyData;       // [x, typeIdx] pairs × AGENCY_INSTANCES

    // ── Clouds ────────────────────────────────────────────────────────
    private static final int CLOUD_COUNT = 6;
    private Texture[] cloudTextures;
    private float[]   cloudPositions;  // [x, y, texIdx] triples

    // ── Stars ─────────────────────────────────────────────────────────
    private float[] starPositions;     // [x, y, radius] triples

    // ── Effect textures ────────────────────────────────────────────────
    private Texture explosionTex;
    private Texture atmosphereBurnTex;
    private Texture bowlTex;

    // ── Effect timers / positions ──────────────────────────────────────
    private float explosionTimer;
    private float explosionX, explosionY;
    private float atmosphereBurnTimer;
    private float atmosphereBurnX, atmosphereBurnY;

    // ── Game entities (created via factory) ───────────────────────────
    private Rocket       rocket;
    private SpaceStation spaceStation;
    private EarthStation earthStation;

    // ── Debris lists (LibGDX Array for GC-friendly iteration) ─────────
    private Array<Debris> flyingDebris;
    private Array<Debris> attachedDebris;

    // ── Strategies ────────────────────────────────────────────────────
    private RocketCollisionStrategy rocketStrategy;
    private RocketMovementStrategy  rocketMovementStrategy;

    // ── HUD elements ──────────────────────────────────────────────────
    private healthbar  rocketHpBar;
    private Fuelbar    fuelBar;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private arrow      navArrow;

    // ── Station warning flash ──────────────────────────────────────────
    private boolean stationWarningActive = false;
    private float   stationWarningPulse  = 0f;

    // ── Game state ─────────────────────────────────────────────────────
    private float   damageCooldownTimer = DAMAGE_COOLDOWN;
    private float   debrisSpawnTimer    = DEBRIS_SPAWN_INTERVAL;
    private int     debrisCollected     = 0;
    private boolean gameWon             = false;
    private boolean gameOver            = false;
    private float   fuel                = 100f;

    // ── Factory ────────────────────────────────────────────────────────
    private GameObjectFactory factory;

    // ── Collision listener ─────────────────────────────────────────────
    private CollisionManager.CollisionListener collisionListener;

    // ── Reusable colour ────────────────────────────────────────────────
    private final Color tempColor = new Color();

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ──────────────────────────────────────────────────────────────────
    //  SHOW
    // ──────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        // Pause / resume — just un-pause, keep all state intact
        if (gameCamera != null) {
            isPaused = false;
            return;
        }

        // ── One-time setup ─────────────────────────────────────────────
        float viewW = Gdx.graphics.getWidth();
        float viewH = Gdx.graphics.getHeight();

        // Factory (all entity creation goes through here)
        factory = new GameObjectFactory(gameMaster.getInputMovement());

        // Cameras
        gameCamera = new OrthographicCamera(viewW, viewH);
        hudCamera  = new OrthographicCamera(viewW, viewH);
        hudCamera.position.set(viewW / 2f, viewH / 2f, 0);
        hudCamera.update();

        // Ground textures
        grassTopTex = new Texture(Gdx.files.internal("Dirt texture/Dirt and grass.png"));
        dirtTex     = new Texture(Gdx.files.internal("Dirt texture/Dirt.png"));

        // Space agency buildings (replace skyscrapers)
        agencyTextures = new Texture[AGENCY_VARIANTS];
        agencyTextures[0] = new Texture(Gdx.files.internal("Ground Assets/mission_control.png"));
        agencyTextures[1] = new Texture(Gdx.files.internal("Ground Assets/satellite_dish.png"));
        agencyTextures[2] = new Texture(Gdx.files.internal("Ground Assets/vab_building.png"));
        agencyTextures[3] = new Texture(Gdx.files.internal("Ground Assets/fuel_tanks.png"));
        agencyTextures[4] = new Texture(Gdx.files.internal("Ground Assets/launch_tower.png"));

        // Seeded deterministic layout: one of each type, then random fill
        agencyData = new float[AGENCY_INSTANCES * 2];
        float[] fixedX = { -900f, -600f, -300f, 400f, 750f };
        for (int i = 0; i < 5; i++) {
            agencyData[i * 2]     = fixedX[i];
            agencyData[i * 2 + 1] = i;
        }
        // Use seeded MathUtils-style via a fixed seed offset
        long seed = 77777L;
        for (int i = 5; i < AGENCY_INSTANCES; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            float x = ((seed >> 17) & 0xFFF) / (float)0xFFF * 2200f - 1100f;
            if (Math.abs(x) < 250f) x += (x < 0 ? -300f : 300f);
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int type = (int)(((seed >> 17) & 0xFF) % AGENCY_VARIANTS);
            agencyData[i * 2]     = x;
            agencyData[i * 2 + 1] = type;
        }

        // Clouds
        cloudTextures = new Texture[CLOUD_COUNT];
        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudTextures[i] = new Texture(
                Gdx.files.internal("tiles/pixelart_clouds/x5/cloud" + (i + 1) + ".png"));
        }
        int numClouds = 30;
        cloudPositions = new float[numClouds * 3];
        long cs = 12345L;
        for (int i = 0; i < numClouds; i++) {
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3]   = ((cs>>17)&0x1FFF)/(float)0x1FFF*3600f-1800f;
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3+1] = CLOUD_BAND_MIN + ((cs>>17)&0xFFF)/(float)0xFFF*(CLOUD_BAND_MAX-CLOUD_BAND_MIN);
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3+2] = (int)(((cs>>17)&0xFF) % CLOUD_COUNT);
        }

        // Stars
        int numStars = 400;
        starPositions = new float[numStars * 3];
        long ss = 99887L;
        for (int i = 0; i < numStars; i++) {
            ss = ss * 6364136223846793005L + 1; starPositions[i*3]   = ((ss>>17)&0x1FFF)/(float)0x1FFF*6000f-3000f;
            ss = ss * 6364136223846793005L + 1; starPositions[i*3+1] = EARTH_ZONE_END + ((ss>>17)&0x3FFF)/(float)0x3FFF*8000f;
            ss = ss * 6364136223846793005L + 1; starPositions[i*3+2] = 0.5f + ((ss>>17)&0xFF)/(float)0xFF*2f;
        }

        // Effect textures
        explosionTex      = new Texture(Gdx.files.internal("Explosion_01.png"));
        atmosphereBurnTex = new Texture(Gdx.files.internal("New space assets/Atomsphere Burn.png"));
        bowlTex           = new Texture(Gdx.files.internal("New space assets/Bowl.png"));

        // ── Entities (via factory) ─────────────────────────────────────
        spaceStation = factory.createSpaceStation(STATION_X, STATION_Y);
        addSceneEntity(spaceStation);

        earthStation = factory.createEarthStation(-EARTH_PAD_W / 2f, -20f);
        addSceneEntity(earthStation);

        rocket = factory.createRocket(0f, 0f);
        addSceneEntity(rocket);
        rocketMovementStrategy = new RocketMovementStrategy();
        gameMaster.getMovementManager().registerEntity(rocket, rocketMovementStrategy);

        Ground ground = factory.createGround(-600f, -50f, 1200f);
        addSceneEntity(ground);

        navArrow = factory.createArrow(rocket, spaceStation);

        // ── Debris lists ───────────────────────────────────────────────
        flyingDebris   = new Array<>();
        attachedDebris = new Array<>();
        spawnInitialDebris();

        // ── HUD ────────────────────────────────────────────────────────
        rocketHpBar = factory.createHealthBar(10f, viewH - 30f);
        fuelBar     = factory.createFuelBar(10f, viewH - 55f);
        font        = new BitmapFont();
        glyphLayout = new GlyphLayout();

        // ── Collision setup ────────────────────────────────────────────
        gameMaster.getCollisionManager().clearListeners();
        collisionListener = this::handleCollision;
        gameMaster.getCollisionManager().addListener(collisionListener);
        setupCollisionStrategies();

        Gdx.input.setInputProcessor(gameMaster.getInputMovement());
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
    }

    // ──────────────────────────────────────────────────────────────────
    //  COLLISION STRATEGY WIRING
    // ──────────────────────────────────────────────────────────────────
    private void setupCollisionStrategies() {
        DamageCalculator rocketDmg  = factory.createDamageCalculator("Rocket");
        DamageCalculator stationDmg = factory.createDamageCalculator("SpaceStation");

        // Rocket strategy
        rocketStrategy = new RocketCollisionStrategy(rocket, rocketDmg);

        rocketStrategy.setLandingCallback(new RocketCollisionStrategy.LandingCallback() {
            @Override public void onSafeLanding() {
                rocketHpBar.setHP(rocket.getHealthPercentage());
                fuelBar.setFuel(rocket.getFuelPercentage());
            }
            @Override public void onCrashLanding(float speed, float angle) {
                triggerExplosion(rocket.getPosX() + rocket.getWidth() / 2f,
                                 rocket.getPosY() + rocket.getHeight() / 2f);
                rocketHpBar.setHP(rocket.getHealthPercentage());
                fuelBar.setFuel(rocket.getFuelPercentage());
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
            }
        });

        rocketStrategy.setRefuelCallback(() -> {
            fuel = 100f;
            fuelBar.setFuel(1f);
            rocketHpBar.setHP(rocket.getHealthPercentage());
            gameMaster.getIoManager().playRefuelEffect();
        });

        rocket.setCollisionStrategy(rocketStrategy);

        // Space Station strategy
        SpaceStationCollisionStrategy stationStrategy =
            new SpaceStationCollisionStrategy(spaceStation, stationDmg);

        stationStrategy.setEventCallback(new SpaceStationCollisionStrategy.StationEventCallback() {
            @Override public void onStationDamaged(float dmg, float pct) {
                gameMaster.getIoManager().playCollisionEffect();
            }
            @Override public void onStationDestroyed() {
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
            }
            @Override public void onRocketDocked() { /* no win sound on docking */ }
        });

        spaceStation.setCollisionStrategy(stationStrategy);
    }

    // ──────────────────────────────────────────────────────────────────
    //  RENDER
    // ──────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        gameMaster.getIoManager().update();
        if (gameCamera == null) return;
        if (isPaused) { drawWorld(0f); drawHUD(); return; }

        // ── Remove destroyed flying debris ────────────────────────────
        for (int i = flyingDebris.size - 1; i >= 0; i--) {
            Debris d = flyingDebris.get(i);
            if (d.isDestroyed()) {
                flyingDebris.removeIndex(i);
                removeSceneEntity(d);
                d.dispose();
            }
        }

        if (!gameWon && !gameOver) {
            gameMaster.getMovementManager().updateMovements(delta);
            rocketStrategy.update(delta);

            // Horizontal boundary clamp
            float left  = -WORLD_HALF_WIDTH;
            float right =  WORLD_HALF_WIDTH - rocket.getWidth();
            if (rocket.getPosX() < left)  { rocket.setPosX(left);  rocket.setVx(0f); }
            if (rocket.getPosX() > right) { rocket.setPosX(right); rocket.setVx(0f); }

            // ── Update flying debris + hot-debris steering ─────────────
            float sCX = STATION_X + STATION_W / 2f;
            float sCY = STATION_Y + STATION_H / 2f;
            stationWarningActive = false;

            for (int i = 0; i < flyingDebris.size; i++) {
                Debris d = flyingDebris.get(i);
                d.update(delta);
                if (d.isHot()) {
                    float dx   = sCX - (d.getPosX() + d.getWidth()  / 2f);
                    float dy   = sCY - (d.getPosY() + d.getHeight() / 2f);
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > 1f) {
                        d.setVx(dx / dist * HOT_DEBRIS_SPEED);
                        d.setVy(dy / dist * HOT_DEBRIS_SPEED);
                    }
                    if (dist < STATION_WARN_RADIUS) stationWarningActive = true;
                }
            }

            // Station warning pulse
            stationWarningPulse = stationWarningActive
                ? stationWarningPulse + delta * 6f
                : 0f;

            // ── Attached debris — follow rocket nose ───────────────────
            float rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
            float rCY = rocket.getPosY() + rocket.getHeight() / 2f;
            float rot = rocket.getRotation();
            for (int i = 0; i < attachedDebris.size; i++) {
                Debris d   = attachedDebris.get(i);
                float off  = rocket.getHeight() / 2f + 12f + i * 10f;
                d.setPosX(rCX + MathUtils.cosDeg(rot) * off - d.getWidth()  / 2f);
                d.setPosY(rCY + MathUtils.sinDeg(rot) * off - d.getHeight() / 2f);
            }

            // ── Atmosphere burn: attached debris burns up below threshold
            if (!attachedDebris.isEmpty() && rocket.getPosY() < ATMOSPHERE_THRESHOLD) {
                atmosphereBurnX = rCX;
                atmosphereBurnY = rCY + rocket.getHeight() / 2f;
                atmosphereBurnTimer = ATMOSPHERE_BURN_DURATION;
                for (int i = 0; i < attachedDebris.size; i++) {
                    attachedDebris.get(i).dispose();
                    debrisCollected++;
                }
                attachedDebris.clear();
                gameMaster.getIoManager().playCollisionEffect();
                checkWinCondition();
            }

            // ── E key: manually drop bowl ──────────────────────────────
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !attachedDebris.isEmpty()) {
                for (int i = 0; i < attachedDebris.size; i++) {
                    Debris d = attachedDebris.get(i);
                    d.setAttached(false);
                    d.setVx(MathUtils.random(-40f, 40f));
                    d.setVy(MathUtils.random(-30f, 10f));
                    flyingDebris.add(d);
                    addSceneEntity(d);
                }
                attachedDebris.clear();
            }

            gameMaster.getCollisionManager().checkCollisions(
                gameMaster.getEntityManager().getEntities());

            // Timers
            if (damageCooldownTimer  > 0f) damageCooldownTimer  -= delta;
            if (explosionTimer       > 0f) explosionTimer       -= delta;
            if (atmosphereBurnTimer  > 0f) atmosphereBurnTimer  -= delta;

            // ── Fuel drain ─────────────────────────────────────────────
            if (gameMaster.getInputMovement().keyUp && fuel > 0f) {
                fuel = Math.max(0f, fuel - FUEL_DRAIN_RATE * delta);
                fuelBar.setFuel(fuel / 100f);
            }
            if (fuel <= 0f && !gameWon) {
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
                gameMaster.getIoManager().playGameOverSound();
            }

            // ── Top up debris count ────────────────────────────────────
            debrisSpawnTimer -= delta;
            int total = flyingDebris.size + attachedDebris.size;
            if (debrisSpawnTimer <= 0f || total < MAX_DEBRIS_COUNT / 2) {
                debrisSpawnTimer = DEBRIS_SPAWN_INTERVAL;
                int need = MAX_DEBRIS_COUNT - total;
                for (int i = 0; i < need; i++) spawnOneDebris();
            }

            // ── Navigation arrow → nearest debris ─────────────────────
            Entity target  = spaceStation;
            float  minDist = Float.MAX_VALUE;
            for (int i = 0; i < flyingDebris.size; i++) {
                Debris d  = flyingDebris.get(i);
                float  dx = (d.getPosX() + d.getWidth()  / 2f) - rCX;
                float  dy = (d.getPosY() + d.getHeight() / 2f) - rCY;
                float  sq = dx * dx + dy * dy;
                if (sq < minDist) { minDist = sq; target = d; }
            }
            navArrow.setTarget(target);

        } else {
            if (gameOver && !gameMaster.getAudioManager().isGameOverSoundPlaying()) {
                gameMaster.getIoManager().playGameOverSound();
            }
        }

        drawWorld(delta);
        drawHUD();
    }

    // ──────────────────────────────────────────────────────────────────
    //  WORLD RENDERING
    // ──────────────────────────────────────────────────────────────────
    private void drawWorld(float delta) {
        gameCamera.position.set(
            rocket.getPosX() + rocket.getWidth()  / 2f,
            rocket.getPosY() + rocket.getHeight() / 2f, 0f);
        gameCamera.update();

        float camX  = gameCamera.position.x;
        float camY  = gameCamera.position.y;
        float halfW = gameCamera.viewportWidth  / 2f;
        float halfH = gameCamera.viewportHeight / 2f;

        // Background clear colour (sky ↔ space gradient)
        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Stars (ShapeRenderer pass)
        float starAlpha = camY >= SPACE_ZONE_START ? 1f
            : camY > EARTH_ZONE_END ? (camY - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END)
            : 0f;
        if (starAlpha > 0f) {
            ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
            sr.setProjectionMatrix(gameCamera.combined);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < starPositions.length; i += 3) {
                float sx = starPositions[i], sy = starPositions[i + 1];
                if (sy < camY - halfH || sy > camY + halfH) continue;
                if (sx < camX - halfW || sx > camX + halfW) continue;
                sr.setColor(1f, 1f, 1f, starAlpha);
                sr.circle(sx, sy, starPositions[i + 2]);
            }
            sr.end();
        }

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        drawBackground(batch, camX, camY, halfW, halfH);

        // Space station
        spaceStation.render(batch, null);

        // Earth station (landing pad)
        earthStation.render(batch, null);

        // Flying debris
        for (int i = 0; i < flyingDebris.size; i++) flyingDebris.get(i).render(batch, null);

        // Attached debris (at bowl)
        for (int i = 0; i < attachedDebris.size; i++) attachedDebris.get(i).render(batch, null);

        // Rocket
        rocket.render(batch, null);

        // Bowl texture at rocket nose
        if (bowlTex != null) {
            float rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
            float rCY = rocket.getPosY() + rocket.getHeight() / 2f;
            float rot = rocket.getRotation();
            float noseX = rCX + MathUtils.cosDeg(rot) * (rocket.getHeight() / 2f);
            float noseY = rCY + MathUtils.sinDeg(rot) * (rocket.getHeight() / 2f);
            float bw = 48f, bh = 24f;
            batch.draw(bowlTex,
                noseX - bw / 2f, noseY - bh / 2f,
                bw / 2f, bh / 2f, bw, bh, 1f, 1f,
                rot - 90f,
                0, 0, bowlTex.getWidth(), bowlTex.getHeight(),
                false, false);
        }

        // Explosion flash
        if (explosionTimer > 0f) {
            batch.setColor(1f, 1f, 1f, explosionTimer / EXPLOSION_DURATION);
            batch.draw(explosionTex,
                explosionX - EXPLOSION_SIZE / 2f, explosionY - EXPLOSION_SIZE / 2f,
                EXPLOSION_SIZE, EXPLOSION_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // Atmosphere burn effect
        if (atmosphereBurnTimer > 0f && atmosphereBurnTex != null) {
            float alpha = atmosphereBurnTimer / ATMOSPHERE_BURN_DURATION;
            batch.setColor(1f, 0.55f + 0.45f * alpha, 0.1f * alpha, alpha);
            batch.draw(atmosphereBurnTex,
                atmosphereBurnX - BURN_SIZE / 2f, atmosphereBurnY - BURN_SIZE / 2f,
                BURN_SIZE, BURN_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();

        // Navigation arrow
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Line);
        navArrow.update(delta);
        navArrow.render(null, sr);
        sr.end();
    }

    // ──────────────────────────────────────────────────────────────────
    //  BACKGROUND LAYERS
    // ──────────────────────────────────────────────────────────────────
    private void drawBackground(SpriteBatch batch, float camX, float camY,
                                float halfW, float halfH) {
        float left  = camX - halfW;
        float right = camX + halfW;
        float bot   = camY - halfH;

        // Ground tiles
        int txS = MathUtils.floor(left  / TILE_SIZE);
        int txE = MathUtils.floor(right / TILE_SIZE);
        int tyS = MathUtils.floor(bot   / TILE_SIZE);
        for (int ty = tyS; ty <= -1; ty++) {
            float wy = ty * (float) TILE_SIZE;
            if (wy + TILE_SIZE > 0f) continue;
            Texture tex = (ty == -1) ? grassTopTex : dirtTex;
            for (int tx = txS; tx <= txE; tx++)
                batch.draw(tex, tx * (float) TILE_SIZE, wy, TILE_SIZE, TILE_SIZE);
        }

        // Space agency buildings
        float bldTop = CLOUD_BAND_MAX + 400f;
        if (camY - halfH < bldTop) {
            for (int i = 0; i < AGENCY_INSTANCES; i++) {
                float bx  = agencyData[i * 2];
                int   idx = (int) agencyData[i * 2 + 1];
                float bw  = AGENCY_W[idx];
                float bh  = AGENCY_H[idx];
                if (bx + bw < left || bx > right || bh < bot) continue;
                batch.draw(agencyTextures[idx], bx, 0f, bw, bh);
            }
        }

        // Clouds
        if (camY + halfH > CLOUD_BAND_MIN && camY - halfH < CLOUD_BAND_MAX + 200f) {
            for (int i = 0; i < cloudPositions.length; i += 3) {
                float cx = cloudPositions[i], cy = cloudPositions[i + 1];
                int  idx = (int) cloudPositions[i + 2];
                Texture c = cloudTextures[idx];
                if (cx + c.getWidth() < left || cx > right) continue;
                batch.draw(c, cx, cy, c.getWidth(), c.getHeight());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  HUD RENDERING
    // ──────────────────────────────────────────────────────────────────
    private void drawHUD() {
        float vW = hudCamera.viewportWidth;
        float vH = hudCamera.viewportHeight;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Left panel background
        sr.setColor(0f, 0f, 0f, 0.50f);
        sr.rect(0f, vH - 100f, 200f, 100f);
        rocketHpBar.render(null, sr);
        fuelBar.render(null, sr);

        // ── Large centered station health bar ─────────────────────────
        float barW = 300f;
        float barX = (vW - barW) / 2f;
        float barY = vH - 28f;

        // Panel background
        sr.setColor(0f, 0f, 0f, 0.60f);
        sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);

        // Warning flash overlay
        if (stationWarningActive) {
            float pulse = (MathUtils.sin(stationWarningPulse) + 1f) / 2f;
            sr.setColor(1f, 0.2f, 0f, 0.4f * pulse);
            sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);
        }

        // Station health fill (green → yellow → red)
        float hp = spaceStation.getHealthPercentage();
        sr.setColor(Color.DARK_GRAY);
        sr.rect(barX, barY, barW, 20f);
        sr.setColor(hp > 0.6f ? Color.GREEN : hp > 0.3f ? Color.YELLOW : Color.RED);
        sr.rect(barX, barY, barW * hp, 20f);

        sr.end();

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP",   165f, vH - 13f);
        font.draw(batch, "FUEL", 165f, vH - 38f);

        // Station label
        font.getData().setScale(0.75f);
        String stLabel = stationWarningActive ? "!! SPACE STATION UNDER THREAT !!" : "SPACE STATION HEALTH";
        if (stationWarningActive) font.setColor(1f, 0.35f, 0f, 1f); else font.setColor(Color.WHITE);
        glyphLayout.setText(font, stLabel);
        font.draw(batch, stLabel, (vW - glyphLayout.width) / 2f, vH - 31f);
        font.setColor(Color.WHITE);

        // Bottom-left stats
        font.getData().setScale(0.8f);
        font.draw(batch, "ALT: "     + (int) rocket.getPosY() + " m",          10f, vH - 65f);
        font.draw(batch, "CLEARED: " + debrisCollected + "/" + WIN_DEBRIS_COUNT, 10f, vH - 80f);

        // Bowl indicator
        int bowlCount = attachedDebris.size;
        font.setColor(bowlCount > 0 ? Color.CYAN : Color.LIGHT_GRAY);
        font.draw(batch, "BOWL: " + bowlCount + "/" + MAX_BOWL_CAPACITY, 10f, vH - 95f);
        font.setColor(Color.WHITE);

        // Control hint at bottom
        font.getData().setScale(0.65f);
        font.setColor(0.75f, 0.75f, 0.75f, 1f);
        font.draw(batch, "[E] drop debris    [land on pad] refuel", 10f, 16f);
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        // Win / game-over overlays
        if (gameWon)  drawCenteredMessage(batch, vW, vH, "YOU WIN!",  Color.GREEN);
        if (gameOver) drawCenteredMessage(batch, vW, vH, "GAME OVER", Color.RED);

        batch.end();
    }

    private void drawCenteredMessage(SpriteBatch batch, float vW, float vH,
                                     String msg, Color color) {
        font.getData().setScale(3f);
        font.setColor(color);
        glyphLayout.setText(font, msg);
        font.draw(batch, msg, (vW - glyphLayout.width) / 2f, vH / 2f + glyphLayout.height / 2f);
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        glyphLayout.setText(font, "Press R to restart");
        font.draw(batch, "Press R to restart",
            (vW - glyphLayout.width) / 2f, vH / 2f - 30f);
    }

    // ──────────────────────────────────────────────────────────────────
    //  ZONE COLOUR (sky → space gradient)
    // ──────────────────────────────────────────────────────────────────
    private void getZoneColor(float alt, Color out) {
        if (alt <= EARTH_ZONE_END) {
            out.set(COLOR_SKY);
        } else if (alt >= SPACE_ZONE_START) {
            out.set(COLOR_SPACE);
        } else {
            float t = (alt - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
            out.r = MathUtils.lerp(COLOR_SKY.r, COLOR_SPACE.r, t);
            out.g = MathUtils.lerp(COLOR_SKY.g, COLOR_SPACE.g, t);
            out.b = MathUtils.lerp(COLOR_SKY.b, COLOR_SPACE.b, t);
            out.a = 1f;
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  COLLISION HANDLING
    // ──────────────────────────────────────────────────────────────────
    private void handleCollision(CollisionManager.CollisionInfo info) {
        if (gameWon || gameOver) return;

        CollidableEntity e1 = (CollidableEntity) info.entity1;
        CollidableEntity e2 = (CollidableEntity) info.entity2;

        // Delegate to entity strategies
        if (e1 instanceof Rocket)       ((Rocket) e1).onCollision(e2);
        if (e2 instanceof Rocket)       ((Rocket) e2).onCollision(e1);
        if (e1 instanceof SpaceStation) ((SpaceStation) e1).onCollision(e2);
        if (e2 instanceof SpaceStation) ((SpaceStation) e2).onCollision(e1);
        if (e1 instanceof Satellite)    ((Satellite) e1).onCollision(e2);
        if (e2 instanceof Satellite)    ((Satellite) e2).onCollision(e1);

        // Bowl catches debris — attach up to MAX_BOWL_CAPACITY
        if (info.isBetween("Rocket", "Debris")) {
            Debris d = (Debris)(info.tag1.equals("Debris") ? e1 : e2);
            if (!d.isDestroyed() && !d.isAttached() && attachedDebris.size < MAX_BOWL_CAPACITY) {
                d.setAttached(true);
                flyingDebris.removeValue(d, true);
                removeSceneEntity(d);
                attachedDebris.add(d);
                gameMaster.getIoManager().playCollisionEffect();
            }
        }

        // Debris hits station
        if (info.isBetween("Debris", "SpaceStation")) {
            Debris d = (Debris)(info.tag1.equals("Debris") ? e1 : e2);
            if (!d.isDestroyed()) {
                d.setDestroyed(true);
                spaceStation.takeDamage(STATION_DAMAGE);
                if (!spaceStation.isAlive()) {
                    gameOver = true;
                    gameMaster.getAudioManager().stopRocketLoop();
                }
                gameMaster.getIoManager().playCollisionEffect();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  SPAWNING  (via GameObjectFactory / DebrisFactory)
    // ──────────────────────────────────────────────────────────────────
    private void spawnInitialDebris() {
        for (int i = 0; i < MAX_DEBRIS_COUNT; i++) spawnOneDebris();
    }

    private void spawnOneDebris() {
        float x = MathUtils.random(-900f, 900f);
        float y = SPACE_ZONE_START + 200f + MathUtils.random(0f, 3000f);
        Debris d = factory.createSpaceDebris(x, y);
        flyingDebris.add(d);
        addSceneEntity(d);
    }

    /** Wires collision strategy onto a satellite (used if satellites are added). */
    private void setupSatelliteStrategy(Satellite satellite) {
        SatelliteCollisionStrategy strategy = new SatelliteCollisionStrategy(
            satellite,
            factory.createDamageCalculator("Satellite"),
            factory.getDebrisFactory()
        );
        strategy.setDebrisSpawnCallback((List<Debris> spawned) -> {
            for (Debris d : spawned) { flyingDebris.add(d); addSceneEntity(d); }
        });
        satellite.setCollisionStrategy(strategy);
    }

    // ──────────────────────────────────────────────────────────────────
    //  HELPERS
    // ──────────────────────────────────────────────────────────────────
    private void triggerExplosion(float x, float y) {
        explosionX     = x;
        explosionY     = y;
        explosionTimer = EXPLOSION_DURATION;
        gameMaster.getIoManager().playCollisionEffect();
    }

    private void checkWinCondition() {
        if (debrisCollected >= WIN_DEBRIS_COUNT && !gameWon) {
            gameWon = true;
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getIoManager().playWinEffect();
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ──────────────────────────────────────────────────────────────────
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (gameCamera != null) {
            gameCamera.viewportWidth  = width;
            gameCamera.viewportHeight = height;
            gameCamera.update();
        }
        if (hudCamera != null) {
            hudCamera.viewportWidth  = width;
            hudCamera.viewportHeight = height;
            hudCamera.position.set(width / 2f, height / 2f, 0f);
            hudCamera.update();
        }
    }

    @Override
    public void hide() {
        if (!isPaused) {
            clearSceneEntities();
            gameMaster.getCollisionManager().clearListeners();
            disposeAll();
            gameCamera = null;
            hudCamera  = null;
        }
    }

    @Override
    public void dispose() {
        clearSceneEntities();
        gameMaster.getCollisionManager().clearListeners();
        disposeAll();
    }

    private void disposeAll() {
        if (grassTopTex      != null) { grassTopTex.dispose();       grassTopTex      = null; }
        if (dirtTex          != null) { dirtTex.dispose();           dirtTex          = null; }
        if (explosionTex     != null) { explosionTex.dispose();      explosionTex     = null; }
        if (atmosphereBurnTex!= null) { atmosphereBurnTex.dispose(); atmosphereBurnTex= null; }
        if (bowlTex          != null) { bowlTex.dispose();           bowlTex          = null; }
        if (font             != null) { font.dispose();              font             = null; }
        if (agencyTextures   != null) {
            for (Texture t : agencyTextures) if (t != null) t.dispose();
            agencyTextures = null;
        }
        if (cloudTextures    != null) {
            for (Texture t : cloudTextures) if (t != null) t.dispose();
            cloudTextures    = null;
        }
        // Dispose any remaining debris
        if (flyingDebris != null) {
            for (int i = 0; i < flyingDebris.size; i++) flyingDebris.get(i).dispose();
            flyingDebris.clear();
        }
        if (attachedDebris != null) {
            for (int i = 0; i < attachedDebris.size; i++) attachedDebris.get(i).dispose();
            attachedDebris.clear();
        }
    }
}
