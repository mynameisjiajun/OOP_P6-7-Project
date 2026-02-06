package io.github.Project.game.entities;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;

/**
 * Player entity - represents the player character.
 * Handles player-specific behavior and rendering.
 */
public class Player extends Entity {
    private String texturePath;
    private float width;
    private float height;
    private float speed;
    
    /**
     * Creates a new Player entity.
     * @param posX Initial X position
     * @param posY Initial Y position
     * @param width Player width
     * @param height Player height
     */
    public Player(float posX, float posY, float width, float height) {
        super(posX, posY);
        this.width = width;
        this.height = height;
        this.speed = 100f; // Default speed
    }
    
    /**
     * Sets the player's texture.
     * @param texturePath Path to the texture file
     */
    public void setTexture(String texturePath) {
        this.texturePath = texturePath;
    }
    
    /**
     * Gets the player's texture path.
     * @return Texture path
     */
    public String getTexturePath() {
        return texturePath;
    }
    
    /**
     * Sets the player's movement strategy.
     * @param strategy The movement strategy to use
     */
    @Override
    public void setMovementStrategy(IMovementStrategy strategy) {
        super.setMovementStrategy(strategy);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update player logic
        // TODO: Handle player input, animations, state changes
        
        if (movementStrategy != null) {
            movementStrategy.updateIdleState(vx, vy);
        }
    }
    
    @Override
    public void render() {
        // Render player sprite
        // TODO: Draw player texture at posX, posY
    }
    
    @Override
    public float getWidth() {
        return width;
    }
    
    @Override
    public float getHeight() {
        return height;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
