package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;

public class Rocket extends CollidableEntity {

    // ── Static rocket texture (shown when not thrusting) ──
    private Texture texture;

    // ── Animation frames (shown when thrusting) ──
    // Frames are 141×517: rocket body (top ~70%) + flame (bottom ~30%)
    private static final int   FRAME_COUNT    = 10;
    private static final float FRAME_DURATION = 1f / 12f; // 12 fps
    private final Texture[] flyFrames = new Texture[FRAME_COUNT];
    private float animTimer = 0f;

    // ── Frame render dimensions ──
    // Scale by entity width so aspect ratio is maintained.
    // Flame occupies ~30% of the 517px frame height → flameH = 517*0.30*(32/141)
    private static final float FRAME_W  = 32f;
    private static final float FRAME_H  = FRAME_W * 517f / 141f;   // ≈ 117
    private static final float FLAME_H  = FRAME_W * 157f / 141f;   // ≈ 36

    private InputMovement input;
    private float width;
    private float height;
    private float rotation;

    public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
        super(x, y, speed, width, height);
        this.width    = width;
        this.height   = height;
        this.input    = input;
        this.rotation = 90; // Start facing up (90° = up in our physics system)

        this.texture = new Texture("rocket.png");

        for (int i = 0; i < FRAME_COUNT; i++) {
            flyFrames[i] = new Texture(
                String.format("rocket_frames/Missile_1_Flying_%03d.png", i));
        }

        this.collisionTag = "Rocket";
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float physRad    = (float) Math.toRadians(rotation);
        float visualRot  = rotation - 90; // subtract 90° because textures already point up

        if (input.keyUp) {
            // ── Animated thrust: cycle through frames, flame included in frame ──
            animTimer += Gdx.graphics.getDeltaTime();
            int frameIdx = (int) (animTimer / FRAME_DURATION) % FRAME_COUNT;
            Texture frame = flyFrames[frameIdx];

            // Position so rocket body aligns with entity bounds,
            // flame hangs below in the direction opposite to thrust.
            // Flame offset: move draw origin downward (in the rocket's local -Y direction).
            float offX = -(float) Math.cos(physRad) * FLAME_H;
            float offY = -(float) Math.sin(physRad) * FLAME_H;

            batch.draw(frame,
                    getPosX() + offX, getPosY() + offY,
                    FRAME_W / 2f, FRAME_H / 2f,
                    FRAME_W, FRAME_H,
                    1, 1, visualRot,
                    0, 0, frame.getWidth(), frame.getHeight(),
                    false, false);
        } else {
            // ── Static texture when not thrusting ──
            animTimer = 0f; // reset so animation starts fresh on next thrust
            batch.draw(texture,
                    getPosX(), getPosY(),
                    width / 2f, height / 2f,
                    width, height,
                    1, 1, visualRot,
                    0, 0, texture.getWidth(), texture.getHeight(),
                    false, false);
        }
    }

    @Override public float getWidth()  { return width;  }
    @Override public float getHeight() { return height; }

    public float getRotation()              { return rotation; }
    public void  setRotation(float rotation){ this.rotation = rotation; }
    public InputMovement getInput()         { return input; }

    public void dispose() {
        if (texture != null) texture.dispose();
        for (Texture t : flyFrames) if (t != null) t.dispose();
    }
}
