package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Pause menu scene.
 * Displayed when the game is paused.
 */
public class PauseScene extends Scene {
    private Stage stage;
    private Skin skin;
    
    /**
     * Creates a new PauseScene.
     * @param gameMaster Reference to the game master
     */
    public PauseScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Pause the current music
        gameMaster.getAudioManager().pauseMusic();
        
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        skin = createSkin();
        
        // Create title label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        Label titleLabel = new Label("PAUSED", skin);
        titleLabel.setFontScale(2);
        
        // Create buttons
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton optionsButton = new TextButton("Options", skin);
        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        
        // Button listeners
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
            }
        });
        
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new OptionsScene(gameMaster));
            }
        });
        
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        });
        
        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        
        table.add(titleLabel).padBottom(50).row();
        table.add(resumeButton).width(200).height(60).padBottom(20).row();
        table.add(optionsButton).width(200).height(60).padBottom(20).row();
        table.add(mainMenuButton).width(200).height(60).row();
        
        stage.addActor(table);
    }
    
    private Skin createSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);
        
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();
        
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);
        
        return skin;
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }
    
    @Override
    public void resume() {
        gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
    }
    
    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
