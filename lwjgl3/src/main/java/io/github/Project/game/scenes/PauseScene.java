package io.github.Project.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.core.Scene;

/**
 * Pause menu scene — space-themed overlay.
 */
public class PauseScene extends StarBackgroundScene {

    private static final Color BG    = new Color(0.00f, 0.00f, 0.04f, 1f);
    private static final Color UP    = new Color(0.08f, 0.12f, 0.28f, 1f);
    private static final Color OVER  = new Color(0.80f, 0.40f, 0.05f, 1f);
    private static final Color DOWN  = new Color(0.50f, 0.25f, 0.02f, 1f);
    private static final Color TITLE = new Color(1.00f, 0.60f, 0.10f, 1f);

    public PauseScene(GameMaster gameMaster) {
        super(gameMaster, BG, UP, OVER, DOWN, TITLE, Color.WHITE, 99L, 100);
    }

    @Override
    public void show() {
        gameMaster.getAudioManager().pauseMusic();
        super.show();
    }

    @Override
    protected void buildUI() {
        Label titleLabel = new Label("// PAUSED //", skin, "title");
        titleLabel.setFontScale(2f);

        TextButton resumeButton   = new TextButton("RESUME",    skin);
        TextButton optionsButton  = new TextButton("OPTIONS",   skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);

        resumeButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getAudioManager().resumeMusic();
                gameMaster.getSceneManager().resumePreviousScene();
            }
        });
        optionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(
                    new OptionsScene(gameMaster, OptionsScene.BackTarget.PAUSE_MENU));
            }
        });
        mainMenuButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                Scene previousScene = gameMaster.getSceneManager().getPreviousScene();
                if (previousScene != null) previousScene.dispose();
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(60).row();
        table.add(resumeButton).width(220).height(60).padBottom(18).row();
        table.add(optionsButton).width(220).height(60).padBottom(18).row();
        table.add(mainMenuButton).width(220).height(60).row();

        stage.addActor(table);
    }

    @Override
    public void resume() {
        gameMaster.getSceneManager().resumePreviousScene();
    }
}
