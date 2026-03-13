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
		this.rotation = 0; // Start facing right
		this.texture = new Texture("rocket.jpg");
		
		this.collisionTag = "Rocket";
	}
	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		batch.draw(texture, getPosX(), getPosY(), width / 2, height / 2, width, height, 1, 1, rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
	}
	@Override
	public float getWidth() {
		return width;
	}
	@Override
	public float getHeight() {
		return height;
	}
	public void dispose() {
		if (texture != null) texture.dispose();
	}
	public InputMovement getInput() {
		return input;
	}
	public void update(float deltaTime) {
		if(input.keyLeft)
			rotation += speed * deltaTime;
		if(input.keyRight)
			rotation -= speed * deltaTime;
	}	
}