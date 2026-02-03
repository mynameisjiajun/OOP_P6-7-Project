package io.github.Project.game.entities;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;

/**
 * Non-Player entity - represents NPCs, enemies, or other AI-controlled entities.
 * Handles AI behavior and rendering.
 */
public class NonPlayer extends Entity {
    private String texturePath;
    private float width;
    private float height;
    private float speed;
    
    /**
     * Creates a new NonPlayer entity.
     * @param posX Initial X position
     * @param posY Initial Y position
     * @param width Entity width
     * @param height Entity height
     */
    public NonPlayer(float posX, float posY, float width, float height) {
        super(posX, posY);
        this.width = width;
        this.height = height;
        this.speed = 50f; // Default speed (slower than player)
    }
    
    /**
     * Sets the entity's texture.
     * @param texturePath Path to the texture file
     */
    public void setTexture(String texturePath) {
        this.texturePath = texturePath;
    }
    
    /**
     * Gets the entity's texture path.
     * @return Texture path
     */
    public String getTexturePath() {
        return texturePath;
    }
    
    /**
     * Sets the entity's movement strategy.
     * @param strategy The movement strategy to use
     */
    @Override
    public void setMovementStrategy(IMovementStrategy strategy) {
        super.setMovementStrategy(strategy);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update NPC logic
        // TODO: Implement AI behavior, pathfinding, state machine
        
        if (movementStrategy != null) {
            movementStrategy.updateIdleState(vx, vy);
        }
    }
    
    @Override
    public void render() {
        // Render NPC sprite
        // TODO: Draw NPC texture at posX, posY
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
