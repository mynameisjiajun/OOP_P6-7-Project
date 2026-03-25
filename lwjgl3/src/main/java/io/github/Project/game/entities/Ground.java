package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * Ground landing pad entity.
 * 
 * GAME MECHANIC: "When rocket returns to Earth and the rocket's butt touches
 * the ground, it should refuel and restore health. If landing is improper,
 * rocket explodes → game over."
 * 
 * This entity represents the landing area where the rocket can safely land
 * to refuel and heal. The landing validation is handled by RocketCollisionStrategy.
 * 
 * COLLISION TAG: "Ground"
 * - Used by RocketCollisionStrategy to detect landing attempts
 * - Triggers landing validation (speed and angle checks)
 */
public class Ground extends CollidableEntity {
    
    private Texture texture;
    private final float width;
    private final float height;
    
    /**
     * Creates a ground landing pad.
     * 
     * @param x X position (usually at rocket spawn point)
     * @param y Y position (usually y=0, ground level)
     * @param width width of landing pad
     * @param height height of landing pad (collision detection zone)
     */
    public Ground(float x, float y, float width, float height) {
        super(x, y, 0, width, height);  // speed = 0 (static)
        
        this.width = width;
        this.height = height;
        
        // Use dirt/grass texture for visual representation
        this.texture = new Texture("Dirt texture/desert top.png");
        
        this.collisionTag = "Ground";
    }
    
    @Override
    public void update(float deltaTime) {
        // Ground is static, no update needed
        updateBounds();
    }
    
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Tile the texture across the ground width
        float tileWidth = texture.getWidth();
        int numTiles = (int) Math.ceil(width / tileWidth);
        
        for (int i = 0; i < numTiles; i++) {
            float x = getPosX() + (i * tileWidth);
            float drawWidth = Math.min(tileWidth, width - (i * tileWidth));
            
            // Updated the draw method to use the correct overload
            batch.draw(texture,
                x, getPosY(),
                0, 0,
                (int) drawWidth, (int) height,
                1.0f, 1.0f, // scaleX and scaleY
                0, // rotation
                0, 0, // srcX and srcY
                (int) drawWidth, (int) height, // srcWidth and srcHeight
                false, false // flipX and flipY
            );
        }
    }
    
    @Override
    public float getWidth() {
        return width;
    }
    
    @Override
    public float getHeight() {
        return height;
    }
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}