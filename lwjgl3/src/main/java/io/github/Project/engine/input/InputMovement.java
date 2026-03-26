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

    // getters

    // true while thrust/up key is held
    public boolean isKeyUp()      { return keyUp; }

    // true while down key is held
    public boolean isKeyDown()    { return keyDown; }

    // true while left key is held
    public boolean isKeyLeft()    { return keyLeft; }

    // true while right key is held
    public boolean isKeyRight()   { return keyRight; }

    // true while left mouse button is held
    public boolean isMouseLeft()  { return mouseLeft; }

    // true while right mouse button is held
    public boolean isMouseRight() { return mouseRight; }

    // current mouse x position
    public int getMouseX()        { return mouseX; }

    // current mouse y position
    public int getMouseY()        { return mouseY; }

    // key pressed
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

    // key released
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

    // mouse button pressed
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.mouseX = screenX;
        this.mouseY = screenY;

        if      (button == Input.Buttons.LEFT)  mouseLeft  = true;
        else if (button == Input.Buttons.RIGHT) mouseRight = true;
        return true;
    }

    // mouse button released
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        this.mouseX = screenX;
        this.mouseY = screenY;

        if      (button == Input.Buttons.LEFT)  mouseLeft  = false;
        else if (button == Input.Buttons.RIGHT) mouseRight = false;
        return true;
    }

    // update mouse position while moving
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.mouseX = screenX;
        this.mouseY = screenY;
        return true;
    }

    // update mouse position while dragging
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.mouseX = screenX;
        this.mouseY = screenY;
        return true;
    }

    // reset all input flags
    public void reset() {
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
        mouseLeft = false;
        mouseRight = false;
    }
}