package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;
import io.github.Project.game.scenes.MainMenuScene;
import io.github.Project.game.scenes.PauseScene;
import io.github.Project.game.scenes.PlayScene;

public class IOManager {

    private final GameMaster gameMaster;

    // prevent key being triggered repeatedly when held
    private boolean pauseKeyLocked;
    private boolean resetKeyLocked;
    private boolean menuKeyLocked;

    public IOManager(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }

    // runs every frame
    public void update() {
        Scene currentScene = gameMaster.getSceneManager().getCurrentScene();
        if (currentScene == null) return;

        handlePauseInput(currentScene);
        handleResetInput(currentScene);
        handleMainMenuInput(currentScene);
        handleRocketAudio(currentScene);
    }

    private void handlePauseInput(Scene currentScene) {
        boolean pausePressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE) ||
                               Gdx.input.isKeyPressed(Input.Keys.P);

        if (pausePressed && !pauseKeyLocked) {
            pauseKeyLocked = true;

            if (currentScene instanceof PlayScene) {
                currentScene.setPaused(true);
                gameMaster.getAudioManager().pauseAllAudio();
                gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
            }
        }

        if (!pausePressed) {
            pauseKeyLocked = false;
        }
    }

    private void handleResetInput(Scene currentScene) {
        boolean resetPressed = Gdx.input.isKeyPressed(Input.Keys.R);

        if (resetPressed && !resetKeyLocked) {
            resetKeyLocked = true;

            if (currentScene instanceof PlayScene) {
                gameMaster.getAudioManager().stopRocketLoop();
                gameMaster.getAudioManager().stopMusic();
                gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
            }
        }

        if (!resetPressed) {
            resetKeyLocked = false;
        }
    }

    private void handleMainMenuInput(Scene currentScene) {
        boolean menuPressed = Gdx.input.isKeyPressed(Input.Keys.M);

        if (menuPressed && !menuKeyLocked) {
            menuKeyLocked = true;

            if (currentScene instanceof PlayScene || currentScene instanceof PauseScene) {
                gameMaster.getAudioManager().stopRocketLoop();
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        }

        if (!menuPressed) {
            menuKeyLocked = false;
        }
    }

    // control rocket sound based on thrust input
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

    // sound triggers (called from other parts of the game)
    public void playCollisionEffect() {
        gameMaster.getAudioManager().playCollisionSound();
    }

    public void playRefuelEffect() {
        gameMaster.getAudioManager().playRefuelSound();
    }

    public void playWinEffect() {
        gameMaster.getAudioManager().playWinSound();
    }

    public void stopRocketEffect() {
        gameMaster.getAudioManager().stopRocketLoop();
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

    public boolean localFileExists(String filePath) {
        return Gdx.files.local(filePath).exists();
    }

    public boolean internalFileExists(String filePath) {
        return Gdx.files.internal(filePath).exists();
    }
}
