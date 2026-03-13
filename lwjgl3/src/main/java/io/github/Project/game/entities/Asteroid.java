package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

public class Asteroid extends CollidableEntity {

    private Texture texture;
    private float width;
    private float height;

    public Asteroid(float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height); // speed = 80
        this.width = width;
        this.height = height;
        this.texture = new Texture("asteroid.jpg");

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.draw(texture, getPosX(), getPosY(), width, height);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }
}