package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.engine.managers.CollisionManager;
import io.github.Project.game.entities.Player;
import io.github.Project.game.entities.Ball;
import io.github.Project.game.movementstrategy.PlayerMovementStrategy;
import io.github.Project.game.movementstrategy.BounceMovementStrategy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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

// Main gameplay scene.
//Renders player and ball entities
// Updates only happen when not paused
 //Entities are scene-scoped
 
public class PlayScene extends Scene {
    private Stage stage;
    private Skin skin;
    private TextButton pauseButton;
    
    // Game objects
    private InputMovement inputMovement;
    private Player player;
    private Ball ball;
    
    // Collision listener reference for cleanup
    private CollisionManager.CollisionListener collisionListener;
    
    public PlayScene(GameMaster gameMaster) {
        super(gameMaster);
    }
    
    @Override
    public void show() {
        // Play background music
        gameMaster.getAudioManager().startDefaultBackgroundMusic();
        
        // Initialize UI Stage
        stage = new Stage(new ScreenViewport());
        
        // Create input handler for player movement (WASD / Arrow keys)
        inputMovement = gameMaster.getInputMovement();
        
        // Use InputMultiplexer so both UI buttons AND keyboard movement work
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(inputMovement);
        Gdx.input.setInputProcessor(multiplexer);
        
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        
        // Create player rectangle at bottom center
        player = new Player(
            screenW / 2f - 50,  // center X
            30,                  // near bottom
            100, 20,             // wide paddle shape
            inputMovement
        );
        
        // Create bouncing ball at center
        ball = new Ball(
            screenW / 2f - 15,  // center X
            screenH / 2f,       // center Y
            30,                  // ball size
            150f,                // ball speed
            screenW, screenH    // screen bounds for bouncing
        );
        
        // FIXED: Use addSceneEntity instead of direct manager calls
        addSceneEntity(player);
        addSceneEntity(ball);
        
        gameMaster.getMovementManager().registerEntity(player, new PlayerMovementStrategy());
        gameMaster.getMovementManager().registerEntity(ball, new BounceMovementStrategy());
        
        // Set up collision listener using CollisionInfo
        collisionListener = new CollisionManager.CollisionListener() {
            @Override
            public void onCollision(CollisionManager.CollisionInfo info) {
                // Check if this is a ball-player collision
                if (info.isBetween("ball", "player")) {
                    gameMaster.getIoManager().playCollisionEffect();

                    Ball b = null;
                    Player p = null;
                    
                    // Identify which entity is which
                    if (info.entity1 instanceof Ball) {
                        b = (Ball) info.entity1;
                        p = (Player) info.entity2;
                    } else {
                        b = (Ball) info.entity2;
                        p = (Player) info.entity1;
                    }
                    
                    if (b != null && p != null) {
                        // Determine bounce direction based on overlap
                        if (info.overlapY < info.overlapX) {
                            // Vertical collision (top/bottom of paddle)
                            b.bounceY();
                            
                            // Push ball out of player to prevent sticking
                            if (b.getPosY() < p.getPosY()) {
                                b.setPosY(p.getPosY() - b.getHeight() - 1);
                            } else {
                                b.setPosY(p.getPosY() + p.getHeight() + 1);
                            }
                        } else {
                            // Horizontal collision (sides of paddle)
                            b.setVx(-b.getVx());
                            
                            // Push ball out of player horizontally
                            if (b.getPosX() < p.getPosX()) {
                                b.setPosX(p.getPosX() - b.getWidth() - 1);
                            } else {
                                b.setPosX(p.getPosX() + p.getWidth() + 1);
                            }
                        }
                    }
                } 
            }
        };
        
        gameMaster.getCollisionManager().addListener(collisionListener);
        
        // Create skin & UI
        skin = createSkin();
        pauseButton = new TextButton("Pause", skin);
        
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(true);
                gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
            }
        });
        
        // Layout - button on top right
        Table table = new Table();
        table.right().top();
        table.setFillParent(true);
        table.pad(20);
        table.add(pauseButton).width(100).height(40);
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
        textButtonStyle.down = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.over = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.font = skin.getFont("default");
        textButtonStyle.fontColor = Color.WHITE;
        
        skin.add("default", textButtonStyle);
        
        return skin;
    }

    // Rendering and updates now happen in the scene.
    //Logic only runs when not paused.
     
    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameMaster.getIoManager().update();
        
        // FIXED: Only update game logic when NOT paused
        if (!isPaused) {
            // Update movement
            gameMaster.getMovementManager().updateMovements(delta);
            
            // FIXED: Pass scene entities to collision manager (no direct manager-to-manager talk)
            gameMaster.getCollisionManager().checkCollisions(getSceneEntities());
            
            // Update entities
            for (Entity entity : getSceneEntities()) {
                entity.update(delta);
            }
        }
        
        // FIXED: Render entities in the scene (always render, even when paused)
        for (Entity entity : getSceneEntities()) {
            entity.render(gameMaster.getSharedBatch(), gameMaster.getSharedShapeRenderer());
        }
        
        // Update and draw UI stage (on top of game)
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
        if (ball != null) {
            ball.setScreenSize(width, height);
        }
    }
    
    @Override
    public void pause() {
        super.pause();
        // Called when game is paused
    }
    
    @Override
    public void dispose() {
        // FIXED: Use clearSceneEntities from base Scene class
        if (collisionListener != null) {
            gameMaster.getCollisionManager().removeListener(collisionListener);
        }
        clearSceneEntities();
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}