package io.github.Project.game.events;

import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Satellite;

import java.util.List;

/**
 * Callback interface for game-level side effects triggered by collisions.
 * Implemented by PlayScene's inner PlaySceneEventHandler and consumed
 * by each collision strategy's static {@code create()} factory method.
 */
public interface GameEventListener {
    void onPadLanding();
    void onCrashLanding(float speed, float angle);
    void onStationDamaged(float dmg, float pct);
    void onStationDestroyed();
    void onDebrisHitFx(float x, float y);
    void onSatelliteDestroyed(Satellite sat);
    void onSatelliteDebrisSpawned(List<Debris> debris);
    void onCollisionSound();
}
