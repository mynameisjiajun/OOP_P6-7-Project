package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import java.util.Random;

public class Asteroid extends CollidableEntity {

    private static final Random RNG = new Random();
    private static final String[] METEOR_TEXTURES = {
        "meteors/Meteor_01.png", "meteors/Meteor_02.png", "meteors/Meteor_03.png",
        "meteors/Meteor_04.png", "meteors/Meteor_05.png", "meteors/Meteor_06.png",
        "meteors/Meteor_07.png", "meteors/Meteor_08.png", "meteors/Meteor_09.png",
        "meteors/Meteor_10.png"
    };

    private Texture texture;
    private float width;
    private float height;
    private float rotation = 0f;
    private float rotationSpeed;
    private boolean destroyed = false;

    public Asteroid(float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.width = width;
        this.height = height;
        int idx = RNG.nextInt(METEOR_TEXTURES.length);
        this.texture = new Texture(METEOR_TEXTURES[idx]);
        this.collisionTag = "Asteroid";
        this.rotationSpeed = -60f + RNG.nextFloat() *120f;
        setVx(0);
        setVy(0);
     }

    @Override
    public void update(float deltaTime) {
    	rotation += rotationSpeed * deltaTime;
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        batch.draw(texture, getPosX(), getPosY(), width / 2, height / 2, width, height, 1, 1, rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }
    public void dispose() {
		if (texture != null) texture.dispose();
	}
    
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}