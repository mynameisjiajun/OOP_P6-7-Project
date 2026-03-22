package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.managers.CollisionManager;
import io.github.Project.engine.scenes.Scene;
import io.github.Project.game.entities.Asteroid;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Fuelbar;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.healthbar;
import io.github.Project.game.entities.arrow;
import io.github.Project.game.movementstrategy.RocketMovementStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayScene extends Scene {

    // ── Zone boundaries (world Y) ──────────────────────────────────────────
    // Ground:      y < 0
    // Lower sky:   0  – 1200   (blue, skyscrapers visible)
    // Cloud band:  900 – 1800  (clouds pass through this range)
    // Upper sky:   1800 – 2500 (clear blue above clouds)
    // Transition:  2500 – 4000 (sky fades to black, stars fade in)
    // Space:       > 4000
    private static final float EARTH_ZONE_END   = 2500f;
    private static final float SPACE_ZONE_START = 4000f;

    private static final float CLOUD_BAND_MIN = 900f;
    private static final float CLOUD_BAND_MAX = 1800f;

    // ── Ground tile size ───────────────────────────────────────────────────
    private static final int TILE_SIZE = 256;

    // ── World horizontal boundary ──────────────────────────────────────────
    private static final float WORLD_HALF_WIDTH = 1200f;

    // ── Space Station placement ────────────────────────────────────────────
    private static final float STATION_X = -75f;
    private static final float STATION_Y = 5500f;
    private static final float STATION_W = 150f;
    private static final float STATION_H = 100f;

    // ── Debris system ──────────────────────────────────────────────────────
    private static final float DEBRIS_SPAWN_INTERVAL = 5f;
    private static final float DEBRIS_SPEED          = 70f;
    private static final float DEBRIS_SPAWN_RADIUS   = 1400f;
    private static final int   WIN_DEBRIS_COUNT      = 15;
    private static final float STATION_DAMAGE        = 0.2f;

    // ── Background rock spawning ───────────────────────────────────────────
    private static final float ROCK_BAND_HEIGHT = 400f;
    private static final float ROCK_FIELD_WIDTH = 2000f;

    // ── Gameplay tuning ────────────────────────────────────────────────────
    private static final float DAMAGE_COOLDOWN  = 1.5f;
    private static final float DAMAGE_PER_HIT   = 0.15f;
    private static final float FUEL_DRAIN_RATE  = 0.03f;

    // ── Background colours ─────────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.40f, 0.70f, 0.95f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Cameras ────────────────────────────────────────────────────────────
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Ground textures ────────────────────────────────────────────────────
    private Texture grassTopTex; // top ground row: dirt + grass surface
    private Texture dirtTex;     // all rows below: plain dirt

    // ── Skyscrapers ────────────────────────────────────────────────────────
    private static final int   SKYSCRAPER_VARIANTS  = 5;
    private static final int   SKYSCRAPER_INSTANCES = 15;
    private static final float SKYSCRAPER_SCALE     = 4f; // 4× native size = ~516×2016 units
    private Texture[] skyscraperTextures;
    // Packed as [x, texIdx] pairs — bottom of each building is always y = 0
    private float[]   skyscraperData;

    // ── Pixel art clouds ──────────────────────────────────────────────────
    private static final int CLOUD_COUNT = 6;
    private Texture[] cloudTextures;
    // [x, y, texIdx] triples
    private float[]   cloudPositions;

    // ── Space stars ────────────────────────────────────────────────────────
    private float[] starPositions; // [x, y, radius] triples

    // ── Explosion flash ────────────────────────────────────────────────────
    private Texture explosionTex;
    private float   explosionTimer;
    private float   explosionX, explosionY;
    private static final float EXPLOSION_DURATION = 0.35f;
    private static final float EXPLOSION_SIZE     = 80f;

    // ── Game entities ──────────────────────────────────────────────────────
    private Rocket       rocket;
    private SpaceStation spaceStation;
    private List<Asteroid> backgroundRocks;
    private List<Debris> flyingDebris;    // fly toward station (tag = "Debris")

    // ── HUD ────────────────────────────────────────────────────────────────
    private healthbar  healthBar;
    private healthbar  stationHealthBar;
    private Fuelbar    fuelBar;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private arrow arrow;

    // ── Game state ─────────────────────────────────────────────────────────
    private float   health        = 1f;
    private float   stationHealth = 1f;
    private float   fuel          = 1f;
    private float   damageCooldownTimer = 0f;
    private float   debrisSpawnTimer    = DEBRIS_SPAWN_INTERVAL;
    private int     debrisCollected     = 0;
    private boolean gameWon  = false;
    private boolean gameOver = false;

    // ── Rock spawn tracking ────────────────────────────────────────────────
    private float  highestSpawnedBand;
    private Random random;

    // ── Collision listener ─────────────────────────────────────────────────
    private CollisionManager.CollisionListener collisionListener;

    // ── Reusable colour ────────────────────────────────────────────────────
    private final Color tempColor = new Color();

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SHOW
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        if (gameCamera != null) { isPaused = false; return; }

        random = new Random();
        float viewW = Gdx.graphics.getWidth();
        float viewH = Gdx.graphics.getHeight();

        // Cameras
        gameCamera = new OrthographicCamera(viewW, viewH);
        hudCamera  = new OrthographicCamera(viewW, viewH);
        hudCamera.position.set(viewW / 2f, viewH / 2f, 0);
        hudCamera.update();

        // Ground textures
        grassTopTex = new Texture(Gdx.files.internal("Dirt texture/Dirt and grass.png"));
        dirtTex     = new Texture(Gdx.files.internal("Dirt texture/Dirt.png"));

        // Skyscraper textures
        skyscraperTextures = new Texture[SKYSCRAPER_VARIANTS];
        for (int i = 0; i < SKYSCRAPER_VARIANTS; i++) {
            skyscraperTextures[i] = new Texture(
                    Gdx.files.internal("Skyscraper/Skyscraper " + (i + 1) + ".png"));
        }

        // Skyscraper world positions (seeded so they're always the same)
        Random skyRng = new Random(55555L);
        skyscraperData = new float[SKYSCRAPER_INSTANCES * 2]; // [x, texIdx]
        for (int i = 0; i < SKYSCRAPER_INSTANCES; i++) {
            skyscraperData[i * 2]     = (skyRng.nextFloat() - 0.5f) * (WORLD_HALF_WIDTH * 2f);
            skyscraperData[i * 2 + 1] = skyRng.nextInt(SKYSCRAPER_VARIANTS);
        }

        // Pixel art clouds — placed in the cloud band
        cloudTextures = new Texture[CLOUD_COUNT];
        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudTextures[i] = new Texture(
                    Gdx.files.internal("tiles/pixelart_clouds/x5/cloud" + (i + 1) + ".png"));
        }
        Random cloudRng = new Random(12345L);
        int numClouds = 30;
        cloudPositions = new float[numClouds * 3];
        for (int i = 0; i < numClouds; i++) {
            cloudPositions[i * 3]     = (cloudRng.nextFloat() - 0.5f) * 3600f;
            cloudPositions[i * 3 + 1] = CLOUD_BAND_MIN + cloudRng.nextFloat() * (CLOUD_BAND_MAX - CLOUD_BAND_MIN);
            cloudPositions[i * 3 + 2] = cloudRng.nextInt(CLOUD_COUNT);
        }

        // Space stars — scattered through transition and space zones
        Random starRng = new Random(99887L);
        int numStars = 400;
        starPositions = new float[numStars * 3];
        for (int i = 0; i < numStars; i++) {
            starPositions[i * 3]     = (starRng.nextFloat() - 0.5f) * 6000f;
            starPositions[i * 3 + 1] = EARTH_ZONE_END + starRng.nextFloat() * 8000f;
            starPositions[i * 3 + 2] = 0.5f + starRng.nextFloat() * 2f;
        }

        explosionTex = new Texture(Gdx.files.internal("Explosion_01.png"));

        // Space Station
        spaceStation = new SpaceStation(STATION_X, STATION_Y, STATION_W, STATION_H);
        addSceneEntity(spaceStation);

        // Rocket
        rocket = new Rocket(0, 0, 0, 32, 64, gameMaster.getInputMovement());
        addSceneEntity(rocket);
        gameMaster.getMovementManager().registerEntity(rocket, new RocketMovementStrategy());

        // Arrow — starts pointing at station, updated each frame to nearest debris
        arrow = new arrow(rocket, spaceStation);

        // Background rocks (spawn from space zone upward)
        backgroundRocks = new ArrayList<>();
        highestSpawnedBand = SPACE_ZONE_START;
        spawnRocksUpTo(STATION_Y + 1000f);

        // Flying debris
        flyingDebris = new ArrayList<>();

        // HUD
        healthBar        = new healthbar(10, viewH - 30, 150, 20);
        fuelBar          = new Fuelbar  (10, viewH - 55, 150, 20);
        stationHealthBar = new healthbar(10, viewH - 80, 150, 20);
        font             = new BitmapFont();
        glyphLayout      = new GlyphLayout();

        // Collisions
        gameMaster.getCollisionManager().clearListeners();
        collisionListener = this::handleCollision;
        gameMaster.getCollisionManager().addListener(collisionListener);

        Gdx.input.setInputProcessor(gameMaster.getInputMovement());
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  RENDER
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        gameMaster.getIoManager().update();
        if (gameCamera == null) return;

        if (isPaused) { drawWorld(0); drawHUD(); return; }

        // Clean up destroyed background rocks
        backgroundRocks.removeIf(r -> {
            if (r.isDestroyed()) { removeSceneEntity(r); r.dispose(); return true; }
            return false;
        }); // Asteroid.isDestroyed() / dispose() — meteor textures

        // Clean up destroyed / collected debris
        flyingDebris.removeIf(d -> {
            if (d.isDestroyed()) { removeSceneEntity(d); d.dispose(); return true; }
            return false;
        });

        if (!gameWon && !gameOver) {
            gameMaster.getMovementManager().updateMovements(delta);

            // Horizontal boundary
            float leftLimit  = -WORLD_HALF_WIDTH;
            float rightLimit =  WORLD_HALF_WIDTH - rocket.getWidth();
            if (rocket.getPosX() < leftLimit)  { rocket.setPosX(leftLimit);  rocket.setVx(0); }
            if (rocket.getPosX() > rightLimit) { rocket.setPosX(rightLimit); rocket.setVx(0); }

            for (Asteroid r : backgroundRocks) r.update(delta);
            for (Debris d : flyingDebris)    d.update(delta);

            gameMaster.getCollisionManager().checkCollisions(
                    gameMaster.getEntityManager().getEntities());

            if (damageCooldownTimer > 0) damageCooldownTimer -= delta;
            if (explosionTimer      > 0) explosionTimer      -= delta;

            // Fuel drain
            if (gameMaster.getInputMovement().keyUp && fuel > 0) {
                fuel -= FUEL_DRAIN_RATE * delta;
                if (fuel < 0) fuel = 0;
                fuelBar.setFuel(fuel);
            }
            if (fuel <= 0 && !gameWon) {
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
            }

            // Spawn flying debris
            debrisSpawnTimer -= delta;
            if (debrisSpawnTimer <= 0) {
                debrisSpawnTimer = DEBRIS_SPAWN_INTERVAL;
                spawnDebris();
            }

            // Spawn more background rocks as rocket climbs
            if (rocket.getPosY() > highestSpawnedBand - 2000) {
                spawnRocksUpTo(rocket.getPosY() + 3000);
            }

            // Arrow tracks nearest flying debris, falls back to station
            Entity arrowTarget = spaceStation;
            float  minDist     = Float.MAX_VALUE;
            float  rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
            float  rCY = rocket.getPosY() + rocket.getHeight() / 2f;
            for (Debris d : flyingDebris) {
                float dx   = (d.getPosX() + d.getWidth()  / 2f) - rCX;
                float dy   = (d.getPosY() + d.getHeight() / 2f) - rCY;
                float dist = dx * dx + dy * dy;
                if (dist < minDist) { minDist = dist; arrowTarget = d; }
            }
            arrow.setTarget(arrowTarget);
        }

        drawWorld(delta);
        drawHUD();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  WORLD RENDERING
    // ──────────────────────────────────────────────────────────────────────
    private void drawWorld(float delta) {
        gameCamera.position.set(
                rocket.getPosX() + rocket.getWidth()  / 2f,
                rocket.getPosY() + rocket.getHeight() / 2f, 0);
        gameCamera.update();

        float camX  = gameCamera.position.x;
        float camY  = gameCamera.position.y;
        float halfW = gameCamera.viewportWidth  / 2f;
        float halfH = gameCamera.viewportHeight / 2f;

        // Sky-to-space background colour
        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Stars fade in during transition zone
        float starAlpha = 0f;
        if (camY > EARTH_ZONE_END && camY < SPACE_ZONE_START) {
            starAlpha = (camY - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
        } else if (camY >= SPACE_ZONE_START) {
            starAlpha = 1f;
        }
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

        // Entities (blink rocket during invincibility)
        for (Entity e : sceneEntities) {
            if (e == rocket && damageCooldownTimer > 0
                    && ((int) (damageCooldownTimer * 10)) % 2 == 0) continue;
            e.render(batch, null);
        }

        // Explosion flash
        if (explosionTimer > 0) {
            float alpha = explosionTimer / EXPLOSION_DURATION;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(explosionTex,
                    explosionX - EXPLOSION_SIZE / 2f,
                    explosionY - EXPLOSION_SIZE / 2f,
                    EXPLOSION_SIZE, EXPLOSION_SIZE);
            batch.setColor(1, 1, 1, 1);
        }

        batch.end();

        // Arrow (ShapeRenderer, Line mode)
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Line);
        arrow.update(delta);
        arrow.render(null, sr);
        sr.end();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  BACKGROUND LAYERS
    // ──────────────────────────────────────────────────────────────────────
    private void drawBackground(SpriteBatch batch, float camX, float camY,
                                float halfW, float halfH) {
        float left  = camX - halfW;
        float right = camX + halfW;
        float bot   = camY - halfH;

        // ── Ground tiles (below y = 0) ─────────────────────────────────────
        int txStart = MathUtils.floor(left  / TILE_SIZE);
        int txEnd   = MathUtils.floor(right / TILE_SIZE);
        int tyStart = MathUtils.floor(bot   / TILE_SIZE);
        // Top row that is still underground (ty = -1 → worldY -256..0)
        for (int ty = tyStart; ty <= -1; ty++) {
            float worldY = ty * (float) TILE_SIZE;
            if (worldY + TILE_SIZE > 0) continue; // clip row that crosses y=0
            Texture tex = (ty == -1) ? grassTopTex : dirtTex;
            for (int tx = txStart; tx <= txEnd; tx++) {
                batch.draw(tex, tx * (float) TILE_SIZE, worldY, TILE_SIZE, TILE_SIZE);
            }
        }

        // ── Skyscrapers (visible while in or below the upper sky) ──────────
        float skyTop = CLOUD_BAND_MAX + 400f; // stop drawing buildings above here
        if (camY - halfH < skyTop) {
            float buildW = skyscraperTextures[0].getWidth()  * SKYSCRAPER_SCALE;
            float buildH = skyscraperTextures[0].getHeight() * SKYSCRAPER_SCALE;
            for (int i = 0; i < SKYSCRAPER_INSTANCES; i++) {
                float bx  = skyscraperData[i * 2];
                int   idx = (int) skyscraperData[i * 2 + 1];
                // Frustum cull
                if (bx + buildW < left || bx > right) continue;
                if (buildH < bot) continue; // building top is below camera bottom
                Texture tex = skyscraperTextures[idx];
                // Use actual texture size scaled up
                float w = tex.getWidth()  * SKYSCRAPER_SCALE;
                float h = tex.getHeight() * SKYSCRAPER_SCALE;
                batch.draw(tex, bx, 0, w, h);
            }
        }

        // ── Clouds (visible when camera overlaps the cloud band) ───────────
        if (camY + halfH > CLOUD_BAND_MIN && camY - halfH < CLOUD_BAND_MAX + 200f) {
            for (int i = 0; i < cloudPositions.length; i += 3) {
                float cx   = cloudPositions[i];
                float cy   = cloudPositions[i + 1];
                int   tidx = (int) cloudPositions[i + 2];
                Texture cloud = cloudTextures[tidx];
                float cw = cloud.getWidth();
                float ch = cloud.getHeight();
                if (cx + cw < left || cx > right) continue;
                batch.draw(cloud, cx, cy, cw, ch);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  HUD RENDERING
    // ──────────────────────────────────────────────────────────────────────
    private void drawHUD() {
        float viewW = hudCamera.viewportWidth;
        float viewH = hudCamera.viewportHeight;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.45f);
        sr.rect(0, viewH - 125, 215, 125);
        healthBar.render(null, sr);
        fuelBar.render(null, sr);
        stationHealthBar.render(null, sr);
        sr.end();

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP",      165, viewH - 13);
        font.draw(batch, "FUEL",    165, viewH - 38);
        font.draw(batch, "STATION", 165, viewH - 63);

        font.draw(batch, "ALT: " + (int) rocket.getPosY() + "m",               10, viewH - 95);
        font.draw(batch, "DEBRIS: " + debrisCollected + "/" + WIN_DEBRIS_COUNT, 10, viewH - 113);

        if (gameWon) {
            font.getData().setScale(3f);
            font.setColor(Color.GREEN);
            glyphLayout.setText(font, "YOU WIN!");
            font.draw(batch, "YOU WIN!",
                    (viewW - glyphLayout.width) / 2f, viewH / 2f + glyphLayout.height / 2f);
            font.getData().setScale(1f);
            font.setColor(Color.WHITE);
            glyphLayout.setText(font, "Press R to restart");
            font.draw(batch, "Press R to restart",
                    (viewW - glyphLayout.width) / 2f, viewH / 2f - 30);
        }
        if (gameOver) {
            font.getData().setScale(3f);
            font.setColor(Color.RED);
            glyphLayout.setText(font, "GAME OVER");
            font.draw(batch, "GAME OVER",
                    (viewW - glyphLayout.width) / 2f, viewH / 2f + glyphLayout.height / 2f);
            font.getData().setScale(1f);
            font.setColor(Color.WHITE);
            glyphLayout.setText(font, "Press R to restart");
            font.draw(batch, "Press R to restart",
                    (viewW - glyphLayout.width) / 2f, viewH / 2f - 30);
        }

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        batch.end();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  ZONE COLOUR (sky blue → space black)
    // ──────────────────────────────────────────────────────────────────────
    private void getZoneColor(float altitude, Color out) {
        if (altitude <= EARTH_ZONE_END) {
            out.set(COLOR_SKY);
        } else if (altitude >= SPACE_ZONE_START) {
            out.set(COLOR_SPACE);
        } else {
            float t = (altitude - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
            out.r = MathUtils.lerp(COLOR_SKY.r, COLOR_SPACE.r, t);
            out.g = MathUtils.lerp(COLOR_SKY.g, COLOR_SPACE.g, t);
            out.b = MathUtils.lerp(COLOR_SKY.b, COLOR_SPACE.b, t);
            out.a = 1f;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  COLLISION HANDLING
    // ──────────────────────────────────────────────────────────────────────
    private void handleCollision(CollisionManager.CollisionInfo info) {
        if (gameWon || gameOver) return;

        // Rocket hits a background rock → rocket takes damage
        if (info.isBetween("Rocket", "Asteroid")) {
            Asteroid rock = (Asteroid) (info.tag1.equals("Asteroid") ? info.entity1 : info.entity2);
            rock.setDestroyed(true);
            if (damageCooldownTimer <= 0) {
                health -= DAMAGE_PER_HIT;
                if (health <= 0) {
                    health = 0; gameOver = true;
                    gameMaster.getAudioManager().stopRocketLoop();
                }
                healthBar.setHP(health);
                damageCooldownTimer = DAMAGE_COOLDOWN;
                explosionX = rocket.getPosX() + rocket.getWidth()  / 2f;
                explosionY = rocket.getPosY() + rocket.getHeight() / 2f;
                explosionTimer = EXPLOSION_DURATION;
                gameMaster.getIoManager().playCollisionEffect();
            }
        }

        // Rocket collects flying debris → score
        if (info.isBetween("Rocket", "Debris")) {
            Debris debris = (Debris) (info.tag1.equals("Debris") ? info.entity1 : info.entity2);
            if (!debris.isDestroyed()) {
                debris.setDestroyed(true);
                debrisCollected++;
                gameMaster.getIoManager().playCollisionEffect();
                if (debrisCollected >= WIN_DEBRIS_COUNT) {
                    gameWon = true;
                    gameMaster.getAudioManager().stopRocketLoop();
                    gameMaster.getIoManager().playWinEffect();
                }
            }
        }

        // Flying debris hits station → station takes damage
        if (info.isBetween("Debris", "SpaceStation")) {
            Debris debris = (Debris) (info.tag1.equals("Debris") ? info.entity1 : info.entity2);
            if (!debris.isDestroyed()) {
                debris.setDestroyed(true);
                stationHealth -= STATION_DAMAGE;
                if (stationHealth <= 0) {
                    stationHealth = 0; gameOver = true;
                    gameMaster.getAudioManager().stopRocketLoop();
                }
                stationHealthBar.setHP(stationHealth);
                gameMaster.getIoManager().playCollisionEffect();
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SPAWNING
    // ──────────────────────────────────────────────────────────────────────
    private void spawnDebris() {
        float stationCX = STATION_X + STATION_W / 2f;
        float stationCY = STATION_Y + STATION_H / 2f;
        int count = 1 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            float angle  = random.nextFloat() * 360f;
            float rad    = (float) Math.toRadians(angle);
            float spawnX = stationCX + (float) Math.cos(rad) * DEBRIS_SPAWN_RADIUS - 25f;
            float spawnY = stationCY + (float) Math.sin(rad) * DEBRIS_SPAWN_RADIUS - 25f;
            Debris d = new Debris(spawnX, spawnY, 0, 50, 50);
            float dx = stationCX - (spawnX + 25f);
            float dy = stationCY - (spawnY + 25f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            d.setVx(dx / dist * DEBRIS_SPEED);
            d.setVy(dy / dist * DEBRIS_SPEED);
            flyingDebris.add(d);
            addSceneEntity(d);
        }
    }

    private void spawnRocksUpTo(float maxY) {
        while (highestSpawnedBand < maxY) {
            int count = 2 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                float x = (random.nextFloat() - 0.5f) * ROCK_FIELD_WIDTH;
                float y = highestSpawnedBand + random.nextFloat() * ROCK_BAND_HEIGHT;
                Asteroid rock = new Asteroid(x, y, 0, 50, 50);
                backgroundRocks.add(rock);
                addSceneEntity(rock);
            }
            highestSpawnedBand += ROCK_BAND_HEIGHT;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (gameCamera != null) {
            gameCamera.viewportWidth = width; gameCamera.viewportHeight = height;
            gameCamera.update();
        }
        if (hudCamera != null) {
            hudCamera.viewportWidth = width; hudCamera.viewportHeight = height;
            hudCamera.position.set(width / 2f, height / 2f, 0);
            hudCamera.update();
        }
    }

    @Override
    public void hide() {
        if (!isPaused) {
            clearSceneEntities();
            gameMaster.getCollisionManager().clearListeners();
            disposeTextures();
            gameCamera = null;
            hudCamera  = null;
        }
    }

    @Override
    public void dispose() {
        clearSceneEntities();
        gameMaster.getCollisionManager().clearListeners();
        disposeTextures();
    }

    private void disposeTextures() {
        if (grassTopTex   != null) { grassTopTex.dispose();   grassTopTex   = null; }
        if (dirtTex       != null) { dirtTex.dispose();       dirtTex       = null; }
        if (explosionTex  != null) { explosionTex.dispose();  explosionTex  = null; }
        if (font          != null) { font.dispose();          font          = null; }
        if (skyscraperTextures != null) {
            for (Texture t : skyscraperTextures) if (t != null) t.dispose();
            skyscraperTextures = null;
        }
        if (cloudTextures != null) {
            for (Texture t : cloudTextures) if (t != null) t.dispose();
            cloudTextures = null;
        }
    }
}
