package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;

/**
 * Player-controlled paddle entity.
 * Changes colour when touching screen boundaries.
 *
 * CHANGE: Removed shapeRenderer.begin() / shapeRenderer.end() from render().
 * The scene is responsible for opening and closing the ShapeRenderer once per
 * frame. Calling begin() inside an entity throws an IllegalStateException in
 * LibGDX if the renderer is already open.
 */
public class Player extends CollidableEntity {

    private String texturePath;
    private InputMovement input;

    /**
     * @param posX   Starting X position
     * @param posY   Starting Y position
     * @param width  Paddle width
     * @param height Paddle height
     * @param input  Shared InputMovement handler (needed by PlayerMovementStrategy)
     */
    public Player(float posX, float posY, float width, float height, InputMovement input) {
        super(posX, posY, 200f, width, height);
        this.input = input;
        this.collisionTag = "player";
    }

    public InputMovement getInput() { return input; }

    /**
     * Renders the paddle with the scene's already-open ShapeRenderer.
     * The scene must call shapeRenderer.begin() before iterating entities.
     */
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        boolean atLeftEdge   = getPosX() <= 0;
        boolean atRightEdge  = getPosX() + bounds.width  >= screenWidth;
        boolean atTopEdge    = getPosY() + bounds.height >= screenHeight;
        boolean atBottomEdge = getPosY() <= 0;

        if (atLeftEdge || atRightEdge || atTopEdge || atBottomEdge) {
            shapeRenderer.setColor(Color.RED);   // Red when touching any boundary
        } else {
            shapeRenderer.setColor(Color.CYAN);  // Normal colour
        }

        shapeRenderer.rect(getPosX(), getPosY(), bounds.width, bounds.height);
    }

    @Override public float getWidth()  { return bounds.width; }
    @Override public float getHeight() { return bounds.height; }

    public String getTexturePath()            { return texturePath; }
    public void   setTexturePath(String path) { this.texturePath = path; }
}
