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
 * "heats up" — PlayScene begins steering it toward the space station.
 *
 * Visual heat level (0→1) is driven by the debris's current speed, so
 * the faster it moves the redder it looks. Slow ambient drift = cool/grey,
 * station-bound debris = yellow/orange, fast reentry fall = blazing red.
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

    // ── Hot threshold range — randomised per instance so debris goes hot at staggered times ──
    private static final float HOT_THRESHOLD_MIN = 35f;
    private static final float HOT_THRESHOLD_MAX = 55f;

    // ── Speed thresholds for visual heat level (units/s) ──────────────────
    private static final float HEAT_SPEED_MIN  = 12f;   // below this: cool
    private static final float HEAT_SPEED_MAX  = 40f;   // at this speed: fully hot visually
    private static final float HOT_HEAT_BONUS  = 0.30f; // added to heatLevel when hot flag fires

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
    private final float baseRotationSpeed;
    private boolean destroyed    = false;
    private final DebrisClass debrisClass;

    // ── Heat / attached state ─────────────────────────────────────────────
    private float   aliveTimer      = 0f;
    private final float hotThreshold;   // randomised per instance (HOT_THRESHOLD_MIN..MAX)
    private boolean hot             = false;
    private float   heatLevel       = 0f;   // 0=cool, 1=fully hot — driven by speed
    private boolean attached        = false;
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

        this.collisionTag      = "Debris";
        this.baseRotationSpeed = MathUtils.random(-60f, 60f);
        this.hotThreshold      = MathUtils.random(HOT_THRESHOLD_MIN, HOT_THRESHOLD_MAX);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);   // applies vx/vy → position

        // Spin speed scales with heat — hotter = more chaotic tumbling
        rotation += baseRotationSpeed * (1f + heatLevel * 1.5f) * deltaTime;
        updateBounds();

        // Track alive time only while freely floating (not attached to rocket)
        if (!attached) {
            aliveTimer += deltaTime;
            if (aliveTimer >= hotThreshold) hot = true;
        }

        // Heat level: driven by current speed + bonus when the hot flag fires
        float speed      = (float) Math.sqrt(getVx() * getVx() + getVy() * getVy());
        float speedHeat  = Math.max(0f, Math.min(1f,
            (speed - HEAT_SPEED_MIN) / (HEAT_SPEED_MAX - HEAT_SPEED_MIN)));
        float targetHeat = Math.min(1f, speedHeat + (hot ? HOT_HEAT_BONUS : 0f));
        // Lerp smoothly — heat builds fast, cools slowly (inertia feel)
        float lerpRate   = targetHeat > heatLevel ? 4f : 1.5f;
        heatLevel += (targetHeat - heatLevel) * lerpRate * deltaTime;

        if (captureCooldown > 0f) {
            captureCooldown = Math.max(0f, captureCooldown - deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Switch to hot texture once visually hot enough
        Texture renderTex = (heatLevel >= 0.75f && sharedHotTexture != null)
            ? sharedHotTexture : texture;

        // Continuous colour gradient: white → yellow → orange → red
        float r, g, b;
        if (heatLevel < 0.33f) {
            float t = heatLevel / 0.33f;
            r = 1f; g = 1f; b = 1f - t;          // white → yellow
        } else if (heatLevel < 0.66f) {
            float t = (heatLevel - 0.33f) / 0.33f;
            r = 1f; g = 1f - t * 0.55f; b = 0f;  // yellow → orange
        } else {
            float t = (heatLevel - 0.66f) / 0.34f;
            r = 1f; g = 0.45f - t * 0.45f; b = 0f; // orange → red
        }
        batch.setColor(r, g, b, 1f);

        // Gentle shimmer pulse when hot (scale oscillates ±5%)
        float pulse = heatLevel > 0.5f
            ? 1f + (float) Math.sin(aliveTimer * 10.0) * 0.05f * heatLevel
            : 1f;

        batch.draw(renderTex,
            getPosX(), getPosY(),
            width / 2f, height / 2f,
            width, height,
            pulse, pulse, rotation,
            0, 0, renderTex.getWidth(), renderTex.getHeight(),
            false, false);

        batch.setColor(1f, 1f, 1f, 1f); // always reset tint
    }

    @Override public float getWidth()  { return width;  }
    @Override public float getHeight() { return height; }

    // ── State accessors ───────────────────────────────────────────────────

    public void    setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public boolean isDestroyed()                   { return destroyed; }

    public boolean isHot()          { return hot; }
    public float   getHeatLevel()   { return heatLevel; }
    public void    forceHot()       { hot = true; }

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
