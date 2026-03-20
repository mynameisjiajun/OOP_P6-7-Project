package io.github.Project.game.entities;
 
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;

public class Rocket extends CollidableEntity {
	private Texture texture;
	private Texture thrusterTexture;
	private InputMovement input;
	private float width;
	private float height;
	private float rotation;

	private static final float THRUSTER_W = 24f;
	private static final float THRUSTER_H = 32f;

	public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
		super(x, y, speed, width, height);
		this.width = width;
		this.height = height;
		this.input = input;
		this.rotation = 90; // Start facing up
		this.texture = new Texture("rocket.png");
		this.thrusterTexture = new Texture("Rocket_Effect_01.png");

		this.collisionTag = "Rocket";
	}

	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		// Draw thruster flame at the exhaust nozzle when thrust is held
		if (input.keyUp) {
			float physRad = (float) Math.toRadians(rotation);
			float cx = getPosX() + width / 2f;
			float cy = getPosY() + height / 2f;
			// Nozzle is opposite to thrust direction, offset by ~60% of height from centre
			float nozzleX = cx - (float) Math.cos(physRad) * height * 0.6f - THRUSTER_W / 2f;
			float nozzleY = cy - (float) Math.sin(physRad) * height * 0.6f - THRUSTER_H / 2f;
			batch.draw(thrusterTexture, nozzleX, nozzleY,
					THRUSTER_W / 2f, THRUSTER_H / 2f,
					THRUSTER_W, THRUSTER_H,
					1, 1, rotation - 90,
					0, 0, thrusterTexture.getWidth(), thrusterTexture.getHeight(), false, false);
		}

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
		if (thrusterTexture != null) thrusterTexture.dispose();
	}
	
	public InputMovement getInput() {
		return input;
	}
}