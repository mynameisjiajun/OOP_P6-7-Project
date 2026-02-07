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
        float directionX = 0;
        float directionY = 0;

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
     // FIX: Use getSpeed()
        float currentSpeed = entity.getSpeed(); 

        // FIX: Use setVx() and setVy()
        entity.setVx(directionX * currentSpeed);
        entity.setVy(directionY * currentSpeed);
    }
}