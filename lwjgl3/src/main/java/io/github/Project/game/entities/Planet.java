package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

/**
 * An animated celestial body (planet, moon, sun) with gravity.
 * Position is stored as the bottom-left of the bounding square;
 * all public helpers expose the centre.
 */
public class Planet extends CollidableEntity {

    private static final float FRAME_DURATION = 1f / 6f; // 6 fps spin

    private final Texture[] frames;
    private final int       frameCount;
    private float           animTimer = 0f;

    private final String name;
    private final float  radius;
    private final float  gravityStrength;
    private final float  gravityRadius;
    private boolean      visited = false;

    /**
     * @param name            display name
     * @param centerX/centerY world-space centre of the planet
     * @param radius          visual and collision radius (world units)
     * @param gravityStrength peak pull at the surface (world units/s²)
     * @param gravityRadius   distance at which gravity fades to zero
     * @param frames          animation frame textures (≥ 1)
     */
    public Planet(String name, float centerX, float centerY, float radius,
                  float gravityStrength, float gravityRadius, Texture[] frames) {
        super(centerX - radius, centerY - radius, 0, radius * 2f, radius * 2f);
        this.name            = name;
        this.radius          = radius;
        this.gravityStrength = gravityStrength;
        this.gravityRadius   = gravityRadius;
        this.frames          = frames;
        this.frameCount      = frames.length;
        this.collisionTag    = "Planet";
    }

    @Override
    public void update(float deltaTime) {
        animTimer += deltaTime;
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        int idx = ((int) (animTimer / FRAME_DURATION)) % frameCount;
        float size = radius * 2f;
        batch.draw(frames[idx], getPosX(), getPosY(), size, size);
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────
    public float getCenterX() { return getPosX() + radius; }
    public float getCenterY() { return getPosY() + radius; }

    // ── Accessors ─────────────────────────────────────────────────────────────
    public String  getName()            { return name;            }
    public float   getRadius()          { return radius;          }
    public float   getGravityStrength() { return gravityStrength; }
    public float   getGravityRadius()   { return gravityRadius;   }
    public boolean isVisited()          { return visited;         }
    public void    setVisited(boolean v){ visited = v;            }

    @Override public float getWidth()  { return radius * 2f; }
    @Override public float getHeight() { return radius * 2f; }

    public void dispose() {
        if (frames != null) for (Texture t : frames) if (t != null) t.dispose();
    }
}
