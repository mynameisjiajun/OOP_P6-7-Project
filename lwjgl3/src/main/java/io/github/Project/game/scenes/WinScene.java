package io.github.Project.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.Project.engine.main.GameMaster;

/**
 * Shown when the player clears enough debris to win.
 * Displays the final score and station health, then offers
 * Play Again or Main Menu.
 */
public class WinScene extends StarBackgroundScene {

    private static final Color BG    = new Color(0.02f, 0.05f, 0.02f, 1f);
    private static final Color UP    = new Color(0.05f, 0.20f, 0.08f, 1f);
    private static final Color OVER  = new Color(0.20f, 0.70f, 0.25f, 1f);
    private static final Color DOWN  = new Color(0.10f, 0.40f, 0.12f, 1f);
    private static final Color TITLE = new Color(0.30f, 1.00f, 0.40f, 1f);

    private final int score;
    private final int stationHpPct;

    public WinScene(GameMaster gameMaster, int score, int stationHpPct) {
        super(gameMaster, BG, UP, OVER, DOWN, TITLE, TITLE, 12345L, 140);
        this.score        = score;
        this.stationHpPct = stationHpPct;
    }

    @Override
    public void show() {
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
        super.show();
    }

    @Override
    protected void buildUI() {
        addLabelStyle("info", Color.LIGHT_GRAY);

        Label titleLabel = new Label("MISSION COMPLETE", skin, "title");
        titleLabel.setFontScale(2.5f);

        Label subLabel   = new Label("The space station is safe!", skin, "info");
        Label scoreLabel = new Label("Debris cleared: " + score + " pieces", skin);
        Label hpLabel    = new Label("Station integrity: " + stationHpPct + "%", skin);
        Label factLabel1 = new Label("In reality: 27,000+ pieces of tracked debris orbit Earth", skin, "info");
        Label factLabel2 = new Label("3,000+ dead satellites still remain in orbit today", skin, "info");
        Label factLabel3 = new Label("It costs millions per year just to track - not remove - this debris", skin, "info");

        TextButton playAgainButton = new TextButton("PLAY AGAIN", skin);
        TextButton menuButton      = new TextButton("MAIN MENU",  skin);

        playAgainButton.addListener(new ClickListener() {
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
        table.add(subLabel).padBottom(32).row();
        table.add(scoreLabel).padBottom(4).row();
        table.add(hpLabel).padBottom(28).row();
        table.add(factLabel1).padBottom(4).row();
        table.add(factLabel2).padBottom(4).row();
        table.add(factLabel3).padBottom(28).row();
        table.add(playAgainButton).width(220).height(60).padBottom(18).row();
        table.add(menuButton).width(220).height(60).row();

        stage.addActor(table);
    }
}
