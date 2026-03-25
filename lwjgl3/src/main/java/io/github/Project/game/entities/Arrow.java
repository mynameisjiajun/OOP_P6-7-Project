package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.Entity;

/**
 * Directional arrow that points from a source entity toward a target entity.
 * Rendered as a two-line arrowhead above the source.
 *
 * CHANGE: Removed shapeRenderer.begin() / shapeRenderer.end() from render().
 * PlayScene opens ShapeRenderer in Line mode before calling arrow.render()
 * and closes it afterwards. Calling begin() inside the entity when the
 * renderer is already open throws an IllegalStateException in LibGDX.
 */
public class Arrow extends Entity {

    private final Entity source;
    private Entity target;

    private final float distanceAboveRocket = 20f;
    private final float arrowHeadSize       = 12f;

    public Arrow(Entity source, Entity target) {
        super(source.getPosX(), source.getPosY(), 0);
        this.source = source;
        this.target = target;
    }

    public void setTarget(Entity target) { this.target = target; }

    @Override
    public void update(float deltaTime) {
        // Position the arrow tip above the top-centre of the source entity
        setPosX(source.getPosX() + source.getWidth() / 2f);
        setPosY(source.getPosY() + source.getHeight() + distanceAboveRocket);
    }

    /**
     * Draws the arrowhead using the scene's already-open ShapeRenderer.
     * The scene must have called shapeRenderer.begin(ShapeType.Line) before
     * iterating arrow.render().
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (shapeRenderer == null) return;

        float targetX = target.getPosX() + target.getWidth()  / 2f;
        float targetY = target.getPosY() + target.getHeight() / 2f;

        float dx    = targetX - getPosX();
        float dy    = targetY - getPosY();
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        shapeRenderer.setColor(Color.RED);

        // Left wing of the arrowhead
        shapeRenderer.line(
            getPosX(), getPosY(),
            getPosX() - MathUtils.cosDeg(angle - 25) * arrowHeadSize,
            getPosY() - MathUtils.sinDeg(angle - 25) * arrowHeadSize
        );

        // Right wing of the arrowhead
        shapeRenderer.line(
            getPosX(), getPosY(),
            getPosX() - MathUtils.cosDeg(angle + 25) * arrowHeadSize,
            getPosY() - MathUtils.sinDeg(angle + 25) * arrowHeadSize
        );
    }

    @Override public float getWidth()  { return arrowHeadSize; }
    @Override public float getHeight() { return arrowHeadSize; }
}
