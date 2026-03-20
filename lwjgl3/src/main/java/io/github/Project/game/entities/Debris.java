package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * Asteroid entity — drifts and rotates in the game world.
 *
 * CHANGE: Replaced java.util.Random with MathUtils.random().
 * MathUtils is already part of LibGDX and is float-native; there is no need
 * to import java.util.Random or hold a static RNG reference.
 *   - MathUtils.random(n-1)       replaces  RNG.nextInt(n)
 *   - MathUtils.random(-60f, 60f) replaces  -60f + RNG.nextFloat() * 120f
 */
public class Debris extends CollidableEntity {

    private static final String[] METEOR_TEXTURES = {
        "Space Debris/Space Debris 1.png", "Space Debris/Space Debris 2.png",
        "Space Debris/Space Debris 3.png", "Space Debris/Space Debris 4.png",
        "Space Debris/Space Debris 5.png", "Space Debris/Space Debris 6.png"
    };

    private Texture texture;
    private final float width;
    private final float height;
    private float rotation      = 0f;
    private float rotationSpeed;
    private boolean destroyed   = false;

    public Debris(float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.width  = width;
        this.height = height;

        // Pick a random texture using MathUtils — no java.util.Random import needed
        int idx = MathUtils.random(METEOR_TEXTURES.length - 1);
        this.texture = new Texture(METEOR_TEXTURES[idx]);

        this.collisionTag = "Debris";

        // Random rotation speed in the range [-60°/s, +60°/s]
        this.rotationSpeed = MathUtils.random(-60f, 60f);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);   // apply vx/vy → position
        rotation += rotationSpeed * deltaTime;
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.draw(texture,
            getPosX(), getPosY(),
            width / 2f, height / 2f,
            width, height,
            1, 1, rotation,
            0, 0, texture.getWidth(), texture.getHeight(),
            false, false);
    }

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public void    setDestroyed(boolean destroyed) { this.destroyed = destroyed; }
    public boolean isDestroyed()                   { return destroyed; }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
