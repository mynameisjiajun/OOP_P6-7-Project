package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.Project.engine.interfaces.IMovementStrategy;


public abstract class TextureObject {
	protected Texture texture;
	protected SpriteBatch spriteBatch;
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	protected float speed;
	protected IMovementStrategy strategy;

	/**
	 * Creates a new texture object.
	 * @param x X position
	 * @param y Y position
	 * @param texture Texture to render
	 */
	public TextureObject(String texturefile, float x, float y, float width, float height, float speed, IMovementStrategy strategy) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.texture = new Texture(texturefile);
		this.batch = new SpriteBatch();
		this.speed = 0;
	}

	// Getters and setters
	public Texture getTexture() {
		return texture;
	}
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
    public void setPosition(float x, float y) {
        this.x = x;
    	this.y = y;
    }
	public float getWidth() {
		return width;
	}
	public float getHeight() {
		return height;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public void setHeight(float height) {
		this.height = height;
	}
	public IMovementStrategy getStrategy() {
		return strategy;
	}
	public void setStrategy(IMovementStrategy strategy) {
		this.strategy = strategy;
	}
	public float getSpeed() {
		return speed;
	}
	public float setSpeed(float speed) {
		return this.speed = speed;
	}
	public abstract void render() {
		batch.begin();
		batch.draw(texture, x, y, width, height);
		batch.end();
	}
	
	public abstract void update(float deltaTime);
	
}