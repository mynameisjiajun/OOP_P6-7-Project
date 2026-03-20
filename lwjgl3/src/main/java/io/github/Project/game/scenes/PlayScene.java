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
import io.github.Project.game.entities.Fuelbar;
import io.github.Project.game.entities.Moon;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.arrow;
import io.github.Project.game.entities.healthbar;
import io.github.Project.game.factory.EntityFactory;
import io.github.Project.game.movementstrategy.RocketMovementStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Main gameplay scene.
 *
 * CHANGES:
 * 1. Entity construction goes through EntityFactory (Pattern 2 — Factory).
 *    All calls like `new Rocket(...)`, `new Moon(...)`, `new healthbar(...)`,
 *    etc. are replaced with factory.createRocket(), factory.createMoon(), etc.
 *
 * 2. EntityFactory is a class field (not a local variable) so it is created
 *    once in show() and reused in spawnAsteroidsUpTo() — avoiding a new
 *    factory allocation on every asteroid spawn call.
 *
 * 3. getZoneColor() uses Color.lerp() instead of manually lerping each channel:
 *    out.set(COLOR_SKY).lerp(COLOR_SPACE, t) replaces four separate lines.
 *
 * 4. Asteroid spawning uses MathUtils.random() instead of java.util.Random.
 *    java.util.Random and its import have been removed entirely from this file.
 *
 * 5. drawHUD() wraps the ShapeRenderer calls for healthBar and fuelBar inside
 *    a single begin(Filled)/end() block — the bars no longer call begin/end
 *    internally, so the scene owns that lifecycle.
 */
public class PlayScene extends Scene {

    // ── Zone boundaries (world Y) ────────────────────────────────────────────
    private static final float EARTH_ZONE_END   = 1000f;
    private static final float SPACE_ZONE_START = 3000f;

    // ── Background tile size ─────────────────────────────────────────────────
    private static final int TILE_SIZE = 256;

    // ── World horizontal boundary ────────────────────────────────────────────
    private static final float WORLD_HALF_WIDTH = 1200f;

    // ── Moon placement ───────────────────────────────────────────────────────
    private static final float MOON_X = -100f;
    private static final float MOON_Y = 5000f;

    // ── Gameplay tuning ──────────────────────────────────────────────────────
    private static final float DAMAGE_COOLDOWN = 1.5f;
    private static final float DAMAGE_PER_HIT  = 0.15f;
    private static final float FUEL_DRAIN_RATE = 0.08f;

    // ── Asteroid spawning ────────────────────────────────────────────────────
    private static final float ASTEROID_BAND_HEIGHT = 400f;
    private static final float ASTEROID_FIELD_WIDTH = 2000f;

    // ── Background colours ───────────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.53f, 0.81f, 0.98f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Cameras ──────────────────────────────────────────────────────────────
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Background tile textures ─────────────────────────────────────────────
    private Texture groundTile;
    private Texture skyTile;
    private Texture spaceTile;

    // ── Explosion flash ──────────────────────────────────────────────────────
    private Texture explosionTex;
    private float   explosionTimer;
    private float   explosionX, explosionY;
    private static final float EXPLOSION_DURATION = 0.35f;
    private static final float EXPLOSION_SIZE     = 80f;

    // ── Entity factory ───────────────────────────────────────────────────────
    // Stored as a field so show() and spawnAsteroidsUpTo() share the same
    // instance — no need to construct a new factory on every spawn call.
    private EntityFactory factory;

    // ── Game entities ────────────────────────────────────────────────────────
    private Rocket         rocket;
    private Moon           moon;
    private List<Asteroid> asteroids;
    private arrow          directionArrow;

    // ── HUD ──────────────────────────────────────────────────────────────────
    private healthbar   healthBar;
    private Fuelbar     fuelBar;
    private BitmapFont  font;
    private GlyphLayout glyphLayout;

    // ── Game state ───────────────────────────────────────────────────────────
    private float   health              = 1f;
    private float   fuel                = 1f;
    private float   damageCooldownTimer = 0f;
    private boolean gameWon             = false;
    private boolean gameOver            = false;

    // ── Asteroid spawn tracking ──────────────────────────────────────────────
    private float highestSpawnedBand;

    // ── Collision listener ───────────────────────────────────────────────────
    private CollisionManager.CollisionListener collisionListener;

