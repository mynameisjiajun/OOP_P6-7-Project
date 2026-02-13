package io.github.Project.engine.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

public class YellowBall extends TextureObject {
	
	public YellowBall(float x, float y, float width, float height, float speed) {
		super("yellow-plastic-ball.jpg",x, y, width, height, speed);
		this.speed = speed;
	}
	
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		batch.begin();
		batch.draw(texture, posX, posY, width, height);
		batch.end();
	}
	public void update(float deltaTime){
	}
}