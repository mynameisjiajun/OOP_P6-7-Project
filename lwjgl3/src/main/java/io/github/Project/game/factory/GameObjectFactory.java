package io.github.Project.game.factory;

import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.*;
import java.util.List;

/**
 * PATTERN: Abstract Factory
 * 
 * Coordinates multiple specialized factories to create complete
 * game object families. This is the high-level factory that client
 * code (PlayScene) interacts with.
 */
public class GameObjectFactory {
    private final EntityFactory entityFactory;
    private final DebrisFactory debrisFactory;
    
    public GameObjectFactory(InputMovement input) {
        this.entityFactory = new EntityFactory(input);
        this.debrisFactory = new DebrisFactory();
        new DamageCalculatorFactory();
    }
    
    // ── Delegate entity creation ────────────────────────────────────────
    
    public Rocket createRocket(float x, float y) {
        return entityFactory.createRocket(x, y);
    }
    
    public SpaceStation createSpaceStation(float x, float y) {
        return entityFactory.createSpaceStation(x, y);
    }
    
    public Satellite createSatellite(float x, float y) {
        return entityFactory.createSatellite(x, y);
    }
    
    public Ground createGround(float x, float y, float width) {
        return entityFactory.createGround(x, y, width);
    }
    
    // ── Delegate debris creation ────────────────────────────────────────
    
    public List<Debris> createSatelliteDebris(float x, float y) {
        return debrisFactory.createSatelliteDebris(x, y);
    }
    
    public Debris createDebris(float x, float y) {
        return debrisFactory.createSingleDebris(x, y);
    }
    
    // ── Delegate damage calculator creation ─────────────────────────────
    
    public DamageCalculator createDamageCalculator(String entityType) {
        return DamageCalculatorFactory.createForEntity(entityType);
    }
    
    // ── Access to specialized factories ─────────────────────────────────
    
    public DebrisFactory getDebrisFactory() {
        return debrisFactory;
    }
}