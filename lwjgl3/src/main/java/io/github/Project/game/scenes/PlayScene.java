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
 *  - Debris that has been floating > 28 s turns "hot" and drifts toward
 *    the space station (station warning flashes red/orange).
 *  - Fly the rocket's bowl into debris to attach it (max 4 at once).
 *  - Press E to launch attached debris in the rocket's facing direction.
 *  - Descend below ATMOSPHERE_THRESHOLD (1400 m) with attached debris —
 *    they burn up and are counted as cleared.
 *  - Land on the EarthStation pad to refuel and heal.
 *  - Clear 10 points of debris to win. Station health reaching 0 = game over.
 *
 * Factory usage: all entity creation goes through GameObjectFactory.
 * LibGDX tools used: MathUtils (random/trig), Array (debris lists),
 *   OrthographicCamera, ShapeRenderer, BitmapFont, GlyphLayout.
 */
public class PlayScene extends Scene {

    // ── Zone boundaries ────────────────────────────────────────────────
    private static final float EARTH_ZONE_END      = 2500f;
    private static final float SPACE_ZONE_START    = 4000f;
    private static final float ATMOSPHERE_THRESHOLD= 1400f;
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
    private static final int   MAX_BOWL_CAPACITY   = 4;
    private static final int   WIN_CLEAR_SCORE     = 10;
    private static final float DEBRIS_SPAWN_INTERVAL = 10f;
    private static final float STATION_DAMAGE      = 0.35f;
    private static final float HOT_DEBRIS_SPEED    = 30f;
    private static final float STATION_WARN_RADIUS = 600f;
    private static final float DEBRIS_SPAWN_MIN_Y_OFFSET = 700f;
    private static final float DEBRIS_SPAWN_MAX_Y_OFFSET = 3700f;
    private static final float DEBRIS_MIN_STATION_SPAWN_DISTANCE = 450f;

    // ── Atmosphere burn effect ─────────────────────────────────────────
    private static final float ATMOSPHERE_BURN_DURATION = 0.6f;
    private static final float BURN_SIZE            = 90f;

    // ── Explosion flash ────────────────────────────────────────────────
    private static final float EXPLOSION_DURATION  = 0.35f;
    private static final float EXPLOSION_SIZE      = 80f;
    private static final float STATION_HIT_FX_DURATION = 0.22f;
    private static final float STATION_HIT_FX_SIZE_MIN = 28f;
    private static final float STATION_HIT_FX_SIZE_MAX = 46f;
    private static final float STATION_SHAKE_DURATION  = 0.35f;
    private static final float STATION_SHAKE_AMPLITUDE = 14f;

    // ── Gameplay tuning ────────────────────────────────────────────────
    private static final float DAMAGE_COOLDOWN     = 1.5f;
    private static final float FUEL_DRAIN_RATE     = 0.02f;
    private static final float BOWL_DRAW_W         = 68f;
    private static final float BOWL_DRAW_H         = 34f;
    private static final float BOWL_CAPTURE_RADIUS = 60f;
    private static final float BOWL_RELEASE_SPEED  = 240f;
    private static final float BOWL_RELEASE_SPREAD = 24f;
    private static final float BOWL_RELEASE_FORWARD_OFFSET = 30f;
    private static final float BOWL_RELEASE_RECAPTURE_DELAY = 0.8f;

    // ── Background colours ─────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.40f, 0.70f, 0.95f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Agency building display sizes [mission, dish, vab, tanks, tower]
    private static final float[] AGENCY_W = { 400f, 100f, 120f, 200f,  60f };
    private static final float[] AGENCY_H = { 200f, 125f, 240f, 160f, 370f };
    private static final float[] AGENCY_BASE_Y = { 0f, 0f, 0f, -22f, 0f };
    private static final int AGENCY_VARIANTS  = 5;
    private static final int AGENCY_INSTANCES = 10;
    private static final float AGENCY_SPAWN_MIN_X     = -1700f;
    private static final float AGENCY_SPAWN_MAX_X     =  1700f;
    private static final float AGENCY_CENTER_EXCLUSION =  320f;
    private static final float AGENCY_MIN_SPACING      =   45f;
    private static final long  AGENCY_LAYOUT_SEED      = 77777L;

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
    private float stationShakeTimer;
    private Array<StationHitFx> stationHitFxList;

