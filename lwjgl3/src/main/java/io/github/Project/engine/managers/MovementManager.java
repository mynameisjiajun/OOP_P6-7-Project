package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import java.util.HashMap;
import java.util.Map;

// 1. REMOVE "implements IMoveable"
public class MovementManager { 
    
    private Map<Entity, IMovementStrategy> entityStrategies; // Map to hold entities and their movement strategies

    public MovementManager() {
        this.entityStrategies = new HashMap<>();
    }

    public void registerEntity(Entity entity, IMovementStrategy strategy) {
        if (entity != null && strategy != null && !entityStrategies.containsKey(entity)) {
            entityStrategies.put(entity, strategy);
        }
    }

    public void unregisterEntity(Entity entity) {
        entityStrategies.remove(entity);
    }

    /**
     * Updates all registered entities' movements.
     */
    public void updateMovements(float deltaTime) {
        for (Map.Entry<Entity, IMovementStrategy> entry : entityStrategies.entrySet()) {
            	Entity entity = entry.getKey();
            	IMovementStrategy strategy = entry.getValue();
            // A. Strategy Phase: Calculate Velocity (Input -> Velocity)
            // (The Player/Entity determines its own desired velocity)

            // B. Physics Phase: Apply Velocity (Velocity -> Position)
            // This is the "Shared Physics" logic that applies to everyone.
            float newX = entity.getPosX() + (entity.getVx() * deltaTime);
            float newY = entity.getPosY() + (entity.getVy() * deltaTime);

            entity.setPosX(newX);
            entity.setPosY(newY);
            strategy.updateVelocity(entity);
        }
    }
    
    // 3. DELETE 'moveIdentity' method entirely.
}