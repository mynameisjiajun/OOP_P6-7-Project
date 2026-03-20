package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.Entity;

public class arrow extends Entity {
	private Entity source;
	private Entity target;
	
	private float length = 40f; // Length of the arrow
	
	public arrow(Entity source, Entity target) {
		super(source.getPosX(), source.getPosY(), 0);
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void update(float deltaTime) {
	setPosX(source.getPosX());
	setPosY(source.getPosY());
	}
	
	@Override
	public void render (SpriteBatch batch, ShapeRenderer shapeRenderer) {
		float dx = target.getPosX() - getPosX();
		float dy = target.getPosY() - getPosY();
		
		float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
		
		float endX = getPosX() + MathUtils.cosDeg(angle) * length;
		float endY = getPosY() + MathUtils.sinDeg(angle) * length;
		
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.line(getPosX(), getPosY(), endX, endY);
		
		float arrowHeadSize = 10f;
		shapeRenderer.line(endX, endY, endX - MathUtils.cosDeg(angle - 20) * arrowHeadSize, endY - MathUtils.sinDeg(angle - 20) * arrowHeadSize);
		shapeRenderer.line(endX, endY, endX - MathUtils.cosDeg(angle + 20) * arrowHeadSize, endY - MathUtils.sinDeg(angle + 20) * arrowHeadSize);
	
		shapeRenderer.end();
	}
	
	public float getWidth() {
		return length;
	}
	
	public float getHeight() {
		return length;
	}
}