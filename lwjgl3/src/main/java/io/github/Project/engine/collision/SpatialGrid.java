package io.github.Project.engine.collision;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.Project.engine.entities.CollidableEntity;

public class SpatialGrid {
    private Map<Point, List<CollidableEntity>> grid;
    private float cellSize;

    public SpatialGrid(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
    }

    public void addEntity(CollidableEntity entity) {
        Point cell = getCell(entity);
        grid.computeIfAbsent(cell, k -> new ArrayList<>()).add(entity);
    }

    public void removeEntity(CollidableEntity entity) {
        Point cell = getCell(entity);
        List<CollidableEntity> entities = grid.get(cell);
        if (entities != null) {
            entities.remove(entity);
            if (entities.isEmpty()) {
                grid.remove(cell);
            }
        }
    }

    public List<CollidableEntity> getNearby(CollidableEntity entity) {
        Point cell = getCell(entity);
        List<CollidableEntity> nearbyEntities = new ArrayList<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Point adjacentCell = new Point(cell.x + dx, cell.y + dy);
                List<CollidableEntity> entities = grid.get(adjacentCell);
                if (entities != null) {
                    nearbyEntities.addAll(entities);
                }
            }
        }

        return nearbyEntities;
    }

    private Point getCell(CollidableEntity entity) {
        int x = (int) Math.floor(entity.getPosX() / cellSize);
        int y = (int) Math.floor(entity.getPosY() / cellSize);
        return new Point(x, y);
    }
}