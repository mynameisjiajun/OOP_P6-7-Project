package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;


public abstract class TextureObject extends Entity {
	protected Texture texture;
	protected SpriteBatch spriteBatch;
	protected float width;
	protected float height;
	/**
	 * Creates a new texture object.
	 * @param x X position
	 * @param y Y position
	 * @param texture Texture to render
	 */
	public TextureObject(String texturefile, float x, float y, float width, float height, float speed) {
		super(x,y,speed);
		this.width = width;
		this.height = height;
		this.texture = new Texture(texturefile);
		this.spriteBatch = new SpriteBatch();
	}

	// Getters and setters
	public Texture getTexture() {
		return texture;
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
}