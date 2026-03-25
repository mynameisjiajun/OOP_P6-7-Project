package io.github.Project.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import io.github.Project.engine.main.GameMaster;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.factory.DebrisFactory;

/**
 * Owns all world-space rendering: background layers, entities, and
 * visual effects (explosion, atmosphere burn, station-hit sparks).
 * Drives the game camera position (including shake) each frame.
 */
public class WorldRenderer {

    // ── Zone boundaries ─────────────────────────────────────────────────
    private static final float EARTH_ZONE_END   = 2500f;
    private static final float SPACE_ZONE_START = 4000f;
    private static final float CLOUD_BAND_MIN   = 900f;
    private static final float CLOUD_BAND_MAX   = 1800f;

    // ── World geometry ───────────────────────────────────────────────────
    private static final int   TILE_SIZE        = 256;
    public  static final float WORLD_HALF_WIDTH = 1200f;

    // ── Agency building display sizes [mission, dish, vab, tanks, tower] ─
    private static final float[] AGENCY_W        = { 400f, 100f, 120f, 200f,  60f };
    private static final float[] AGENCY_H        = { 200f, 125f, 240f, 160f, 370f };
    private static final float[] AGENCY_BASE_Y   = { -45f, -45f, -45f, -67f, -45f };
    private static final int     AGENCY_VARIANTS  = 5;
    private static final int     AGENCY_INSTANCES = 10;
    private static final float   AGENCY_SPAWN_MIN_X      = -1700f;
    private static final float   AGENCY_SPAWN_MAX_X      =  1700f;
    private static final float   AGENCY_CENTER_EXCLUSION =  320f;
    private static final float   AGENCY_MIN_SPACING      =   45f;
    private static final long    AGENCY_LAYOUT_SEED      = 77777L;

    // ── Clouds ───────────────────────────────────────────────────────────
    private static final int CLOUD_COUNT = 6;

    // ── Effects ──────────────────────────────────────────────────────────
    private static final float EXPLOSION_DURATION       = 0.35f;
    private static final float EXPLOSION_SIZE           = 80f;
    private static final float ATMOSPHERE_BURN_DURATION = 0.6f;
    private static final float BURN_SIZE                = 90f;
    private static final float STATION_HIT_FX_DURATION  = 0.22f;
    private static final float STATION_HIT_FX_SIZE_MIN  = 28f;
    private static final float STATION_HIT_FX_SIZE_MAX  = 46f;

    // ── Bowl draw size ───────────────────────────────────────────────────
    private static final float BOWL_DRAW_W = 68f;
    private static final float BOWL_DRAW_H = 34f;

    // ── Background colours ───────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.40f, 0.70f, 0.95f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── StationHitFx inner class ─────────────────────────────────────────
    private static class StationHitFx {
        float x, y, size, timer;
        StationHitFx(float x, float y, float size) {
            this.x = x; this.y = y; this.size = size;
            this.timer = STATION_HIT_FX_DURATION;
        }
    }

    // ── Dependencies ─────────────────────────────────────────────────────
    private final GameMaster    gameMaster;
    private final Rocket        rocket;
    private final SpaceStation  spaceStation;
    private final EarthStation  earthStation;
    private final Array<Satellite> satellites;
    private final DebrisFactory debrisManager;

    // ── Background textures ──────────────────────────────────────────────
    private Texture   grassTopTex;
    private Texture   dirtTex;
    private Texture[] agencyTextures;
    private float[]   agencyData;
    private Texture[] cloudTextures;
    private float[]   cloudPositions;
    private float[]   starPositions;

    // ── Effect textures ──────────────────────────────────────────────────
    private Texture explosionTex;
    private Texture atmosphereBurnTex;
    private Texture bowlTex;

    // ── Effect state ─────────────────────────────────────────────────────
    private float explosionTimer;
    private float explosionX, explosionY;
    private float atmosphereBurnTimer;
    private float atmosphereBurnX, atmosphereBurnY;
    private final Array<StationHitFx> stationHitFxList = new Array<>();

    // ── Reusable colour ──────────────────────────────────────────────────
    private final Color tempColor = new Color();

    // ── Constructor ──────────────────────────────────────────────────────

