package io.github.Project.engine.managers;
import io.github.Project.engine.entities.Entity;

/**
 * Interface for Collision Management.
 * Defines the contract for handling interactions between entities.
 */
public interface CollisionManager {
    // Core method to resolve interaction between any two Entities
    void handleCollision(Entity e1, Entity e2);
}