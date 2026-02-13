package io.github.Project.engine.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.Project.engine.entities.Entity;

public class YellowBall extends TextureObject {
	private Texture texture;
	private SpriteBatch spriteBatch;
	
	public YellowBall(float x, float y, float width, float height, float speed) {
		super("yellow-plastic-ball.jpg",x, y, width, height, speed);
		this.speed = speed;
	}
	
	public void render () {
		spriteBatch.begin();
		spriteBatch.draw(texture, x, y, width, height);
		spriteBatch.end();
	}
	public void update(float deltaTime){
	}
}