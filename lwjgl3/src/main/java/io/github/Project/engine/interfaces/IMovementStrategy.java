package io.github.Project.engine.interfaces;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.InputMovement;

// 1. The Interface
public interface IMovementStrategy {
    // Now we calculate the full movement velocity
    void updateVelocity(Entity entity);
}

// 2. The Player Strategy
class PlayerMovementStrategy implements IMovementStrategy {
    
    private InputMovement input;

    public PlayerMovementStrategy(InputMovement input) {
        this.input = input;
    }

    @Override
    public void updateVelocity(Entity entity) {
        // 1. Reset velocity to 0 (so we stop immediately when keys are released)
        float targetVx = 0;
        float targetVy = 0;

        // 2. Determine Direction from Input
        // (Note: We use temporary variables first to handle normalization)
        float dirX = 0;
        float dirY = 0;

        if (input.keyUp)    dirY = 1;
        if (input.keyDown)  dirY = -1;
        if (input.keyLeft)  dirX = -1;
        if (input.keyRight) dirX = 1;

        // 3. Apply Normalization (Fix diagonal speed)
        if (dirX != 0 && dirY != 0) {
            dirX *= 0.7071f;
            dirY *= 0.7071f;
        }

        // 4. Calculate Final Velocity (Direction * Speed)
        // The strategy now controls the Magnitude of the vector
        targetVx = dirX * entity.speed;
        targetVy = dirY * entity.speed;

        // 5. Apply to Entity
        entity.vx = targetVx;
        entity.vy = targetVy;
    }
}