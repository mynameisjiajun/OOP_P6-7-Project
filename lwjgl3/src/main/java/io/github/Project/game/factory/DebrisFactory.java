package io.github.Project.game.factory;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Rocket;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Specialized Factory + Manager
 *
 * Creates all debris objects AND manages the live debris state:
 * flying/attached lists, hot-debris steering, bowl mechanics,
 * Kessler cascade, and win-condition tracking.
 *
 * Owned by GameObjectFactory; obtained via factory.getDebrisFactory().
 * Call setStationCenter() and setRocket() before spawnInitial().
 */
public class DebrisFactory {

    // ── Creation constants ───────────────────────────────────────────────
    private static final float DEBRIS_SIZE          = 50f;
    private static final float MIN_SPAWN_DISTANCE   = 30f;
    private static final float MAX_SPAWN_DISTANCE   = 120f;
    private static final float MIN_VELOCITY         = 30f;
    private static final float MAX_VELOCITY         = 80f;
    private static final int   SATELLITE_MIN_DEBRIS = 3;
    private static final int   SATELLITE_MAX_DEBRIS = 6;

    // ── Management constants ─────────────────────────────────────────────
    public  static final int   MAX_BOWL_CAPACITY             = 4;
    public  static final int   WIN_CLEAR_SCORE               = 20;
    public  static final float ATMOSPHERE_THRESHOLD          = 1400f;
    private static final int   MAX_DEBRIS_COUNT              = 28;
    private static final float DEBRIS_SPAWN_INTERVAL         = 10f;
    private static final float HOT_DEBRIS_SPEED              = 80f;
    private static final float HOT_STEER_STRENGTH            = 0.3f;   // steering lerp rate
    private static final float REENTRY_GRAVITY               = 120f;  // downward pull on launched debris (units/s²)
    private static final float STATION_WARN_RADIUS           = 600f;
    private static final float DEBRIS_SPAWN_MIN_Y_OFFSET     = 1200f; // raised so debris falls into station zone
    private static final float DEBRIS_SPAWN_MAX_Y_OFFSET     = 6000f; // wider range for varied fall distances
    private static final float COLD_DEBRIS_EVICT_Y           = 3800f; // below this, cold debris is respawned above
    private static final float DEBRIS_MIN_STATION_SPAWN_DIST = 450f;
    private static final float SPACE_ZONE_START              = 4000f;
    private static final int   KESSLER_TRIGGER_SCORE         = 13;
    private static final float KESSLER_SPAWN_INTERVAL        = 4f;
    private static final int   KESSLER_MAX_DEBRIS            = 30;
    private static final float BOWL_CAPTURE_RADIUS           = 60f;
    private static final float BOWL_RELEASE_SPEED            = 240f;
    private static final float BOWL_RELEASE_SPREAD           = 24f;
    private static final float BOWL_RELEASE_FORWARD_OFFSET   = 30f;
    private static final float BOWL_RELEASE_RECAPTURE_DELAY  = 0.8f;

    // ── Callbacks ────────────────────────────────────────────────────────
    @FunctionalInterface public interface EntityCallback { void apply(Debris d); }
    @FunctionalInterface public interface BurnCallback   { void onBurn(float x, float y); }

    private EntityCallback onEntityAdded;
    private EntityCallback onEntityRemoved;
    private BurnCallback   onAtmosphereBurn;
    private Runnable       onKesslerActivated;
    private Runnable       onWinConditionReached;
    private Runnable       onFirstHotDebris;

    // ── Live state ───────────────────────────────────────────────────────
    private final Array<Debris> flying   = new Array<>();
    private final Array<Debris> attached = new Array<>();

    private boolean kesslerActive       = false;
    private float   kesslerSpawnTimer   = 0f;
    private float   kesslerPulse        = 0f;
    private float   debrisSpawnTimer    = DEBRIS_SPAWN_INTERVAL;
    private int     debrisCollected     = 0;
    private boolean stationWarning      = false;
    private float   stationWarningPulse = 0f;
    private boolean hotDebrisNotified   = false;

    // ── Scene references (set before spawnInitial) ───────────────────────
    private float  stationCX;
    private float  stationCY;
    private Rocket rocket;

    // ────────────────────────────────────────────────────────────────────
    //  SETUP
    // ────────────────────────────────────────────────────────────────────

    public void setStationCenter(float cx, float cy) {
        this.stationCX = cx;
        this.stationCY = cy;
    }

    public void setRocket(Rocket rocket) { this.rocket = rocket; }

    // ── Callback setters ─────────────────────────────────────────────────

