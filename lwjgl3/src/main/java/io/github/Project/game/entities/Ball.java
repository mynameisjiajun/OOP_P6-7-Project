package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * A ball that bounces off the screen edges.
 *
 * CHANGES:
 * 1. Removed batch.begin() / batch.end() from render() — the scene owns the
 *    batch lifecycle. Calling begin() inside an entity crashes LibGDX if the
 *    batch is already open (which it always is when the scene iterates entities).
 * 2. Replaced Math.random() + Math.PI + Math.cos/sin (double) with
 *    MathUtils.random(MathUtils.PI2) and MathUtils.cos/sin (float-native),
 *    removing the need for (float) casts entirely.
 */
public class Ball extends CollidableEntity {

    private float screenWidth;
    private float screenHeight;
    private Texture texture;

    /**
     * Creates a new Ball that bounces around the screen.
     *
     * @param posX        Starting X position
     * @param posY        Starting Y position
     * @param size        Ball diameter (used for both width and height)
     * @param speed       Ball speed in world-units per second
     * @param screenWidth Screen width for boundary bouncing
     * @param screenHeight Screen height for boundary bouncing
     */
    public Ball(float posX, float posY, float size, float speed,
                float screenWidth, float screenHeight) {
        super(posX, posY, speed, size, size);
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;

        this.texture = new Texture("yellow-plastic-ball.jpg");
        this.collisionTag = "ball";

        // Random initial direction using MathUtils (float-native, no casts needed)
        float angle = MathUtils.random(MathUtils.PI2); // PI2 = 2π
        setVx(MathUtils.cos(angle) * speed);
        setVy(MathUtils.sin(angle) * speed);
    }

    public void setScreenSize(float width, float height) {
        this.screenWidth  = width;
        this.screenHeight = height;
    }

    public float getScreenWidth()  { return screenWidth; }
    public float getScreenHeight() { return screenHeight; }

    public void bounceY() {
        setVy(-getVy());
    }

    /**
     * Renders the ball using the scene's already-open SpriteBatch.
     *
     * NOTE: The scene calls batch.begin() before iterating entities and
     * batch.end() after. Entities must NOT call begin/end themselves.
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.draw(texture, getPosX(), getPosY(), bounds.width, bounds.height);
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }

    @Override public float getWidth()  { return bounds.width; }
    @Override public float getHeight() { return bounds.height; }
}
