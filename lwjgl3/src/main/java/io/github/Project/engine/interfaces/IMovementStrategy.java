package io.github.Project.engine.interfaces;

import io.github.Project.engine.entities.Entity;

public interface IMovementStrategy {
    void updateMovement(Entity entity, float deltaTime);
}