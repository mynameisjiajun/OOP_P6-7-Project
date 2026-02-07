package io.github.Project.engine.interfaces;

import io.github.Project.engine.entities.Entity;

public interface IMovementStrategy {
    /**
     * Calculates the velocity for the given entity.
     * @param entity The entity to update.
     */
    void updateVelocity(Entity entity);
}