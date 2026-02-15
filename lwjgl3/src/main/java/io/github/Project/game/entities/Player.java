package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;  

public class Player extends CollidableEntity {

    private String texturePath;
    private InputMovement input;

    /**
     * @param input The InputMovement manager (needed for the strategy)
     */
    public Player(float posX, float posY, float width, float height, InputMovement input) {
        // Initialize Parent (CollidableEntity -> Entity)
        super(posX, posY, 200f, width, height);
        this.input = input; // Store the input reference for the strategy

        // Set the Strategy immediately using the Inner Class
    }

    public InputMovement getInput() {
    	return input;
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Draw as a cyan rectangle using the shared ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(posX, posY, bounds.width, bounds.height);
        shapeRenderer.end();
    }

    @Override
    public float getWidth() { return bounds.width; }

    @Override
    public float getHeight() { return bounds.height; }

    public String getTexturePath() { return texturePath; }
    public void setTexturePath(String path) { this.texturePath = path; }
}