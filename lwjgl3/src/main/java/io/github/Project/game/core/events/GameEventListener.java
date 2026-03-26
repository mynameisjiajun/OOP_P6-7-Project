package io.github.Project.game.core.events;

import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Satellite;

import java.util.List;

// handles game-related events triggered by collisions
public interface GameEventListener {

    // rocket lands successfully
    default void onPadLanding() {}

    // rocket crashes on landing
    default void onCrashLanding(float speed, float angle) {}

    // space station takes damage
    default void onStationDamaged(float dmg, float pct) {}

    // space station destroyed (game over)
    default void onStationDestroyed() {}

    // trigger debris impact visual effect
    default void onDebrisHitFx(float x, float y) {}

    // satellite destroyed
    default void onSatelliteDestroyed(Satellite sat) {}

    // debris spawned from satellite destruction
    default void onSatelliteDebrisSpawned(List<Debris> debris) {}

    // play collision sound
    default void onCollisionSound() {}
}
