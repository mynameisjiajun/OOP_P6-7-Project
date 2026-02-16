package io.github.Project.engine.objects;

import com.badlogic.gdx.Gdx; // Make sure this is imported
import com.badlogic.gdx.graphics.Color;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
import io.github.Project.engine.entities.Entity;

/**
 * Abstract base class for shape-based objects.
 * Provides common functionality for shapes with color and movement.
 */
public abstract class ShapeObject extends Entity {
    protected Color color;
    protected float width;
    protected float height;
    
    /**
     * Creates a new shape object.
     * @param x X position
     * @param y Y position
     * @param width Width of the shape
     * @param height Height of the shape
     * @param speed Movement speed
     * @param color Shape color
     */
    public ShapeObject(float x, float y, float width, float height, float speed, Color color) {
        super(x, y, speed);
        this.width = width;
        this.height = height;
        this.color = color;
        // No longer creating own ShapeRenderer - use shared one from GameMaster
=======
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

public abstract class ShapeObject extends CollidableEntity {
    protected Color color;
    protected ShapeRenderer shapeRenderer;

    public ShapeObject(float x, float y, float width, float height, float speed, Color color) {
        super(x, y, speed, width, height);
        this.color = color;
        this.shapeRenderer = new ShapeRenderer();
>>>>>>> Stashed changes
=======
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

public abstract class ShapeObject extends CollidableEntity {
    protected Color color;
    protected ShapeRenderer shapeRenderer;

    public ShapeObject(float x, float y, float width, float height, float speed, Color color) {
        super(x, y, speed, width, height);
        this.color = color;
        this.shapeRenderer = new ShapeRenderer();
>>>>>>> Stashed changes
    }

    @Override
    public float getWidth() { return bounds.width; }

    @Override
    public float getHeight() { return bounds.height; }

    @Override
    public void render() {
        // --- FIX: Tell the renderer how big the screen is ---
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.updateMatrices();
        // ---------------------------------------------------

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(getPosX(), getPosY(), getWidth(), getHeight());
        shapeRenderer.end();
    }
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    // FIXED: This was a setter disguised as a getter!
    public float getHeight() {
        return this.height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}