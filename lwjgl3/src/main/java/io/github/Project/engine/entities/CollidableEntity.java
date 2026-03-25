package io.github.Project.engine.entities;

import com.badlogic.gdx.math.Rectangle;

public abstract class CollidableEntity extends Entity {
    /** Physical hitbox used for overlap/collision detection. */
    protected Rectangle bounds;

    /** Identifies this entity's type during collision dispatch. */
    protected String collisionTag = "default";

    /**
     * Constructs a collidable entity with an axis-aligned bounding box.
     *
     * @param posX   initial X position in world units
     * @param posY   initial Y position in world units
     * @param speed  base movement speed
     * @param width  hitbox width in world units
     * @param height hitbox height in world units
     */
    protected CollidableEntity(float posX, float posY, float speed, float width, float height) {
        super(posX, posY, speed);
        this.bounds = new Rectangle(posX, posY, width, height);
    }

    /**
     * Syncs the hitbox position to this entity's current world position.
     * Must be called each frame after position is updated.
     */
    public void updateBounds() {
        bounds.setPosition(getPosX(), getPosY());
    }

    /**
     * Returns the axis-aligned bounding box used for collision detection.
     *
     * @return this entity's hitbox rectangle
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the collision tag that identifies this entity's type.
     *
     * @return collision tag string (e.g. "debris", "station", "rocket")
     */
    public String getCollisionTag() {
        return collisionTag;
    }

    /**
     * Sets the collision tag for this entity.
     *
     * @param tag the tag string to assign
     */
    public void setCollisionTag(String tag) {
        this.collisionTag = tag;
    }

    /**
     * Called by CollisionManager when this entity collides with another.
     * Override in subclasses to define collision behaviour.
     * Default implementation does nothing.
     *
     * @param other the entity this entity collided with
     */
    public void onCollision(CollidableEntity other) {
        // no-op — subclasses override as needed
    }
}