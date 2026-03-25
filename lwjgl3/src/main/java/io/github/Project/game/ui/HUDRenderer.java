package io.github.Project.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.game.collisionstrategies.RocketCollisionStrategy;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.healthbar;
import io.github.Project.game.factory.DebrisFactory;

/**
 * Renders the in-game HUD: stats panel, station health bar,
 * Kessler warning, landing guide, repair message, control hints,
 * and fact/notification popups.
 */
public class HUDRenderer {

    // ── Notification constants ───────────────────────────────────────────
    private static final float NOTIF_DURATION = 5.5f;
    private static final float NOTIF_FADE_IN  = NOTIF_DURATION * 0.12f;
    private static final float NOTIF_FADE_OUT = NOTIF_DURATION * 0.20f;

    private final GameMaster    gameMaster;
    private final Rocket        rocket;
    private final SpaceStation  spaceStation;
    private final DebrisFactory debrisManager;

    private final BitmapFont  font;
    private final GlyphLayout glyphLayout;
    private final healthbar   stationHealthBar;

    // ── Notification state ───────────────────────────────────────────────
    private String notifText  = null;
    private float  notifTimer = 0f;

    /** Shows a fact/toast notification at the bottom of the screen. */
    public void show(String text) {
        this.notifText  = text;
        this.notifTimer = NOTIF_DURATION;
    }

    public HUDRenderer(GameMaster gameMaster, Rocket rocket,
                       SpaceStation spaceStation,
                       DebrisFactory debrisManager,
                       healthbar stationHealthBar) {
        this.gameMaster   = gameMaster;
        this.rocket       = rocket;
        this.spaceStation = spaceStation;
        this.debrisManager = debrisManager;
        this.font         = new BitmapFont();
        this.glyphLayout  = new GlyphLayout();
        this.stationHealthBar = stationHealthBar;
    }

    public void draw(OrthographicCamera hudCamera,
                     float repairCooldownTimer,
                     float repairMessageTimer,
                     float stationRepairAmount) {

        float vW = hudCamera.viewportWidth;
        float vH = hudCamera.viewportHeight;

        boolean stationWarningActive = debrisManager.isStationWarningActive();
        float   stationWarningPulse  = debrisManager.getStationWarningPulse();
        boolean kesslerActive        = debrisManager.isKesslerActive();
        float   kesslerPulse         = debrisManager.getKesslerPulse();

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Top-left stats panel
        sr.setColor(0f, 0f, 0f, 0.55f);
        sr.rect(0f, vH - 135f, 230f, 135f);

        // Top-centre station health bar background
        float barW = 300f;
        float barX = (vW - barW) / 2f;
        float barY = vH - 28f;
        sr.setColor(0f, 0f, 0f, 0.60f);
        sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);

