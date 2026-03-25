package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;

import io.github.Project.game.events.GameEventListener;
import io.github.Project.game.collisionstrategies.RocketCollisionStrategy;
import io.github.Project.game.collisionstrategies.SatelliteCollisionStrategy;
import io.github.Project.game.collisionstrategies.SpaceStationCollisionStrategy;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
import io.github.Project.game.entities.Ground;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.arrow;
import io.github.Project.game.factory.GameObjectFactory;
import io.github.Project.game.movementstrategy.RocketMovementStrategy;
import io.github.Project.game.factory.DebrisFactory;
import io.github.Project.game.rendering.WorldRenderer;
import io.github.Project.game.ui.HUDRenderer;

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
 *  - Clear 20 points of debris to win. Station health reaching 0 = game over.
 *
 * Rendering and debris management are delegated to WorldRenderer,
 * HUDRenderer and DebrisFactory.
 */
public class PlayScene extends Scene {

    // ── Space station placement ────────────────────────────────────────
    private static final float STATION_X = -150f;
    private static final float STATION_Y = 4500f;
    private static final float STATION_W = 300f;
    private static final float STATION_H = 163f;

    // ── Earth station (landing pad) ────────────────────────────────────
    private static final float EARTH_PAD_W = 300f;

    // ── Satellite system ───────────────────────────────────────────────
    private static final int   SATELLITE_COUNT            = 7;
    private static final float SATELLITE_ZONE_MIN_Y       = 5000f;
    private static final float SATELLITE_ZONE_MAX_Y       = 7000f;
    private static final float SATELLITE_X_MIN            = -800f;
    private static final float SATELLITE_X_MAX            =  800f;
    private static final float SATELLITE_MIN_STATION_DIST = 700f;
    private static final float SATELLITE_MIN_SPACING      = 400f;

    // ── Gameplay tuning ────────────────────────────────────────────────
    private static final float STATION_REPAIR_AMOUNT  = 20f;
    private static final float REPAIR_COOLDOWN        = 12f;
    private static final float REPAIR_MSG_DURATION    = 2.5f;

    // ── World boundary ─────────────────────────────────────────────────
    private static final float WORLD_HALF_WIDTH = 1200f;

    // ── Cameras ────────────────────────────────────────────────────────
    private OrthographicCamera gameCamera;
    private OrthographicCamera hudCamera;

    // ── Game entities ──────────────────────────────────────────────────
    private Rocket       rocket;
    private SpaceStation spaceStation;
    private EarthStation earthStation;
    private Array<Satellite> satellites;

    // ── Strategies ────────────────────────────────────────────────────
    private RocketMovementStrategy  rocketMovementStrategy;
    private RocketCollisionStrategy rocketStrategy;

    // ── Navigation arrow ──────────────────────────────────────────────
    private arrow navArrow;

    // ── Factory ────────────────────────────────────────────────────────
    private GameObjectFactory factory;

    // ── Managers ───────────────────────────────────────────────────────
    private DebrisFactory debrisManager;
    private WorldRenderer worldRenderer;
    private HUDRenderer   hudRenderer;

    // ── Collision listener ────────────────────────────────────────────
    private GameEventListener gameEventListener;

    // ── Game state ─────────────────────────────────────────────────────
    private boolean gameWon             = false;
    private boolean gameOver            = false;
    private String  gameOverReason      = "Space station destroyed";
    private float   repairMessageTimer  = 0f;
    private float   repairCooldownTimer = 0f;

    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    // ──────────────────────────────────────────────────────────────────
    //  SHOW
    // ──────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        gameMaster.getInputMovement().reset();

        // Pause / resume — just un-pause, keep all state intact
        if (gameCamera != null) {
            isPaused = false;
            Gdx.input.setInputProcessor(gameMaster.getInputMovement());
            return;
        }

        // ── One-time setup ─────────────────────────────────────────────
        factory = new GameObjectFactory(gameMaster.getInputMovement());

        initCameras();
        initEntities();
        initDebris();
        initRenderers();
        initCollisionStrategies();

        for (int i = 0; i < SATELLITE_COUNT; i++) spawnOneSatellite();

