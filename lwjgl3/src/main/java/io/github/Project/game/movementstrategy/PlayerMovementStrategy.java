package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Player;

public class PlayerMovementStrategy implements IMovementStrategy {
    
	@Override
	public void updateVelocity(Entity entity) {
	    Player player = (Player) entity;
	    InputMovement input = player.getInput();
	    
	    // Calculate movement
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
	    
	    // NEW: Apply screen boundaries
	    float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
	    float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
	    
	    // Constrain horizontal movement
	    if (player.getPosX() < 0) {
	        player.setPosX(0);
	        player.setVx(0);
	    }
	    if (player.getPosX() + player.getWidth() > screenWidth) {
	        player.setPosX(screenWidth - player.getWidth());
	        player.setVx(0);
	    }
	    
	    // Constrain vertical movement
	    if (player.getPosY() < 0) {
	        player.setPosY(0);
	        player.setVy(0);
	    }
	    if (player.getPosY() + player.getHeight() > screenHeight) {
	        player.setPosY(screenHeight - player.getHeight());
	        player.setVy(0);
	    }
	}
	// player.printDebugInfo();  // Uncomment this to see debug info
}