    public WorldRenderer(GameMaster gameMaster, Rocket rocket,
                         SpaceStation spaceStation, EarthStation earthStation,
                         Array<Satellite> satellites, DebrisFactory debrisManager) {
        this.gameMaster   = gameMaster;
        this.rocket       = rocket;
        this.spaceStation = spaceStation;
        this.earthStation = earthStation;
        this.satellites   = satellites;
        this.debrisManager = debrisManager;
    }

    // ── Init ─────────────────────────────────────────────────────────────

    public void init() {
        // Ground textures
        grassTopTex = new Texture(Gdx.files.internal("Dirt texture/desert top.png"));
        dirtTex     = new Texture(Gdx.files.internal("Dirt texture/Dirt.png"));

        // Space agency buildings
        agencyTextures = new Texture[AGENCY_VARIANTS];
        agencyTextures[0] = new Texture(Gdx.files.internal("Ground Assets/mission_control.png"));
        agencyTextures[1] = new Texture(Gdx.files.internal("Ground Assets/satellite_dish.png"));
        agencyTextures[2] = new Texture(Gdx.files.internal("Ground Assets/vab_building.png"));
        agencyTextures[3] = new Texture(Gdx.files.internal("Ground Assets/fuel_tanks.png"));
        agencyTextures[4] = new Texture(Gdx.files.internal("Ground Assets/launch_tower.png"));

        agencyData = new float[AGENCY_INSTANCES * 2];
        float[] fixedX = { -1300f, -900f, -450f, 450f, 900f };
        for (int i = 0; i < AGENCY_VARIANTS; i++) {
            agencyData[i * 2]     = fixedX[i];
            agencyData[i * 2 + 1] = i;
        }
        long seed = AGENCY_LAYOUT_SEED;
        for (int i = AGENCY_VARIANTS; i < AGENCY_INSTANCES; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int type = (int)(((seed >> 17) & 0xFF) % AGENCY_VARIANTS);
            float x = 0f;
            boolean placed = false;
            for (int attempt = 0; attempt < 32; attempt++) {
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                x = ((seed >> 17) & 0xFFF) / (float) 0xFFF
                    * (AGENCY_SPAWN_MAX_X - AGENCY_SPAWN_MIN_X) + AGENCY_SPAWN_MIN_X;
                if (Math.abs(x) < AGENCY_CENTER_EXCLUSION)
                    x += (x < 0f ? -AGENCY_CENTER_EXCLUSION : AGENCY_CENTER_EXCLUSION);
                if (!isAgencySpawnTooClose(i, x, type)) { placed = true; break; }
            }
            if (!placed) {
                for (float candidate = AGENCY_SPAWN_MIN_X; candidate <= AGENCY_SPAWN_MAX_X; candidate += 25f) {
                    if (Math.abs(candidate) < AGENCY_CENTER_EXCLUSION) continue;
                    if (!isAgencySpawnTooClose(i, candidate, type)) { x = candidate; placed = true; break; }
                }
            }
            if (!placed) x = AGENCY_SPAWN_MAX_X + (i - AGENCY_VARIANTS + 1) * 260f;
            agencyData[i * 2]     = x;
            agencyData[i * 2 + 1] = type;
        }

        // Clouds
        cloudTextures = new Texture[CLOUD_COUNT];
        for (int i = 0; i < CLOUD_COUNT; i++)
            cloudTextures[i] = new Texture(Gdx.files.internal("tiles/pixelart_clouds/x5/cloud" + (i + 1) + ".png"));

        int numClouds = 30;
        cloudPositions = new float[numClouds * 3];
        long cs = 12345L;
        for (int i = 0; i < numClouds; i++) {
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3]   = ((cs>>17)&0x1FFF)/(float)0x1FFF*3600f-1800f;
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3+1] = CLOUD_BAND_MIN + ((cs>>17)&0xFFF)/(float)0xFFF*(CLOUD_BAND_MAX-CLOUD_BAND_MIN);
            cs = cs * 6364136223846793005L + 1; cloudPositions[i*3+2] = (int)(((cs>>17)&0xFF) % CLOUD_COUNT);
        }

        // Stars
        int numStars = 400;
        starPositions = new float[numStars * 3];
        long ss = 99887L;
        for (int i = 0; i < numStars; i++) {
            ss = ss * 6364136223846793005L + 1; starPositions[i*3]   = ((ss>>17)&0x1FFF)/(float)0x1FFF*6000f-3000f;
            ss = ss * 6364136223846793005L + 1; starPositions[i*3+1] = EARTH_ZONE_END + ((ss>>17)&0x3FFF)/(float)0x3FFF*8000f;
            ss = ss * 6364136223846793005L + 1; starPositions[i*3+2] = 0.5f + ((ss>>17)&0xFF)/(float)0xFF*2f;
        }

        // Effect textures
        explosionTex      = new Texture(Gdx.files.internal("Explosion_01.png"));
        atmosphereBurnTex = new Texture(Gdx.files.internal("New space assets/Atomsphere Burn.png"));
        bowlTex           = new Texture(Gdx.files.internal("New space assets/Bowl.png"));
    }

    // ── Update ───────────────────────────────────────────────────────────

    public void update(float delta) {
        // Tick station-hit spark timers
        for (int i = stationHitFxList.size - 1; i >= 0; i--) {
            StationHitFx fx = stationHitFxList.get(i);
            fx.timer -= delta;
            if (fx.timer <= 0f) stationHitFxList.removeIndex(i);
        }
        if (explosionTimer      > 0f) explosionTimer      -= delta;
        if (atmosphereBurnTimer > 0f) atmosphereBurnTimer -= delta;
    }

    // ── Effect triggers ──────────────────────────────────────────────────

    public void triggerExplosion(float x, float y) {
        explosionX     = x;
        explosionY     = y;
        explosionTimer = EXPLOSION_DURATION;
    }

    public void triggerAtmosphereBurn(float x, float y) {
        atmosphereBurnX     = x;
        atmosphereBurnY     = y;
        atmosphereBurnTimer = ATMOSPHERE_BURN_DURATION;
    }

    public void triggerStationHitFx(float hitX, float hitY) {
        stationHitFxList.add(new StationHitFx(
            hitX, hitY,
            MathUtils.random(STATION_HIT_FX_SIZE_MIN, STATION_HIT_FX_SIZE_MAX)));
        if (stationHitFxList.size > 24) stationHitFxList.removeIndex(0);
        spaceStation.triggerShake(0.35f, 14f);
    }

    // ── Draw ─────────────────────────────────────────────────────────────

    public void draw(float delta, OrthographicCamera gameCamera) {
        // Camera position — follow rocket, add shake offsets
        float baseCamX = rocket.getPosX() + rocket.getWidth()  / 2f;
        float baseCamY = rocket.getPosY() + rocket.getHeight() / 2f;

        if (spaceStation.getShakeTimer() > 0f) {
            float t     = spaceStation.getShakeTimer() / spaceStation.getShakeDuration();
            float amp   = spaceStation.getShakeAmplitude() * t * 0.5f;
            float phase = spaceStation.getShakeTimer() * 60f;
            baseCamX += MathUtils.sin(phase) * amp;
            baseCamY += MathUtils.cos(phase * 0.8f) * amp * 0.4f;
        }
        for (int i = 0; i < satellites.size; i++) {
            Satellite sat = satellites.get(i);
            if (sat.getShakeTimer() > 0f) {
                float t     = sat.getShakeTimer() / sat.getShakeDuration();
                float amp   = sat.getShakeAmplitude() * t * 0.5f;
                float phase = sat.getShakeTimer() * 65f;
                baseCamX += MathUtils.sin(phase) * amp;
                baseCamY += MathUtils.cos(phase * 0.9f) * amp * 0.4f;
                break;
            }
        }
        gameCamera.position.set(baseCamX, baseCamY, 0f);
        gameCamera.update();

        float camX  = gameCamera.position.x;
        float camY  = gameCamera.position.y;
        float halfW = gameCamera.viewportWidth  / 2f;
        float halfH = gameCamera.viewportHeight / 2f;

        // Background clear colour (sky ↔ space gradient)
        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Stars (ShapeRenderer pass)
        float starAlpha = camY >= SPACE_ZONE_START ? 1f
            : camY > EARTH_ZONE_END ? (camY - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END)
            : 0f;
        if (starAlpha > 0f) {
            ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
            sr.setProjectionMatrix(gameCamera.combined);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < starPositions.length; i += 3) {
                float sx = starPositions[i], sy = starPositions[i + 1];
                if (sy < camY - halfH || sy > camY + halfH) continue;
                if (sx < camX - halfW || sx > camX + halfW) continue;
                sr.setColor(1f, 1f, 1f, starAlpha);
                sr.circle(sx, sy, starPositions[i + 2]);
            }
            sr.end();
        }

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        drawBackground(batch, camX, camY, halfW, halfH);

        // Space station
        spaceStation.render(batch, null);

        // Earth station (landing pad)
        earthStation.render(batch, null);

        // Mini explosion bursts on station impacts
        for (int i = 0; i < stationHitFxList.size; i++) {
            StationHitFx fx = stationHitFxList.get(i);
            float alpha = fx.timer / STATION_HIT_FX_DURATION;
            batch.setColor(1f, 0.75f, 0.3f, alpha);
            batch.draw(explosionTex,
                fx.x - fx.size / 2f, fx.y - fx.size / 2f, fx.size, fx.size);
        }
        batch.setColor(1f, 1f, 1f, 1f);

        // Satellites
        for (int i = 0; i < satellites.size; i++) satellites.get(i).render(batch, null);

        // Flying debris — hot debris rendered with orange-red tint
        Array<Debris> flying = debrisManager.getFlying();
        for (int i = 0; i < flying.size; i++) {
            Debris d = flying.get(i);
            if (d.isHot()) batch.setColor(1f, 0.35f, 0.15f, 1f);
            d.render(batch, null);
            if (d.isHot()) batch.setColor(1f, 1f, 1f, 1f);
        }

        // Attached debris (at bowl)
        Array<Debris> attached = debrisManager.getAttached();
        for (int i = 0; i < attached.size; i++) attached.get(i).render(batch, null);

        // Rocket
        rocket.render(batch, null);

        // Bowl texture at rocket nose
        if (bowlTex != null) {
            float rCX  = rocket.getPosX() + rocket.getWidth()  / 2f;
            float rCY  = rocket.getPosY() + rocket.getHeight() / 2f;
            float rot  = rocket.getRotation();
            float noseX = rCX + MathUtils.cosDeg(rot) * (rocket.getHeight() / 2f);
            float noseY = rCY + MathUtils.sinDeg(rot) * (rocket.getHeight() / 2f);
            batch.draw(bowlTex,
                noseX - BOWL_DRAW_W / 2f, noseY - BOWL_DRAW_H / 2f,
                BOWL_DRAW_W / 2f, BOWL_DRAW_H / 2f, BOWL_DRAW_W, BOWL_DRAW_H,
                1f, 1f, rot - 90f,
                0, 0, bowlTex.getWidth(), bowlTex.getHeight(),
                false, false);
        }

        // Explosion flash
        if (explosionTimer > 0f) {
            batch.setColor(1f, 1f, 1f, explosionTimer / EXPLOSION_DURATION);
            batch.draw(explosionTex,
                explosionX - EXPLOSION_SIZE / 2f, explosionY - EXPLOSION_SIZE / 2f,
                EXPLOSION_SIZE, EXPLOSION_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // Atmosphere burn effect
        if (atmosphereBurnTimer > 0f && atmosphereBurnTex != null) {
            float alpha = atmosphereBurnTimer / ATMOSPHERE_BURN_DURATION;
            batch.setColor(1f, 0.55f + 0.45f * alpha, 0.1f * alpha, alpha);
            batch.draw(atmosphereBurnTex,
                atmosphereBurnX - BURN_SIZE / 2f, atmosphereBurnY - BURN_SIZE / 2f,
                BURN_SIZE, BURN_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();

        // Satellite health bars + nav arrow (ShapeRenderer in world space)
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < satellites.size; i++) {
            Satellite sat = satellites.get(i);
            float bw = sat.getWidth();
            float bx = sat.getPosX();
            float by = sat.getPosY() + sat.getHeight() + 6f;
            float hp = sat.getHealthPercentage();
            sr.setColor(0.2f, 0.2f, 0.2f, 0.7f);
            sr.rect(bx, by, bw, 6f);
            if      (hp > 0.5f)  sr.setColor(Color.GREEN);
            else if (hp > 0.25f) sr.setColor(Color.YELLOW);
            else                 sr.setColor(Color.RED);
            sr.rect(bx, by, bw * hp, 6f);
        }
        sr.end();
    }

    // ── Dispose ──────────────────────────────────────────────────────────

    public void dispose() {
        if (grassTopTex       != null) { grassTopTex.dispose();       grassTopTex       = null; }
        if (dirtTex           != null) { dirtTex.dispose();           dirtTex           = null; }
        if (explosionTex      != null) { explosionTex.dispose();      explosionTex      = null; }
        if (atmosphereBurnTex != null) { atmosphereBurnTex.dispose(); atmosphereBurnTex = null; }
        if (bowlTex           != null) { bowlTex.dispose();           bowlTex           = null; }
        if (agencyTextures    != null) {
            for (Texture t : agencyTextures) if (t != null) t.dispose();
            agencyTextures = null;
        }
        if (cloudTextures     != null) {
            for (Texture t : cloudTextures) if (t != null) t.dispose();
            cloudTextures = null;
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private void drawBackground(SpriteBatch batch, float camX, float camY,
                                float halfW, float halfH) {
        float left  = camX - halfW;
        float right = camX + halfW;
        float bot   = camY - halfH;

        // Ground tiles
        int txS = MathUtils.floor(left  / TILE_SIZE);
        int txE = MathUtils.floor(right / TILE_SIZE);
        int tyS = MathUtils.floor(bot   / TILE_SIZE);
        for (int ty = tyS; ty <= -1; ty++) {
            float wy = ty * (float) TILE_SIZE;
            if (wy + TILE_SIZE > 0f) continue;
            Texture tex = (ty == -1) ? grassTopTex : dirtTex;
            for (int tx = txS; tx <= txE; tx++)
                batch.draw(tex, tx * (float) TILE_SIZE, wy, TILE_SIZE, TILE_SIZE);
        }

        // Space agency buildings
        float bldTop = CLOUD_BAND_MAX + 400f;
        if (camY - halfH < bldTop) {
            for (int i = 0; i < AGENCY_INSTANCES; i++) {
                float bx  = agencyData[i * 2];
                int   idx = (int) agencyData[i * 2 + 1];
                float bw  = AGENCY_W[idx];
                float bh  = AGENCY_H[idx];
                float by  = AGENCY_BASE_Y[idx];
                if (bx + bw < left || bx > right || by + bh < bot) continue;
                batch.draw(agencyTextures[idx], bx, by, bw, bh);
            }
        }

        // Clouds
        if (camY + halfH > CLOUD_BAND_MIN && camY - halfH < CLOUD_BAND_MAX + 200f) {
            for (int i = 0; i < cloudPositions.length; i += 3) {
                float cx  = cloudPositions[i], cy = cloudPositions[i + 1];
                int   idx = (int) cloudPositions[i + 2];
                Texture c = cloudTextures[idx];
                if (cx + c.getWidth() < left || cx > right) continue;
                batch.draw(c, cx, cy, c.getWidth(), c.getHeight());
            }
        }
    }

    private void getZoneColor(float alt, Color out) {
        if (alt <= EARTH_ZONE_END) {
            out.set(COLOR_SKY);
        } else if (alt >= SPACE_ZONE_START) {
            out.set(COLOR_SPACE);
        } else {
            float t = (alt - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
            out.r = MathUtils.lerp(COLOR_SKY.r, COLOR_SPACE.r, t);
            out.g = MathUtils.lerp(COLOR_SKY.g, COLOR_SPACE.g, t);
            out.b = MathUtils.lerp(COLOR_SKY.b, COLOR_SPACE.b, t);
            out.a = 1f;
        }
    }

    private boolean isAgencySpawnTooClose(int placedCount, float x, int type) {
        float halfW   = AGENCY_W[type] * 0.5f;
        float centerX = x + halfW;
        for (int j = 0; j < placedCount; j++) {
            float otherX      = agencyData[j * 2];
            int   otherType   = (int) agencyData[j * 2 + 1];
            float otherHalfW  = AGENCY_W[otherType] * 0.5f;
            float otherCenterX = otherX + otherHalfW;
            float minCenterDist = halfW + otherHalfW + AGENCY_MIN_SPACING;
            if (Math.abs(centerX - otherCenterX) < minCenterDist) return true;
        }
        return false;
    }
}
