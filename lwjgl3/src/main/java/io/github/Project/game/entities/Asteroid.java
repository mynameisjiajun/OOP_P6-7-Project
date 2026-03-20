package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.CollidableEntity;

public class Asteroid extends CollidableEntity {

    private static final String[] TEXTURES = {
        "meteors/Meteor_01.png", "meteors/Meteor_02.png", "meteors/Meteor_03.png",
        "meteors/Meteor_04.png", "meteors/Meteor_05.png", "meteors/Meteor_06.png",
        "meteors/Meteor_07.png", "meteors/Meteor_08.png", "meteors/Meteor_09.png",
        "meteors/Meteor_10.png"
    };

    private final Texture texture;
    private final float   width;
    private final float   height;
    private float         rotation      = 0f;
    private final float   rotationSpeed;
    private boolean       destroyed     = false;

    public Asteroid(float x, float y, float speed, float width, float height) {
        super(x, y, speed, width, height);
        this.width         = width;
        this.height        = height;
        this.texture       = new Texture(TEXTURES[MathUtils.random(TEXTURES.length - 1)]);
        this.rotationSpeed = MathUtils.random(-60f, 60f);
        this.collisionTag  = "Asteroid";
    }

    @Override
    public void update(float deltaTime) {
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

    @Override public float getWidth()  { return width;  }
    @Override public float getHeight() { return height; }

    public void    setDestroyed(boolean v) { destroyed = v; }
    public boolean isDestroyed()           { return destroyed; }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
