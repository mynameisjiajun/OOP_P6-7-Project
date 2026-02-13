package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.InputMovement;
import io.github.Project.engine.interfaces.IMovementStrategy;

public class Player extends CollidableEntity {

    private String texturePath;

    /**
     * @param input The InputMovement manager (needed for the strategy)
     */
    public Player(float posX, float posY, float width, float height, InputMovement input) {
        // Initialize Parent (CollidableEntity -> Entity)
        super(posX, posY, 200f, width, height);

        // Set the Strategy immediately using the Inner Class
        this.movementStrategy = new PlayerInputStrategy(input);
    }

    // --- The Inner Strategy Class ---
    private static class PlayerInputStrategy implements IMovementStrategy {
        
        private final InputMovement input;

        public PlayerInputStrategy(InputMovement input) {
            this.input = input;
        }

        @Override
        public void updateVelocity(Entity entity) {
            float dirX = 0;
            float dirY = 0;

            if (input.keyUp)    dirY = 1;
            if (input.keyDown)  dirY = -1;
            if (input.keyLeft)  dirX = -1;
            if (input.keyRight) dirX = 1;

            // Normalize diagonals
            if (dirX != 0 && dirY != 0) {
                dirX *= 0.7071f;
                dirY *= 0.7071f;
            }

            float speed = entity.getSpeed();
            entity.setVx(dirX * speed);
            entity.setVy(dirY * speed);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (movementStrategy != null) {
            movementStrategy.updateVelocity(this);
        }
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