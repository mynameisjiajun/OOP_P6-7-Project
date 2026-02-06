package io.github.Project.engine.interfaces;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

public class InputMovement extends InputAdapter {
    
    // Boolean flags to track direction state
    public boolean keyUp;
    public boolean keyDown;
    public boolean keyLeft;
    public boolean keyRight;

    /**
     * Called when a key is pressed down.
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            // Support both WASD and Arrow Keys
            case Keys.W:
            case Keys.UP:
                keyUp = true;
                break;
            case Keys.A:
            case Keys.LEFT:
            	keyLeft = true;
                break;
            case Keys.S:
            case Keys.DOWN:
            	keyDown = true;
                break;
            case Keys.D:
            case Keys.RIGHT:
            	keyRight = true;
                break;
        }
        return true; // Signal that we handled the input
    }

    /**
     * Called when a key is released.
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.W:
            case Keys.UP:
            	keyUp = false;
                break;
            case Keys.A:
            case Keys.LEFT:
            	keyLeft = false;
                break;
            case Keys.S:
            case Keys.DOWN:
            	keyDown = false;
                break;
            case Keys.D:
            case Keys.RIGHT:
            	keyRight = false;
                break;
        }
        return true;
    }
    
    // Optional: Helper method to reset keys (useful when changing scenes)
    public void reset() {
    	keyLeft = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
    }
}