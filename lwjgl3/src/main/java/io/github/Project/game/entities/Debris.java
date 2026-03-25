package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * Flying space debris that drifts through the space zone.
 *
 * After HOT_DEBRIS_THRESHOLD seconds of floating freely the debris
 * "heats up" — it switches to the hot texture and PlayScene begins
 * steering it toward the space station (danger mechanic).
 *
 * The rocket's bowl can attach up to MAX_BOWL_CAPACITY debris at once.
 * Attached debris follow the rocket nose and burn up when the rocket
 * descends through the atmosphere threshold.
 *
 * Hot texture is shared across all instances (static) to avoid
 * loading a new GPU texture for every piece of debris.
 */
public class Debris extends CollidableEntity {
    public enum DebrisClass {
        SMALL(0.75f, 1, 0.75f),
        MEDIUM(1.00f, 1, 1.00f),
        LARGE(1.30f, 2, 1.45f);

        private final float sizeScale;
        private final int clearScore;
        private final float stationDamageMultiplier;

        DebrisClass(float sizeScale, int clearScore, float stationDamageMultiplier) {
            this.sizeScale = sizeScale;
            this.clearScore = clearScore;
            this.stationDamageMultiplier = stationDamageMultiplier;
        }

        public float getSizeScale() { return sizeScale; }
        public int getClearScore() { return clearScore; }
        public float getStationDamageMultiplier() { return stationDamageMultiplier; }
    }

    // ── How long before debris turns "hot" and drifts toward station ─────
    public static final float HOT_DEBRIS_THRESHOLD = 28f;

    // ── Regular debris texture variants ──────────────────────────────────
    private static final String[] DEBRIS_TEXTURES = {
        "Space Debris/Space Debris 1.png", "Space Debris/Space Debris 2.png",
        "Space Debris/Space Debris 3.png", "Space Debris/Space Debris 4.png",
        "Space Debris/Space Debris 5.png", "Space Debris/Space Debris 6.png"
    };

    // ── Shared hot texture (loaded once for all instances) ────────────────
    private static Texture sharedHotTexture  = null;
    private static int     hotTextureUsers   = 0;

    // ── Per-instance fields ───────────────────────────────────────────────
    private Texture texture;
    private final float width;
    private final float height;
    private float   rotation     = 0f;
    private float   rotationSpeed;
    private boolean destroyed    = false;
    private final DebrisClass debrisClass;

    // ── Hot / attached state ──────────────────────────────────────────────
    private float   aliveTimer   = 0f;
    private boolean hot          = false;
    private boolean attached     = false;
    private boolean reentryCandidate = false;
    private float   captureCooldown = 0f;

    public Debris(float x, float y, float speed, float width, float height) {
        this(x, y, speed, width, height, DebrisClass.MEDIUM);
    }

    public Debris(float x, float y, float speed, float width, float height, DebrisClass debrisClass) {
        super(x, y, speed,
            width * debrisClass.getSizeScale(),
            height * debrisClass.getSizeScale());
        this.debrisClass = debrisClass;
        this.width  = width * debrisClass.getSizeScale();
        this.height = height * debrisClass.getSizeScale();

        // Random regular texture (LibGDX MathUtils — no java.util.Random needed)
        int idx = MathUtils.random(DEBRIS_TEXTURES.length - 1);
        this.texture = new Texture(DEBRIS_TEXTURES[idx]);

        // Shared hot texture — load once, ref-count for safe disposal
        acquireHotTexture();

        this.collisionTag  = "Debris";
        this.rotationSpeed = MathUtils.random(-60f, 60f);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);   // applies vx/vy → position
        rotation += rotationSpeed * deltaTime;
        updateBounds();

        // Track alive time only while freely floating
        if (!attached) {
            aliveTimer += deltaTime;
            if (aliveTimer >= HOT_DEBRIS_THRESHOLD) hot = true;
        }
        if (captureCooldown > 0f) {
            captureCooldown = Math.max(0f, captureCooldown - deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        Texture renderTex = (hot && sharedHotTexture != null) ? sharedHotTexture : texture;
        batch.draw(renderTex,
            getPosX(), getPosY(),
            width / 2f, height / 2f,
            width, height,
            1f, 1f, rotation,
            0, 0, renderTex.getWidth(), renderTex.getHeight(),
            false, false);
    }

    @Override public float getWidth()  { return width;  }
    @Override public float getHeight() { return height; }

    // ── State accessors ───────────────────────────────────────────────────

    public void    setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public boolean isDestroyed()                   { return destroyed; }

    public boolean isHot()                         { return hot; }
    public void    forceHot()                      { hot = true; }

    public boolean isAttached()                    { return attached; }
    public void    setAttached(boolean attached)   { this.attached = attached; }

    public boolean isReentryCandidate()                    { return reentryCandidate; }
    public void    setReentryCandidate(boolean candidate)  { this.reentryCandidate = candidate; }
    public boolean canBeCaptured()                         { return captureCooldown <= 0f; }
    public void    setCaptureCooldown(float seconds)       { this.captureCooldown = Math.max(0f, seconds); }
    public DebrisClass getDebrisClass()                    { return debrisClass; }
    public int getClearScore()                             { return debrisClass.getClearScore(); }
    public float getStationDamageMultiplier()              { return debrisClass.getStationDamageMultiplier(); }

    // ── Static hot-texture lifecycle (avoids writing statics from instance) ─

    private static void acquireHotTexture() {
        if (sharedHotTexture == null) {
            sharedHotTexture = new Texture("New space assets/Space_Debris_Hot.png");
        }
        hotTextureUsers++;
    }

    private static void releaseHotTexture() {
        hotTextureUsers--;
        if (hotTextureUsers <= 0 && sharedHotTexture != null) {
            sharedHotTexture.dispose();
            sharedHotTexture = null;
            hotTextureUsers  = 0;
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────

    public void dispose() {
        if (texture != null) { texture.dispose(); texture = null; }
        releaseHotTexture();
    }
}
