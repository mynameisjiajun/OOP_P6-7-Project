package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.Entity;

public class arrow extends Entity {
    private Entity source;
    private Entity target;

    private float distanceAboveRocket = 20f;
    private float arrowHeadSize = 12f;

    public arrow(Entity source, Entity target) {
        super(source.getPosX(), source.getPosY(), 0);
        this.source = source;
        this.target = target;
    }

    @Override
    public void update(float deltaTime) {
        // Position arrow above the top-center of the rocket
        setPosX(source.getPosX() + source.getWidth() / 2f);
        setPosY(source.getPosY() + source.getHeight() + distanceAboveRocket);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (shapeRenderer == null) return;

        float targetX = target.getPosX() + target.getWidth() / 2f;
        float targetY = target.getPosY() + target.getHeight() / 2f;

        float dx = targetX - getPosX();
        float dy = targetY - getPosY();

        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        shapeRenderer.line(
            getPosX(), getPosY(),
            getPosX() - MathUtils.cosDeg(angle - 25) * arrowHeadSize,
            getPosY() - MathUtils.sinDeg(angle - 25) * arrowHeadSize
        );

        shapeRenderer.line(
            getPosX(), getPosY(),
            getPosX() - MathUtils.cosDeg(angle + 25) * arrowHeadSize,
            getPosY() - MathUtils.sinDeg(angle + 25) * arrowHeadSize
        );

        shapeRenderer.end();
    }

    @Override
    public float getWidth() {
        return arrowHeadSize;
    }

    @Override
    public float getHeight() {
        return arrowHeadSize;
    }
}
