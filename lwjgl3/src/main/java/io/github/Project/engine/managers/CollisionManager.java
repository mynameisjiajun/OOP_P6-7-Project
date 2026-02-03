package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages collision detection between entities.
 * Handles checking and resolving collisions.
 */
public class CollisionManager {
    private EntityManager entityManager;
    
    /**
     * Creates a new CollisionManager.
     * @param entityManager Reference to the entity manager
     */
    public CollisionManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /**
     * Checks for collisions between all entities.
     */
    public void checkCollisions() {
        List<Entity> entities = entityManager.getEntities();
        
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity entityA = entities.get(i);
                Entity entityB = entities.get(j);
                
                if (checkCollision(entityA, entityB)) {
                    handleCollision(entityA, entityB);
                }
            }
        }
    }
    
    /**
     * Checks if two entities are colliding using AABB collision detection.
     * @param entityA First entity
     * @param entityB Second entity
     * @return true if colliding, false otherwise
     */
    private boolean checkCollision(Entity entityA, Entity entityB) {
        return entityA.getPosX() < entityB.getPosX() + entityB.getWidth() &&
               entityA.getPosX() + entityA.getWidth() > entityB.getPosX() &&
               entityA.getPosY() < entityB.getPosY() + entityB.getHeight() &&
               entityA.getPosY() + entityA.getHeight() > entityB.getPosY();
    }
    
    /**
     * Handles collision between two entities.
     * @param entityA First entity
     * @param entityB Second entity
     */
    private void handleCollision(Entity entityA, Entity entityB) {
        // Override this method in subclasses or use a callback system
        // to define specific collision behaviors
    }
}
