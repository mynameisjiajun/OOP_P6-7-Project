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
        
        //Set collision tag for identification
        this.collisionTag = "player";
    }

    public InputMovement getInput() {
        return input;
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Change color when at boundaries
        float screenWidth = com.badlogic.gdx.Gdx.graphics.getWidth();
        float screenHeight = com.badlogic.gdx.Gdx.graphics.getHeight();
        
        boolean atLeftEdge = posX <= 0;
        boolean atRightEdge = posX + bounds.width >= screenWidth;
        boolean atTopEdge = posY + bounds.height >= screenHeight;
        boolean atBottomEdge = posY <= 0;
        
        if (atLeftEdge || atRightEdge || atTopEdge || atBottomEdge) {
            shapeRenderer.setColor(Color.RED);  // RED when touching boundary!
        } else {
            shapeRenderer.setColor(Color.CYAN);  // Normal color
        }
        
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