package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

// 1. REMOVE "implements IMoveable"
public class MovementManager { 
    
    private List<Entity> entities;

    public MovementManager() {
        this.entities = new ArrayList<>();
    }

    public void registerEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public void unregisterEntity(Entity entity) {
        entities.remove(entity);
    }

    /**
     * Updates all registered entities' movements.
     */
    public void updateMovements(float deltaTime) {
        for (Entity entity : entities) {
            // 2. LOGIC MOVED HERE
            // We don't need 'moveIdentity'. We just do the work right here.

            // A. Strategy Phase: Calculate Velocity (Input -> Velocity)
            // (The Player/Entity determines its own desired velocity)
            entity.update(deltaTime); 

            // B. Physics Phase: Apply Velocity (Velocity -> Position)
            // This is the "Shared Physics" logic that applies to everyone.
            float newX = entity.getPosX() + (entity.getVx() * deltaTime);
            float newY = entity.getPosY() + (entity.getVy() * deltaTime);

            entity.setPosX(newX);
            entity.setPosY(newY);
        }
    }
    
    // 3. DELETE 'moveIdentity' method entirely.
}