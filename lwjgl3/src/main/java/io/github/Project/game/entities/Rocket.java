package io.github.Project.game.entities;
 
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;

public class Rocket extends CollidableEntity {
	private Texture texture;
	private InputMovement input;
	private float width;
	private float height;
	private float rotation;
	private float speed;
	
	public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
		super(x, y, speed, width, height);
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.input = input;
		this.rotation = 90; // Start facing up
		this.texture = new Texture("rocket.png");
		
		this.collisionTag = "Rocket";
	}
	
	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		// Subtract 90° because the texture already points up; physics rotation treats 90° as "up"
		batch.draw(texture, getPosX(), getPosY(), width / 2, height / 2, width, height, 1, 1, rotation - 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
	}
	
	@Override
	public float getWidth() {
		return width;
	}
	
	@Override
	public float getHeight() {
		return height;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	// These are now safely back inside the class!
	public void dispose() {
		if (texture != null) texture.dispose();
	}
	
	public InputMovement getInput() {
		return input;
	}
}