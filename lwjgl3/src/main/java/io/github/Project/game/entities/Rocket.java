package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.engine.interfaces.ICollisionStrategy;

//player controlled rocket
public class Rocket extends CollidableEntity {

    private ICollisionStrategy collisionStrategy;

    // Rendering
    private Texture staticTexture;
    private static final int FRAME_COUNT = 10;
    private static final float FRAME_DURATION = 1f / 12f;
    private final Texture[] frameTextures = new Texture[FRAME_COUNT];
    private Animation<TextureRegion> thrustAnimation;
    private float animStateTime = 0f;

    // Physics and dimensions 
    private final float drawFlame;
    private final float drawH;
    private final float originX;
    private final float originY;
    private final InputMovement input;
    private final float width;
    private final float height;
    private float rotation;

    public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
        super(x, y, speed, width, height);

        this.width = width;
        this.height = height;
        this.input = input;
        this.rotation = 90f;

        this.drawFlame = height * (157f / 360f);
        this.drawH = height * (517f / 360f);
        this.originX = width / 2f;
        this.originY = drawFlame + height / 2f;

        this.staticTexture = new Texture("images/entities/rocket.png");

        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frameTextures[i] = new Texture(
                String.format("images/entities/rocket_frames/Missile_1_Flying_%03d.png", i));
            frames[i] = new TextureRegion(frameTextures[i]);
        }
        thrustAnimation = new Animation<>(FRAME_DURATION, frames);
        thrustAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.collisionTag = "Rocket";
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float visualRot = rotation - 90f;

        if (input.isKeyUp()) {
            // Thrust animation
            animStateTime += Gdx.graphics.getDeltaTime();
            TextureRegion frame = thrustAnimation.getKeyFrame(animStateTime);

            batch.draw(frame,
                getPosX(), getPosY() - drawFlame,
                originX, originY,
                width, drawH,
                1f, 1f,
                visualRot);

        } else {
            // Static texture
            animStateTime = 0f;
            batch.draw(staticTexture,
                getPosX(), getPosY(),
                width / 2f, height / 2f,
                width, height,
                1f, 1f, visualRot,
                0, 0, staticTexture.getWidth(), staticTexture.getHeight(),
                false, false);
        }
    }

    // Collision strategy 

    public void setCollisionStrategy(ICollisionStrategy strategy) {
        this.collisionStrategy = strategy;
    }

    public ICollisionStrategy getCollisionStrategy() {
        return collisionStrategy;
    }

    public void onCollision(CollidableEntity other) {
        if (collisionStrategy != null) {
            collisionStrategy.handleCollision(this, other);
        }
    }

    //  Getters/Setters

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public float getRotation()         { return rotation; }
    public void setRotation(float r)   { this.rotation = r; }
    public InputMovement getInput()    { return input; }

    // Cleanup

    public void dispose() {
        if (staticTexture != null) staticTexture.dispose();
        for (Texture t : frameTextures) if (t != null) t.dispose();
    }
}
