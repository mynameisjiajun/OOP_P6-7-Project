package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.game.entities.Ball;

public class BounceMovementStrategy implements IMovementStrategy {

    @Override
    public void updateVelocity(Entity entity) {

        Ball ball = (Ball) entity;

        if (ball.getPosX() <= 0) {
            ball.setPosX(0);
            ball.setVx(Math.abs(ball.getVx()));
        } 
        else if (ball.getPosX() + ball.getWidth() >= ball.getScreenWidth()) {
            ball.setPosX(ball.getScreenWidth() - ball.getWidth());
            ball.setVx(-Math.abs(ball.getVx()));
        }

        if (ball.getPosY() <= 0) {
            ball.setPosY(0);
            ball.setVy(Math.abs(ball.getVy()));
        } 
        else if (ball.getPosY() + ball.getHeight() >= ball.getScreenHeight()) {
            ball.setPosY(ball.getScreenHeight() - ball.getHeight());
            ball.setVy(-Math.abs(ball.getVy()));
        }
    }
}
