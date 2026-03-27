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

    /**
     * Lifecycle state of a debris piece.
     *
     * Using an enum prevents the invalid boolean combinations that were
     * possible with separate {@code attached}, {@code reentryCandidate},
     * and {@code destroyed} fields (e.g. both attached AND reentry at once).
     */
    public enum DebrisState {
        /** Freely drifting in the space zone — the default state. */
        FLYING,
        /** Locked onto the rocket's nose bowl; position is driven by the rocket. */
        ATTACHED,
        /** Launched downward by the player; subject to reentry gravity and atmosphere burn. */
        REENTRY,
        /** Hit by a collision; will be removed from the world next frame. */
        DESTROYED
    }

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

    // Hot threshold range — randomised per instance so debris goes hot at staggered times
    private static final float HOT_THRESHOLD_MIN = 35f;
    private static final float HOT_THRESHOLD_MAX = 55f;

    // Kessler mode speed-based heat (units/s)
    private static final float HEAT_SPEED_MIN         = 12f;  // below this: cool in Kessler
    private static final float HEAT_SPEED_MAX_KESSLER = 60f;  // fully red at this speed in Kessler
    private boolean            kesslerMode            = false;

    // Regular debris texture variants
    private static final String[] DEBRIS_TEXTURES = {
        "images/entities/debris/Space Debris 1.png", "images/entities/debris/Space Debris 2.png",
        "images/entities/debris/Space Debris 3.png", "images/entities/debris/Space Debris 4.png",
        "images/entities/debris/Space Debris 5.png", "images/entities/debris/Space Debris 6.png"
    };

    // Shared hot texture (loaded once for all instances)
    private static Texture sharedHotTexture  = null;
    private static int     hotTextureUsers   = 0;

    // Per-instance fields
    private Texture texture;
    private final float width;
    private final float height;
    private float   rotation     = 0f;
    private final float baseRotationSpeed;
    private final DebrisClass debrisClass;

    // Heat / motion state
    private DebrisState state           = DebrisState.FLYING;
    private float   aliveTimer      = 0f;
    private final float hotThreshold;   // randomised per instance (HOT_THRESHOLD_MIN..MAX)
    private boolean hot             = false;
    private float   heatLevel       = 0f;   // 0=cool, 1=fully hot — driven by speed
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
        if (state != DebrisState.ATTACHED) {
            aliveTimer += deltaTime;
            if (aliveTimer >= hotThreshold) hot = true;
        }

        // Heat level: only active in Kessler mode — driven by speed (orange→red)
        if (kesslerMode) {
            float speed     = (float) Math.sqrt(getVx() * getVx() + getVy() * getVy());
            float target    = Math.max(0f, Math.min(1f,
                (speed - HEAT_SPEED_MIN) / (HEAT_SPEED_MAX_KESSLER - HEAT_SPEED_MIN)));
            float lerpRate  = target > heatLevel ? 4f : 1.5f;
            heatLevel += (target - heatLevel) * lerpRate * deltaTime;
        } else {
            heatLevel = 0f;
        }

        if (captureCooldown > 0f) {
            captureCooldown = Math.max(0f, captureCooldown - deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Hot texture only in Kessler mode at max speed
        Texture renderTex = (kesslerMode && heatLevel >= 0.99f && sharedHotTexture != null)
            ? sharedHotTexture : texture;

        if (kesslerMode) {
            // Kessler: orange → red gradient based on speed
            float g = 0.45f * (1f - heatLevel);
            batch.setColor(1f, g, 0f, 1f);
        } else if (hot) {
            // Normal hot: flat orange tint
            batch.setColor(1f, 0.45f, 0f, 1f);
        }
        // else: no tint (normal colour)

        // Shimmer pulse only in Kessler mode
        float pulse = (kesslerMode && heatLevel > 0.5f)
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

    // State accessors

    /**
     * Sets the lifecycle state of this debris piece.
     * Prefer this over the individual boolean setters that have been removed;
     * it guarantees only one valid state is active at a time.
     *
     * @param state the new state
     */
    public void       setState(DebrisState state)  { this.state = state; }

    /** @return the current lifecycle state */
    public DebrisState getState()                  { return state; }

    // Convenience state-check helpers (backed by the DebrisState enum)
    public boolean isDestroyed()       { return state == DebrisState.DESTROYED; }
    public boolean isAttached()        { return state == DebrisState.ATTACHED;  }
    public boolean isReentryCandidate(){ return state == DebrisState.REENTRY;   }

    public boolean isHot()                   { return hot; }
    public float   getHeatLevel()            { return heatLevel; }
    public void    forceHot()                { hot = true; }
    public void    setKesslerMode(boolean on){ kesslerMode = on; }

    public boolean canBeCaptured()                   { return captureCooldown <= 0f; }
    public void    setCaptureCooldown(float seconds) { this.captureCooldown = Math.max(0f, seconds); }
    public DebrisClass getDebrisClass()              { return debrisClass; }
    public int         getClearScore()               { return debrisClass.getClearScore(); }
    public float       getStationDamageMultiplier()  { return debrisClass.getStationDamageMultiplier(); }

    // Static hot-texture lifecycle (avoids writing statics from instance)

    private static void acquireHotTexture() {
        if (sharedHotTexture == null) {
            sharedHotTexture = new Texture("images/entities/debris/Space_Debris_Hot.png");
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

    // Cleanup

    public void dispose() {
        if (texture != null) { texture.dispose(); texture = null; }
        releaseHotTexture();
    }
}
