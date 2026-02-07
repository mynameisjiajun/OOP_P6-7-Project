package io.github.Project.engine.interfaces;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

public class InputMovement extends InputAdapter {
    
    // --- Keyboard State ---
    public boolean keyUp;
    public boolean keyDown;
    public boolean keyLeft;
    public boolean keyRight;

    // --- Mouse State ---
    public boolean mouseLeft;
    public boolean mouseRight;
    public int mouseX;
    public int mouseY;

    /**
     * Called when a key is pressed down.
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
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
        return true;
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

    /**
     * Called when a mouse button is pressed.
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Update the coordinates of the click
        this.mouseX = screenX;
        this.mouseY = screenY;

        switch (button) {
            case Input.Buttons.LEFT:
                mouseLeft = true;
                break;
            case Input.Buttons.RIGHT:
                mouseRight = true;
                break;
        }
        return true;
    }

    /**
     * Called when a mouse button is released.
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Update coordinates (optional, but good for drag release logic)
        this.mouseX = screenX;
        this.mouseY = screenY;

        switch (button) {
            case Input.Buttons.LEFT:
                mouseLeft = false;
                break;
            case Input.Buttons.RIGHT:
                mouseRight = false;
                break;
        }
        return true;
    }

    /**
     * Updates mouse coordinates without clicking (Hovering).
     * Required if you want to track the mouse while it moves.
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.mouseX = screenX;
        this.mouseY = screenY;
        return true;
    }
    
    /**
     * Updates mouse coordinates while clicking and dragging.
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.mouseX = screenX;
        this.mouseY = screenY;
        return true;
    }

    // Reset all flags (useful for game over or scene changes)
    public void reset() {
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
        mouseLeft = false;
        mouseRight = false;
    }
}