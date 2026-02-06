package io.github.Project.engine.entities;

import com.badlogic.gdx.math.Rectangle;

/**
 * An entity that can collide with other CollidableEntities.
 * Has rectangular bounds for collision detection.
 */
public class CollidableEntity extends BaseEntity {
    protected Rectangle bounds;

    public CollidableEntity(float x, float y, float width, float height) {
        super(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        // Update bounds position to match transform
        bounds.setPosition(transform.x, transform.y);
        return bounds;
    }
}
