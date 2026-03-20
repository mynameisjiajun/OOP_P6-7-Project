package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;

/**
 * Player-controlled rocket entity.
 *
 * ── ANIMATED FRAME SIZE FIX ──────────────────────────────────────────────────
 *
 * The thrust animation frames are 141×517 px and contain two distinct regions:
 *   - Rocket body: top 360 px  (517 - 157)
 *   - Flame:       bottom 157 px
 *
 * Previous code derived FRAME_W/FRAME_H by scaling the full frame to width=32,
 * which made the body portion render at 32*(360/141) ≈ 81.7 px tall — 27% taller
 * than the static rocket (64 px). This caused the visible stretching.
 *
 * FIX: Instead of scaling from the asset's native aspect ratio, compute the draw
 * dimensions so the body portion renders at exactly entity_height (64 px).
 * Keep the flame proportional to the body using the asset's body/flame ratio:
 *
 *   body  pixels in asset = 360  (= 517 - 157)
 *   flame pixels in asset = 157
 *
 *   DRAW_FLAME = height * (157 / 360)   ← flame scaled to match body=height
 *   DRAW_H     = height * (517 / 360)   ← total frame height (body + flame)
 *   DRAW_W     = width                  ← unchanged (32 px)
 *
 * At height=64:  DRAW_FLAME ≈ 27.9 px,  DRAW_H ≈ 91.9 px
 * Body rendered = DRAW_H - DRAW_FLAME = 64 px  ✓  matches static sprite exactly.
 *
 * ── DRAW POSITION & PIVOT ────────────────────────────────────────────────────
 *
 *   drawY   = posY - DRAW_FLAME   (shift down so flame hangs below entity bounds)
 *   originX = width / 2f          (horizontal centre)
 *   originY = DRAW_FLAME + height / 2f  (body centre measured from frame bottom)
 *
 * This guarantees:  world pivot Y = drawY + originY = posY + height/2
 * — always the entity body centre, at every rotation angle.
 */
public class Rocket extends CollidableEntity {

    // ── Static texture (shown when not thrusting) ────────────────────────────
    private Texture staticTexture;

    // ── Thrust animation ─────────────────────────────────────────────────────
    private static final int   FRAME_COUNT    = 10;
    private static final float FRAME_DURATION = 1f / 12f; // 12 fps

    private final Texture[] frameTextures = new Texture[FRAME_COUNT];
    private Animation<TextureRegion> thrustAnimation;
    private float animStateTime = 0f;

    // ── Computed draw dimensions (set in constructor from entity size) ────────
    // Derived from asset body/flame pixel split (360 body + 157 flame = 517 total)
    // so the rendered body = entity height exactly.
    private final float drawFlame; // rendered flame height  = height * (157/360)
    private final float drawH;     // rendered frame height  = height * (517/360)
    private final float originX;   // rotation pivot X       = width / 2
    private final float originY;   // rotation pivot Y       = drawFlame + height/2

    private final InputMovement input;
    private final float width;
    private final float height;
    private float rotation;

    public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
        super(x, y, speed, width, height);
        this.width    = width;
        this.height   = height;
        this.input    = input;
        this.rotation = 90f; // facing up (90° = up in our coordinate system)

        // Precompute draw dimensions based on entity size.
        // Asset body/flame split: 360 body px + 157 flame px = 517 total px.
        this.drawFlame = height * (157f / 360f);  // ≈ 27.9 at height=64
        this.drawH     = height * (517f / 360f);  // ≈ 91.9 at height=64
        this.originX   = width  / 2f;             // horizontal centre
        this.originY   = drawFlame + height / 2f; // body centre from frame bottom

        this.staticTexture = new Texture("rocket.png");

        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frameTextures[i] = new Texture(
                String.format("rocket_frames/Missile_1_Flying_%03d.png", i));
            frames[i] = new TextureRegion(frameTextures[i]);
        }
        thrustAnimation = new Animation<>(FRAME_DURATION, frames);
        thrustAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.collisionTag = "Rocket";
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Subtract 90° because the textures already point upward
        float visualRot = rotation - 90f;

        if (input.keyUp) {
            animStateTime += Gdx.graphics.getDeltaTime();
            TextureRegion frame = thrustAnimation.getKeyFrame(animStateTime);

            // Draw the frame so:
            //   body-bottom aligns with posY  →  drawY = posY - drawFlame
            //   pivot = entity body centre    →  originX/Y as precomputed above
            // TextureRegion overload (10 args, no boolean):
            // draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotation)
            batch.draw(frame,
                getPosX(),            getPosY() - drawFlame,
                originX,              originY,
                width,                drawH,
                1f,                   1f,
                visualRot);

        } else {
            animStateTime = 0f;

            // Raw Texture overload (includes srcX/srcY/srcW/srcH + flipX/flipY):
            batch.draw(staticTexture,
                getPosX(), getPosY(),
                width / 2f, height / 2f,
                width, height,
                1f, 1f, visualRot,
                0, 0, staticTexture.getWidth(), staticTexture.getHeight(),
                false, false);
        }
    }

    @Override public float getWidth()  { return width; }
    @Override public float getHeight() { return height; }

    public float         getRotation()               { return rotation; }
    public void          setRotation(float rotation) { this.rotation = rotation; }
    public InputMovement getInput()                  { return input; }

    public void dispose() {
        if (staticTexture != null) staticTexture.dispose();
        for (Texture t : frameTextures) if (t != null) t.dispose();
    }
}