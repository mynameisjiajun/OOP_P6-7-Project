package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMoveable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages entity movement.
 * Handles updating entity positions based on their movement strategies.
 */
public class MovementManager implements IMoveable {
    private List<Entity> entities;
    
    /**
     * Creates a new MovementManager.
     */
    public MovementManager() {
        this.entities = new ArrayList<>();
    }
    
    /**
     * Registers an entity for movement management.
     * @param entity The entity to manage
     */
    public void registerEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }
    //testing
    /**
     * Unregisters an entity from movement management.
     * @param entity The entity to remove
     */
    public void unregisterEntity(Entity entity) {
        entities.remove(entity);
    }
    
    /**
     * Updates all registered entities' movements.
     * @param deltaTime Time elapsed since last update
     */
    public void updateMovements(float deltaTime) {
        for (Entity entity : entities) {
            moveIdentity(entity, deltaTime, entity.getVx(), entity.getVy());
        }
    }
    
    @Override
    public void moveIdentity(Entity entity, float deltaTime, float vx, float vy) {
        if (entity == null) return;
        
        // Apply movement strategy if available
        if (entity.getMovementStrategy() != null) {
            entity.getMovementStrategy().updateIdleState(vx, vy);
        }
        
        // Update position based on velocity
        entity.setPosX(entity.getPosX() + vx * deltaTime);
        entity.setPosY(entity.getPosY() + vy * deltaTime);
    }
}
