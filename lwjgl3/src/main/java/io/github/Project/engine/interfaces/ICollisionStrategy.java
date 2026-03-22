package io.github.Project.engine.interfaces;

import io.github.Project.engine.entities.CollidableEntity;

public interface ICollisionStrategy {
    
    /**
     * Handles collision response when this entity collides with another.
     * 
     * @param self the entity that owns this strategy
     * @param other the entity that was collided with
     */
    void handleCollision(CollidableEntity self, CollidableEntity other);
}