    public void setOnEntityAdded(EntityCallback cb)       { this.onEntityAdded         = cb; }
    public void setOnEntityRemoved(EntityCallback cb)     { this.onEntityRemoved        = cb; }
    public void setOnAtmosphereBurn(BurnCallback cb)      { this.onAtmosphereBurn       = cb; }
    public void setOnKesslerActivated(Runnable cb)        { this.onKesslerActivated     = cb; }
    public void setOnWinConditionReached(Runnable cb)     { this.onWinConditionReached  = cb; }
    public void setOnFirstHotDebris(Runnable cb)          { this.onFirstHotDebris       = cb; }

    // ────────────────────────────────────────────────────────────────────
    //  INITIALISE
    // ────────────────────────────────────────────────────────────────────

    public void spawnInitial() {
        for (int i = 0; i < MAX_DEBRIS_COUNT; i++) spawnOne();
    }

    // ────────────────────────────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────────────────────────────

    public void update(float delta) {
        // Remove destroyed flying debris
        for (int i = flying.size - 1; i >= 0; i--) {
            Debris d = flying.get(i);
            if (d.isDestroyed()) {
                flying.removeIndex(i);
                if (onEntityRemoved != null) onEntityRemoved.apply(d);
                d.dispose();
            }
        }

        stationWarning = false;

        for (int i = 0; i < flying.size; i++) {
            Debris d = flying.get(i);
            d.update(delta);

            // Atmosphere re-entry (launched by player)
            if (d.isReentryCandidate() && d.getPosY() < ATMOSPHERE_THRESHOLD) {
                float bx = d.getPosX() + d.getWidth()  / 2f;
                float by = d.getPosY() + d.getHeight() / 2f;
                flying.removeIndex(i);
                if (onEntityRemoved  != null) onEntityRemoved.apply(d);
                if (onAtmosphereBurn != null) onAtmosphereBurn.onBurn(bx, by);
                debrisCollected += d.getClearScore();
                d.dispose();
                checkWin();
                i--;
                continue;
            }

            // Reentry gravity — pulls launched debris down toward atmosphere
            if (d.isReentryCandidate()) {
                d.setVy(d.getVy() - REENTRY_GRAVITY * delta);
                continue; // skip zone eviction and steering for launched debris
            }

            // Evict cold debris that has drifted below the space zone — respawn above
            if (!d.isHot() && d.getPosY() < COLD_DEBRIS_EVICT_Y) {
                flying.removeIndex(i);
                if (onEntityRemoved != null) onEntityRemoved.apply(d);
                d.dispose();
                spawnOne();
                i--;
                continue;
            }

            // Hot debris steering toward station (gradual lerp — not instant snap)
            if (d.isHot()) {
                if (!hotDebrisNotified) {
                    hotDebrisNotified = true;
                    if (onFirstHotDebris != null) onFirstHotDebris.run();
                }
                float dx   = stationCX - (d.getPosX() + d.getWidth()  / 2f);
                float dy   = stationCY - (d.getPosY() + d.getHeight() / 2f);
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > 1f) {
                    float targetVx = dx / dist * HOT_DEBRIS_SPEED;
                    float targetVy = dy / dist * HOT_DEBRIS_SPEED;
                    d.setVx(d.getVx() + (targetVx - d.getVx()) * HOT_STEER_STRENGTH * delta);
                    d.setVy(d.getVy() + (targetVy - d.getVy()) * HOT_STEER_STRENGTH * delta);
                }
                if (dist < STATION_WARN_RADIUS) stationWarning = true;
            }
        }

        stationWarningPulse = stationWarning ? stationWarningPulse + delta * 6f : 0f;

        // Kessler Syndrome — triggers near end-game
        if (debrisCollected >= KESSLER_TRIGGER_SCORE && !kesslerActive) {
            kesslerActive = true;
            kesslerSpawnTimer = KESSLER_SPAWN_INTERVAL;
            for (int i = 0; i < flying.size; i++) flying.get(i).forceHot();
            if (onKesslerActivated != null) onKesslerActivated.run();
        }
        kesslerPulse = kesslerActive ? kesslerPulse + delta * 5f : 0f;
        if (kesslerActive) {
            kesslerSpawnTimer -= delta;
            if (kesslerSpawnTimer <= 0f && flying.size < KESSLER_MAX_DEBRIS) {
                kesslerSpawnTimer = KESSLER_SPAWN_INTERVAL;
                spawnOne();
                spawnOne();
                if (flying.size > 0) flying.peek().forceHot();
            }
        }

        // Debris top-up
        if (!kesslerActive) {
            debrisSpawnTimer -= delta;
            int total = flying.size + attached.size;
            if (debrisSpawnTimer <= 0f || total < MAX_DEBRIS_COUNT / 2) {
                debrisSpawnTimer = DEBRIS_SPAWN_INTERVAL;
                int need = MAX_DEBRIS_COUNT - total;
                for (int i = 0; i < need; i++) spawnOne();
            }
        }

