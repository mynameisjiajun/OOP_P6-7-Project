package io.github.Project.game.entities;

import io.github.Project.engine.entities.Entity;
// Removed the BounceStrategy import to keep this class generic

public class NonPlayer extends Entity {
    
    private String texturePath;
    private float width;
    private float height;

    /**
     * Creates a new NonPlayer entity.
     * @param posX Initial X position
     * @param posY Initial Y position
     * @param width Entity width
     * @param height Entity height
     */
    public NonPlayer(float posX, float posY, float width, float height) {
        // 1. Initialize Parent
        // Pass 50f (default speed) to the super constructor.
        super(posX, posY, 50f); 
        
        this.width = width;
        this.height = height;

        // Note: movementStrategy is now null by default.
        // You must call setMovementStrategy() from your GameMaster/Level class
        // to make this entity move.
    }

    @Override
    public void update(float deltaTime) {
        // Only run logic if a strategy has been assigned
        if (movementStrategy != null) {
            movementStrategy.updateVelocity(this);
        }
    }

    // --- Texture Handling ---
    public void setTexture(String texturePath) {
        this.texturePath = texturePath;
    }

    public String getTexturePath() {
        return texturePath;
    }

    // --- Inherited Methods ---

    @Override
    public void render() {
        // Render NPC sprite logic
    }

    @Override
    public float getWidth() { return width; }

    @Override
    public float getHeight() { return height; }
}