    // ── Reusable colour (avoids allocation every frame) ──────────────────────
    private final Color tempColor = new Color();

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  SHOW
    // ────────────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        if (gameCamera != null) {
            isPaused = false;
            return;
        }

        float viewW = Gdx.graphics.getWidth();
        float viewH = Gdx.graphics.getHeight();

        // ── Cameras ──────────────────────────────────────────────────────────
        gameCamera = new OrthographicCamera(viewW, viewH);
        hudCamera  = new OrthographicCamera(viewW, viewH);
        hudCamera.position.set(viewW / 2f, viewH / 2f, 0);
        hudCamera.update();

        // ── Background tile textures ──────────────────────────────────────────
        groundTile   = new Texture(Gdx.files.internal("tiles/grass.png"));
        skyTile      = new Texture(Gdx.files.internal("tiles/sky.png"));
        spaceTile    = new Texture(Gdx.files.internal("tiles/space.png"));
        explosionTex = new Texture(Gdx.files.internal("Explosion_01.png"));

        // ── Entity factory — Pattern 2 (Factory) ─────────────────────────────
        // Created once here and reused throughout the scene lifetime,
        // including inside spawnAsteroidsUpTo() which is called repeatedly.
        factory = new EntityFactory(gameMaster.getInputMovement());

        // Moon first — RocketMovementStrategy references it
        moon = factory.createMoon(MOON_X, MOON_Y);
        addSceneEntity(moon);

        // Rocket
        rocket = factory.createRocket(0, 0);
        addSceneEntity(rocket);
        gameMaster.getMovementManager().registerEntity(rocket, new RocketMovementStrategy(moon));

        // Direction arrow (created after both rocket and moon)
        directionArrow = factory.createArrow(rocket, moon);

        // ── Asteroids ─────────────────────────────────────────────────────────
        asteroids = new ArrayList<>();
        highestSpawnedBand = SPACE_ZONE_START;
        spawnAsteroidsUpTo(MOON_Y + 1000f);

        // ── HUD ───────────────────────────────────────────────────────────────
        healthBar   = factory.createHealthBar(10, viewH - 30);
        fuelBar     = factory.createFuelBar(10, viewH - 55);
        font        = new BitmapFont();
        glyphLayout = new GlyphLayout();

        // ── Collision listener ────────────────────────────────────────────────
        gameMaster.getCollisionManager().clearListeners();
        collisionListener = info -> handleCollision(info);
        gameMaster.getCollisionManager().addListener(collisionListener);

        // ── Input & music ─────────────────────────────────────────────────────
        Gdx.input.setInputProcessor(gameMaster.getInputMovement());
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  RENDER
    // ────────────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        gameMaster.getIoManager().update();

        if (gameCamera == null) return;

        if (isPaused) {
            drawWorld(0);
            drawHUD();
            return;
        }

        // Remove destroyed asteroids each frame
        asteroids.removeIf(asteroid -> {
            if (asteroid.isDestroyed()) {
                asteroid.dispose();
                return true;
            }
            return false;
        });

        if (!gameWon && !gameOver) {
            gameMaster.getMovementManager().updateMovements(delta);

            // Horizontal world boundary
            float leftLimit  = -WORLD_HALF_WIDTH;
            float rightLimit =  WORLD_HALF_WIDTH - rocket.getWidth();
            if (rocket.getPosX() < leftLimit) {
                rocket.setPosX(leftLimit);
                rocket.setVx(0);
            } else if (rocket.getPosX() > rightLimit) {
                rocket.setPosX(rightLimit);
                rocket.setVx(0);
            }

            for (Asteroid a : asteroids) a.update(delta);

            gameMaster.getCollisionManager().checkCollisions(
                    gameMaster.getEntityManager().getEntities());

            if (damageCooldownTimer > 0) damageCooldownTimer -= delta;
            if (explosionTimer      > 0) explosionTimer      -= delta;

            if (gameMaster.getInputMovement().keyUp && fuel > 0) {
                fuel -= FUEL_DRAIN_RATE * delta;
                if (fuel < 0) fuel = 0;
                fuelBar.setFuel(fuel);
            }

            if (fuel <= 0 && !gameWon) {
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
            }

            if (rocket.getPosY() > highestSpawnedBand - 2000) {
                spawnAsteroidsUpTo(rocket.getPosY() + 3000);
            }
        }

        drawWorld(delta);
        drawHUD();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  WORLD RENDERING
    // ────────────────────────────────────────────────────────────────────────
    private void drawWorld(float delta) {
        gameCamera.position.set(
                rocket.getPosX() + rocket.getWidth()  / 2f,
                rocket.getPosY() + rocket.getHeight() / 2f,
                0);
        gameCamera.update();

        float camX  = gameCamera.position.x;
        float camY  = gameCamera.position.y;
        float halfW = gameCamera.viewportWidth  / 2f;
        float halfH = gameCamera.viewportHeight / 2f;

        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        drawTiledBackground(batch, camX, camY, halfW, halfH);

        // Render entities — blink rocket during invincibility cooldown
        for (Entity e : sceneEntities) {
            if (e == rocket && damageCooldownTimer > 0
                    && ((int)(damageCooldownTimer * 10)) % 2 == 0) continue;
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

        // Arrow overlay — ShapeRenderer in Line mode
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        directionArrow.update(delta);
        directionArrow.render(null, sr);
        sr.end();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  HUD RENDERING
    // ────────────────────────────────────────────────────────────────────────
    private void drawHUD() {
        float viewW = hudCamera.viewportWidth;
        float viewH = hudCamera.viewportHeight;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);

        // Single begin/end block covers the background panel, health bar,
        // and fuel bar. The bar entities no longer call begin/end themselves —
        // the scene owns the ShapeRenderer lifecycle entirely.
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.45f);
        sr.rect(0, viewH - 100, 210, 100);  // semi-transparent HUD background
        healthBar.render(null, sr);
        fuelBar.render(null, sr);
        sr.end();

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP",   165, viewH - 13);
        font.draw(batch, "FUEL", 165, viewH - 38);

        int   altitude   = (int) rocket.getPosY();
        float distToMoon = MOON_Y - rocket.getPosY();
        font.draw(batch, "ALT: " + altitude + "m",  10, viewH - 70);
        if (distToMoon > 0)
            font.draw(batch, "MOON: " + (int) distToMoon + "m", 10, viewH - 90);

        if (!gameWon && distToMoon > 0) {
            float dx  = (MOON_X + moon.getWidth() / 2f) - (rocket.getPosX() + rocket.getWidth() / 2f);
            String dir = Math.abs(dx) < 100 ? "^" : (dx > 0 ? ">>>" : "<<<");
            font.draw(batch, "MOON " + dir, viewW - 120, viewH - 15);
        }

        if (gameWon) {
            font.getData().setScale(3f);
            font.setColor(Color.GREEN);
            glyphLayout.setText(font, "YOU WIN!");
            font.draw(batch, "YOU WIN!",
                    (viewW - glyphLayout.width) / 2f,
                    viewH / 2f + glyphLayout.height / 2f);
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
                    (viewW - glyphLayout.width) / 2f,
                    viewH / 2f + glyphLayout.height / 2f);
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

    // ────────────────────────────────────────────────────────────────────────
    //  TILED BACKGROUND
    // ────────────────────────────────────────────────────────────────────────
    private void drawTiledBackground(SpriteBatch batch, float camX, float camY,
                                     float halfW, float halfH) {
        float left    = camX - halfW;
        float screenW = halfW * 2f;

        int startTX = MathUtils.floor(left           / TILE_SIZE);
        int endTX   = MathUtils.floor((camX + halfW) / TILE_SIZE);
        int startTY = MathUtils.floor((camY - halfH) / TILE_SIZE);
        int endTY   = MathUtils.floor((camY + halfH) / TILE_SIZE);

        for (int ty = startTY; ty <= endTY; ty++) {
            for (int tx = startTX; tx <= endTX; tx++) {
                float worldX      = tx * TILE_SIZE;
                float worldY      = ty * TILE_SIZE;
                float tileCenterY = worldY + TILE_SIZE / 2f;

                if (worldY + TILE_SIZE <= 0) continue; // fully underground

                if (tileCenterY < EARTH_ZONE_END) {
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                } else if (tileCenterY > SPACE_ZONE_START) {
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                } else {
                    // Atmosphere fade: space underneath, sky alpha fades out
                    float t = (tileCenterY - EARTH_ZONE_END)
                            / (SPACE_ZONE_START - EARTH_ZONE_END);
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1 - t);
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1);
                }
            }
        }

        // Ground strip centred at world y=0 (launch pad surface)
        batch.draw(groundTile, left, -TILE_SIZE, screenW, TILE_SIZE * 2f);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  ZONE COLOUR
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Sets {@code out} to the sky/space blend colour for the given altitude.
     * Uses Color.lerp() to interpolate all four channels in one call instead
     * of manually lerping r, g, b, a separately.
     */
    private void getZoneColor(float altitude, Color out) {
        if (altitude <= EARTH_ZONE_END) {
            out.set(COLOR_SKY);
        } else if (altitude >= SPACE_ZONE_START) {
            out.set(COLOR_SPACE);
        } else {
            float t = (altitude - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
            out.set(COLOR_SKY).lerp(COLOR_SPACE, t);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  COLLISION HANDLING
    // ────────────────────────────────────────────────────────────────────────
    private void handleCollision(CollisionManager.CollisionInfo info) {
        if (gameWon || gameOver) return;

        if (info.isBetween("Rocket", "Asteroid")) {
            Asteroid asteroid = (Asteroid)(info.tag1.equals("Asteroid") ? info.entity1 : info.entity2);
            asteroid.setDestroyed(true);

            if (damageCooldownTimer <= 0) {
                health -= DAMAGE_PER_HIT;
                if (health <= 0) {
                    health   = 0;
                    gameOver = true;
                    gameMaster.getAudioManager().stopRocketLoop();
                }
                healthBar.setHP(health);
                damageCooldownTimer = DAMAGE_COOLDOWN;

                explosionX     = rocket.getPosX() + rocket.getWidth()  / 2f;
                explosionY     = rocket.getPosY() + rocket.getHeight() / 2f;
                explosionTimer = EXPLOSION_DURATION;
                gameMaster.getIoManager().playCollisionEffect();
            }
        }

        if (info.isBetween("Rocket", "Moon")) {
            gameWon = true;
            gameMaster.getIoManager().playWinEffect();
            gameMaster.getAudioManager().stopRocketLoop();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  ASTEROID SPAWNING
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Spawns bands of asteroids up to {@code maxY} world units.
     *
     * Uses the shared {@code factory} field — no new EntityFactory is created
     * here. This method is called repeatedly as the rocket climbs, so reusing
     * the field avoids unnecessary object allocation on each call.
     *
     * Uses MathUtils.random() throughout — java.util.Random is not needed.
     */
    private void spawnAsteroidsUpTo(float maxY) {
        while (highestSpawnedBand < maxY) {
            int count = 2 + MathUtils.random(2); // 2, 3, or 4 asteroids per band
            for (int i = 0; i < count; i++) {
                float x = MathUtils.random(-ASTEROID_FIELD_WIDTH / 2f,
                                            ASTEROID_FIELD_WIDTH / 2f);
                float y = highestSpawnedBand + MathUtils.random(0f, ASTEROID_BAND_HEIGHT);
                Asteroid asteroid = factory.createAsteroid(x, y);
                asteroids.add(asteroid);
                addSceneEntity(asteroid);
            }
            highestSpawnedBand += ASTEROID_BAND_HEIGHT;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  LIFECYCLE
    // ────────────────────────────────────────────────────────────────────────
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
            hudCamera.position.set(width / 2f, height / 2f, 0);
            hudCamera.update();
        }
    }

    @Override
    public void hide() {
        if (!isPaused) {
            clearSceneEntities();
            gameMaster.getCollisionManager().clearListeners();
            if (groundTile   != null) { groundTile.dispose();   groundTile   = null; }
            if (skyTile      != null) { skyTile.dispose();      skyTile      = null; }
            if (spaceTile    != null) { spaceTile.dispose();    spaceTile    = null; }
            if (explosionTex != null) { explosionTex.dispose(); explosionTex = null; }
            if (font         != null) { font.dispose();         font         = null; }
            gameCamera = null;
            hudCamera  = null;
        }
    }

    @Override
    public void dispose() {
        clearSceneEntities();
        gameMaster.getCollisionManager().clearListeners();
        if (groundTile   != null) groundTile.dispose();
        if (skyTile      != null) skyTile.dispose();
        if (spaceTile    != null) spaceTile.dispose();
        if (explosionTex != null) explosionTex.dispose();
        if (font         != null) font.dispose();
    }
}