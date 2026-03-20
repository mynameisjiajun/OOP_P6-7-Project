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
import io.github.Project.game.entities.arrow;
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
    private static final float WORLD_HALF_WIDTH = 1200f;

    // ── Moon placement ──
    private static final float MOON_X    = -100f;
    private static final float MOON_Y    = 5000f;
    private static final float MOON_SIZE = 200f;

    // ── Gameplay tuning ──
    private static final float DAMAGE_COOLDOWN  = 1.5f;
    private static final float DAMAGE_PER_HIT   = 0.15f;
    private static final float FUEL_DRAIN_RATE  = 0.08f;

    // ── Asteroid spawning ──
    private static final float ASTEROID_BAND_HEIGHT = 400f;
    private static final float ASTEROID_FIELD_WIDTH = 2000f;

    // ── Background colours ──
    private static final Color COLOR_SKY   = new Color(0.53f, 0.81f, 0.98f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── Cameras ──
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Background tile textures ──
    private Texture groundTile;
    private Texture skyTile;
    private Texture spaceTile;

    // ── Explosion flash ──
    private Texture explosionTex;
    private float   explosionTimer;
    private float   explosionX, explosionY;
    private static final float EXPLOSION_DURATION = 0.35f;
    private static final float EXPLOSION_SIZE     = 80f;

    // ── Game entities ──
    private Rocket        rocket;
    private Moon          moon;
    private List<Asteroid> asteroids;

    // ── HUD ──
    private healthbar  healthBar;
    private Fuelbar    fuelBar;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private arrow arrow;

    // ── Game state ──
    private float   health = 1f;
    private float   fuel   = 1f;
    private float   damageCooldownTimer = 0f;
    private boolean gameWon  = false;
    private boolean gameOver = false;

    // ── Asteroid spawn tracking ──
    private float  highestSpawnedBand;
    private Random random;

    // ── Collision listener ──
    private CollisionManager.CollisionListener collisionListener;

    // ── Reusable colour ──
    private final Color tempColor = new Color();

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ────────────────────────────────────────────
    //  SHOW
    // ────────────────────────────────────────────
    @Override
    public void show() {
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
        skyTile    = new Texture(Gdx.files.internal("tiles/sky.png"));
        spaceTile  = new Texture(Gdx.files.internal("tiles/space.png"));

        // ── Explosion flash texture ──
        explosionTex = new Texture(Gdx.files.internal("Explosion_01.png"));

        // ── Moon (created first so strategy can reference it) ──
        moon = new Moon(MOON_X, MOON_Y, MOON_SIZE, MOON_SIZE);
        addSceneEntity(moon);

        // ── Rocket ──
        rocket = new Rocket(0, 0, 0, 32, 64, gameMaster.getInputMovement());
        addSceneEntity(rocket);
        gameMaster.getMovementManager().registerEntity(rocket, new RocketMovementStrategy(moon));
        
        // ── Arrow pointing to moon (created after moon and rocket as it refers to both ──
        arrow = new arrow (rocket, moon);
        // ── Asteroids ──
        asteroids = new ArrayList<>();
        highestSpawnedBand = SPACE_ZONE_START;
        spawnAsteroidsUpTo(MOON_Y + 1000f);

        // ── HUD ──
        healthBar   = new healthbar(10, viewH - 30, 150, 20);
        fuelBar     = new Fuelbar(10, viewH - 55, 150, 20);
        font        = new BitmapFont();
        glyphLayout = new GlyphLayout();

        // ── Collision listener ──
        gameMaster.getCollisionManager().clearListeners();
        collisionListener = info -> handleCollision(info);
        gameMaster.getCollisionManager().addListener(collisionListener);

        // ── Input ──
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

        if (gameCamera == null) return;

        if (isPaused) {
            drawWorld(0);
            drawHUD();
            return;
        }
        
     // This tells the list: "Look at every asteroid; if its destroyed flag is true, remove it."
        asteroids.removeIf(asteroid -> {
            if (asteroid.isDestroyed()) {
                asteroid.dispose(); // This calls the dispose() you already have in Asteroid.java
                return true; 
            }
            return false;
        });

        if (!gameWon && !gameOver) {
            gameMaster.getMovementManager().updateMovements(delta);

            // ── Horizontal boundary ──
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
            
         // Check if player ran out of fuel
            if (fuel <= 0 && !gameWon) {
                gameOver = true;
                gameMaster.getAudioManager().stopRocketLoop(); 
                // This stops the engine sound since the rocket is now a floating hunk of metal
            }

            if (rocket.getPosY() > highestSpawnedBand - 2000) {
                spawnAsteroidsUpTo(rocket.getPosY() + 3000);
            }
        }

        drawWorld(delta);
        drawHUD();
    }

    // ────────────────────────────────────────────
    //  WORLD RENDERING
    // ────────────────────────────────────────────
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

        // Entities — blink rocket during invincibility
        for (Entity e : sceneEntities) {
            if (e == rocket && damageCooldownTimer > 0
                    && ((int) (damageCooldownTimer * 10)) % 2 == 0) continue;
            e.render(batch, null);
        }

        // Explosion flash at collision point
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
    	ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
		sr.setProjectionMatrix(gameCamera.combined);
		arrow.update(delta);
		arrow.render(null, sr);

    }
    

    // ────────────────────────────────────────────
    //  HUD RENDERING
    // ────────────────────────────────────────────
    private void drawHUD() {
        float viewW = hudCamera.viewportWidth;
        float viewH = hudCamera.viewportHeight;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.45f);
        sr.rect(0, viewH - 100, 210, 100);
        sr.end();

        healthBar.render(null, sr);
        fuelBar.render(null, sr);

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
            float dx  = (MOON_X + MOON_SIZE / 2f) - (rocket.getPosX() + rocket.getWidth() / 2f);
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

    // ────────────────────────────────────────────
    //  TILED BACKGROUND
    // ────────────────────────────────────────────
    private void drawTiledBackground(SpriteBatch batch, float camX, float camY,
                                     float halfW, float halfH) {
        float left    = camX - halfW;
        float screenW = halfW * 2f;

        int startTX = MathUtils.floor(left / TILE_SIZE);
        int endTX   = MathUtils.floor((camX + halfW) / TILE_SIZE);
        int startTY = MathUtils.floor((camY - halfH) / TILE_SIZE);
        int endTY   = MathUtils.floor((camY + halfH) / TILE_SIZE);

        // ── Sky and space tiles (skip the ground band; drawn separately below) ──
        for (int ty = startTY; ty <= endTY; ty++) {
            for (int tx = startTX; tx <= endTX; tx++) {
                float worldX      = tx * TILE_SIZE;
                float worldY      = ty * TILE_SIZE;
                float tileCenterY = worldY + TILE_SIZE / 2f;

                // Skip tiles that are fully underground — ground strip handles those
                if (worldY + TILE_SIZE <= 0) continue;

                if (tileCenterY < EARTH_ZONE_END) {
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                } else if (tileCenterY > SPACE_ZONE_START) {
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                } else {
                    // Atmosphere fade: space underneath, sky fades out on top
                    float t = (tileCenterY - EARTH_ZONE_END)
                            / (SPACE_ZONE_START - EARTH_ZONE_END);
                    batch.draw(spaceTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1 - t);
                    batch.draw(skyTile, worldX, worldY, TILE_SIZE, TILE_SIZE);
                    batch.setColor(1, 1, 1, 1);
                }
            }
        }

        // ── Ground strip: grass.png drawn full-width, straddling y=0 ──
        // The asset has sky at the top and underground at the bottom.
        // Drawing it from y=-TILE_SIZE to y=+TILE_SIZE centres the grass
        // surface right at world y=0 (the launch pad level).
        float groundH = TILE_SIZE * 2f;
        batch.draw(groundTile, left, -TILE_SIZE, screenW, groundH);
    }

    // ────────────────────────────────────────────
    //  ZONE COLOUR
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

        // 1. Check for Rocket vs Asteroid
        if (info.isBetween("Rocket", "Asteroid")) {
            
            // This identifies which entity in the collision is the asteroid and marks it dead
            Asteroid asteroid = (Asteroid) (info.tag1.equals("Asteroid") ? info.entity1 : info.entity2);
            asteroid.setDestroyed(true); 
            // --------------------------------

            // 2. Handle the Rocket damage (only if cooldown is over)
            if (damageCooldownTimer <= 0) {
                health -= DAMAGE_PER_HIT;
                if (health <= 0) {
                    health = 0;
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
