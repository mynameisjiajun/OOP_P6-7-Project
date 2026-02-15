package io.github.Project.engine.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * A middle-tier class that adds collision boundaries to an Entity.
 */
public abstract class CollidableEntity extends Entity {
    // physical hitbox position of the entity
    protected Rectangle bounds;
    
    // Collision tag for identifying entity types
    protected String collisionTag = "default";

    public CollidableEntity(float posX, float posY, float speed, float width, float height) {
        super(posX, posY, speed);
        // Initialize the rectangle at the entity's position
        this.bounds = new Rectangle(posX, posY, width, height);
    }

    /**
     * Updates the position to follow the entity.
     */
    public void updateBounds() {
        bounds.setPosition(getPosX(), getPosY());
    }

    public Rectangle getBounds() {
        return bounds;
    }
    
    // NEW: Collision tag accessors
    public String getCollisionTag() {
        return collisionTag;
    }
    
    public void setCollisionTag(String tag) {
        this.collisionTag = tag;
    }
}