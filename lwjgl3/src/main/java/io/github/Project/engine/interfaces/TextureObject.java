package io.github.Project.engine.interfaces;

/**
 * Interface for texture/sprite handling.
 * Defines methods for rendering textured objects.
 */
public interface TextureObject {
    /**
     * Gets the texture identifier or path.
     * @return String representing the texture
     */
    String getTexture();
    
    /**
     * Sets the texture for this object.
     * @param texture The texture path or identifier
     */
    void setTexture(String texture);
    
    /**
     * Gets the width of the texture.
     * @return Width as float
     */
    float getWidth();
    
    /**
     * Gets the height of the texture.
     * @return Height as float
     */
    float getHeight();
    
    /**
     * Gets the X position for rendering.
     * @return X coordinate as float
     */
    float getX();
    
    /**
     * Gets the Y position for rendering.
     * @return Y coordinate as float
     */
    float getY();
    
    /**
     * Gets the movement strategy for texture animations.
     * @return IMovementStrategy instance
     */
    IMovementStrategy getMovementStrategy();
    
    /**
     * Sets the movement strategy.
     * @param strategy The movement strategy to use
     */
    void setMovementStrategy(IMovementStrategy strategy);
}
