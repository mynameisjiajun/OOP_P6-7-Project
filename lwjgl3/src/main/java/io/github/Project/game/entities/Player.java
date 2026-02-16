package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
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
=======
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.interfaces.InputMovement;
import io.github.Project.engine.objects.ShapeObject;
import io.github.Project.engine.entities.Entity;

public class Player extends ShapeObject {

    public Player(float posX, float posY, float width, float height, InputMovement input) {
=======
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.interfaces.InputMovement;
import io.github.Project.engine.objects.ShapeObject;
import io.github.Project.engine.entities.Entity;

public class Player extends ShapeObject {

    public Player(float posX, float posY, float width, float height, InputMovement input) {
>>>>>>> Stashed changes
        super(posX, posY, width, height, 200f, Color.RED);
        this.movementStrategy = new PlayerInputStrategy(input);
    }

    // --- FIX 3: Implement Update (Solves update error) ---
    @Override
    public void update(float deltaTime) {
        // This connects the entity to the strategy we defined below
        if (movementStrategy != null) {
            movementStrategy.updateVelocity(this);
        }
    }

    // --- Inner Strategy Class ---
    private static class PlayerInputStrategy implements IMovementStrategy {
        private final InputMovement input;

        public PlayerInputStrategy(InputMovement input) { this.input = input; }

        @Override
        public void updateVelocity(Entity entity) {
            float dirX = 0; float dirY = 0;

            if (input.keyUp)    dirY = 1;
            if (input.keyDown)  dirY = -1;
            if (input.keyLeft)  dirX = -1;
            if (input.keyRight) dirX = 1;

            if (dirX != 0 && dirY != 0) {
                dirX *= 0.7071f;
                dirY *= 0.7071f;
            }
            entity.setVx(dirX * entity.getSpeed());
            entity.setVy(dirY * entity.getSpeed());
        }
    }
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}