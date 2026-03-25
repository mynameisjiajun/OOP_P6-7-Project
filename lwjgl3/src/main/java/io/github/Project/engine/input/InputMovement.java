package io.github.Project.engine.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

public class InputMovement extends InputAdapter {

    // Keyboard State
    private boolean keyUp;
    private boolean keyDown;
    private boolean keyLeft;
    private boolean keyRight;

    // Mouse State
    private boolean mouseLeft;
    private boolean mouseRight;
    private int mouseX;
    private int mouseY;

    // ── Getters ──────────────────────────────────────────────────────────

    /** @return true while the thrust/up key is held */
    public boolean isKeyUp()      { return keyUp; }

    /** @return true while the brake/down key is held */
    public boolean isKeyDown()    { return keyDown; }

    /** @return true while the rotate-left key is held */
    public boolean isKeyLeft()    { return keyLeft; }

    /** @return true while the rotate-right key is held */
    public boolean isKeyRight()   { return keyRight; }

    /** @return true while the primary mouse button is held */
    public boolean isMouseLeft()  { return mouseLeft; }

    /** @return true while the secondary mouse button is held */
    public boolean isMouseRight() { return mouseRight; }

    /** @return current mouse X in screen coordinates */
    public int getMouseX()        { return mouseX; }

    /** @return current mouse Y in screen coordinates */
    public int getMouseY()        { return mouseY; }

    // Called when a key is pressed down.

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.W:
            case Keys.UP:    keyUp    = true; break;
            case Keys.A:
            case Keys.LEFT:  keyLeft  = true; break;
            case Keys.S:
            case Keys.DOWN:  keyDown  = true; break;
            case Keys.D:
            case Keys.RIGHT: keyRight = true; break;
            default:         break;
        }
        return true;
    }

    // Called when a key is released.
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.W:
            case Keys.UP:    keyUp    = false; break;
            case Keys.A:
            case Keys.LEFT:  keyLeft  = false; break;
            case Keys.S:
            case Keys.DOWN:  keyDown  = false; break;
            case Keys.D:
            case Keys.RIGHT: keyRight = false; break;
            default:         break;
        }
        return true;
    }

    // Called when a mouse button is pressed.

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Update the coordinates of the click
        this.mouseX = screenX;
        this.mouseY = screenY;

        if      (button == Input.Buttons.LEFT)  mouseLeft  = true;
        else if (button == Input.Buttons.RIGHT) mouseRight = true;
        return true;
    }

    // Called when a mouse button is released.
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Update coordinates (optional, but good for drag release logic)
        this.mouseX = screenX;
        this.mouseY = screenY;

        if      (button == Input.Buttons.LEFT)  mouseLeft  = false;
        else if (button == Input.Buttons.RIGHT) mouseRight = false;
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
    
    // Updates mouse coordinates while clicking and dragging.

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