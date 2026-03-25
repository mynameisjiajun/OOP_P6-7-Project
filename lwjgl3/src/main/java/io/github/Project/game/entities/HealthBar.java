package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

/**
 * HUD health bar entity.
 * Rendered by the scene's already-open ShapeRenderer (Filled mode).
 * Uses a dark track with a colour-transitioning fill (green → amber → red).
 */
public class HealthBar extends Entity {

    private float width;
    private float height;
    private float healthPercentage = 1f;

    public HealthBar(float x, float y, float width, float height) {
        super(x, y, 0);
        this.width  = width;
        this.height = height;
    }

    public void setHP(float percentage) {
        this.healthPercentage = percentage;
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer sr) {
        // Dark track background
        sr.setColor(0.12f, 0.12f, 0.18f, 1f);
        sr.rect(getPosX(), getPosY(), width, height);

        // Colour-transitioning fill
        if      (healthPercentage > 0.6f) sr.setColor(0.22f, 0.82f, 0.35f, 1f); // green
        else if (healthPercentage > 0.3f) sr.setColor(0.98f, 0.72f, 0.08f, 1f); // amber
        else                              sr.setColor(0.92f, 0.22f, 0.15f, 1f); // red

        if (healthPercentage > 0f) {
            sr.rect(getPosX() + 1f, getPosY() + 1f,
                    (width - 2f) * healthPercentage, height - 2f);
        }
    }

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public void dispose() { /* no resources */ }
}
