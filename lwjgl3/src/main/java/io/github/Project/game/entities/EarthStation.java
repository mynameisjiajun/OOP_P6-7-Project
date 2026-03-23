package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * Earth-side refuelling and launch pad.
 *
 * The rocket must perform a safe landing on this pad to restore
 * fuel and health. Collision tag "EarthStation" lets the
 * RocketCollisionStrategy distinguish it from regular ground.
 */
public class EarthStation extends CollidableEntity {

    private Texture texture;
    private final float width;
    private final float height;

    public EarthStation(float x, float y, float width, float height) {
        super(x, y, 0, width, height);
        this.texture = new Texture("Ground Assets/Earth_Station.png");
        this.width   = width;
        this.height  = height;
        this.collisionTag = "EarthStation";
    }

    @Override
    public void update(float deltaTime) {
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.draw(texture, getPosX(), getPosY(), width, height);
    }

    @Override public float getWidth()  { return width;  }
    @Override public float getHeight() { return height; }

    public void dispose() {
        if (texture != null) { texture.dispose(); texture = null; }
    }
}