        // Bowl mechanics — update attached positions + auto-catch
        if (rocket != null) {
            float rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
            float rCY = rocket.getPosY() + rocket.getHeight() / 2f;
            float rot = rocket.getRotation();

            for (int i = 0; i < attached.size; i++) {
                Debris d  = attached.get(i);
                float off = rocket.getHeight() / 2f + 12f + i * 10f;
                d.setPosX(rCX + MathUtils.cosDeg(rot) * off - d.getWidth()  / 2f);
                d.setPosY(rCY + MathUtils.sinDeg(rot) * off - d.getHeight() / 2f);
            }

            tryAutoCatch(rCX, rCY, rot);

            if (!attached.isEmpty() && rocket.getPosY() < ATMOSPHERE_THRESHOLD) {
                clearAttachedAtAtmosphere(rCX, rCY + rocket.getHeight() / 2f);
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  BOWL MECHANICS
    // ────────────────────────────────────────────────────────────────────

    public void attachDebris(Debris d) {
        if (d == null || d.isDestroyed() || d.isAttached()) return;
        if (!d.canBeCaptured()) return;
        if (attached.size >= MAX_BOWL_CAPACITY) return;
        d.setAttached(true);
        d.setReentryCandidate(false);
        flying.removeValue(d, true);
        if (onEntityRemoved != null) onEntityRemoved.apply(d);
        attached.add(d);
    }

    public void releaseAttachedInFacingDirection() {
        if (attached.isEmpty() || rocket == null) return;
        float rCX = rocket.getPosX() + rocket.getWidth()  / 2f;
        float rCY = rocket.getPosY() + rocket.getHeight() / 2f;
        float rot = rocket.getRotation();
        float dirX = MathUtils.cosDeg(rot), dirY = MathUtils.sinDeg(rot);
        float sideX = -dirY, sideY = dirX;
        int count = attached.size;
        for (int i = 0; i < count; i++) {
            Debris d     = attached.get(i);
            float spread = (i - (count - 1) * 0.5f) * BOWL_RELEASE_SPREAD;
            float forward = rocket.getHeight() * 0.5f + BOWL_RELEASE_FORWARD_OFFSET + i * 4f;
            d.setAttached(false);
            d.setReentryCandidate(true);
            d.setCaptureCooldown(BOWL_RELEASE_RECAPTURE_DELAY);
            d.setPosX(rCX + dirX * forward + sideX * spread * 0.4f - d.getWidth()  / 2f);
            d.setPosY(rCY + dirY * forward + sideY * spread * 0.4f - d.getHeight() / 2f);
            d.setVx(dirX * BOWL_RELEASE_SPEED + sideX * spread);
            d.setVy(dirY * BOWL_RELEASE_SPEED + sideY * spread);
            flying.add(d);
            if (onEntityAdded != null) onEntityAdded.apply(d);
        }
        attached.clear();
    }

    // ────────────────────────────────────────────────────────────────────
    //  CREATION METHODS
    // ────────────────────────────────────────────────────────────────────

    /** Slow-drifting debris for filling the space zone. */
    public Debris createSpaceDebris(float x, float y) {
        Debris debris = new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE, pickSpaceDebrisClass());
        float speed = MathUtils.random(10f, 30f);
        float angle = MathUtils.random(0f, 360f);
        debris.setVx(MathUtils.cosDeg(angle) * speed);
        // Downward bias so debris naturally drifts toward the station zone and builds heat
        debris.setVy(MathUtils.sinDeg(angle) * speed - MathUtils.random(8f, 18f));
        return debris;
    }

    /** Fast debris spawned when a satellite explodes. */
    public List<Debris> createSatelliteDebris(float x, float y) {
        int count = MathUtils.random(SATELLITE_MIN_DEBRIS, SATELLITE_MAX_DEBRIS);
        return createDebrisCloud(x, y, count);
    }

    public List<Debris> createDebrisCloud(float centerX, float centerY, int count) {
        List<Debris> debrisList = new ArrayList<>();
        float angleStep   = 360f / count;
        float angleOffset = MathUtils.random(0f, 360f);
        for (int i = 0; i < count; i++) {
            float angle    = i * angleStep + angleOffset + MathUtils.random(-angleStep * 0.3f, angleStep * 0.3f);
            float angleRad = (float) Math.toRadians(angle);
            float distance = MathUtils.random(MIN_SPAWN_DISTANCE, MAX_SPAWN_DISTANCE);
            float x        = centerX + (float) Math.cos(angleRad) * distance;
            float y        = centerY + (float) Math.sin(angleRad) * distance;
            Debris debris  = new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE, pickExplosionDebrisClass());
            float velocity = MathUtils.random(MIN_VELOCITY, MAX_VELOCITY);
            debris.setVx((float) Math.cos(angleRad) * velocity);
            debris.setVy((float) Math.sin(angleRad) * velocity);
            debrisList.add(debris);
        }
        return debrisList;
    }

