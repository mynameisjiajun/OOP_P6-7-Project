package io.github.Project.game.factory;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.*;

import java.util.List;

/**
 * PATTERN: Abstract Factory
 *
 * Coordinates all specialised factories (EntityFactory, DebrisFactory,
 * DamageCalculatorFactory) so PlayScene only needs to talk to one object.
 * New entity types are added here without touching PlayScene.
 */
public class GameObjectFactory {

    private final EntityFactory entityFactory;
    private final DebrisFactory debrisFactory;

    public GameObjectFactory(InputMovement input) {
        this.entityFactory = new EntityFactory(input);
        this.debrisFactory = new DebrisFactory();
    }

    // ── World entity creation ────────────────────────────────────────────

    public Rocket createRocket(float x, float y) {
        return entityFactory.createRocket(x, y);
    }

    public SpaceStation createSpaceStation(float x, float y) {
        return entityFactory.createSpaceStation(x, y);
    }

    public EarthStation createEarthStation(float x, float y) {
        return entityFactory.createEarthStation(x, y);
    }

    public Satellite createSatellite(float x, float y) {
        return entityFactory.createSatellite(x, y);
    }

    public Ground createGround(float x, float y, float width) {
        return entityFactory.createGround(x, y, width);
    }

    public Moon createMoon(float x, float y) {
        return entityFactory.createMoon(x, y);
    }

    public arrow createArrow(Entity source, Entity target) {
        return entityFactory.createArrow(source, target);
    }

    // ── Debris creation ──────────────────────────────────────────────────

    /** Slow-drifting debris for filling the space zone. */
    public Debris createSpaceDebris(float x, float y) {
        return debrisFactory.createSpaceDebris(x, y);
    }

    /** Fast debris spawned when a satellite explodes. */
    public List<Debris> createSatelliteDebris(float x, float y) {
        return debrisFactory.createSatelliteDebris(x, y);
    }

    // ── HUD element creation ─────────────────────────────────────────────

    public healthbar createHealthBar(float x, float y) {
        return entityFactory.createHealthBar(x, y);
    }

    /** Custom-sized health bar — e.g. the large centered station bar. */
    public healthbar createHealthBar(float x, float y, float width, float height) {
        return entityFactory.createHealthBar(x, y, width, height);
    }

    public Fuelbar createFuelBar(float x, float y) {
        return entityFactory.createFuelBar(x, y);
    }

    // ── Damage calculators ───────────────────────────────────────────────

    public DamageCalculator createDamageCalculator(String entityType) {
        return DamageCalculatorFactory.createForEntity(entityType);
    }

    // ── Access to specialised factories ─────────────────────────────────

    public DebrisFactory getDebrisFactory() { return debrisFactory; }
    public EntityFactory getEntityFactory() { return entityFactory; }
}