    private static class StationHitFx {
        float x, y, size, timer;
        StationHitFx(float x, float y, float size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.timer = STATION_HIT_FX_DURATION;
        }
    }

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

        // Seeded deterministic layout: one of each type, then spaced random fill
        agencyData = new float[AGENCY_INSTANCES * 2];
        float[] fixedX = { -1300f, -900f, -450f, 450f, 900f };
        for (int i = 0; i < AGENCY_VARIANTS; i++) {
            agencyData[i * 2]     = fixedX[i];
            agencyData[i * 2 + 1] = i;
        }
        long seed = AGENCY_LAYOUT_SEED;
        for (int i = AGENCY_VARIANTS; i < AGENCY_INSTANCES; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int type = (int)(((seed >> 17) & 0xFF) % AGENCY_VARIANTS);

            float x = 0f;
            boolean placed = false;
            for (int attempt = 0; attempt < 32; attempt++) {
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                x = ((seed >> 17) & 0xFFF) / (float) 0xFFF
                    * (AGENCY_SPAWN_MAX_X - AGENCY_SPAWN_MIN_X)
                    + AGENCY_SPAWN_MIN_X;
                if (Math.abs(x) < AGENCY_CENTER_EXCLUSION) {
                    x += (x < 0f ? -AGENCY_CENTER_EXCLUSION : AGENCY_CENTER_EXCLUSION);
                }
                if (!isAgencySpawnTooClose(i, x, type)) {
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                for (float candidate = AGENCY_SPAWN_MIN_X; candidate <= AGENCY_SPAWN_MAX_X; candidate += 25f) {
                    if (Math.abs(candidate) < AGENCY_CENTER_EXCLUSION) continue;
                    if (!isAgencySpawnTooClose(i, candidate, type)) {
                        x = candidate;
                        placed = true;
                        break;
                    }
                }
            }
            if (!placed) {
                // Keep impossible placements out of the main play area.
                x = AGENCY_SPAWN_MAX_X + (i - AGENCY_VARIANTS + 1) * 260f;
            }

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
        stationHitFxList = new Array<>();
        stationShakeTimer = 0f;
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

        updateStationImpactEffects(delta);

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
                if (d.isReentryCandidate() && d.getPosY() < ATMOSPHERE_THRESHOLD) {
                    atmosphereBurnX = d.getPosX() + d.getWidth() / 2f;
                    atmosphereBurnY = d.getPosY() + d.getHeight() / 2f;
                    atmosphereBurnTimer = ATMOSPHERE_BURN_DURATION;
                    flyingDebris.removeIndex(i);
                    removeSceneEntity(d);
                    d.dispose();
                    debrisCollected += d.getClearScore();
                    checkWinCondition();
                    i--;
                    continue;
                }
                // Debris launched by player for re-entry should not get re-attracted
                // toward the station.
                if (!d.isReentryCandidate() && d.isHot()) {
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
            tryAutoCatchDebrisAtBowl(rCX, rCY, rot);

            // ── Atmosphere burn: attached debris burns up below threshold
            if (!attachedDebris.isEmpty() && rocket.getPosY() < ATMOSPHERE_THRESHOLD) {
                clearAttachedDebrisAtAtmosphere(rCX, rCY + rocket.getHeight() / 2f);
            }

            // ── E key: manually drop bowl ──────────────────────────────
            if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !attachedDebris.isEmpty()) {
                releaseAttachedDebrisInFacingDirection(rCX, rCY, rot);
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

        // Space station (with short rumble on impact)
        float stationX = spaceStation.getPosX();
        float stationY = spaceStation.getPosY();
        if (stationShakeTimer > 0f) {
            float t = stationShakeTimer / STATION_SHAKE_DURATION;
            float amp = STATION_SHAKE_AMPLITUDE * t;
            float phase = stationShakeTimer * 70f;
            float shakeX = MathUtils.sin(phase) * amp + MathUtils.random(-amp * 0.25f, amp * 0.25f);
            float shakeY = MathUtils.cos(phase * 0.7f) * amp * 0.45f
                         + MathUtils.random(-amp * 0.20f, amp * 0.20f);
            spaceStation.setPosX(stationX + shakeX);
            spaceStation.setPosY(stationY + shakeY);
            spaceStation.render(batch, null);
            spaceStation.setPosX(stationX);
            spaceStation.setPosY(stationY);
        } else {
            spaceStation.render(batch, null);
        }

        // Earth station (landing pad)
        earthStation.render(batch, null);

        // Mini explosion bursts on station impacts
        for (int i = 0; i < stationHitFxList.size; i++) {
            StationHitFx fx = stationHitFxList.get(i);
            float alpha = fx.timer / STATION_HIT_FX_DURATION;
            batch.setColor(1f, 0.75f, 0.3f, alpha);
            batch.draw(explosionTex,
                fx.x - fx.size / 2f, fx.y - fx.size / 2f,
                fx.size, fx.size);
        }
        batch.setColor(1f, 1f, 1f, 1f);

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
            float bw = BOWL_DRAW_W, bh = BOWL_DRAW_H;
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
                float by  = AGENCY_BASE_Y[idx];
                if (bx + bw < left || bx > right || by + bh < bot) continue;
                batch.draw(agencyTextures[idx], bx, by, bw, bh);
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
        font.draw(batch, "SCORE: " + debrisCollected + "/" + WIN_CLEAR_SCORE, 10f, vH - 80f);

        // Bowl indicator
        int bowlCount = attachedDebris.size;
        font.setColor(bowlCount > 0 ? Color.CYAN : Color.LIGHT_GRAY);
        font.draw(batch, "BOWL: " + bowlCount + "/" + MAX_BOWL_CAPACITY, 10f, vH - 95f);
        font.setColor(Color.WHITE);

        // Control hint at bottom
        font.getData().setScale(0.65f);
        font.setColor(0.75f, 0.75f, 0.75f, 1f);
        font.draw(batch, "[E] launch debris    [land on pad] refuel", 10f, 16f);
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
            attachDebris(d);
        }

        // Debris hits station
        if (info.isBetween("Debris", "SpaceStation")) {
            Debris d = (Debris)(info.tag1.equals("Debris") ? e1 : e2);
            if (!d.isDestroyed()) {
                triggerStationHitEffects(d.getPosX() + d.getWidth() / 2f,
                                         d.getPosY() + d.getHeight() / 2f);
                d.setDestroyed(true);
                spaceStation.takeDamage(STATION_DAMAGE * d.getStationDamageMultiplier());
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
        float x = 0f, y = 0f;
        float stationCX = STATION_X + STATION_W / 2f;
        float stationCY = STATION_Y + STATION_H / 2f;
        float minStationDistSq = DEBRIS_MIN_STATION_SPAWN_DISTANCE * DEBRIS_MIN_STATION_SPAWN_DISTANCE;
        for (int attempt = 0; attempt < 24; attempt++) {
            x = MathUtils.random(-900f, 900f);
            y = SPACE_ZONE_START + DEBRIS_SPAWN_MIN_Y_OFFSET
                + MathUtils.random(0f, DEBRIS_SPAWN_MAX_Y_OFFSET - DEBRIS_SPAWN_MIN_Y_OFFSET);
            float dx = x - stationCX;
            float dy = y - stationCY;
            if (dx * dx + dy * dy >= minStationDistSq) break;
        }
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
    private void updateStationImpactEffects(float delta) {
        if (stationShakeTimer > 0f) {
            stationShakeTimer = Math.max(0f, stationShakeTimer - delta);
        }
        for (int i = stationHitFxList.size - 1; i >= 0; i--) {
            StationHitFx fx = stationHitFxList.get(i);
            fx.timer -= delta;
            if (fx.timer <= 0f) stationHitFxList.removeIndex(i);
        }
    }

    private void triggerStationHitEffects(float hitX, float hitY) {
        if (stationHitFxList == null) return;
        stationHitFxList.add(new StationHitFx(
            hitX,
            hitY,
            MathUtils.random(STATION_HIT_FX_SIZE_MIN, STATION_HIT_FX_SIZE_MAX)));
        if (stationHitFxList.size > 24) {
            stationHitFxList.removeIndex(0);
        }
        stationShakeTimer = STATION_SHAKE_DURATION;
    }

    private void clearAttachedDebrisAtAtmosphere(float effectX, float effectY) {
        if (attachedDebris.isEmpty()) return;
        atmosphereBurnX = effectX;
        atmosphereBurnY = effectY;
        atmosphereBurnTimer = ATMOSPHERE_BURN_DURATION;
        for (int i = 0; i < attachedDebris.size; i++) {
            Debris d = attachedDebris.get(i);
            debrisCollected += d.getClearScore();
            d.dispose();
        }
        attachedDebris.clear();
        gameMaster.getIoManager().playCollisionEffect();
        checkWinCondition();
    }

    private void releaseAttachedDebrisInFacingDirection(float rocketCenterX, float rocketCenterY,
                                                        float rocketRotation) {
        if (attachedDebris.isEmpty()) return;
        float dirX = MathUtils.cosDeg(rocketRotation);
        float dirY = MathUtils.sinDeg(rocketRotation);
        float sideX = -dirY;
        float sideY = dirX;
        int count = attachedDebris.size;

        for (int i = 0; i < count; i++) {
            Debris d = attachedDebris.get(i);
            float spread = (i - (count - 1) * 0.5f) * BOWL_RELEASE_SPREAD;
            float forward = rocket.getHeight() * 0.5f + BOWL_RELEASE_FORWARD_OFFSET + i * 4f;
            d.setAttached(false);
            d.setReentryCandidate(true);
            d.setCaptureCooldown(BOWL_RELEASE_RECAPTURE_DELAY);
            d.setPosX(rocketCenterX + dirX * forward + sideX * spread * 0.4f - d.getWidth() / 2f);
            d.setPosY(rocketCenterY + dirY * forward + sideY * spread * 0.4f - d.getHeight() / 2f);
            d.setVx(dirX * BOWL_RELEASE_SPEED + sideX * spread);
            d.setVy(dirY * BOWL_RELEASE_SPEED + sideY * spread);
            flyingDebris.add(d);
            addSceneEntity(d);
        }
        attachedDebris.clear();
    }

    private void tryAutoCatchDebrisAtBowl(float rocketCenterX, float rocketCenterY, float rocketRotation) {
        if (attachedDebris.size >= MAX_BOWL_CAPACITY) return;

        float noseX = rocketCenterX + MathUtils.cosDeg(rocketRotation) * (rocket.getHeight() / 2f);
        float noseY = rocketCenterY + MathUtils.sinDeg(rocketRotation) * (rocket.getHeight() / 2f);
        float captureRadiusSq = BOWL_CAPTURE_RADIUS * BOWL_CAPTURE_RADIUS;

        Debris best = null;
        float bestSq = Float.MAX_VALUE;
        for (int i = 0; i < flyingDebris.size; i++) {
            Debris d = flyingDebris.get(i);
            if (d.isDestroyed() || d.isAttached() || !d.canBeCaptured()) continue;

            float dCX = d.getPosX() + d.getWidth() / 2f;
            float dCY = d.getPosY() + d.getHeight() / 2f;
            float dx = dCX - noseX;
            float dy = dCY - noseY;
            float sq = dx * dx + dy * dy;

            if (sq <= captureRadiusSq && sq < bestSq) {
                bestSq = sq;
                best = d;
            }
        }

        if (best != null) attachDebris(best);
    }

    private void attachDebris(Debris debris) {
        if (debris == null || debris.isDestroyed() || debris.isAttached()) return;
        if (!debris.canBeCaptured()) return;
        if (attachedDebris.size >= MAX_BOWL_CAPACITY) return;

        debris.setAttached(true);
        debris.setReentryCandidate(false);
        flyingDebris.removeValue(debris, true);
        removeSceneEntity(debris);
        attachedDebris.add(debris);
        gameMaster.getIoManager().playCollisionEffect();
    }

    private boolean isAgencySpawnTooClose(int placedCount, float x, int type) {
        float halfW = AGENCY_W[type] * 0.5f;
        float centerX = x + halfW;
        for (int j = 0; j < placedCount; j++) {
            float otherX = agencyData[j * 2];
            int otherType = (int) agencyData[j * 2 + 1];
            float otherHalfW = AGENCY_W[otherType] * 0.5f;
            float otherCenterX = otherX + otherHalfW;
            float minCenterDistance = halfW + otherHalfW + AGENCY_MIN_SPACING;
            if (Math.abs(centerX - otherCenterX) < minCenterDistance) return true;
        }
        return false;
    }

    private void triggerExplosion(float x, float y) {
        explosionX     = x;
        explosionY     = y;
        explosionTimer = EXPLOSION_DURATION;
        gameMaster.getIoManager().playCollisionEffect();
    }

    private void checkWinCondition() {
        if (debrisCollected >= WIN_CLEAR_SCORE && !gameWon) {
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
        if (stationHitFxList != null) {
            stationHitFxList.clear();
        }
    }
}
