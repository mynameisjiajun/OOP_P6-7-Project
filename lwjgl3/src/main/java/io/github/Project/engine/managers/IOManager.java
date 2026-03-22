package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;
import io.github.Project.game.scenes.MainMenuScene;
import io.github.Project.game.scenes.PauseScene;
import io.github.Project.game.scenes.PlayScene;

/**
 * Handles keyboard shortcut input (pause / reset / menu) and file I/O.
 *
 * CHANGES:
 * 1. Replaced manual boolean lock flags (pauseKeyLocked, etc.) with
 *    Gdx.input.isKeyJustPressed(). isKeyJustPressed() is true for exactly
 *    ONE frame — the frame the key transitions from up to down — which is
 *    exactly the behaviour the lock flags were replicating manually.
 *    This removes six lines of flag bookkeeping per key.
 *
 * 2. handleRocketAudio() still uses isKeyPressed() (held-key check) for the
 *    thrust sound because that needs to fire continuously while W is held —
 *    isKeyJustPressed() would be wrong there.
 *
 * NOTE: The instanceof checks against PlayScene / PauseScene remain here
 * because IOManager needs to react differently depending on the active scene.
 * The proper long-term fix would be to add a scene-level callback interface
 * (e.g. onPauseRequested()) so IOManager has no compile-time dependency on
 * concrete scene classes; that refactor is tracked separately.
 */
public class IOManager {

    private final GameMaster gameMaster;

    public IOManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    /** Called every frame from the active scene's render(). */
    public void update() {
        Scene currentScene = gameMaster.getSceneManager().getCurrentScene();
        if (currentScene == null) return;

        handlePauseInput(currentScene);
        handleResetInput(currentScene);
        handleMainMenuInput(currentScene);
        handleRocketAudio(currentScene);
    }

    // ── Input handlers ───────────────────────────────────────────────────────

    /**
     * Pauses the game and transitions to PauseScene when ESCAPE or P is
     * pressed once while in PlayScene.
     *
     * isKeyJustPressed() fires for exactly one frame, replacing the old
     * pauseKeyLocked boolean that manually blocked repeated triggers.
     */
    private void handlePauseInput(Scene currentScene) {
        boolean pauseJustPressed = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                                || Gdx.input.isKeyJustPressed(Input.Keys.P);

        if (pauseJustPressed && currentScene instanceof PlayScene) {
            currentScene.setPaused(true);
            gameMaster.getAudioManager().pauseAllAudio();
            gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
        }
    }

    /**
     * Resets (restarts) the game when R is pressed once while in PlayScene.
     */
    private void handleResetInput(Scene currentScene) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && currentScene instanceof PlayScene) {
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getAudioManager().stopMusic();
            gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
        }
    }

    /**
     * Returns to the main menu when M is pressed once while in PlayScene
     * or PauseScene.
     */
    private void handleMainMenuInput(Scene currentScene) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                && (currentScene instanceof PlayScene || currentScene instanceof PauseScene)) {
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
        }
    }

    /**
     * Drives the rocket thrust sound loop based on held-key state.
     * Uses isKeyPressed() (continuous) rather than isKeyJustPressed() (single frame)
     * because the sound must play for the entire duration the key is held.
     */
    private void handleRocketAudio(Scene currentScene) {
        if (!(currentScene instanceof PlayScene) || currentScene.isPaused()) {
            gameMaster.getAudioManager().stopRocketLoop();
            return;
        }

        if (gameMaster.getInputMovement() == null) {
            gameMaster.getAudioManager().stopRocketLoop();
            return;
        }

        if (gameMaster.getInputMovement().keyUp) {
            gameMaster.getAudioManager().playRocketLoop();
        } else {
            gameMaster.getAudioManager().stopRocketLoop();
        }
    }

    // ── Sound effect triggers (called from scenes) ───────────────────────────

    public void playCollisionEffect() { gameMaster.getAudioManager().playCollisionSound(); }
    public void playRefuelEffect()    { gameMaster.getAudioManager().playRefuelSound(); }
    public void playWinEffect()       { gameMaster.getAudioManager().playWinSound(); }
    public void stopRocketEffect()    { gameMaster.getAudioManager().stopRocketLoop(); }
    public void playGameOverSound() {
        gameMaster.getAudioManager().playSoundEffect(AudioManager.SFX_WIN);
    }

    // ── File I/O ─────────────────────────────────────────────────────────────

    public String readInternalFile(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        return file.readString();
    }

    public void writeLocalFile(String filePath, String content) {
        FileHandle file = Gdx.files.local(filePath);
        file.writeString(content, false);
    }

    public String readLocalFile(String filePath) {
        FileHandle file = Gdx.files.local(filePath);
        return file.exists() ? file.readString() : null;
    }

    public boolean localFileExists(String filePath)    { return Gdx.files.local(filePath).exists(); }
    public boolean internalFileExists(String filePath) { return Gdx.files.internal(filePath).exists(); }
}