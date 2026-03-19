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
import io.github.Project.game.entities.healthbar;
import io.github.Project.game.movementstrategy.RocketMovementStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayScene extends Scene {

    // ── Zone boundaries (world Y) ──
    private static final float EARTH_ZONE_END   = 1000f;
    private static final float SPACE_ZONE_START = 3000f;

    // ── Background tile size ──
    private static final int TILE_SIZE = 256;

    // ── World horizontal boundary ──
    private static final float WORLD_HALF_WIDTH = 1200f;   // rocket is clamped to ±1200

    // ── Moon placement (directly above launch pad) ──
    private static final float MOON_X    = -100f;
    private static final float MOON_Y    = 5000f;
    private static final float MOON_SIZE = 200f;           // clearly larger than asteroids

    // ── Gameplay tuning ──
    private static final float DAMAGE_COOLDOWN  = 1.5f;
    private static final float DAMAGE_PER_HIT   = 0.15f;
    private static final float FUEL_DRAIN_RATE  = 0.08f;

    // ── Asteroid spawning ──
    private static final float ASTEROID_BAND_HEIGHT  = 400f;
    private static final float ASTEROID_FIELD_WIDTH  = 2000f;  // spread across ±1000 units

    // ── Background colours (used for glClearColor underneath tiles) ──
    private static final Color COLOR_SKY   = new Color(0.53f, 0.81f, 0.98f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Cameras ──
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Background tile textures ──
    private Texture groundTile;
    private Texture skyTile;
    private Texture spaceTile;

    // ── Game entities ──
    private Rocket rocket;
    private Moon moon;
    private List<Asteroid> asteroids;

    // ── HUD (not in EntityManager) ──
    private healthbar healthBar;
    private Fuelbar fuelBar;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    // ── Game state ──
    private float health = 1f;
    private float fuel   = 1f;
    private float damageCooldownTimer = 0f;
    private boolean gameWon  = false;
    private boolean gameOver = false;

    // ── Asteroid spawn tracking ──
    private float highestSpawnedBand;
    private Random random;

    // ── Collision listener (stored for cleanup) ──
    private CollisionManager.CollisionListener collisionListener;

    // ── Reusable colour (avoids per-frame allocation) ──
    private final Color tempColor = new Color();

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ────────────────────────────────────────────
    //  SHOW — one-time initialisation
    // ────────────────────────────────────────────
    @Override
    public void show() {
        // Resuming from pause — just unpause
        if (gameCamera != null) {
            isPaused = false;
            return;
        }

        random = new Random();
        float viewW = Gdx.graphics.getWidth();
        float viewH = Gdx.graphics.getHeight();

        // ── Cameras ──
        gameCamera = new OrthographicCamera(viewW, viewH);
        hudCamera  = new OrthographicCamera(viewW, viewH);
        hudCamera.position.set(viewW / 2f, viewH / 2f, 0);
        hudCamera.update();

        // ── Background tile textures ──
        groundTile = new Texture(Gdx.files.internal("tiles/grass.png"));
        groundTile.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        skyTile = new Texture(Gdx.files.internal("tiles/sky.png"));
        skyTile.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        spaceTile = new Texture(Gdx.files.internal("tiles/space.png"));
        spaceTile.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // ── Rocket ──
        rocket = new Rocket(0, 0, 0, 32, 64, gameMaster.getInputMovement());
        addSceneEntity(rocket);
        gameMaster.getMovementManager().registerEntity(rocket, new RocketMovementStrategy());

        // ── Moon ──
        moon = new Moon(MOON_X, MOON_Y, MOON_SIZE, MOON_SIZE);
        addSceneEntity(moon);

        // ── Asteroids ──
        asteroids = new ArrayList<>();
        highestSpawnedBand = SPACE_ZONE_START;
        spawnAsteroidsUpTo(MOON_Y + 1000f);

        // ── HUD ──
        healthBar  = new healthbar(10, viewH - 30, 150, 20);
        fuelBar    = new Fuelbar(10, viewH - 55, 150, 20);
        font       = new BitmapFont();
        glyphLayout = new GlyphLayout();

        // ── Collision listener ──
        gameMaster.getCollisionManager().clearListeners();
        collisionListener = new CollisionManager.CollisionListener() {
            @Override
            public void onCollision(CollisionManager.CollisionInfo info) {
                handleCollision(info);
            }
        };
        gameMaster.getCollisionManager().addListener(collisionListener);

        // ── Restore keyboard input processor (MainMenuScene overrides it with a Stage) ──
        Gdx.input.setInputProcessor(gameMaster.getInputMovement());

        // ── Music ──
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
    }

    // ────────────────────────────────────────────
    //  RENDER
    // ────────────────────────────────────────────
    @Override
    public void render(float delta) {
        gameMaster.getIoManager().update();

        // io update may have switched scenes (e.g. R/M pressed), nulling gameCamera via hide()
        if (gameCamera == null) return;

        if (isPaused) {
            drawWorld();
            drawHUD();
            return;
        }

        if (!gameWon && !gameOver) {
            gameMaster.getMovementManager().updateMovements(delta);

            // ── Horizontal world boundary ──
            float leftLimit  = -WORLD_HALF_WIDTH;
            float rightLimit =  WORLD_HALF_WIDTH - rocket.getWidth();
            if (rocket.getPosX() < leftLimit) {
                rocket.setPosX(leftLimit);
                rocket.setVx(0);
            } else if (rocket.getPosX() > rightLimit) {
                rocket.setPosX(rightLimit);
                rocket.setVx(0);
            }

            for (Asteroid a : asteroids) {
                a.update(delta);
            }

            gameMaster.getCollisionManager().checkCollisions(
                    gameMaster.getEntityManager().getEntities());

            if (damageCooldownTimer > 0) damageCooldownTimer -= delta;

            if (gameMaster.getInputMovement().keyUp && fuel > 0) {
                fuel -= FUEL_DRAIN_RATE * delta;
                if (fuel < 0) fuel = 0;
                fuelBar.setFuel(fuel);
            }

            if (rocket.getPosY() > highestSpawnedBand - 2000) {
                spawnAsteroidsUpTo(rocket.getPosY() + 3000);
            }
        }

        drawWorld();
        drawHUD();
    }

    // ────────────────────────────────────────────
    //  WORLD RENDERING
    // ────────────────────────────────────────────
    private void drawWorld() {
        // Lock camera on rocket centre
        gameCamera.position.set(
                rocket.getPosX() + rocket.getWidth()  / 2f,
                rocket.getPosY() + rocket.getHeight() / 2f,
                0);
        gameCamera.update();

        float camX    = gameCamera.position.x;
        float camY    = gameCamera.position.y;
        float halfW   = gameCamera.viewportWidth  / 2f;
        float halfH   = gameCamera.viewportHeight / 2f;
        // ── Clear screen with zone colour ──
        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── Draw tiled background ──
        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        drawTiledBackground(batch, camX, camY, halfW, halfH);
        batch.end();

        // ── Draw entities (rocket, moon, asteroids) ──
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (Entity e : sceneEntities) {
            // Blink rocket during invincibility frames
            if (e == rocket && damageCooldownTimer > 0
                    && ((int) (damageCooldownTimer * 10)) % 2 == 0) {
                continue;
            }
            e.render(batch, null);
        }
        batch.end();
    }

    // ────────────────────────────────────────────
    //  HUD RENDERING
    // ────────────────────────────────────────────
    private void drawHUD() {
        float viewW = hudCamera.viewportWidth;
        float viewH = hudCamera.viewportHeight;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);

        // Dark panel behind HUD so text is always readable regardless of bg colour
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.45f);
        sr.rect(0, viewH - 100, 210, 100);
        sr.end();

        // Health & fuel bars
        healthBar.render(null, sr);
        fuelBar.render(null, sr);

        // Text
        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP",   165, viewH - 13);
        font.draw(batch, "FUEL", 165, viewH - 38);

        int altitude    = (int) rocket.getPosY();
        float distToMoon = MOON_Y - rocket.getPosY();
        font.draw(batch, "ALT: " + altitude + "m", 10, viewH - 70);
        if (distToMoon > 0) {
            font.draw(batch, "MOON: " + (int) distToMoon + "m", 10, viewH - 90);
        }

        // Direction arrow
        if (!gameWon && distToMoon > 0) {
            float dx = (MOON_X + MOON_SIZE / 2f) - (rocket.getPosX() + rocket.getWidth() / 2f);
            String dir = Math.abs(dx) < 100 ? "^" : (dx > 0 ? ">>>" : "<<<");
            font.draw(batch, "MOON " + dir, viewW - 120, viewH - 15);
        }

        // Win overlay
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

        // Game over overlay
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

    // ────────────────────────────────────────────
    //  TILED BACKGROUND
    // ────────────────────────────────────────────
    private void drawTiledBackground(SpriteBatch batch, float camX, float camY,
                                     float halfW, float halfH) {
        int startTX = MathUtils.floor((camX - halfW) / TILE_SIZE);
        int endTX   = MathUtils.floor((camX + halfW) / TILE_SIZE);
        int startTY = MathUtils.floor((camY - halfH) / TILE_SIZE);
        int endTY   = MathUtils.floor((camY + halfH) / TILE_SIZE);

        for (int tx = startTX; tx <= endTX; tx++) {
            for (int ty = startTY; ty <= endTY; ty++) {
                float worldX      = tx * TILE_SIZE;
                float worldY      = ty * TILE_SIZE;
                float tileCenterY = worldY + TILE_SIZE / 2f;

                if (worldY + TILE_SIZE <= 0 || worldY < 0) {
                    // Ground layer
                    batch.draw(groundTile, worldX, worldY, TILE_SIZE, TILE_SIZE);

                } else if (tileCenterY < EARTH_ZONE_END) {
                    // Sky zone
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);

                } else if (tileCenterY > SPACE_ZONE_START) {
                    // Space zone
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);

                } else {
                    // Atmosphere transition — space base, sky fades out on top
                    float t = (tileCenterY - EARTH_ZONE_END)
                            / (SPACE_ZONE_START - EARTH_ZONE_END);
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1 - t);
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1);
                }
            }
        }
    }

    // ────────────────────────────────────────────
    //  ZONE COLOUR (sky → space, used for glClearColor)
    // ────────────────────────────────────────────
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

    // ────────────────────────────────────────────
    //  COLLISION HANDLING
    // ────────────────────────────────────────────
    private void handleCollision(CollisionManager.CollisionInfo info) {
        if (gameWon || gameOver) return;

        if (info.isBetween("Rocket", "Asteroid") && damageCooldownTimer <= 0) {
            health -= DAMAGE_PER_HIT;
            if (health <= 0) {
                health = 0;
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop();
            }
            healthBar.setHP(health);
            damageCooldownTimer = DAMAGE_COOLDOWN;
            gameMaster.getIoManager().playCollisionEffect();
        }

        if (info.isBetween("Rocket", "Moon")) {
            gameWon = true;
            gameMaster.getIoManager().playWinEffect();
            gameMaster.getAudioManager().stopRocketLoop();
        }
    }

    // ────────────────────────────────────────────
    //  ASTEROID SPAWNING
    // ────────────────────────────────────────────
    private void spawnAsteroidsUpTo(float maxY) {
        while (highestSpawnedBand < maxY) {
            int count = 2 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                float x = (random.nextFloat() - 0.5f) * ASTEROID_FIELD_WIDTH;
                float y = highestSpawnedBand + random.nextFloat() * ASTEROID_BAND_HEIGHT;
                Asteroid asteroid = new Asteroid(x, y, 0, 50, 50);
                asteroids.add(asteroid);
                addSceneEntity(asteroid);
            }
            highestSpawnedBand += ASTEROID_BAND_HEIGHT;
        }
    }

    // ────────────────────────────────────────────
    //  LIFECYCLE
    // ────────────────────────────────────────────
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
        // isPaused is set to true by IOManager BEFORE switching to PauseScene,
        // so if isPaused == false here, we are being replaced (R / M), not paused.
        if (!isPaused) {
            clearSceneEntities();
            gameMaster.getCollisionManager().clearListeners();
            if (groundTile != null) { groundTile.dispose(); groundTile = null; }
            if (skyTile    != null) { skyTile.dispose();    skyTile    = null; }
            if (spaceTile  != null) { spaceTile.dispose();  spaceTile  = null; }
            if (font       != null) { font.dispose();       font       = null; }
            // Null cameras so show() fully re-initialises if this instance is ever reused
            gameCamera = null;
            hudCamera  = null;
        }
    }

    @Override
    public void dispose() {
        clearSceneEntities();
        gameMaster.getCollisionManager().clearListeners();
        if (groundTile != null) groundTile.dispose();
        if (skyTile    != null) skyTile.dispose();
        if (spaceTile  != null) spaceTile.dispose();
        if (font       != null) font.dispose();
    }
}
