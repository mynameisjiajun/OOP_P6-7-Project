package com.team.project.engine.interfaces;

/**
 * Interface for movement strategies.
 * Allows different movement behaviors to be implemented.
 */
public interface IMovementStrategy {
    /**
     * Updates the entity's idle state.
     * @param vx Current velocity in x direction
     * @param vy Current velocity in y direction
     */
    void updateIdleState(float vx, float vy);
}