    /** Single fast debris (used by satellite explosions). */
    public Debris createSingleDebris(float x, float y) {
        Debris debris = new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE, pickExplosionDebrisClass());
        float velocity = MathUtils.random(MIN_VELOCITY, MAX_VELOCITY);
        float angle    = MathUtils.random(0f, 360f);
        debris.setVx(MathUtils.cosDeg(angle) * velocity);
        debris.setVy(MathUtils.sinDeg(angle) * velocity);
        return debris;
    }

    // ────────────────────────────────────────────────────────────────────
    //  ACCESSORS
    // ────────────────────────────────────────────────────────────────────

    public Array<Debris> getFlying()              { return flying; }
    public Array<Debris> getAttached()            { return attached; }
    public int     getDebrisCollected()           { return debrisCollected; }
    public boolean isKesslerActive()              { return kesslerActive; }
    public float   getKesslerPulse()              { return kesslerPulse; }
    public boolean isStationWarningActive()       { return stationWarning; }
    public float   getStationWarningPulse()       { return stationWarningPulse; }

    // ────────────────────────────────────────────────────────────────────
    //  CLEANUP
    // ────────────────────────────────────────────────────────────────────

    public void dispose() {
        for (int i = 0; i < flying.size;   i++) flying.get(i).dispose();
        for (int i = 0; i < attached.size; i++) attached.get(i).dispose();
        flying.clear();
        attached.clear();
    }

    // ────────────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ────────────────────────────────────────────────────────────────────

    private void spawnOne() {
        float minDistSq = DEBRIS_MIN_STATION_SPAWN_DIST * DEBRIS_MIN_STATION_SPAWN_DIST;
        float x = 0f, y = 0f;
        for (int attempt = 0; attempt < 24; attempt++) {
            x = MathUtils.random(-900f, 900f);
            y = SPACE_ZONE_START + DEBRIS_SPAWN_MIN_Y_OFFSET
                + MathUtils.random(0f, DEBRIS_SPAWN_MAX_Y_OFFSET - DEBRIS_SPAWN_MIN_Y_OFFSET);
            float dx = x - stationCX, dy = y - stationCY;
            if (dx * dx + dy * dy >= minDistSq) break;
        }
        Debris d = createSpaceDebris(x, y);
        flying.add(d);
        if (onEntityAdded != null) onEntityAdded.apply(d);
    }

    private void tryAutoCatch(float rCX, float rCY, float rot) {
        if (attached.size >= MAX_BOWL_CAPACITY) return;
        float noseX = rCX + MathUtils.cosDeg(rot) * (rocket.getHeight() / 2f);
        float noseY = rCY + MathUtils.sinDeg(rot) * (rocket.getHeight() / 2f);
        float capSq = BOWL_CAPTURE_RADIUS * BOWL_CAPTURE_RADIUS;
        Debris best = null;
        float bestSq = Float.MAX_VALUE;
        for (int i = 0; i < flying.size; i++) {
            Debris d = flying.get(i);
            if (d.isDestroyed() || d.isAttached() || !d.canBeCaptured()) continue;
            float dx = (d.getPosX() + d.getWidth() / 2f) - noseX;
            float dy = (d.getPosY() + d.getHeight() / 2f) - noseY;
            float sq = dx * dx + dy * dy;
            if (sq <= capSq && sq < bestSq) { bestSq = sq; best = d; }
        }
        if (best != null) attachDebris(best);
    }

    private void clearAttachedAtAtmosphere(float bx, float by) {
        if (onAtmosphereBurn != null) onAtmosphereBurn.onBurn(bx, by);
        for (int i = 0; i < attached.size; i++) {
            debrisCollected += attached.get(i).getClearScore();
            attached.get(i).dispose();
        }
        attached.clear();
        checkWin();
    }

    private void checkWin() {
        if (debrisCollected >= WIN_CLEAR_SCORE && onWinConditionReached != null)
            onWinConditionReached.run();
    }

    private Debris.DebrisClass pickSpaceDebrisClass() {
        float r = MathUtils.random();
        if (r < 0.45f) return Debris.DebrisClass.SMALL;
        if (r < 0.85f) return Debris.DebrisClass.MEDIUM;
        return Debris.DebrisClass.LARGE;
    }

    private Debris.DebrisClass pickExplosionDebrisClass() {
        float r = MathUtils.random();
        if (r < 0.30f) return Debris.DebrisClass.SMALL;
        if (r < 0.75f) return Debris.DebrisClass.MEDIUM;
        return Debris.DebrisClass.LARGE;
    }
}
