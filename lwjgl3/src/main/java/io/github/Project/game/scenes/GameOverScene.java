package io.github.Project.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.Project.engine.main.GameMaster;

/**
 * Shown when the space station is destroyed.
 * Displays the reason and final score, then offers Try Again or Main Menu.
 */
public class GameOverScene extends StarBackgroundScene {

    private static final Color BG    = new Color(0.05f, 0.01f, 0.01f, 1f);
    private static final Color UP    = new Color(0.22f, 0.06f, 0.06f, 1f);
    private static final Color OVER  = new Color(0.80f, 0.20f, 0.10f, 1f);
    private static final Color DOWN  = new Color(0.50f, 0.10f, 0.05f, 1f);
    private static final Color TITLE = new Color(1.00f, 0.25f, 0.15f, 1f);
    private static final Color STARS = new Color(0.80f, 0.20f, 0.10f, 1f);

    private final int    score;
    private final String reason;

    public GameOverScene(GameMaster gameMaster, int score, String reason) {
        super(gameMaster, BG, UP, OVER, DOWN, TITLE, STARS, 99887L, 80);
        this.score  = score;
        this.reason = reason;
    }

    @Override
    public void show() {
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
        super.show();
    }

    @Override
    protected void buildUI() {
        addLabelStyle("info", new Color(0.85f, 0.55f, 0.55f, 1f));

        Label titleLabel  = new Label("MISSION FAILED", skin, "title");
        titleLabel.setFontScale(2.5f);
        Label reasonLabel = new Label(reason, skin, "info");
        Label scoreLabel  = new Label("Debris cleared: " + score, skin);

        TextButton tryAgainButton = new TextButton("TRY AGAIN", skin);
        TextButton menuButton     = new TextButton("MAIN MENU", skin);

        tryAgainButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
            }
        });
        menuButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(12).row();
        table.add(reasonLabel).padBottom(32).row();
        table.add(scoreLabel).padBottom(48).row();
        table.add(tryAgainButton).width(220).height(60).padBottom(18).row();
        table.add(menuButton).width(220).height(60).row();

        stage.addActor(table);
    }
}
