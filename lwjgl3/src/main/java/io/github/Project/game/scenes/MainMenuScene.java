package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.Project.engine.main.GameMaster;

/**
 * Main menu scene — space-themed entry screen.
 */
public class MainMenuScene extends StarBackgroundScene {

    private static final Color BG    = new Color(0.02f, 0.02f, 0.08f, 1f);
    private static final Color UP    = new Color(0.08f, 0.12f, 0.28f, 1f);
    private static final Color OVER  = new Color(0.80f, 0.40f, 0.05f, 1f);
    private static final Color DOWN  = new Color(0.50f, 0.25f, 0.02f, 1f);
    private static final Color TITLE = new Color(1.00f, 0.60f, 0.10f, 1f);

    public MainMenuScene(GameMaster gameMaster) {
        super(gameMaster, BG, UP, OVER, DOWN, TITLE, Color.WHITE, 42L, 120);
    }

    @Override
    public void show() {
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
        super.show();
    }

    @Override
    protected void buildUI() {
        Label titleLabel = new Label("ROCKET JOURNEY", skin, "title");
        titleLabel.setFontScale(2.5f);
        Label subLabel = new Label("Protect the Space Station!", skin);

        TextButton playButton    = new TextButton("LAUNCH",  skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton    = new TextButton("EXIT",    skin);

        playButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
            }
        });
        optionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(
                    new OptionsScene(gameMaster, OptionsScene.BackTarget.MAIN_MENU));
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                Gdx.app.exit();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(8).row();
        table.add(subLabel).padBottom(60).row();
        table.add(playButton).width(220).height(60).padBottom(18).row();
        table.add(optionsButton).width(220).height(60).padBottom(18).row();
        table.add(exitButton).width(220).height(60).row();

        stage.addActor(table);
    }
}
