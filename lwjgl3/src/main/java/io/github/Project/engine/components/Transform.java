package com.team.project.engine.components;

/**
 * Component that holds position and rotation data.
 */
public class Transform {
    public float x;
    public float y;
    public float rotation;

    public Transform(float x, float y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
    }
}
