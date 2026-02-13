package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Main play/game scene.
 * This is where the actual gameplay happens.
 */
public class PlayScene extends Scene {
    private Stage stage;
    private Skin skin;
    private TextButton pauseButton;
    private boolean isPlaying = false;
    
    /**
     * Creates a new PlayScene.
     * @param gameMaster Reference to the game master
     */
    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Called when scene becomes active
        // Play background music
        gameMaster.getAudioManager()
                  .startDefaultBackgroundMusic();
        
        // Initialize UI Stage
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        // Create skin programmatically
        skin = createSkin();
        
        // Create pause button
        pauseButton = new TextButton("Pause", skin);
        
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
            }
        });
        
        // Create table for layout - button on top right
        Table table = new Table();
        table.right().top();
        table.setFillParent(true);
        table.pad(20);
        
        table.add(pauseButton).width(100).height(40);
        
        stage.addActor(table);
    }
    
    /**
     * Creates a basic skin for UI elements programmatically
     */
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
        textButtonStyle.down = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.over = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.font = skin.getFont("default");
        textButtonStyle.fontColor = Color.WHITE;
        
        skin.add("default", textButtonStyle);
        
        return skin;
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Check for ESC key to pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
        }
        
        // Update simulation logic
        // TODO: Update game state, physics, AI, etc.
        
        // Render simulation
        // TODO: Draw entities, background, UI, etc.
        
        // Update and draw UI stage
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
    public void pause() {
        // Called when game is paused
        gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        // TODO: Dispose textures, sounds, etc.
    }
}
