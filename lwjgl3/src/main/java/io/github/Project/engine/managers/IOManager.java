package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.core.Scene;
import io.github.Project.game.scenes.MainMenuScene;
import io.github.Project.game.scenes.PauseScene;
import io.github.Project.game.scenes.PlayScene;

//handles keyboard input (pause, reset, menu) and file I/O
//uses isKeyJustPressed() for single-trigger actions (no manual key locks)
//rocket audio uses isKeyPressed() for continuous input
//input behaviour depends on current scene (PlayScene / PauseScene)

public class IOManager {

    private final GameMaster gameMaster;

    public IOManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    // called every frame to proces input and audio
    public void update() {
        Scene currentScene = gameMaster.getSceneManager().getCurrentScene();
        if (currentScene == null) return;

        handlePauseInput(currentScene);
        handleResetInput(currentScene);
        handleMainMenuInput(currentScene);
        handleRocketAudio(currentScene);
    }

    // Input handlers

    // Pauses the game and switch to pause scene
    private void handlePauseInput(Scene currentScene) {
        boolean pauseJustPressed = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                                || Gdx.input.isKeyJustPressed(Input.Keys.P);

        if (pauseJustPressed && currentScene instanceof PlayScene) {
            currentScene.setPaused(true);
            gameMaster.getAudioManager().pauseAllAudio();
            gameMaster.getSceneManager().pauseAndSetState(new PauseScene(gameMaster));
        }
    }

    // Restarts Game when R is pressed
    private void handleResetInput(Scene currentScene) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && currentScene instanceof PlayScene) {
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getAudioManager().stopMusic();
            gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
        }
    }

    // Returns to main menu when M is pressed 
    private void handleMainMenuInput(Scene currentScene) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)
                && (currentScene instanceof PlayScene || currentScene instanceof PauseScene)) {
            gameMaster.getAudioManager().stopRocketLoop();
            gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
        }
    }

    // control rocket thrust sound based on key hold
    private void handleRocketAudio(Scene currentScene) {
        if (!(currentScene instanceof PlayScene) || currentScene.isPaused()) {
            gameMaster.getAudioManager().stopRocketLoop();
            return;
        }

        if (gameMaster.getInputMovement() == null) {
            gameMaster.getAudioManager().stopRocketLoop();
            return;
        }

        if (gameMaster.getInputMovement().isKeyUp()) {
            gameMaster.getAudioManager().playRocketLoop();
        } else {
            gameMaster.getAudioManager().stopRocketLoop();
        }
    }

    // Sound effect triggers (called from scenes)

    public void playCollisionEffect() { gameMaster.getAudioManager().playCollisionSound(); }
    public void playRefuelEffect()    { gameMaster.getAudioManager().playRefuelSound(); }
    public void playWinEffect()       { gameMaster.getAudioManager().playWinSound(); }
    public void stopRocketEffect()    { gameMaster.getAudioManager().stopRocketLoop(); }
    public void playGameOverSound() {
        gameMaster.getAudioManager().playGameOverSound();
    }

    // File I/O

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
