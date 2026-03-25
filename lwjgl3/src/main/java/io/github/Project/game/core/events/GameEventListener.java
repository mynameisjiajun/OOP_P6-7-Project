package io.github.Project.game.core.events;

import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Satellite;

import java.util.List;

/**
 * Callback interface for game-level side effects triggered by collisions.
 * Implemented by PlayScene's inner PlaySceneEventHandler and consumed
 * by each collision strategy's static {@code create()} factory method.
 */
public interface GameEventListener {

    /** Called when the rocket lands successfully on the landing pad. */
    default void onPadLanding() {}

    /**
     * Called when the rocket crashes into the ground at unsafe speed or angle.
     *
     * @param speed impact speed in world units per second
     * @param angle rocket's angle at impact, in degrees
     */
    default void onCrashLanding(float speed, float angle) {}

    /**
     * Called each time the space station takes damage from a debris hit.
     *
     * @param dmg amount of damage dealt this hit
     * @param pct station health remaining as a fraction (0.0 – 1.0)
     */
    default void onStationDamaged(float dmg, float pct) {}

    /** Called when the space station's health reaches zero. */
    default void onStationDestroyed() {}

    /**
     * Called to trigger a visual hit effect at the point of debris impact.
     *
     * @param x world X coordinate of the impact
     * @param y world Y coordinate of the impact
     */
    default void onDebrisHitFx(float x, float y) {}

    /**
     * Called when a satellite is destroyed (e.g. hit by debris or rocket).
     *
     * @param sat the destroyed satellite instance
     */
    default void onSatelliteDestroyed(Satellite sat) {}

    /**
     * Called after a satellite explodes and scatters secondary debris.
     *
     * @param debris list of newly spawned debris fragments
     */
    default void onSatelliteDebrisSpawned(List<Debris> debris) {}

    /** Called to play a generic collision sound effect. */
    default void onCollisionSound() {}
}
