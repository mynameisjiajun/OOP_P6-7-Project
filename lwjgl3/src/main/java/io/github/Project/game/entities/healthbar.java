package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

/**
 * HUD health bar entity.
 *
 * CHANGE: Removed shapeRenderer.begin() / shapeRenderer.end() from render().
 * The scene opens ShapeRenderer.ShapeType.Filled once before rendering all
 * HUD elements and closes it afterwards. Entities must not call begin/end.
 */
public class healthbar extends Entity {

    private float width;
    private float height;
    private float healthPercentage = 1f; // 1.0 = 100% health

    public healthbar(float x, float y, float width, float height) {
        super(x, y, 0); // No movement
        this.width  = width;
        this.height = height;
    }

    /** @param percentage value between 0.0 (dead) and 1.0 (full health) */
    public void setHP(float percentage) {
        this.healthPercentage = percentage;
    }

    /**
     * Renders the health bar using the scene's already-open ShapeRenderer
     * (must be in Filled mode when this is called).
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Background track (red — shows lost health)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(getPosX(), getPosY(), width, height);

        // Foreground fill (green) proportional to remaining health
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(getPosX(), getPosY(), width * healthPercentage, height);
    }

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public void dispose() { /* No resources to dispose */ }
}