        Gdx.input.setInputProcessor(gameMaster.getInputMovement());
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
    }

    private void initCameras() {
        float viewW = Gdx.graphics.getWidth();
        float viewH = Gdx.graphics.getHeight();
        gameCamera = new OrthographicCamera(viewW, viewH);
        hudCamera  = new OrthographicCamera(viewW, viewH);
        hudCamera.position.set(viewW / 2f, viewH / 2f, 0);
        hudCamera.update();
    }

    private void initEntities() {
        spaceStation = factory.createSpaceStation(STATION_X, STATION_Y);
        addSceneEntity(spaceStation);

        earthStation = factory.createEarthStation(-EARTH_PAD_W / 2f, -50f);
        addSceneEntity(earthStation);

        rocket = factory.createRocket(0f, -30f);
        addSceneEntity(rocket);
        rocketMovementStrategy = new RocketMovementStrategy();
        gameMaster.getMovementManager().registerEntity(rocket, rocketMovementStrategy);

        Ground ground = factory.createGround(-600f, -50f, 1200f);
        addSceneEntity(ground);

        navArrow = factory.createArrow(rocket, spaceStation);
        satellites = new Array<>();
    }

    private void initDebris() {
        debrisManager = factory.getDebrisFactory();
        debrisManager.setStationCenter(STATION_X + STATION_W / 2f, STATION_Y + STATION_H / 2f);
        debrisManager.setRocket(rocket);

        debrisManager.setOnEntityAdded(d -> addSceneEntity(d));
        debrisManager.setOnEntityRemoved(d -> removeSceneEntity(d));

        debrisManager.setOnAtmosphereBurn((x, y) -> {
            if (worldRenderer != null) worldRenderer.triggerAtmosphereBurn(x, y);
        });

        debrisManager.setOnKesslerActivated(() ->
            hudRenderer.show(
                "KESSLER CASCADE: collisions now create more debris - clear the last pieces before it's too late!"));

        debrisManager.setOnFirstHotDebris(() ->
            hudRenderer.show(
                "Real orbital debris travels at ~28,000 km/h - 10x faster than a rifle bullet"));

        debrisManager.setOnWinConditionReached(() -> {
            gameWon = true;
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getIoManager().playWinEffect();
        });

        debrisManager.spawnInitial();
    }

    private void initRenderers() {
        worldRenderer = new WorldRenderer(gameMaster, rocket, spaceStation,
            earthStation, satellites, debrisManager);
        worldRenderer.init();

        io.github.Project.game.entities.healthbar stationBar =
            factory.createHealthBar(0f, 0f, 300f, 20f);
        hudRenderer = new HUDRenderer(gameMaster, rocket, spaceStation, debrisManager, stationBar);
        hudRenderer.show(
            "Over 27,000 pieces of tracked debris orbit Earth - this game shows why it matters");
    }

    private void initCollisionStrategies() {
        gameEventListener = createGameEventListener();
        rocketStrategy = RocketCollisionStrategy.create(rocket, debrisManager, gameEventListener);
        SpaceStationCollisionStrategy.create(spaceStation, factory, gameEventListener);
    }

    private GameEventListener createGameEventListener() {
        return new PlaySceneEventHandler();
    }



    // ──────────────────────────────────────────────────────────────────
    //  RENDER
    // ──────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        gameMaster.getIoManager().update();
        if (gameCamera == null) return;

        if (isPaused) {
            drawScene(delta);
            return;
        }

        if (!gameWon && !gameOver) {
            updateGameplay(delta);
            updateNavArrow();
        } else {
            handleGameEnd();
            return;
        }

        drawScene(delta);
    }

    private void updateGameplay(float delta) {
        gameMaster.getMovementManager().updateMovements(delta);
        rocketStrategy.update(delta);

        // Horizontal boundary clamp
        float left  = -WORLD_HALF_WIDTH;
        float right =  WORLD_HALF_WIDTH - rocket.getWidth();
        if (rocket.getPosX() < left)  { rocket.setPosX(left);  rocket.setVx(0f); }
        if (rocket.getPosX() > right) { rocket.setPosX(right); rocket.setVx(0f); }

        // Auto-level assist near ground
        if (rocket.getPosY() < 450f) {
            float rot  = rocket.getRotation();
            float diff = 90f - rot;
            if (diff > 180f)  diff -= 360f;
            if (diff < -180f) diff += 360f;
            rocket.setRotation(rot + diff * 2.5f * delta);
        }

        debrisManager.update(delta);
        worldRenderer.update(delta);

        // E key: release bowl in facing direction
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            debrisManager.releaseAttachedInFacingDirection();
        }

        gameMaster.getCollisionManager().checkCollisions(
            gameMaster.getEntityManager().getEntities());

        // Timers
        if (repairMessageTimer  > 0f) repairMessageTimer  -= delta;
        if (repairCooldownTimer > 0f) repairCooldownTimer -= delta;
    }

    private void updateNavArrow() {
        float rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
        float rCY = rocket.getPosY() + rocket.getHeight() / 2f;
        io.github.Project.engine.entities.Entity target = spaceStation;
        float minDist = Float.MAX_VALUE;
        Array<Debris> flying = debrisManager.getFlying();
        for (int i = 0; i < flying.size; i++) {
            Debris d  = flying.get(i);
            float  dx = (d.getPosX() + d.getWidth()  / 2f) - rCX;
            float  dy = (d.getPosY() + d.getHeight() / 2f) - rCY;
            float  sq = dx * dx + dy * dy;
            if (sq < minDist) { minDist = sq; target = d; }
        }
        navArrow.setTarget(target);
    }

    private void handleGameEnd() {
        if (gameWon) {
            gameMaster.getSceneManager().setState(
                new WinScene(gameMaster, debrisManager.getDebrisCollected(),
                             (int)(spaceStation.getHealthPercentage() * 100)));
        } else if (gameOver) {
            gameMaster.getIoManager().playGameOverSound();
            gameMaster.getSceneManager().setState(
                new GameOverScene(gameMaster, debrisManager.getDebrisCollected(), gameOverReason));
        }
    }

    private void drawScene(float delta) {
        worldRenderer.draw(delta, gameCamera);

        // Nav arrow in world space — after camera update inside draw()
        com.badlogic.gdx.graphics.glutils.ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        navArrow.update(delta);
        navArrow.render(null, sr);
        sr.end();

        hudRenderer.draw(hudCamera, repairCooldownTimer, repairMessageTimer, STATION_REPAIR_AMOUNT);
    }



    // ──────────────────────────────────────────────────────────────────
    //  SATELLITE SPAWNING
    // ──────────────────────────────────────────────────────────────────
    private void spawnOneSatellite() {
        float stationCX = STATION_X + STATION_W / 2f;
        float stationCY = STATION_Y + STATION_H / 2f;

        Satellite sat = factory.spawnSatellite(
            stationCX, stationCY, satellites,
            SATELLITE_MIN_STATION_DIST, SATELLITE_MIN_SPACING,
            SATELLITE_X_MIN, SATELLITE_X_MAX,
            SATELLITE_ZONE_MIN_Y, SATELLITE_ZONE_MAX_Y);

        SatelliteCollisionStrategy.create(sat, factory, gameEventListener);
        satellites.add(sat);
        addSceneEntity(sat);
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
            disposeAll();
            gameCamera = null;
            hudCamera  = null;
        }
    }

    @Override
    public void dispose() {
        clearSceneEntities();
        disposeAll();
    }

    private void disposeAll() {
        if (worldRenderer != null) { worldRenderer.dispose(); worldRenderer = null; }
        if (hudRenderer   != null) { hudRenderer.dispose();   hudRenderer   = null; }
        if (debrisManager != null) { debrisManager.dispose(); debrisManager = null; }
        if (satellites    != null) {
            for (int i = 0; i < satellites.size; i++) satellites.get(i).dispose();
            satellites.clear();
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  INNER CLASS: Game event handler
    // ──────────────────────────────────────────────────────────────────

    /**
     * Named inner class that handles collision-triggered game events.
     * Accesses PlayScene fields directly for scene-level side effects
     * (sounds, FX, game state transitions).
     */
    private class PlaySceneEventHandler implements GameEventListener {

        @Override public void onPadLanding() {
            if (repairCooldownTimer > 0f) return;
            spaceStation.heal(STATION_REPAIR_AMOUNT);
            repairMessageTimer  = REPAIR_MSG_DURATION;
            repairCooldownTimer = REPAIR_COOLDOWN;
            gameMaster.getIoManager().playRefuelEffect();
        }

        @Override public void onCrashLanding(float speed, float angle) {
            float rocketCX = rocket.getPosX() + rocket.getWidth() / 2f;
            if (rocketCX >= earthStation.getPosX()
                    && rocketCX <= earthStation.getPosX() + earthStation.getWidth()) {
                onPadLanding();
                return;
            }
            worldRenderer.triggerExplosion(
                rocket.getPosX() + rocket.getWidth()  / 2f,
                rocket.getPosY() + rocket.getHeight() / 2f);
            gameMaster.getIoManager().playCollisionEffect();
            gameOver = true;
            gameOverReason = "Rocket crashed on landing";
            gameMaster.getAudioManager().stopRocketLoop();
        }

        @Override public void onStationDamaged(float dmg, float pct) {
            gameMaster.getIoManager().playCollisionEffect();
        }

        @Override public void onStationDestroyed() {
            gameOver = true;
            gameOverReason = "Space station destroyed";
            gameMaster.getAudioManager().stopRocketLoop();
        }

        @Override public void onDebrisHitFx(float x, float y) {
            worldRenderer.triggerStationHitFx(x, y);
        }

        @Override public void onSatelliteDestroyed(Satellite sat) {
            satellites.removeValue(sat, true);
            removeSceneEntity(sat);
            sat.dispose();
            worldRenderer.triggerExplosion(
                sat.getPosX() + sat.getWidth()  / 2f,
                sat.getPosY() + sat.getHeight() / 2f);
            gameMaster.getIoManager().playCollisionEffect();
            hudRenderer.show(
                "Each satellite destroyed creates a debris cloud - exactly how Kessler Syndrome begins");
        }

        @Override public void onSatelliteDebrisSpawned(List<Debris> spawned) {
            for (Debris d : spawned) {
                debrisManager.getFlying().add(d);
                addSceneEntity(d);
            }
        }

        @Override public void onCollisionSound() {
            gameMaster.getIoManager().playCollisionEffect();
        }
    }
}
