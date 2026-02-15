package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.Project.engine.entities.CollidableEntity;

// A ball that bounces off the screen edges.

public class Ball extends CollidableEntity {

    private float screenWidth;
    private float screenHeight;
    private Texture texture;

    /**
     * Creates a new Ball that bounces around the screen.
     * @param posX Starting X position
     * @param posY Starting Y position
     * @param size Ball diameter
     * @param speed Ball speed
     * @param screenWidth Screen width for bouncing
     * @param screenHeight Screen height for bouncing
     */
    public Ball(float posX, float posY, float size, float speed,
                float screenWidth, float screenHeight) {
        super(posX, posY, speed, size, size);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Load the ball texture
        this.texture = new Texture(Gdx.files.internal("yellow-plastic-ball.jpg"));

        // Set the bounce strategy
        this.collisionTag = "ball";

        // Give the ball a random initial direction
        double angle = Math.random() * 2 * Math.PI;
        this.vx = (float) Math.cos(angle) * speed;
        this.vy = (float) Math.sin(angle) * speed;
    }
    public void setScreenSize(float width, float height) {
		this.screenWidth = width;
		this.screenHeight = height;
	}
    public float getScreenWidth() { 
    	return screenWidth; 
    	}
    public float getScreenHeight() { 
    	return screenHeight; 
    	}
    
    public void bounceY() {
		this.vy = -this.vy;
	}

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // Draw the ball using the shared SpriteBatch
        batch.begin();
        batch.draw(texture, posX, posY, bounds.width, bounds.height);
        batch.end();
    }

    /**
     * Dispose of the ball texture.
     */
    public void dispose() {
        if (texture != null) texture.dispose();
    }

    @Override
    public float getWidth() { return bounds.width; }

    @Override
    public float getHeight() { return bounds.height; }
    }
