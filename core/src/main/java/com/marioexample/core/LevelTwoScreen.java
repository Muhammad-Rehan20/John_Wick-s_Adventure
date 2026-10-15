package com.marioexample.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Simple Level Two screen.
 *
 * - Background: iceB.jpg
 * - Simple ground with a few thorns
 * - Every thorn has a coin above it
 * - Exactly 4 enemies
 * - Portal at the end
 * - Player can ONLY shoot ice bullets in this level (freeze enemies, don't kill them)
 * - When the player enters the portal, we show "Level 2 Completed"
 *   and softly transition to {@link GameplayScreen} (final boss level).
 */
public class LevelTwoScreen implements Screen {

    // World config – match final level aspect/zoom
    private static final float WORLD_WIDTH = 24f;
    private static final float WORLD_HEIGHT = 13.5f;
    // Total horizontal length of this level (can be wider than the camera width)
    private static final float LEVEL_LENGTH = 50f;

    // Player config (kept same as GameplayScreen so physics feel identical)
    private static final float PLAYER_HEIGHT = 2.5f;
    private static final float PLAYER_WIDTH = PLAYER_HEIGHT * 904f / 1277f;
    private static final float PLAYER_SPEED = 10f;
    private static final float PLAYER_ACCELERATION = 60f;
    private static final float PLAYER_DECELERATION = 80f;
    private static final float GRAVITY = -40f;
    private static final float JUMP_VELOCITY = 16f;

    // Bullet config (only ice bullets here)
    private static final int MAX_BULLETS = 6;
    private static final float BULLET_SPEED = 20f;
    private static final float BULLET_COOLDOWN = 0.25f;

    // Enemy config
    private static final int ENEMY_COUNT = 3;
    // Slightly larger enemies in Level 2 (keep square ratio so sprite is not stretched)
    private static final float ENEMY_WIDTH = 2.5f;
    private static final float ENEMY_HEIGHT = 2.5f;

    // HUD / Exit button (match final level style)
    private static final float EXIT_BTN_WIDTH = 80f;
    private static final float EXIT_BTN_HEIGHT = 30f;

    // Core references
    private final MainGame game;
    private final SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera hudCamera;

    // Rendering helpers
    private BitmapFont font;
    private GlyphLayout layout;

    // Textures
    private Texture backgroundTexture;
    private Texture groundTexture;
    private Texture standingTexture;
    private Texture jumpTexture;
    private Texture walk1Texture;
    private Texture walk2Texture;
    private Texture walk3Texture;
    private Texture enemyTexture;
    private Texture frozenEnemyTexture;
    private Texture iceBulletTexture;
    private Texture coinTexture;
    private Texture thornTexture;
    private Texture portalTexture;

    // Sounds (similar to final level but simplified for this intro level)
    private Sound jumpSound;
    private Sound iceBulletSound;
    private Sound coinSound;
    private Sound enemyDeathSound;
    private Sound levelCompleteSound;
    private Music backgroundMusic;

    // Player state
    private Rectangle playerRect;
    private float playerVX;
    private float playerVY;
    private boolean onGround;
    private boolean facingRight = true;
    private float walkAnimTime = 0f;

    // Simple animation state
    private enum PlayerAnimState { IDLE, WALKING, JUMPING }
    private PlayerAnimState playerAnimState = PlayerAnimState.IDLE;
    private static final float WALK_ANIM_SPEED = 8f;

    // Level geometry
    private Array<Rectangle> platforms = new Array<Rectangle>();
    private Array<Rectangle> hazards = new Array<Rectangle>();
    private Rectangle goalPortal;
    private float levelWidth;
    
    // Trenches (gaps in ground that kill player if fallen into)
    private Array<Rectangle> trenches = new Array<Rectangle>();

    // Enemies
    private float[] enemyX = new float[ENEMY_COUNT];
    private float[] enemyY = new float[ENEMY_COUNT];
    private float[] enemyVX = new float[ENEMY_COUNT];
    private boolean[] enemyAlive = new boolean[ENEMY_COUNT];
    private float[] enemyLeftPatrol = new float[ENEMY_COUNT];
    private float[] enemyRightPatrol = new float[ENEMY_COUNT];
    private boolean[] enemyFrozen = new boolean[ENEMY_COUNT];  // Whether enemy is frozen by ice bullet
    private float[] enemyFrozenTimer = new float[ENEMY_COUNT];  // Timer for frozen enemy fade-out

    // Coins (one above each thorn)
    private float[] coinX;
    private float[] coinY;
    private boolean[] coinActive;
    private int coinsCollected = 0;

    // Player bullets (ice only)
    private float[] bulletX = new float[MAX_BULLETS];
    private float[] bulletY = new float[MAX_BULLETS];
    private float[] bulletVX = new float[MAX_BULLETS];
    private boolean[] bulletActive = new boolean[MAX_BULLETS];
    private float bulletCooldownTimer = 0f;

    // Game state
    private int lives = 3;
    private int score = 0;
    private boolean levelComplete = false;
    private boolean playerDead = false;
    private float levelCompleteTimer = 0f;
    private float playerAlpha = 1f;

    public LevelTwoScreen(MainGame game) {
        this.game = game;
        this.batch = game.batch;

        worldCamera = new OrthographicCamera();
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        worldCamera.zoom = 1.1f; // same zoom as final level
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f; // same vertical offset as final level

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font = new BitmapFont();
        layout = new GlyphLayout();

        loadTextures();
        loadSounds();
        initLevelLayout();
        resetPlayer();
    }

    private void loadTextures() {
        backgroundTexture = new Texture(Gdx.files.internal("iceB.jpg"));
        groundTexture = new Texture(Gdx.files.internal("ground.png"));
        standingTexture = new Texture(Gdx.files.internal("standing.png"));
        jumpTexture = new Texture(Gdx.files.internal("jump.png"));
        walk1Texture = new Texture(Gdx.files.internal("walk1.png"));
        walk2Texture = new Texture(Gdx.files.internal("walk2.png"));
        walk3Texture = new Texture(Gdx.files.internal("walk3.png"));
        // Level 2 uses the same enemy sprite as final level, with freezing mechanism
        enemyTexture = new Texture(Gdx.files.internal("enemy.png"));
        frozenEnemyTexture = new Texture(Gdx.files.internal("frozenenemy.png"));
        iceBulletTexture = new Texture(Gdx.files.internal("icebullet.png"));
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        thornTexture = new Texture(Gdx.files.internal("thorn.png"));
        portalTexture = new Texture(Gdx.files.internal("door.png"));
    }

    private void loadSounds() {
        try {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "jump.wav not found for Level 2");
        }
        try {
            iceBulletSound = Gdx.audio.newSound(Gdx.files.internal("icebullet.mp3"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "icebullet.mp3 not found for Level 2");
        }
        try {
            coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "coin.wav not found for Level 2");
        }
        try {
            enemyDeathSound = Gdx.audio.newSound(Gdx.files.internal("enemy_death.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "enemy_death.wav not found for Level 2");
        }

        try {
            levelCompleteSound = Gdx.audio.newSound(Gdx.files.internal("level_complete.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "level_complete.wav not found for Level 2");
        }
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.25f);
                // Stop any existing music before playing to prevent duplicates
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                backgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.log("Sound", "background_music.mp3 not found for Level 2");
        }
    }

    private void initLevelLayout() {
        // Ground platforms - segmented to create 3 trenches
        float groundHeight = 1.2f;
        float groundY = 0f;
        
        // Segment 1: Start to first trench
        platforms.add(new Rectangle(0f, groundY, 8f, groundHeight));
        
        // Trench 1: Gap from 8f to 12f (width 4f - jumpable)
        float trench1X = 8f;
        float trench1Width = 4f;
        trenches.add(new Rectangle(trench1X, -10f, trench1Width, 15f)); // Deep enough to kill if fallen
        
        // Segment 2: After trench 1
        platforms.add(new Rectangle(12f, groundY, 8f, groundHeight));
        
        // Trench 2: Gap from 20f to 24f (width 4f - jumpable)
        float trench2X = 20f;
        float trench2Width = 4f;
        trenches.add(new Rectangle(trench2X, -10f, trench2Width, 15f));
        
        // Segment 3: After trench 2
        platforms.add(new Rectangle(24f, groundY, 8f, groundHeight));
        
        // Trench 3: Gap from 32f to 36f (width 4f - jumpable)
        float trench3X = 32f;
        float trench3Width = 4f;
        trenches.add(new Rectangle(trench3X, -10f, trench3Width, 15f));
        
        // Segment 4: After trench 3 to portal
        platforms.add(new Rectangle(36f, groundY, LEVEL_LENGTH - 36f, groundHeight));

        // Coins above trenches (no thorns near trenches)
        // Coins above trenches (higher up for better visibility and challenge)
        float coinAboveTrenchY = groundY + groundHeight + 2.5f;
        float[] trenchCoinXs = new float[] {
            trench1X + trench1Width / 2f,  // Coin above trench 1
            trench2X + trench2Width / 2f,  // Coin above trench 2
            trench3X + trench3Width / 2f   // Coin above trench 3
        };
        
        // Thorns placed away from trenches (before first trench and after last trench)
        float hazardSize = 1.1f;
        float hazardY = groundY + groundHeight;
        
        // Thorn positions (avoiding trenches at 8-12, 20-24, 32-36)
        float[] thornXs = new float[] { 
            4f,   // Before first trench
            16f,  // Between trench 1 and 2
            28f,  // Between trench 2 and 3
            41f   // After third trench
        };
        
        int thornCount = thornXs.length;
        int totalCoins = thornCount + trenchCoinXs.length; // Thorns coins + trench coins
        coinX = new float[totalCoins];
        coinY = new float[totalCoins];
        coinActive = new boolean[totalCoins];
        
        int coinIndex = 0;
        
        // Add coins above thorns
        for (int i = 0; i < thornCount; i++) {
            float x = thornXs[i];
            Rectangle h = new Rectangle(x, hazardY, hazardSize, hazardSize);
            hazards.add(h);
            // Coin directly above each thorn
            coinX[coinIndex] = x + hazardSize / 2f;
            coinY[coinIndex] = hazardY + hazardSize + 1.2f;
            coinActive[coinIndex] = true;
            coinIndex++;
        }
        
        // Add coins above trenches
        for (int i = 0; i < trenchCoinXs.length; i++) {
            coinX[coinIndex] = trenchCoinXs[i];
            coinY[coinIndex] = coinAboveTrenchY;
            coinActive[coinIndex] = true;
            coinIndex++;
        }

        // Portal at the far right
        float portalWidth = 3.0f;
        float portalX = LEVEL_LENGTH - portalWidth - 1f;
        goalPortal = new Rectangle(portalX, 0f, portalWidth, 1f);
        if (portalTexture != null) {
            float aspect = (float) portalTexture.getHeight() / (float) portalTexture.getWidth();
            goalPortal.height = goalPortal.width * aspect;
        } else {
            goalPortal.height = 5f;
        }

        levelWidth = LEVEL_LENGTH;

        // Enemies: 3 simple ground patrols (positioned on ground segments, avoiding trenches)
        setEnemy(0, 13.5f, groundY + groundHeight, 12f, 19f, 1.5f); // On segment 2, between trenches 1-2
        setEnemy(1, 25.5f, groundY + groundHeight, 24f, 31f, 1.7f); // On segment 3, between trenches 2-3
        // Final enemy patrols well before the portal to give a visible gap
        setEnemy(2, 38f, groundY + groundHeight, 36f, 43f, 1.6f);   // On segment 4, after trench 3
        
        // Initialize frozen state arrays
        for (int i = 0; i < ENEMY_COUNT; i++) {
            enemyFrozen[i] = false;
            enemyFrozenTimer[i] = 0f;
        }
    }

    private void setEnemy(int index, float x, float y, float left, float right, float speed) {
        enemyX[index] = x;
        enemyY[index] = y;
        enemyVX[index] = speed;
        enemyAlive[index] = true;
        enemyLeftPatrol[index] = left;
        enemyRightPatrol[index] = right;
        enemyFrozen[index] = false;
        enemyFrozenTimer[index] = 0f;
    }

    private void resetPlayer() {
        playerRect = new Rectangle(2f, 1.2f, PLAYER_WIDTH, PLAYER_HEIGHT);
        playerVX = 0f;
        playerVY = 0f;
        onGround = false;
        facingRight = true;
        levelComplete = false;
        playerDead = false;
        levelCompleteTimer = 0f;
        playerAlpha = 1f;

        // Reset bullets
        for (int i = 0; i < MAX_BULLETS; i++) {
            bulletActive[i] = false;
        }
        bulletCooldownTimer = 0f;
        
        // Reset enemies to initial state (unfreeze them and reset positions)
        float groundLevel = 1.2f;
        setEnemy(0, 13.5f, groundLevel, 12f, 19f, 1.5f);
        setEnemy(1, 25.5f, groundLevel, 24f, 31f, 1.7f);
        setEnemy(2, 38f, groundLevel, 36f, 43f, 1.6f);
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background stretched to screen
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        hudCamera.setToOrtho(false, screenW, screenH);
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, screenW, screenH);
        batch.end();

        // Draw world
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        // Platforms
        for (Rectangle p : platforms) {
            float tileSize = 1f;
            int tiles = MathUtils.ceil(p.width / tileSize);
            for (int i = 0; i < tiles; i++) {
                float gx = p.x + i * tileSize;
                batch.draw(groundTexture, gx, p.y, tileSize, p.height);
            }
        }

        // Portal
        batch.draw(portalTexture, goalPortal.x, goalPortal.y, goalPortal.width, goalPortal.height);

        // Hazards (thorns)
        for (Rectangle h : hazards) {
            batch.draw(thornTexture, h.x, h.y, h.width, h.height);
        }

        // Coins
        for (int i = 0; i < coinX.length; i++) {
            if (!coinActive[i]) continue;
            float size = 1.0f;
            batch.draw(coinTexture, coinX[i] - size / 2f, coinY[i] - size / 2f, size, size);
        }

        // Enemies (texture, flip horizontally based on movement direction, handle frozen state)
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i]) continue;

            // Choose texture and alpha based on frozen state
            Texture tex = enemyFrozen[i] ? frozenEnemyTexture : enemyTexture;
            float alpha = 1f;
            if (enemyFrozen[i]) {
                // Fade out over 5 seconds
                float fadeProgress = Math.min(enemyFrozenTimer[i] / 5f, 1f);
                alpha = 1f - fadeProgress;
            }
            batch.setColor(1f, 1f, 1f, alpha);

            if (enemyVX[i] > 0 && !enemyFrozen[i]) {
                // Moving right: flip horizontally (original faces left)
                batch.draw(tex, enemyX[i] + ENEMY_WIDTH, enemyY[i], -ENEMY_WIDTH, ENEMY_HEIGHT);
            } else {
                // Moving left, idle, or frozen: normal orientation
                batch.draw(tex, enemyX[i], enemyY[i], ENEMY_WIDTH, ENEMY_HEIGHT);
            }
            batch.setColor(Color.WHITE);
        }

        // Bullets (ice only)
        float bw = 1.2f;
        float bh = 0.4f;
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            batch.draw(iceBulletTexture, bulletX[i], bulletY[i] - bh / 2f, bw, bh);
        }

        // Player
        batch.setColor(1f, 1f, 1f, playerAlpha);
        Texture currentTex = standingTexture;
        if (playerAnimState == PlayerAnimState.JUMPING) {
            currentTex = jumpTexture;
        } else if (playerAnimState == PlayerAnimState.WALKING) {
            int frameIndex = (int)(walkAnimTime * WALK_ANIM_SPEED) % 3;
            currentTex = (frameIndex == 0) ? walk1Texture : (frameIndex == 1 ? walk2Texture : walk3Texture);
        }
        if (facingRight) {
            batch.draw(currentTex, playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        } else {
            batch.draw(currentTex, playerRect.x + playerRect.width, playerRect.y, -playerRect.width, playerRect.height);
        }
        batch.setColor(Color.WHITE);

        batch.end();

        // HUD / texts
        renderHud();
    }

    private void handleInput(float delta) {
        // Handle mouse/touch for exit button (screen coordinates)
        if (Gdx.input.justTouched()) {
            float sx = Gdx.input.getX();
            float sy = Gdx.graphics.getHeight() - Gdx.input.getY(); // invert Y
            float btnX = Gdx.graphics.getWidth() - EXIT_BTN_WIDTH - 10f;
            float btnY = Gdx.graphics.getHeight() - EXIT_BTN_HEIGHT - 10f;
            if (sx >= btnX && sx <= btnX + EXIT_BTN_WIDTH && sy >= btnY && sy <= btnY + EXIT_BTN_HEIGHT) {
                Gdx.app.exit();
                return;
            }
        }

        // Allow restart with R when dead or after finishing the level
        if (playerDead || levelComplete) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                // Stop music before recreating screen to prevent duplicates
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                // Simple restart: recreate Level 2 screen
                game.setScreen(new LevelTwoScreen(game));
            }
            return;
        }

        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (left) {
            facingRight = false;
            playerVX -= PLAYER_ACCELERATION * delta;
            if (playerVX < -PLAYER_SPEED) playerVX = -PLAYER_SPEED;
        } else if (right) {
            facingRight = true;
            playerVX += PLAYER_ACCELERATION * delta;
            if (playerVX > PLAYER_SPEED) playerVX = PLAYER_SPEED;
        } else {
            if (playerVX > 0) {
                playerVX -= PLAYER_DECELERATION * delta;
                if (playerVX < 0) playerVX = 0;
            } else if (playerVX < 0) {
                playerVX += PLAYER_DECELERATION * delta;
                if (playerVX > 0) playerVX = 0;
            }
        }

        // Animate
        if (!onGround || playerVY > 0) {
            playerAnimState = PlayerAnimState.JUMPING;
        } else if (Math.abs(playerVX) > 0.1f) {
            playerAnimState = PlayerAnimState.WALKING;
            walkAnimTime += delta;
        } else {
            playerAnimState = PlayerAnimState.IDLE;
            walkAnimTime = 0f;
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (onGround) {
                playerVY = JUMP_VELOCITY;
                onGround = false;
                if (jumpSound != null) {
                    jumpSound.play(0.6f);
                }
            }
        }

        // Shooting (K) – ONLY ice bullets in this level
        bulletCooldownTimer -= delta;
        if (bulletCooldownTimer < 0) bulletCooldownTimer = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.K) && bulletCooldownTimer <= 0) {
            shootIceBullet();
            bulletCooldownTimer = BULLET_COOLDOWN;
        }
    }

    private void shootIceBullet() {
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) {
                bulletActive[i] = true;
                // Bullet spawns from gun nozzle: near front edge (95% of width), at gun height (60% of height)
                float muzzleOffsetX = playerRect.width * 0.95f;
                float muzzleOffsetY = playerRect.height * 0.6f;
                if (facingRight) {
                    bulletX[i] = playerRect.x + muzzleOffsetX;
                    bulletVX[i] = BULLET_SPEED;
                } else {
                    // When facing left, nozzle is at 5% from left edge (same as final level)
                    bulletX[i] = playerRect.x + playerRect.width * (1f - 0.95f);
                    bulletVX[i] = -BULLET_SPEED;
                }
                bulletY[i] = playerRect.y + muzzleOffsetY;
                if (iceBulletSound != null) {
                    iceBulletSound.play(0.7f);
                }
                break;
            }
        }
    }

    private void update(float delta) {
        if (levelComplete) {
            levelCompleteTimer += delta;
            float fadeDuration = 1.3f;
            playerAlpha = Math.max(0f, 1f - levelCompleteTimer / fadeDuration);

            if (levelCompleteTimer > 2.0f) {
                // Soft transition to final level - ensure music is stopped
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                game.setScreen(new GameplayScreen(game));
            }
            return;
        }

        if (playerDead) {
            return;
        }

        // Physics
        playerVY += GRAVITY * delta;
        float newX = playerRect.x + playerVX * delta;
        float newY = playerRect.y + playerVY * delta;

        // Horizontal collisions
        Rectangle newRectX = new Rectangle(newX, playerRect.y, playerRect.width, playerRect.height);
        for (Rectangle p : platforms) {
            if (newRectX.overlaps(p)) {
                if (playerVX > 0) {
                    newX = p.x - playerRect.width;
                } else if (playerVX < 0) {
                    newX = p.x + p.width;
                }
                playerVX = 0;
                newRectX.x = newX;
            }
        }

        // Vertical collisions
        Rectangle newRectY = new Rectangle(newX, newY, playerRect.width, playerRect.height);
        onGround = false;
        for (Rectangle p : platforms) {
            if (newRectY.overlaps(p)) {
                if (playerVY < 0) {
                    newY = p.y + p.height;
                    onGround = true;
                } else if (playerVY > 0) {
                    newY = p.y - playerRect.height;
                }
                playerVY = 0;
                newRectY.y = newY;
            }
        }

        playerRect.x = newX;
        playerRect.y = newY;

        // Clamp to world
        if (playerRect.x < 0) playerRect.x = 0;
        if (playerRect.x + playerRect.width > levelWidth)
            playerRect.x = levelWidth - playerRect.width;

        // Check if player falls off world or into a trench
        if (playerRect.y < -5) {
            killPlayer();
        } else {
            // Check if player is in a trench and below ground level
            float groundLevel = 1.2f; // Ground platform height
            for (Rectangle trench : trenches) {
                // Check if player's X position overlaps with trench
                if (playerRect.x + playerRect.width > trench.x && 
                    playerRect.x < trench.x + trench.width) {
                    // Player is in trench area, check if below ground
                    if (playerRect.y < groundLevel) {
                        killPlayer();
                        break;
                    }
                }
            }
        }

        // Enemies patrol
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i]) continue;

            if (enemyFrozen[i]) {
                // While frozen, don't move or interact, just count down fade timer
                enemyFrozenTimer[i] += delta;
                if (enemyFrozenTimer[i] >= 5f) {
                    // After 5 seconds, remove enemy completely
                    enemyAlive[i] = false;
                    enemyFrozen[i] = false;
                }
                continue;
            }

            enemyX[i] += enemyVX[i] * delta;
            if (enemyX[i] < enemyLeftPatrol[i]) {
                enemyX[i] = enemyLeftPatrol[i];
                enemyVX[i] = Math.abs(enemyVX[i]);
            } else if (enemyX[i] + ENEMY_WIDTH > enemyRightPatrol[i]) {
                enemyX[i] = enemyRightPatrol[i] - ENEMY_WIDTH;
                enemyVX[i] = -Math.abs(enemyVX[i]);
            }
        }

        // Bullets
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            bulletX[i] += bulletVX[i] * delta;
            if (bulletX[i] < -5 || bulletX[i] > levelWidth + 5) {
                bulletActive[i] = false;
            }
        }

        // Bullet-enemy collisions
        Rectangle bulletRect = new Rectangle();
        Rectangle enemyRect = new Rectangle();
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            bulletRect.set(bulletX[i], bulletY[i] - 0.2f, 1.2f, 0.4f);
            for (int j = 0; j < ENEMY_COUNT; j++) {
                if (!enemyAlive[j]) continue;
                enemyRect.set(enemyX[j], enemyY[j], ENEMY_WIDTH, ENEMY_HEIGHT);
                if (bulletRect.overlaps(enemyRect)) {
                    bulletActive[i] = false;
                    // Ice bullet: freeze enemy instead of killing
                    enemyFrozen[j] = true;
                    enemyFrozenTimer[j] = 0f;
                    enemyVX[j] = 0f;
                    break;
                }
            }
        }

        // Player-enemy collisions
        Rectangle playerHit = new Rectangle(playerRect);
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i] || enemyFrozen[i]) continue;  // Skip frozen enemies
            enemyRect.set(enemyX[i], enemyY[i], ENEMY_WIDTH, ENEMY_HEIGHT);
            if (playerHit.overlaps(enemyRect)) {
                killPlayer();
                break;
            }
        }

        // Player-thorn collisions
        Rectangle thornRect = new Rectangle();
        for (Rectangle h : hazards) {
            thornRect.set(h);
            if (playerHit.overlaps(thornRect)) {
                killPlayer();
                break;
            }
        }

        // Coins
        for (int i = 0; i < coinX.length; i++) {
            if (!coinActive[i]) continue;
            Rectangle coinRect = new Rectangle(coinX[i] - 0.5f, coinY[i] - 0.5f, 1f, 1f);
            if (playerHit.overlaps(coinRect)) {
                coinActive[i] = false;
                coinsCollected++;
                score += 10;
                if (coinSound != null) {
                    coinSound.play(0.7f);
                }
            }
        }

        // Portal / level complete (no boss requirement here)
        if (playerHit.overlaps(goalPortal)) {
            if (!levelComplete) {
                levelComplete = true;
                if (levelCompleteSound != null) {
                    levelCompleteSound.play(0.8f);
                }
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
            }
        }

        // Camera follow – same style as final level
        float halfViewportWidth = worldCamera.viewportWidth / 2f;
        float camX = playerRect.x + playerRect.width / 2f;
        camX = MathUtils.clamp(camX, halfViewportWidth, levelWidth - halfViewportWidth);
        worldCamera.position.x = camX;
        // Keep vertical offset so ground sits a bit lower against the background
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f;
    }

    private void killPlayer() {
        lives--;
        if (lives > 0) {
            resetPlayer();
        } else {
            playerDead = true;
        }
    }

    private void renderHud() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        String title = "Level 2 – Ice Run";
        String controls = "Move: LEFT/RIGHT or A/D   Jump: SPACE/W/UP   Shoot Ice: K";
        String stats = "Score: " + score + "   Coins: " + coinsCollected + "   Lives: " + lives;

        float x = 20f;
        float y = Gdx.graphics.getHeight() - 20f;
        drawShadowedText(title, x, y, Color.WHITE);
        drawShadowedText(controls, x, y - 25f, Color.LIGHT_GRAY);
        drawShadowedText(stats, x, y - 50f, Color.GOLD);

        // Exit button (top-right) – same visual style as final level
        float btnX = Gdx.graphics.getWidth() - EXIT_BTN_WIDTH - 10f;
        float btnY = Gdx.graphics.getHeight() - 10f;
        String exitLabel = "Exit";
        drawShadowedText(exitLabel, btnX + 15f, btnY - 5f, Color.RED);

        if (levelComplete) {
            String msg = "Level 2 Completed!";
            layout.setText(font, msg);
            float mx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float my = Gdx.graphics.getHeight() / 2f;
            drawShadowedText(msg, mx, my, Color.GREEN);

            String next = "Get ready for the Final Boss...";
            layout.setText(font, next);
            float nx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            drawShadowedText(next, nx, my - 30f, Color.WHITE);
        } else if (playerDead) {
            String msg = "You Died! Restart the game to try again.";
            layout.setText(font, msg);
            float mx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float my = Gdx.graphics.getHeight() / 2f;
            drawShadowedText(msg, mx, my, Color.RED);
        }

        batch.end();
    }

    private void drawShadowedText(String text, float x, float y, Color color) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    @Override
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
    }

    @Override
    public void show() {
        // Ensure background music is playing when screen is shown
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    @Override
    public void hide() {
        // Stop background music when screen is hidden to prevent overlap
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (groundTexture != null) groundTexture.dispose();
        if (standingTexture != null) standingTexture.dispose();
        if (jumpTexture != null) jumpTexture.dispose();
        if (walk1Texture != null) walk1Texture.dispose();
        if (walk2Texture != null) walk2Texture.dispose();
        if (walk3Texture != null) walk3Texture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (iceBulletTexture != null) iceBulletTexture.dispose();
        if (frozenEnemyTexture != null) frozenEnemyTexture.dispose();
        if (coinTexture != null) coinTexture.dispose();
        if (thornTexture != null) thornTexture.dispose();
        if (portalTexture != null) portalTexture.dispose();
        if (font != null) font.dispose();
        if (jumpSound != null) jumpSound.dispose();
        if (iceBulletSound != null) iceBulletSound.dispose();
        if (coinSound != null) coinSound.dispose();
        if (enemyDeathSound != null) enemyDeathSound.dispose();

        if (levelCompleteSound != null) levelCompleteSound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}


