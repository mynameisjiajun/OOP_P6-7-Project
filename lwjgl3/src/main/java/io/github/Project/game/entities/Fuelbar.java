package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

/**
 * HUD fuel bar entity.
 *
 * CHANGE: Removed shapeRenderer.begin() / shapeRenderer.end() from render().
 * The scene opens ShapeRenderer.ShapeType.Filled once before rendering all
 * HUD elements and closes it afterwards. Entities must not call begin/end.
 */
public class Fuelbar extends Entity {

    private float width;
    private float height;
    private float fuelPercentage = 1f; // 1.0 = 100% full

    public Fuelbar(float x, float y, float width, float height) {
        super(x, y, 0); // No movement
        this.width  = width;
        this.height = height;
    }

    /** @param percentage value between 0.0 (empty) and 1.0 (full) */
    public void setFuel(float percentage) {
        this.fuelPercentage = percentage;
    }

    /**
     * Renders the fuel bar using the scene's already-open ShapeRenderer
     * (must be in Filled mode when this is called).
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Background track (gray)
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.rect(getPosX(), getPosY(), width, height);

        // Foreground fill (gold) proportional to remaining fuel
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(getPosX(), getPosY(), width * fuelPercentage, height);
    }

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public void dispose() { /* No resources to dispose */ }
}
