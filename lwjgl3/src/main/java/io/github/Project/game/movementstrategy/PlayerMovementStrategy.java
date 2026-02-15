package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.interfaces.InputMovement;
import io.github.Project.game.entities.Player;

public class PlayerMovementStrategy implements IMovementStrategy {
    
    @Override
	public void updateVelocity(Entity entity) {
    	Player player = (Player) entity;
    	InputMovement input = player.getInput();
    	float dirX = 0;
        float dirY = 0;

        if (input.keyUp)    dirY = 1;
        if (input.keyDown)  dirY = -1;
        if (input.keyLeft)  dirX = -1;
        if (input.keyRight) dirX = 1;

        // Normalize diagonals
        if (dirX != 0 && dirY != 0) {
            dirX *= 0.7071f;
            dirY *= 0.7071f;
        }

        float speed = entity.getSpeed();
        entity.setVx(dirX * speed);
        entity.setVy(dirY * speed);
    }
}