        if (stationWarningActive) {
            float pulse = (MathUtils.sin(stationWarningPulse) + 1f) / 2f;
            sr.setColor(1f, 0.2f, 0f, 0.4f * pulse);
            sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);
        }

        float hp = spaceStation.getHealthPercentage();
        stationHealthBar.setPosX(barX);
        stationHealthBar.setPosY(barY);
        stationHealthBar.setHP(hp);
        stationHealthBar.render(null, sr);

        // Kessler warning banner
        if (kesslerActive) {
            float pulse = (MathUtils.sin(kesslerPulse) + 1f) / 2f;
            sr.setColor(0.8f, 0f, 0f, 0.35f + 0.25f * pulse);
            sr.rect(0f, vH / 2f - 22f, vW, 44f);
        }

        sr.end();

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Station label
        font.getData().setScale(0.75f);
        String stLabel = stationWarningActive ? "!! STATION UNDER THREAT !!" : "SPACE STATION HEALTH";
        font.setColor(stationWarningActive ? new Color(1f, 0.35f, 0f, 1f) : Color.WHITE);
        glyphLayout.setText(font, stLabel);
        font.draw(batch, stLabel, (vW - glyphLayout.width) / 2f, vH - 31f);
        font.setColor(Color.WHITE);

        // Kessler warning text
        if (kesslerActive) {
            font.getData().setScale(0.9f);
            font.setColor(1f, 0.25f, 0.1f, 1f);
            String kMsg = "!! KESSLER CASCADE ACTIVE !!";
            glyphLayout.setText(font, kMsg);
            font.draw(batch, kMsg, (vW - glyphLayout.width) / 2f, vH / 2f + 8f);
            font.setColor(Color.WHITE);
        }

        // Top-left stats
        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "ALT:   " + (int) rocket.getPosY() + " m",                          10f, vH - 15f);
        font.draw(batch, "SCORE: " + debrisManager.getDebrisCollected() + "/" + DebrisFactory.WIN_CLEAR_SCORE, 10f, vH - 35f);

        int inFlight = debrisManager.getFlying().size;
        Color debrisColor;
        if      (inFlight > 10) debrisColor = Color.RED;
        else if (inFlight >  5) debrisColor = Color.YELLOW;
        else                    debrisColor = Color.LIGHT_GRAY;
        font.setColor(debrisColor);
        font.draw(batch, "ORBIT: " + inFlight + " debris", 10f, vH - 55f);
        font.setColor(Color.WHITE);

        int bowlCount = debrisManager.getAttached().size;
        font.setColor(bowlCount > 0 ? Color.CYAN : Color.LIGHT_GRAY);
        font.draw(batch, "BOWL:  " + bowlCount + "/" + DebrisFactory.MAX_BOWL_CAPACITY, 10f, vH - 75f);
        font.setColor(Color.WHITE);

        font.getData().setScale(0.65f);
        font.setColor(0.65f, 0.65f, 0.65f, 1f);
        font.draw(batch, "STATION: " + (int)(spaceStation.getHealthPercentage() * 100) + "% HP", 10f, vH - 95f);

        // Pad repair cooldown
        if (repairCooldownTimer > 0f) {
            font.getData().setScale(0.8f);
            font.setColor(1f, 0.6f, 0.1f, 1f);
            font.draw(batch, "PAD: ready in " + (int) Math.ceil(repairCooldownTimer) + "s", 10f, vH - 118f);
        } else {
            font.getData().setScale(0.8f);
            font.setColor(0.2f, 1f, 0.4f, 1f);
            font.draw(batch, "PAD: READY", 10f, vH - 118f);
        }
        font.setColor(Color.WHITE);

        // Station repair message
        if (repairMessageTimer > 0f) {
            float alpha = Math.min(1f, repairMessageTimer / (2.5f * 0.3f));
            font.getData().setScale(0.9f);
            font.setColor(0.30f, 1.00f, 0.40f, alpha);
            glyphLayout.setText(font, "STATION REPAIRED +" + (int) stationRepairAmount);
            font.draw(batch, glyphLayout, (vW - glyphLayout.width) / 2f, vH / 2f + 50f);
            font.setColor(Color.WHITE);
        }

        // Fact / notification popup
        if (notifTimer > 0f) notifTimer -= Gdx.graphics.getDeltaTime();
        if (notifText != null && notifTimer > 0f) {
            float alpha;
            if      (notifTimer > NOTIF_DURATION - NOTIF_FADE_IN) alpha = 1f - (notifTimer - (NOTIF_DURATION - NOTIF_FADE_IN)) / NOTIF_FADE_IN;
            else if (notifTimer < NOTIF_FADE_OUT)                  alpha = notifTimer / NOTIF_FADE_OUT;
            else                                                    alpha = 1f;

            font.getData().setScale(1.1f);
            font.setColor(0.95f, 0.88f, 0.55f, alpha);

            String[] words = notifText.split(" ");
            StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
            for (String w : words) {
                glyphLayout.setText(font, sb1 + w + " ");
                if (glyphLayout.width < vW * 0.75f) sb1.append(w).append(" ");
                else                                sb2.append(w).append(" ");
            }
            String l1 = sb1.toString().trim();
            String l2 = sb2.toString().trim();
            glyphLayout.setText(font, l1);
            float l1x = (vW - glyphLayout.width) / 2f;
            font.draw(batch, l1, l1x + 1f, 72f);  // bold: draw twice with 1px offset
            font.draw(batch, l1, l1x,       72f);
            if (!l2.isEmpty()) {
                glyphLayout.setText(font, l2);
                float l2x = (vW - glyphLayout.width) / 2f;
                font.draw(batch, l2, l2x + 1f, 50f);
                font.draw(batch, l2, l2x,       50f);
            }
            font.setColor(Color.WHITE);
        }

        // Landing guide (shown when near ground)
        if (rocket.getPosY() < 500f) {
            batch.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(0f, 0f, 0f, 0.70f);
            sr.rect(6f, 108f, 230f, 80f);
            sr.end();
            batch.begin();

            float speed   = Math.abs(rocket.getVy());
            float angle   = rocket.getRotation();
            boolean spdOk = speed <= RocketCollisionStrategy.MAX_SAFE_LANDING_SPEED;
            boolean angOk = angle >= RocketCollisionStrategy.MIN_UPRIGHT_ANGLE
                         && angle <= RocketCollisionStrategy.MAX_UPRIGHT_ANGLE;

            font.getData().setScale(1.1f);
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
            font.draw(batch, "LANDING APPROACH", 14f, 182f);

            font.getData().setScale(1.0f);
            font.setColor(spdOk ? Color.GREEN : Color.RED);
            font.draw(batch, "SPD: " + (int) speed + " / " + (int) RocketCollisionStrategy.MAX_SAFE_LANDING_SPEED, 14f, 160f);

            font.setColor(angOk ? Color.GREEN : Color.RED);
            font.draw(batch, "ANG: " + (int) angle + "  (aim 65-115)", 14f, 136f);

            font.setColor(Color.WHITE);
        }

        // Control hints
        font.getData().setScale(0.85f);
        font.setColor(0.70f, 0.70f, 0.70f, 1f);
        font.draw(batch, "[W / UP] Thrust    [A / D] Rotate    [E] Launch debris", 10f, 36f);
        font.draw(batch, "[Land on pad] Repair station    [ESC] Pause", 10f, 16f);
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        batch.end();
    }

    public void dispose() {
        if (font != null) font.dispose();
    }
}
