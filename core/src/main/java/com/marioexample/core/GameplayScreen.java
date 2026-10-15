package com.marioexample.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;

/**
 * ============================================================================
 * GAMEPLAY SCREEN - The Final Boss Level (Level 3)
 * ============================================================================
 * 
 * WHAT IS THIS CLASS?
 * -------------------
 * This is the most complex level in the game - the final boss fight! It's called
 * "GameplayScreen" but think of it as "FinalBossScreen" or "Level 3".
 * 
 * This file is LONG and COMPLEX because it has many features:
 * - Multiple platforms (floating tiles)
 * - Regular enemies that can be frozen
 * - A BOSS enemy with special behavior
 * - Two types of bullets (fire and ice)
 * - Complex collision detection
 * - Boss health system
 * - Music that changes during boss fight
 * 
 * DON'T PANIC! This guide will help you understand it step by step.
 * 
 * ============================================================================
 * OOP CONCEPTS FOR BEGINNERS
 * ============================================================================
 * 
 * 1. CLASS:
 *    - This is a blueprint that defines everything about Level 3
 *    - Think of it like a recipe for making a cake
 * 
 * 2. IMPLEMENTS SCREEN:
 *    - This class "implements" (follows) the Screen interface
 *    - It's like signing a contract: "I promise to have these methods"
 *    - Required methods: render(), show(), hide(), dispose(), etc.
 * 
 * 3. ENCAPSULATION:
 *    - All data (variables) and behavior (methods) are in this one class
 *    - Private variables can only be accessed from inside this class
 *    - This protects data from being changed accidentally
 * 
 * 4. STATIC FINAL CONSTANTS:
 *    - Values that NEVER change (like gravity = -40)
 *    - Written in UPPERCASE: GRAVITY, PLAYER_SPEED, etc.
 *    - Easy to find and adjust game settings
 * 
 * 5. METHODS:
 *    - Functions that do specific tasks
 *    - Break complex code into smaller, understandable pieces
 *    - Examples: handleInput(), update(), render()
 * 
 * ============================================================================
 * HOW THE GAME LOOP WORKS (THE BIG PICTURE)
 * ============================================================================
 * 
 * Every frame (60 times per second), LibGDX calls the render() method:
 * 
 * 1. handleInput(delta)  →  "What did the player do?"
 *    - Check keyboard/mouse
 *    - Move player left/right
 *    - Jump
 *    - Shoot bullets
 * 
 * 2. update(delta)  →  "What should happen?"
 *    - Apply gravity (pull player down)
 *    - Update player position
 *    - Check collisions (player vs platforms, enemies, hazards)
 *    - Move enemies
 *    - Move bullets
 *    - Check if bullets hit enemies/boss
 *    - Update boss behavior
 *    - Check coin collection
 *    - Update camera (follow player)
 * 
 * 3. render()  →  "Draw everything on screen"
 *    - Clear screen
 *    - Draw background
 *    - Draw platforms
 *    - Draw enemies
 *    - Draw bullets
 *    - Draw player
 *    - Draw boss
 *    - Draw HUD (score, health, etc.)
 * 
 * This happens 60 times per second, creating smooth animation!
 * 
 * ============================================================================
 * FILE STRUCTURE (WHERE TO FIND THINGS)
 * ============================================================================
 * 
 * SECTION 1: CONSTANTS (~Lines 57-90)
 *    - Game settings that never change
 *    - Player physics values (speed, gravity, jump height)
 *    - World dimensions
 *    - Boss arena boundaries
 *    - Bullet settings
 * 
 * SECTION 2: CORE OBJECTS (~Lines 92-122)
 *    - References to game engine tools
 *    - Camera (what part of world we see)
 *    - SpriteBatch (for drawing images)
 *    - ShapeRenderer (for drawing shapes like health bars)
 * 
 * SECTION 3: PLAYER DATA (~Lines 124-148)
 *    - Player position (Rectangle)
 *    - Player textures (standing, jumping, walking)
 *    - Player velocity (speed and direction)
 *    - Player state (on ground? facing right? etc.)
 * 
 * SECTION 4: BULLET SYSTEM (~Lines 150-175)
 *    - Arrays to store multiple bullets
 *    - Bullet positions, velocities, types
 *    - Two types: FIRE (damage) and ICE (freeze)
 * 
 * SECTION 5: ENEMY SYSTEM (~Lines 177-201)
 *    - Enemy positions and patrol zones
 *    - Freezing system (enemies can be frozen by ice bullets)
 * 
 * SECTION 6: BOSS SYSTEM (~Lines 203-227)
 *    - Boss position and health
 *    - Boss bullets (boss shoots at player!)
 *    - Boss activation (fight starts when player enters arena)
 * 
 * SECTION 7: GAME STATE (~Lines 229-260)
 *    - Score, lives, level complete flag
 *    - Coins, platforms, hazards
 *    - Sounds and music
 * 
 * SECTION 8: CONSTRUCTOR (~Lines 339-530)
 *    - Runs when level is created
 *    - Loads all images and sounds
 *    - Sets up the level layout
 *    - Initializes player, enemies, boss
 * 
 * SECTION 9: MAIN RENDER METHOD (~Lines 732-1040)
 *    - Draws everything on screen
 *    - Called 60 times per second
 * 
 * SECTION 10: INPUT HANDLING (~Lines 1042-1170)
 *    - Checks keyboard/mouse input
 *    - Moves player, handles jumping, shooting
 * 
 * SECTION 11: UPDATE METHOD (~Lines 1096-1356)
 *    - Updates all game logic
 *    - Physics, collisions, AI, etc.
 *    - THIS IS THE HEART OF THE GAME!
 * 
 * SECTION 12: HELPER METHODS (~Lines 1358+)
 *    - Smaller methods that do specific tasks
 *    - updateBoss(), handleCollisions(), etc.
 * 
 * ============================================================================
 * KEY CONCEPTS EXPLAINED SIMPLY
 * ============================================================================
 * 
 * 1. RECTANGLES:
 *    - Used to represent positions and sizes of game objects
 *    - playerRect stores: x, y, width, height
 *    - Makes collision detection easy (check if rectangles overlap)
 * 
 * 2. ARRAYS:
 *    - Store multiple values of the same type
 *    - enemyX[] = array of enemy X positions
 *    - bulletActive[] = array of true/false (is bullet on screen?)
 * 
 * 3. COLLISION DETECTION:
 *    - Check if two rectangles overlap
 *    - Example: if (playerRect.overlaps(enemyRect)) { player dies }
 * 
 * 4. DELTA TIME:
 *    - Time since last frame (usually ~0.016 seconds)
 *    - Used to make movement smooth regardless of framerate
 *    - Example: position += speed * delta (move based on time, not frames)
 * 
 * 5. BOOLEAN FLAGS:
 *    - True/false values that track game state
 *    - onGround = true when player is on platform
 *    - bossAlive = false when boss is defeated
 * 
 * 6. TIMERS:
 *    - Count up using delta time
 *    - Used for delays, cooldowns, animations
 *    - Example: bulletCooldownTimer -= delta (countdown to next shot)
 * 
 * ============================================================================
 * BOSS FIGHT MECHANICS (THE COMPLEX PART!)
 * ============================================================================
 * 
 * The boss fight has several systems working together:
 * 
 * 1. BOSS ACTIVATION:
 *    - Boss only "wakes up" when player enters the arena (X position > 46)
 *    - Music switches from background music to boss fight music
 * 
 * 2. BOSS MOVEMENT:
 *    - Boss patrols left and right within the arena
 *    - Moves at constant speed, bounces off walls
 * 
 * 3. BOSS SHOOTING:
 *    - Boss shoots fire bullets at random intervals
 *    - Timer counts up, when it reaches threshold, boss shoots
 *    - Bullets move toward player's position
 * 
 * 4. BOSS HEALTH:
 *    - Starts with 25 health points
 *    - Fire bullets deal 2 damage
 *    - Ice bullets deal 1 damage
 *    - When health reaches 0, boss dies and explodes
 * 
 * 5. BOSS BULLETS:
 *    - Boss can have multiple bullets on screen at once
 *    - Each bullet is tracked in an array
 *    - Bullets are removed when they hit player or go off screen
 * 
 * ============================================================================
 * HOW TO READ THIS CODE (LEARNING STRATEGY)
 * ============================================================================
 * 
 * STEP 1: Read the header (you're doing this now!)
 *    - Understand what the file does
 *    - Learn the structure
 * 
 * STEP 2: Look at constants
 *    - These are easy to understand
 *    - They're just numbers that control game behavior
 * 
 * STEP 3: Find the constructor
 *    - See how everything is set up initially
 *    - Understand what gets loaded and initialized
 * 
 * STEP 4: Study the render() method
 *    - See how things are drawn
 *    - Understand the drawing order
 * 
 * STEP 5: Study handleInput()
 *    - See how player controls work
 *    - Understand input checking
 * 
 * STEP 6: Study update() method CAREFULLY
 *    - This is the most complex part
 *    - Read it step by step
 *    - Each section does one thing
 * 
 * STEP 7: Study helper methods one at a time
 *    - Each method does one specific task
 *    - Read the comments in each method
 * 
 * TIPS:
 * - Don't try to understand everything at once!
 * - Focus on one feature at a time
 * - Ask yourself: "What does this part do? Why is it needed?"
 * - Experiment: Change values and see what happens
 * 
 * ============================================================================
 * COMMON QUESTIONS BEGINNERS ASK
 * ============================================================================
 * 
 * Q: Why is this file so long?
 * A: Because it does MANY things: player, enemies, boss, bullets, collisions,
 *    sounds, etc. In professional games, this might be split into multiple
 *    files, but for learning, having it all in one place helps you see how
 *    everything connects.
 * 
 * Q: What's the difference between fire and ice bullets?
 * A: Fire bullets kill enemies instantly. Ice bullets freeze enemies (they
 *    stop moving and fade out over 5 seconds).
 * 
 * Q: How does collision detection work?
 * A: We use Rectangle objects. Each game object has a Rectangle with x, y,
 *    width, height. We check if rectangles overlap using .overlaps() method.
 * 
 * Q: Why do we use delta time?
 * A: So the game runs at the same speed on fast computers and slow computers.
 *    Movement is based on TIME, not frames per second.
 * 
 * Q: What does "static final" mean?
 * A: "static" = shared by all instances. "final" = cannot be changed.
 *    Together = a constant value that never changes.
 * 
 * ============================================================================
 * 
 * @author Game Developer
 * @version 1.0
 */
public class GameplayScreen implements Screen {

    // ========================================================================
    // SECTION 1: GAME CONSTANTS (Configuration Values)
    // ========================================================================
    // These are "static final" constants - they never change and are shared
    // by all instances of this class. Using constants makes it easy to adjust
    // game balance without searching through the code.
    
    /** World dimensions in game units (not pixels) */
    private static final float WORLD_WIDTH = 24f;
    private static final float WORLD_HEIGHT = 13.5f;

    /** Player physics constants */
    private static final float PLAYER_HEIGHT = 2.5f;  // Player height in game units
    private static final float PLAYER_WIDTH = PLAYER_HEIGHT * 904f / 1277f;  // Maintains sprite aspect ratio
    private static final float PLAYER_SPEED = 10f;  // Maximum horizontal speed
    private static final float PLAYER_ACCELERATION = 60f;  // How quickly player reaches max speed
    private static final float PLAYER_DECELERATION = 80f;  // Friction when stopping (faster than acceleration)
    private static final float AIR_CONTROL = 0.5f;  // Reduced movement control while in air (50% of ground control)
    private static final float JUMP_VELOCITY = 16f;  // Initial upward speed when jumping
    private static final float JUMP_HOLD_TIME = 0.2f;  // Maximum time to hold jump for extra height
    private static final float COYOTE_TIME = 0.1f;  // Grace period: can still jump briefly after leaving ground
    private static final float GRAVITY = -40f;  // Downward acceleration (negative = down)

    /** Boss fight arena boundaries */
    private static final float BOSS_ARENA_START_X = 46f;  // Where boss fight begins
    private static final float BOSS_ARENA_END_X = BOSS_ARENA_START_X + WORLD_WIDTH;  // End of boss arena

    /** Bullet system constants */
    private static final int MAX_BULLETS = 10;  // Maximum bullets on screen at once
    private static final float BULLET_SPEED = 20f;  // How fast bullets travel
    private static final float BULLET_COOLDOWN = 0.25f;  // Time between shots (prevents spam)

    /** Enemy system constants */
    private static final int MAX_ENEMIES = 10;  // Maximum enemies in the level

    // ========================================================================
    // SECTION 2: CORE GAME OBJECTS (References to Main Game and Rendering)
    // ========================================================================
    // These are references to objects we need to draw graphics and manage the game.
    
    /** Reference to the main game class (allows access to shared resources) */
    private final MainGame game;
    
    /** SpriteBatch is used to draw all images/textures on screen */
    private final SpriteBatch batch;

    /** Camera for the game world (follows player, shows game area) */
    private OrthographicCamera worldCamera;
    
    /** Camera for HUD elements (UI that stays on screen, like score) */
    private OrthographicCamera hudCamera;
    
    /** Used to draw shapes (like health bars) */
    private ShapeRenderer shapeRenderer;

    /** Font for drawing text on screen */
    private BitmapFont font;
    
    /** Helper to measure text size before drawing */
    private GlyphLayout layout;
    
    /** Background image texture */
    private Texture backgroundTexture;
    
    /** Region of background for scrolling effect */
    private TextureRegion backgroundRegion;

    // ========================================================================
    // SECTION 3: PLAYER DATA (Position, Movement, Animation)
    // ========================================================================
    // All information about the player character is stored here.
    
    /** Player's position and size (Rectangle stores x, y, width, height) */
    private Rectangle playerRect;
    /** Player sprite textures for different animations */
    private Texture standingTexture;  // When player is idle/not moving
    private Texture jumpTexture;      // When player is jumping
    private Texture walk1Texture;     // Walking animation frame 1
    private Texture walk2Texture;     // Walking animation frame 2
    private Texture walk3Texture;     // Walking animation frame 3
    
    /** Player velocity (speed and direction) */
    private float playerVX;  // Horizontal velocity (positive = right, negative = left)
    private float playerVY;  // Vertical velocity (positive = up, negative = down)
    
    /** Player state flags */
    private boolean onGround;  // True if player is standing on a platform
    private float timeSinceGrounded = 0f;  // Timer for "coyote time" (can jump briefly after leaving ground)
    private boolean jumpHeld = false;  // True if jump button is currently pressed
    private float jumpHoldTimer = 0f;  // Timer for variable jump height (longer hold = higher jump)
    private boolean facingRight = true;  // Which direction player is facing (for sprite flipping)
    
    /** Player animation system */
    // Enum defines possible animation states (OOP concept: enum is a type with fixed values)
    private enum PlayerAnimState { IDLE, WALKING, JUMPING }
    private PlayerAnimState playerAnimState = PlayerAnimState.IDLE;  // Current animation state
    private float walkAnimTime = 0f;  // Timer for cycling through walk frames
    private static final float WALK_ANIM_SPEED = 8f;  // How fast to cycle through walk frames (frames per second)

    // ========================================================================
    // SECTION 4: LEVEL DATA (Platforms, Hazards, Goal)
    // ========================================================================
    // Information about the level layout and objects.
    
    /** List of all platforms the player can stand on */
    private Array<Rectangle> platforms;
    
    /** List of all hazards (thorns) that kill the player */
    private Array<Rectangle> hazards;
    
    /** The goal portal the player must reach to complete the level */
    private Rectangle goalFlag;
    
    /** Total width of the level in game units */
    private float levelWidth;
    
    /** Textures for level objects */
    private Texture groundTexture;   // Platform/ground tile texture
    private Texture thornTexture;    // Hazard (thorn ball) texture
    private Texture portalTexture;    // Goal portal texture
    private Texture endTexture;      // End screen texture (currently disabled)

    // ========================================================================
    // SECTION 5: ENEMY DATA (Regular Enemies)
    // ========================================================================
    // Information about all enemies in the level.
    // We use arrays to store multiple enemies efficiently.
    
    /** Enemy size constants */
    private static final float ENEMY_WIDTH = 2.0f;
    private static final float ENEMY_HEIGHT = 2.0f;
    /** Enemy position and movement data (arrays store data for each enemy) */
    private float[] enemyX = new float[MAX_ENEMIES];  // X position of each enemy
    private float[] enemyY = new float[MAX_ENEMIES];  // Y position of each enemy
    private float[] enemyVX = new float[MAX_ENEMIES];  // Horizontal velocity of each enemy
    private boolean[] enemyAlive = new boolean[MAX_ENEMIES];  // Whether each enemy is alive
    private float[] enemyLeftPatrol = new float[MAX_ENEMIES];  // Left boundary of patrol area
    private float[] enemyRightPatrol = new float[MAX_ENEMIES];  // Right boundary of patrol area
    private boolean[] enemyFrozen = new boolean[MAX_ENEMIES];  // Whether enemy is frozen by ice bullet
    private float[] enemyFrozenTimer = new float[MAX_ENEMIES];  // Timer for frozen enemy fade-out
    
    /** Enemy textures */
    private Texture enemyTexture;  // Normal enemy sprite
    private Texture frozenEnemyTexture;  // Frozen enemy sprite (blue/icy)

    // ========================================================================
    // SECTION 6: BOSS DATA (Final Boss Fight)
    // ========================================================================
    // Information about the final boss enemy.
    /** Boss textures and position */
    private Texture bossTexture;  // Boss sprite
    private Texture bossFireTexture;  // Muzzle flash effect when boss shoots
    private Rectangle bossRect;  // Boss position and size
    private boolean bossAlive = true;  // Whether boss is still alive
    private int bossHealth = 25;  // Boss health points (fire bullet = 2 damage, ice = 1 damage)
    private float bossFloatTimer = 0f;  // Timer for boss movement (currently unused)
    private float bossBaseY = 3f;  // Base Y position (boss stays at fixed height)
    private boolean bossActivated = false;  // Fight only starts when player enters boss arena
    private float bossVX = 2.0f;  // Horizontal patrol speed (boss moves left/right)
    
    /** Boss bullet system (boss shoots fire at player) */
    private static final int MAX_BOSS_BULLETS = 10;  // Maximum boss bullets on screen
    private float[] bossBulletX = new float[MAX_BOSS_BULLETS];  // X position of each boss bullet
    private float[] bossBulletY = new float[MAX_BOSS_BULLETS];  // Y position of each boss bullet
    private float[] bossBulletVX = new float[MAX_BOSS_BULLETS];  // Horizontal velocity of each boss bullet
    private boolean[] bossBulletActive = new boolean[MAX_BOSS_BULLETS];  // Whether each bullet is active
    private float bossShootTimer = 0f;  // Timer counting up to next shot
    private static final float BOSS_BULLET_SPEED = 14f;  // Speed of boss bullets
    private static final float BOSS_SHOOT_MIN_INTERVAL = 1.2f;  // Minimum time between shots
    private static final float BOSS_SHOOT_MAX_INTERVAL = 2.4f;  // Maximum time between shots
    private float bossNextShotTime = 1.5f;  // Random time until next shot

    // ========================================================================
    // SECTION 7: PLAYER BULLET DATA
    // ========================================================================
    // Information about bullets the player shoots.
    
    /** Bullet type constants (using integers to represent types) */
    private static final int BULLET_TYPE_FIRE = 0;  // Fire bullet: kills enemies, does 2 damage to boss
    private static final int BULLET_TYPE_ICE = 1;   // Ice bullet: freezes enemies, does 1 damage to boss
    /** Current bullet type selected by player (F key = fire, I key = ice) */
    private int currentBulletType = BULLET_TYPE_FIRE;
    
    /** Player bullet data (arrays store data for each bullet) */
    private float[] bulletX = new float[MAX_BULLETS];  // X position of each bullet
    private float[] bulletY = new float[MAX_BULLETS];  // Y position of each bullet
    private float[] bulletVX = new float[MAX_BULLETS];  // Horizontal velocity of each bullet
    private boolean[] bulletActive = new boolean[MAX_BULLETS];  // Whether each bullet is active
    private int[] bulletType = new int[MAX_BULLETS];  // Type of each bullet (fire or ice)
    private float bulletCooldownTimer = 0f;  // Time remaining until player can shoot again
    
    /** Bullet textures */
    private Texture fireBulletTexture;  // Fire bullet sprite
    private Texture iceBulletTexture;   // Ice bullet sprite

    // ========================================================================
    // SECTION 8: COIN DATA
    // ========================================================================
    // Information about collectible coins in the level.
    
    private static final int MAX_COINS = 15;  // Maximum coins in the level
    private float[] coinX = new float[MAX_COINS];  // X position of each coin
    private float[] coinY = new float[MAX_COINS];  // Y position of each coin
    private boolean[] coinActive = new boolean[MAX_COINS];  // Whether each coin is collectible
    private int coinsCollected = 0;  // Total coins collected by player
    private Texture coinTexture;  // Coin sprite
    private float coinAnimTime = 0f;  // Timer for coin sparkle animation

    // ========================================================================
    // SECTION 9: EXPLOSION EFFECT DATA
    // ========================================================================
    // Visual effects when enemies are killed.
    
    private Texture burningEnemySheet;  // Sprite sheet containing explosion animation frames
    private Animation<TextureRegion> burningEnemyAnimation;  // Animation object that cycles through frames
    private static final int MAX_EXPLOSIONS = 20;  // Maximum explosion effects on screen
    private float[] explosionX = new float[MAX_EXPLOSIONS];  // X position of each explosion
    private float[] explosionY = new float[MAX_EXPLOSIONS];  // Y position of each explosion
    private float[] explosionStateTime = new float[MAX_EXPLOSIONS];  // Animation timer for each explosion
    private boolean[] explosionActive = new boolean[MAX_EXPLOSIONS];  // Whether each explosion is playing
    private boolean[] explosionIsBoss = new boolean[MAX_EXPLOSIONS];  // True if explosion is from boss (larger size)

    // ========================================================================
    // SECTION 10: GAME STATE DATA
    // ========================================================================
    // Overall game status and player progress.
    
    private int score = 0;  // Player's current score
    private int lives = 3;  // Number of lives remaining
    private boolean levelComplete = false;  // True when player reaches the portal
    private boolean playerDead = false;  // True when player has no lives left
    private float playerAlpha = 1.0f;  // Transparency for fade-out effect when reaching portal (1.0 = opaque, 0.0 = invisible)
    private float levelCompleteTimer = 0f;  // Timer for fade-out animation

    // ========================================================================
    // SECTION 11: REUSABLE COLLISION RECTANGLES
    // ========================================================================
    // These rectangles are reused each frame to avoid creating new objects
    // (which would slow down the game). This is an optimization technique.
    
    private final Rectangle tmpPlayerHitRect = new Rectangle();  // For player collision detection
    private final Rectangle tmpEnemyHitRect = new Rectangle();   // For enemy collision detection
    private final Rectangle tmpThornHitRect = new Rectangle();  // For thorn hazard collision detection

    // ========================================================================
    // SECTION 12: HUD (Heads-Up Display) CONSTANTS
    // ========================================================================
    // Constants for UI elements that appear on screen.
    
    private static final float EXIT_BTN_WIDTH = 80f;   // Exit button width in pixels
    private static final float EXIT_BTN_HEIGHT = 30f;  // Exit button height in pixels
    
    // ========================================================================
    // SECTION 13: SOUND AND MUSIC DATA
    // ========================================================================
    // Audio resources for sound effects and background music.
    
    /** Sound effects (short audio clips that play once) */
    private Sound jumpSound;  // Sound when player jumps
    private Sound shootSound;  // Fallback sound for shooting (if specific sounds not available)
    private Sound fireBulletSound;  // Sound when firing fire bullet
    private Sound iceBulletSound;  // Sound when firing ice bullet
    private Sound coinSound;  // Sound when collecting coin
    private Sound enemyDeathSound;  // Sound when enemy dies
    private Sound enemyGrowlSound;  // Periodic growl sound from enemies
    private Sound levelCompleteSound;
    private Sound bossGrowlSound;  // Periodic growl sound from boss
    
    /** Background music (long audio tracks that loop continuously) */
    private Music backgroundMusic;  // Main level background music
    private Music bossFightMusic;  // Music that plays during boss fight
    
    /** Sound effect timers */
    private float enemyGrowlTimer = 0f;  // Timer for periodic enemy growls
    private float bossGrowlTimer = 0f;  // Timer for periodic boss growls
    private static final float ENEMY_GROWL_INTERVAL = 3.0f;  // Enemy growl every 3 seconds
    private static final float BOSS_GROWL_INTERVAL = 2.5f;  // Boss growl every 2.5 seconds

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================
    
    /**
     * Constructor: Initializes the gameplay screen.
     * This is called when the game screen is first created.
     * 
     * OOP CONCEPT: Constructor - special method that runs when object is created.
     * It sets up all the initial state of the game.
     * 
     * @param game Reference to the main game class (allows access to shared resources)
     */
    public GameplayScreen(MainGame game) {
        // Store references to main game and shared sprite batch
        this.game = game;
        this.batch = game.batch;

        // Initialize rendering tools
        shapeRenderer = new ShapeRenderer();  // For drawing shapes (health bars, etc.)
        
        // Set up world camera (follows player, shows game world)
        worldCamera = new OrthographicCamera();
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);  // Create camera with world dimensions
        worldCamera.zoom = 1.1f;  // Slightly zoom out for better view
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f;  // Raise camera so ground appears lower

        // Set up HUD camera (for UI elements that stay on screen)
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize text rendering
        font = new BitmapFont();  // Default font for drawing text
        layout = new GlyphLayout();  // Helper to measure text size

        // ====================================================================
        // Load all image textures (sprites) from assets folder
        // ====================================================================
        // OOP CONCEPT: We're creating Texture objects to hold image data.
        // These are loaded from files in the assets folder.
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        backgroundRegion = new TextureRegion(backgroundTexture);
        groundTexture = new Texture(Gdx.files.internal("ground.png"));
        standingTexture = new Texture(Gdx.files.internal("standing.png"));
        jumpTexture = new Texture(Gdx.files.internal("jump.png"));
        walk1Texture = new Texture(Gdx.files.internal("walk1.png"));
        walk2Texture = new Texture(Gdx.files.internal("walk2.png"));
        walk3Texture = new Texture(Gdx.files.internal("walk3.png"));
        enemyTexture = new Texture(Gdx.files.internal("enemy.png"));
        frozenEnemyTexture = new Texture(Gdx.files.internal("frozenenemy.png"));
        fireBulletTexture = new Texture(Gdx.files.internal("firebullet.png"));
        iceBulletTexture = new Texture(Gdx.files.internal("icebullet.png"));
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        thornTexture = new Texture(Gdx.files.internal("thorn.png"));
        portalTexture = new Texture(Gdx.files.internal("door.png"));
        bossTexture = new Texture(Gdx.files.internal("boss.png"));
        bossFireTexture = new Texture(Gdx.files.internal("bossfire.png"));
        
        // Load end screen texture
        endTexture = new Texture(Gdx.files.internal("end.jpg"));
        burningEnemySheet = new Texture(Gdx.files.internal("burningenemy.png"));
        initBurningEnemyAnimation();

        // Load sound effects (gracefully handle missing files)
        loadSounds();

        initLevel();
        resetLevel();
    }

    private void initBurningEnemyAnimation() {
        // burningenemy.png is a 3x3 sprite sheet: play in reading order
        int FRAME_COLS = 3;
        int FRAME_ROWS = 3;

        int frameWidth = burningEnemySheet.getWidth() / FRAME_COLS;
        int frameHeight = burningEnemySheet.getHeight() / FRAME_ROWS;

        TextureRegion[][] tmp = TextureRegion.split(burningEnemySheet, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int r = 0; r < FRAME_ROWS; r++) {
            for (int c = 0; c < FRAME_COLS; c++) {
                frames[index++] = tmp[r][c];
            }
        }
        // 0.04f per frame → ~0.36s total (faster animation)
        burningEnemyAnimation = new Animation<TextureRegion>(0.04f, frames);
    }

    private void loadSounds() {
        // Load sound effects (use try-catch to handle missing files gracefully)
        try {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "jump.wav not found, continuing without sound");
        }
        try {
            shootSound = Gdx.audio.newSound(Gdx.files.internal("shoot.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "shoot.wav not found, continuing without sound");
        }
        try {
            fireBulletSound = Gdx.audio.newSound(Gdx.files.internal("firebullet.mp3"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "firebullet.mp3 not found, continuing without sound");
        }
        try {
            iceBulletSound = Gdx.audio.newSound(Gdx.files.internal("icebullet.mp3"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "icebullet.mp3 not found, continuing without sound");
        }
        try {
            coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "coin.wav not found, continuing without sound");
        }
        try {
            enemyDeathSound = Gdx.audio.newSound(Gdx.files.internal("enemy_death.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "enemy_death.wav not found, continuing without sound");
        }
        try {
            enemyGrowlSound = Gdx.audio.newSound(Gdx.files.internal("enemy_growl.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "enemy_growl.wav not found, continuing without sound");
        }

        try {
            levelCompleteSound = Gdx.audio.newSound(Gdx.files.internal("level_complete.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "level_complete.wav not found, continuing without sound");
        }
        try {
            bossGrowlSound = Gdx.audio.newSound(Gdx.files.internal("bossgrowl.mp3"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "bossgrowl.mp3 not found, continuing without sound");
        }
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.25f);  // 25% volume (lowered)
                backgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.log("Sound", "background_music.mp3 not found, continuing without music");
        }
        try {
            bossFightMusic = Gdx.audio.newMusic(Gdx.files.internal("finalfight.mp3"));
            if (bossFightMusic != null) {
                bossFightMusic.setLooping(true);
                bossFightMusic.setVolume(0.15f);  // Very low volume (15%) because the file is already loud
            }
        } catch (Exception e) {
            Gdx.app.log("Sound", "finalfight.mp3 not found, continuing without boss fight music");
        }
    }

    /**
     * Initializes the level layout.
     * Creates all platforms, hazards, enemies, coins, and sets up the boss arena.
     * This is called once when the game starts.
     */
    private void initLevel() {
        // Create empty lists for platforms and hazards
        platforms = new Array<Rectangle>();
        hazards = new Array<Rectangle>();

        // Ground platform - mostly continuous with small strategic gaps
        platforms.add(new Rectangle(0, 0, 10f, 1.2f));      // start section (longer for safety)
        platforms.add(new Rectangle(11.5f, 0, 4f, 1.2f));   // small gap, then platform
        platforms.add(new Rectangle(17f, 0, 5f, 1.2f));     // continuous section
        platforms.add(new Rectangle(23f, 0, 6f, 1.2f));    // mid section
        platforms.add(new Rectangle(30f, 0, 5f, 1.2f));    // late section
        platforms.add(new Rectangle(36f, 0, 12f, 1.2f));   // pre-boss continuous section
        // Final flat arena ground from left edge of boss scene all the way to the right edge
        platforms.add(new Rectangle(BOSS_ARENA_START_X, 0, BOSS_ARENA_END_X - BOSS_ARENA_START_X, 1.2f));

        // Floating platforms for vertical gameplay
        float tileW = 3.5f;
        float tileH = 1.0f;
        // Early section - easy jumps with intermediate step
        platforms.add(new Rectangle(4f, 2.3f, tileW, tileH));  // intermediate step (positioned between ground and first platform)
        platforms.add(new Rectangle(6f, 3.5f, tileW, tileH));  // first floating platform
        platforms.add(new Rectangle(12f, 4.5f, tileW, tileH));
        // Mid section - medium difficulty
        platforms.add(new Rectangle(18f, 5.5f, tileW, tileH));
        platforms.add(new Rectangle(25f, 4.0f, tileW, tileH));
        platforms.add(new Rectangle(30f, 6.0f, tileW, tileH));
        // Late section near boss area: two ascending floating tiles immediately after the last enemy.
        // Player will stand on these to fight the boss. The spacing is tuned so that when standing
        // on the top tile, the player's bullets and the boss's bullets travel at exactly the same height.
        float bossPlatformY1 = 1.6f;  // first step just above ground
        float bossPlatformY2 = 3.162f;  // second step - raised by 2% (was 3.1f, now 3.162f)
        // Place them right after the last normal enemy (which patrols up to ~45)
        platforms.add(new Rectangle(BOSS_ARENA_START_X + 0.5f, bossPlatformY1, tileW, tileH));          // first step (yellow 1)
        platforms.add(new Rectangle(BOSS_ARENA_START_X + 3.8f, bossPlatformY2, tileW * 1.6f, tileH));   // second, longer step (yellow 2)

        // Hazards (thorn balls) - varied placement, not blocking main path
        float hazardY = 1.2f;
        float hazardSize = 1.1f;
        // Ground hazards - placed in gaps or on sides, not blocking forward progress
        hazards.add(new Rectangle(10.5f, hazardY, hazardSize, hazardSize));  // in first small gap
        hazards.add(new Rectangle(16f, hazardY, hazardSize, hazardSize));    // between platforms
        hazards.add(new Rectangle(22f, hazardY, hazardSize, hazardSize));   // mid section
        hazards.add(new Rectangle(29f, hazardY, hazardSize, hazardSize));    // late section
        // Air hazards on platforms (more challenging, optional paths)
        hazards.add(new Rectangle(7.5f, 3.5f + tileH, hazardSize, hazardSize)); // on first platform
        hazards.add(new Rectangle(19.5f, 5.5f + tileH, hazardSize, hazardSize)); // on mid platform

        // Portal door at the very right of the boss scene.
        // Width is fixed in world units; height is adjusted to keep the sprite's aspect ratio.
        // Enlarge the portal so it dominates the far-right of the scene
        float portalWidth = 4.2f;
        float portalX = BOSS_ARENA_END_X - portalWidth;  // flush with far right edge of screen/arena
        goalFlag = new Rectangle(portalX, 0f, portalWidth, 1f);
        if (portalTexture != null) {
            float portalAspect = (float) portalTexture.getHeight() / (float) portalTexture.getWidth();
            goalFlag.height = goalFlag.width * portalAspect;
        } else {
            goalFlag.height = 6f; // sensible fallback
        }
        levelWidth = BOSS_ARENA_END_X;

        // Boss setup – floating above the final arena before the portal
        if (bossTexture != null) {
            float bossWidth = 6f;
            float bossAspect = (float) bossTexture.getHeight() / (float) bossTexture.getWidth();
            float bossHeight = bossWidth * bossAspect;
            // Base Y adjusted so that when the player stands on the top floating tile,
            // the dragon's mouth fire and the player's bullets are at exactly the same height.
            // Top tile is at y=3.1f, player stands on it (y=4.1f), bullet at y=4.1f + 1.5f = 5.6f
            // Boss bullet should be at 5.6f, so with height*0.52 offset, we need bossBaseY ≈ 2.5f
            bossBaseY = 2.5f;
            // Place boss just to the left of the portal (red area in your sketch)
            float bossX = portalX - bossWidth - 1.5f;
            bossRect = new Rectangle(bossX, bossBaseY, bossWidth, bossHeight);
        } else {
            bossRect = new Rectangle(BOSS_ARENA_END_X - 10f, bossBaseY, 6f, 6f);
        }
        bossAlive = true;
        bossHealth = 25;
        bossFloatTimer = 0f;
        bossActivated = false;

        // Coins – strategically placed for interesting collection paths
        for (int i = 0; i < MAX_COINS; i++) {
            coinActive[i] = false;
        }
        // Ground level coins (easy, on continuous ground sections)
        placeCoin(3f, 2.2f);    // early section, safe
        placeCoin(7f, 2.2f);    // still on first ground platform
        placeCoin(12.5f, 2.2f);  // on second ground platform
        placeCoin(18.5f, 2.2f); // on third ground platform
        placeCoin(24.5f, 2.2f); // mid section ground
        placeCoin(31.5f, 2.2f); // late section ground
        placeCoin(39f, 2.2f);    // final section ground
        placeCoin(45f, 2.2f);   // near goal
        
        // Platform coins (on reachable floating platforms)
        placeCoin(4.5f, 3.5f);   // on intermediate step platform (easily reachable)
        placeCoin(7.5f, 4.7f);   // on first floating platform (reachable from intermediate)
        placeCoin(13.5f, 5.7f);  // on second floating platform
        placeCoin(19.5f, 6.7f); // on third floating platform
        placeCoin(26.5f, 5.2f); // on fourth floating platform (lower, easier)
        placeCoin(37.5f, 6.2f); // on sixth floating platform

        // Enemies
        setupEnemies();
    }

    private void placeCoin(float x, float y) {
        for (int i = 0; i < MAX_COINS; i++) {
            if (!coinActive[i]) {
                coinX[i] = x;
                coinY[i] = y;
                coinActive[i] = true;
                break;
            }
        }
    }

    private void setupEnemies() {
        for (int i = 0; i < MAX_ENEMIES; i++) {
            enemyAlive[i] = false;
            enemyFrozen[i] = false;
            enemyFrozenTimer[i] = 0f;
        }
        // Ground enemies with varied speeds and patrols - on continuous ground sections
        setEnemy(0, 6f, 1.2f, 4f, 10f, 1.5f);      // early section (on first platform)
        setEnemy(1, 13f, 1.2f, 11.5f, 15.5f, 1.8f); // on second platform
        setEnemy(2, 19f, 1.2f, 17f, 22f, 1.6f);     // on third platform
        setEnemy(3, 25f, 1.2f, 23f, 29f, 1.9f);     // faster enemy on mid section
        setEnemy(4, 32f, 1.2f, 30f, 35f, 1.4f);     // late section
        setEnemy(5, 39f, 1.2f, 36f, 45f, 1.7f);     // near goal (on final continuous section)
        
        // Platform enemies (more challenging!), all placed before the final open boss area
        setEnemy(6, 7.5f, 3.5f + ENEMY_HEIGHT, 6f, 10f, 1.3f);   // on first platform
        setEnemy(7, 19.5f, 5.5f + ENEMY_HEIGHT, 18f, 22f, 1.5f); // on mid platform
        setEnemy(8, 31.5f, 6.0f + ENEMY_HEIGHT, 30f, 34f, 1.6f); // on high platform
        // enemy 9 removed from the area near the portal to keep last stretch empty
    }

    private void setEnemy(int index, float x, float y, float left, float right, float speed) {
        if (index < 0 || index >= MAX_ENEMIES) return;
        enemyX[index] = x;
        enemyY[index] = y;
        enemyVX[index] = speed;
        enemyAlive[index] = true;
        enemyLeftPatrol[index] = left;
        enemyRightPatrol[index] = right;
    }

    /**
     * Resets the level to its initial state.
     * Called when player dies (if they have lives left) or when restarting.
     * Resets player position, enemies, bullets, coins, and game state.
     */
    private void resetLevel() {
        // Player at start - positioned on top of first platform (platform top is at y=1.2)
        float startPlatformTop = 1.2f;
        playerRect = new Rectangle(2, startPlatformTop, PLAYER_WIDTH, PLAYER_HEIGHT);
        playerVX = 0;
        playerVY = 0;
        onGround = false;
        timeSinceGrounded = 0f;
        jumpHoldTimer = 0f;
        jumpHeld = false;
        facingRight = true;  // Reset facing direction

        // Reset enemies
        setupEnemies();

        // Reset boss
        bossAlive = true;
        bossHealth = 25;
        bossFloatTimer = 0f;
        bossGrowlTimer = 0f;
        bossActivated = false;
        // Stop boss fight music if playing
        if (bossFightMusic != null && bossFightMusic.isPlaying()) {
            bossFightMusic.stop();
        }
        // Restart background music if not playing
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }

        // Reset coins (reactivate all that had positions)
        coinsCollected = 0;
        for (int i = 0; i < MAX_COINS; i++) {
            if (coinX[i] != 0 || coinY[i] != 0) {
                coinActive[i] = true;
            }
        }

        // Reset bullets
        for (int i = 0; i < MAX_BULLETS; i++) {
            bulletActive[i] = false;
        }
        bulletCooldownTimer = 0;
        
        // Reset level complete state
        levelComplete = false;
        playerAlpha = 1.0f;
        levelCompleteTimer = 0f;

        levelComplete = false;
        playerDead = false;

        // Reset camera (keep vertical offset so ground appears a bit lower)
        worldCamera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f + 0.7f, 0);
        worldCamera.update();
    }

    /**
     * ========================================================================
     * MAIN RENDER METHOD
     * ========================================================================
     * This is the most important method - it's called every frame by LibGDX
     * to draw everything on screen. The game loop works like this:
     * 1. handleInput() - Check what keys player pressed
     * 2. update() - Update all game objects (player, enemies, bullets, etc.)
     * 3. render() - Draw everything on screen
     * 
     * OOP CONCEPT: This method overrides the render() method from the Screen
     * interface. This is called "method overriding" - we provide our own
     * implementation of a method defined in an interface.
     * 
     * @param delta Time since last frame in seconds (used for smooth animation)
     */
    @Override
    public void render(float delta) {
        // Step 1: Process player input (movement, jumping, shooting)
        handleInput(delta);
        
        // Step 2: Update all game objects (physics, collisions, AI, etc.)
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // If level is complete, skip world rendering and only show end screen
        if (levelComplete) {
            renderHud();
            return;
        }

        worldCamera.update();

        // Parallax background drawn in screen space, covering the full window
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        float parallax = 0.5f; // stronger scroll so wide background is noticeable
        float cameraX = worldCamera.position.x;
        float texWidth = backgroundTexture.getWidth();

        // Map cameraX in [0, levelWidth] into a [0, 1 - uSpan] UV offset
        float uSpan = (float) screenW / texWidth;
        float t = MathUtils.clamp(cameraX / levelWidth, 0f, 1f);
        float uOffset = t * (1f - uSpan) * parallax;
        backgroundRegion.setU(uOffset);
        backgroundRegion.setU2(uOffset + uSpan);
        backgroundRegion.setV(0f);
        backgroundRegion.setV2(1f);

        hudCamera.setToOrtho(false, screenW, screenH);
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        batch.draw(backgroundRegion, 0, 0, screenW, screenH);
        batch.end();

        // Draw world using world camera
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        // Draw all platforms with ground texture (so they're all visible)
        for (Rectangle platform : platforms) {
            float tileWorldSize = 1.0f;
            int tileCount = MathUtils.ceil(platform.width / tileWorldSize);
            for (int i = 0; i < tileCount; i++) {
                float gx = platform.x + i * tileWorldSize;
                float gy = platform.y;
                boolean flipX = (i % 2 == 1);
                boolean flipY = (i % 4 >= 2);
                float drawW = flipX ? -tileWorldSize : tileWorldSize;
                float drawH = flipY ? -platform.height : platform.height;
                float drawX = flipX ? gx + tileWorldSize : gx;
                float drawY = flipY ? gy + platform.height : gy;
                batch.draw(groundTexture, drawX, drawY, drawW, drawH);
            }
        }

        // Portal door – draw with correct aspect ratio based on goalFlag rectangle
        batch.draw(portalTexture, goalFlag.x, goalFlag.y, goalFlag.width, goalFlag.height);

        // Coins (textured, with simple sparkle/pulse, larger size)
        float sparkleScale = 0.9f + 0.2f * MathUtils.sin(coinAnimTime * 5f);
        for (int i = 0; i < MAX_COINS; i++) {
            if (coinActive[i]) {
                float baseSize = 1.2f;  // Increased from 0.8f to 1.2f (50% larger)
                float size = baseSize * sparkleScale;
                float drawX = coinX[i] - size / 2f;
                float drawY = coinY[i] - size / 2f;
                batch.draw(coinTexture, drawX, drawY, size, size);
            }
        }

        // Hazards (thorn textures, slightly embedded into ground for 3D look)
        for (Rectangle h : hazards) {
            float embed = 0.3f;
            float drawY = h.y - embed;
            // keep width and height equal so the original 1024x1024 image is not distorted
            float size = h.width; // width == height
            batch.draw(thornTexture, h.x, drawY, size, size);
        }

        // Explosion effects from burning enemies (fire bullet/stomp)
        if (burningEnemyAnimation != null) {
            for (int i = 0; i < MAX_EXPLOSIONS; i++) {
                if (!explosionActive[i]) continue;
                TextureRegion frame = burningEnemyAnimation.getKeyFrame(explosionStateTime[i], false);
                // Boss explosions are much larger (3x size)
                float explosionSize = explosionIsBoss[i] ? (ENEMY_WIDTH * 3f) : ENEMY_WIDTH;
                float explosionHeight = explosionIsBoss[i] ? (ENEMY_HEIGHT * 3f) : ENEMY_HEIGHT;
                // Center the larger boss explosion on the boss position
                float drawX = explosionIsBoss[i] ? (explosionX[i] - explosionSize / 2f + ENEMY_WIDTH / 2f) : explosionX[i];
                float drawY = explosionIsBoss[i] ? (explosionY[i] - explosionHeight / 2f + ENEMY_HEIGHT / 2f) : explosionY[i];
                batch.draw(frame, drawX, drawY, explosionSize, explosionHeight);
            }
        }

        // Enemies (texture, flip horizontally based on movement direction, handle frozen state)
        for (int i = 0; i < MAX_ENEMIES; i++) {
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

        // Final boss (floating in the last arena)
        if (bossAlive && bossTexture != null && bossRect != null && bossActivated) {
            // When shooting, briefly overlay the bossfire sprite as a muzzle flash around the head
            batch.draw(bossTexture, bossRect.x, bossRect.y, bossRect.width, bossRect.height);
            if (bossFireTexture != null && bossShootTimer < 0.2f) {
                batch.draw(bossFireTexture, bossRect.x, bossRect.y, bossRect.width, bossRect.height);
            }
        }

        // Player bullets (texture, fire vs ice)
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (bulletActive[i]) {
                float bw = 1.2f;
                float bh = 0.4f;
                Texture tex = (bulletType[i] == BULLET_TYPE_ICE) ? iceBulletTexture : fireBulletTexture;
                batch.draw(tex, bulletX[i], bulletY[i] - bh / 2f, bw, bh);
            }
        }

        // Boss fire bullets (use same sprite as player firebullet)
        float bossBw = 1.2f;
        float bossBh = 0.4f;
        for (int i = 0; i < MAX_BOSS_BULLETS; i++) {
            if (bossBulletActive[i]) {
                batch.draw(fireBulletTexture, bossBulletX[i], bossBulletY[i] - bossBh / 2f, bossBw, bossBh);
            }
        }

        // Player (fade out when reaching portal, flip based on facing direction)
        batch.setColor(1f, 1f, 1f, playerAlpha);
        
        // Select texture based on animation state
        Texture currentPlayerTexture = standingTexture;
        if (playerAnimState == PlayerAnimState.JUMPING) {
            currentPlayerTexture = jumpTexture;
        } else if (playerAnimState == PlayerAnimState.WALKING) {
            // Cycle through walk frames: walk1 -> walk2 -> walk3 -> walk1...
            int frameIndex = (int)(walkAnimTime * WALK_ANIM_SPEED) % 3;
            if (frameIndex == 0) {
                currentPlayerTexture = walk1Texture;
            } else if (frameIndex == 1) {
                currentPlayerTexture = walk2Texture;
            } else {
                currentPlayerTexture = walk3Texture;
            }
        } else {
            currentPlayerTexture = standingTexture;
        }
        
        // Flip sprite horizontally if facing left
        if (facingRight) {
            batch.draw(currentPlayerTexture, playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        } else {
            // Draw flipped horizontally
            batch.draw(currentPlayerTexture, 
                playerRect.x + playerRect.width, playerRect.y,  // Adjust x position
                -playerRect.width, playerRect.height);  // Negative width flips horizontally
        }
        batch.setColor(Color.WHITE);

        batch.end();

        // HUD
        renderHud();
    }

    /**
     * Handles all player input (keyboard and mouse).
     * This method checks which keys are pressed and updates player movement accordingly.
     * 
     * OOP CONCEPT: This is a private method - it can only be called from within this class.
     * This is "encapsulation" - we hide implementation details from outside.
     * 
     * @param delta Time since last frame (for smooth movement)
     */
    private void handleInput(float delta) {
        // Handle mouse/touch for exit button (in screen coordinates)
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

        if (playerDead || levelComplete) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                lives = 3;
                score = 0;
                resetLevel();
            }
            return;
        }

        // Proper physics-based horizontal movement with acceleration/deceleration
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        
        float controlMultiplier = onGround ? 1f : AIR_CONTROL;
        float acceleration = PLAYER_ACCELERATION * controlMultiplier;
        float deceleration = PLAYER_DECELERATION * controlMultiplier;
        
        if (leftPressed) {
            facingRight = false;
            if (playerVX > -PLAYER_SPEED) {
                playerVX -= acceleration * delta;
                if (playerVX < -PLAYER_SPEED) playerVX = -PLAYER_SPEED;
            }
        } else if (rightPressed) {
            facingRight = true;
            if (playerVX < PLAYER_SPEED) {
                playerVX += acceleration * delta;
                if (playerVX > PLAYER_SPEED) playerVX = PLAYER_SPEED;
            }
        } else {
            // Apply friction/deceleration when no input
            if (playerVX > 0) {
                playerVX -= deceleration * delta;
                if (playerVX < 0) playerVX = 0;
            } else if (playerVX < 0) {
                playerVX += deceleration * delta;
                if (playerVX > 0) playerVX = 0;
            }
        }
        
        // Update player animation state
        if (!onGround || playerVY > 0) {
            // Jumping or in air
            playerAnimState = PlayerAnimState.JUMPING;
            walkAnimTime = 0f;  // Reset walk animation when jumping
        } else if (Math.abs(playerVX) > 0.1f) {
            // Walking (moving horizontally)
            playerAnimState = PlayerAnimState.WALKING;
            walkAnimTime += delta;  // Advance walk animation
        } else {
            // Idle (standing still)
            playerAnimState = PlayerAnimState.IDLE;
            walkAnimTime = 0f;  // Reset walk animation when idle
        }

        // Jump input handling with coyote time and variable jump height
        boolean jumpJustPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP);
        jumpHeld = Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.UP);

        // Check if can jump (on ground or within coyote time)
        boolean canJump = onGround || timeSinceGrounded < COYOTE_TIME;
        
        if (jumpJustPressed && canJump && playerVY <= 0) {
            playerVY = JUMP_VELOCITY;
            onGround = false;
            timeSinceGrounded = COYOTE_TIME;  // Reset coyote time
            jumpHoldTimer = 0f;
            if (jumpSound != null) {
                jumpSound.play(0.6f);  // Play at 60% volume
            }
        }
        
        // Variable jump height: apply extra upward force while holding jump
        if (jumpHeld && playerVY > 0 && jumpHoldTimer < JUMP_HOLD_TIME) {
            jumpHoldTimer += delta;
            // Apply additional upward force (reduces gravity effect)
            playerVY += (GRAVITY * delta * 0.5f) * (1f - (jumpHoldTimer / JUMP_HOLD_TIME));
        }

        // Shooting
        bulletCooldownTimer -= delta;
        if (bulletCooldownTimer < 0) bulletCooldownTimer = 0;
        // Bullet type selection: F = fire, I = ice
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            currentBulletType = BULLET_TYPE_FIRE;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            currentBulletType = BULLET_TYPE_ICE;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.K) && bulletCooldownTimer <= 0) {
            shootBullet();
            bulletCooldownTimer = BULLET_COOLDOWN;
        }
    }

    /**
     * Creates a new bullet when player shoots.
     * Finds an available bullet slot and initializes it with the current bullet type.
     * Bullets are fired from the player's gun position (muzzle).
     */
    private void shootBullet() {
        // Find first available bullet slot (one that's not currently active)
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) {
                bulletActive[i] = true;
                bulletType[i] = currentBulletType;
                // Approximate gun muzzle near front side (depends on facing), slightly above mid-height
                float muzzleOffsetX = playerRect.width * 0.95f;
                float muzzleOffsetY = playerRect.height * 0.6f;
                if (facingRight) {
                    bulletX[i] = playerRect.x + muzzleOffsetX;
                    bulletVX[i] = BULLET_SPEED;
                } else {
                    bulletX[i] = playerRect.x + playerRect.width * (1f - 0.95f); // near left side
                    bulletVX[i] = -BULLET_SPEED;
                }
                bulletY[i] = playerRect.y + muzzleOffsetY;
                // Play different sound based on bullet type
                if (currentBulletType == BULLET_TYPE_FIRE && fireBulletSound != null) {
                    fireBulletSound.play(0.7f);  // Fire bullet sound
                } else if (currentBulletType == BULLET_TYPE_ICE && iceBulletSound != null) {
                    iceBulletSound.play(0.7f);  // Ice bullet sound
                } else if (shootSound != null) {
                    // Fallback to original shoot sound if specific sounds not available
                    shootSound.play(1.0f);
                }
                break;
            }
        }
    }

    /**
     * Main update method - called every frame to update all game objects.
     * This handles:
     * - Player physics (gravity, movement, collisions)
     * - Enemy AI and movement
     * - Boss behavior
     * - Bullet movement and collisions
     * - Coin collection
     * - Level completion
     * - Camera following
     * 
     * This is the "game loop" - everything that happens in the game is updated here.
     * 
     * @param delta Time since last frame (ensures smooth movement regardless of framerate)
     */
    private void update(float delta) {
        // Handle player fade-out and movement to portal center when level complete
        if (levelComplete) {
            levelCompleteTimer += delta;
            float fadeDuration = 1.0f;  // 1 second fade-out
            
            // Fade out player
            if (levelCompleteTimer < fadeDuration) {
                playerAlpha = 1.0f - (levelCompleteTimer / fadeDuration);
            } else {
                playerAlpha = 0f;
            }
            
            // Move player towards center of portal
            float portalCenterX = goalFlag.x + goalFlag.width / 2f;
            float portalCenterY = goalFlag.y + goalFlag.height / 2f;
            float playerCenterX = playerRect.x + playerRect.width / 2f;
            float playerCenterY = playerRect.y + playerRect.height / 2f;
            
            float moveSpeed = 5f * delta;
            if (Math.abs(playerCenterX - portalCenterX) > 0.1f) {
                if (playerCenterX < portalCenterX) {
                    playerRect.x += moveSpeed;
                } else {
                    playerRect.x -= moveSpeed;
                }
            }
            if (Math.abs(playerCenterY - portalCenterY) > 0.1f) {
                if (playerCenterY < portalCenterY) {
                    playerRect.y += moveSpeed;
                } else {
                    playerRect.y -= moveSpeed;
                }
            }
            return;  // Don't process normal gameplay when level is complete
        }
        
        if (playerDead) {
            return;
        }

        // Gravity
        playerVY += GRAVITY * delta;

        float newX = playerRect.x + playerVX * delta;
        float newY = playerRect.y + playerVY * delta;

        // Base rectangles for predicted position
        Rectangle newRectX = new Rectangle(newX, playerRect.y, playerRect.width, playerRect.height);
        Rectangle newRectY = new Rectangle(playerRect.x, newY, playerRect.width, playerRect.height);

        // Tighter hitbox only for horizontal collisions (ignore transparent pixels on sides)
        float insetX = playerRect.width * 0.25f;  // Match getPlayerHitRect side inset
        float insetY = playerRect.height * 0.15f; // Match getPlayerHitRect vertical inset
        Rectangle hitRectX = new Rectangle(
                newRectX.x + insetX,
                newRectX.y + insetY,
                newRectX.width - insetX * 2f,
                newRectX.height - insetY * 2f
        );

        onGround = false;

        // Horizontal collisions with platforms (only block if hitting from the side, not from above/below)
        for (Rectangle p : platforms) {
            if (hitRectX.overlaps(p)) {
                // Only block horizontal movement if we're hitting from the side
                // Check if player is on top of platform (player bottom is above platform top with small tolerance)
                float playerBottom = playerRect.y;
                float playerTop = playerRect.y + playerRect.height;
                float platformTop = p.y + p.height;
                float platformBottom = p.y;
                
                boolean onTop = playerBottom >= platformTop - 0.2f;  // player standing on platform
                boolean below = playerTop <= platformBottom + 0.2f;   // player below platform
                
                if (!onTop && !below) {
                    // Hitting from the side - block movement
                    if (playerVX > 0) {
                        newX = p.x - playerRect.width;
                    } else if (playerVX < 0) {
                        newX = p.x + p.width;
                    }
                    playerVX = 0;
                    newRectX.x = newX;
                    // Keep hitbox in sync with base rect
                    hitRectX.x = newRectX.x + insetX;
                }
                // If on top or below, allow horizontal movement to continue
            }
        }

        // Vertical collisions with platforms.
        // Use a small "feet" hitbox at the bottom of the player so that only the visible
        // boots/legs interact with the ground, not transparent pixels under the gun.
        float feetInsetX = playerRect.width * 0.25f;      // same side inset as getPlayerHitRect
        float feetHeight = playerRect.height * 0.28f;     // bottom 28% of the sprite are the feet/legs
        Rectangle feetRectY = new Rectangle(
                newRectY.x + feetInsetX,
                newRectY.y,
                newRectY.width - feetInsetX * 2f,
                feetHeight
        );

        // Separate smaller hitbox for the head when jumping up into platforms
        float headInsetX = playerRect.width * 0.25f;
        float headHeight = playerRect.height * 0.18f;     // top 18% is the head/upper gun
        Rectangle headRectY = new Rectangle(
                newRectY.x + headInsetX,
                newRectY.y + newRectY.height - headHeight,
                newRectY.width - headInsetX * 2f,
                headHeight
        );

        for (Rectangle p : platforms) {
            if (playerVY < 0) {
                // Landing / falling down: use feet hitbox only so gun/upper body do not interact
                if (feetRectY.overlaps(p)) {
                    newY = p.y + p.height;
                    onGround = true;
                    // Reset coyote time and jump hold timer when landing
                    timeSinceGrounded = 0f;
                    jumpHoldTimer = 0f;
                    playerVY = 0;
                    newRectY.y = newY;
                }
            } else if (playerVY > 0) {
                // Jumping up: use head hitbox to bump into platforms above
                if (headRectY.overlaps(p)) {
                    newY = p.y - playerRect.height;
                    playerVY = 0;
                    newRectY.y = newY;
                }
            }
        }

        // Update coyote time (track time since leaving ground)
        if (!onGround) {
            timeSinceGrounded += delta;
        } else {
            timeSinceGrounded = 0f;
        }

        // When the boss fight is active, lock the player inside the boss arena,
        // preventing backtracking to the left like classic Mario boss rooms.
        if (bossActivated) {
            if (newX < BOSS_ARENA_START_X) {
                newX = BOSS_ARENA_START_X;
                playerVX = Math.max(0f, playerVX); // don't push back to the left
            }
        }

        playerRect.x = newX;
        playerRect.y = newY;

        // Check hazards with tighter hitboxes
        Rectangle playerHit = getPlayerHitRect();
        for (Rectangle h : hazards) {
            if (playerHit.overlaps(getThornHitRect(h))) {
                killPlayer();
                break;
            }
        }

        // Check falling off world
        if (playerRect.y < -5) {
            killPlayer();
        }

        // Coin animation timer
        coinAnimTime += delta;

        // Activate boss fight only when player reaches boss arena (boss is on screen)
        if (!bossActivated && bossRect != null) {
            float playerCenterX = playerRect.x + playerRect.width / 2f;
            if (playerCenterX >= BOSS_ARENA_START_X + 1f) {
                bossActivated = true;
                // Switch to boss fight music
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                if (bossFightMusic != null) {
                    bossFightMusic.play();
                }
            }
        }

        // Update enemies
        updateEnemies(delta);

        // Update boss floating behaviour and attacks
        updateBoss(delta);
        
        // Periodic enemy growls (if any enemies are alive)
        enemyGrowlTimer += delta;
        if (enemyGrowlTimer >= ENEMY_GROWL_INTERVAL) {
            enemyGrowlTimer = 0f;
            // Check if any enemy is alive
            boolean hasAliveEnemy = false;
            for (int i = 0; i < MAX_ENEMIES; i++) {
                if (enemyAlive[i]) {
                    hasAliveEnemy = true;
                    break;
                }
            }
            // Play growl sound if there are alive enemies
            if (hasAliveEnemy && enemyGrowlSound != null) {
                enemyGrowlSound.play(0.4f);  // Play at 40% volume (ambient sound)
            }
        }
        
        // Periodic boss growls during boss fight
        if (bossActivated && bossAlive) {
            bossGrowlTimer += delta;
            if (bossGrowlTimer >= BOSS_GROWL_INTERVAL) {
                bossGrowlTimer = 0f;
                if (bossGrowlSound != null) {
                    bossGrowlSound.play(0.5f);  // Play at 50% volume
                }
            }
        }

        // Player-enemy collisions
        handlePlayerEnemyCollisions();

        // Update bullets
        updateBullets(delta);

        // Bullet-enemy collisions
        handleBulletEnemyCollisions();

        // Bullet-boss collisions
        handleBulletBossCollisions();

        // Boss bullet logic (movement & collisions)
        updateBossBullets(delta);

        // Coins
        handleCoins();

        // Goal - use tighter hitbox (reuse playerHit from hazards check above)
        if (!bossAlive && playerHit.overlaps(goalFlag)) {
            if (!levelComplete) {
                levelComplete = true;
                levelCompleteTimer = 0f;
                if (levelCompleteSound != null) {
                    levelCompleteSound.play(0.8f);  // Play at 80% volume
                }
                // Stop background music and boss fight music when level completes
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                if (bossFightMusic != null && bossFightMusic.isPlaying()) {
                    bossFightMusic.stop();
                }
            }
        }

        // Camera follow
        updateCamera();
    }

    private void updateBoss(float delta) {
        if (!bossAlive || bossRect == null || !bossActivated) return;
        bossFloatTimer += delta;
        // Keep vertical position fixed so fire always leaves at same height as player bullets
        bossRect.y = bossBaseY;

        // Horizontal patrol within a limited band in front of the portal
        float leftLimit = BOSS_ARENA_START_X + 6f;
        float rightLimit = BOSS_ARENA_END_X - 6f - bossRect.width;
        bossRect.x += bossVX * delta;
        if (bossRect.x < leftLimit) {
            bossRect.x = leftLimit;
            bossVX = Math.abs(bossVX);
        } else if (bossRect.x > rightLimit) {
            bossRect.x = rightLimit;
            bossVX = -Math.abs(bossVX);
        }

        // Shooting timer (randomised intervals)
        bossShootTimer += delta;
        if (bossShootTimer >= bossNextShotTime) {
            bossShootTimer = 0f;
            bossNextShotTime = MathUtils.random(BOSS_SHOOT_MIN_INTERVAL, BOSS_SHOOT_MAX_INTERVAL);
            shootBossFire();
        }
    }

    private void shootBossFire() {
        if (bossRect == null) return;
        for (int i = 0; i < MAX_BOSS_BULLETS; i++) {
            if (!bossBulletActive[i]) {
                bossBulletActive[i] = true;
                // Mouth position near left side of boss sprite
                // Offset adjusted to match player bullet height when player is on top tile
                float mouthOffsetX = bossRect.width * 0.18f;
                float mouthOffsetY = bossRect.height * 0.50f;  // Slightly adjusted for perfect alignment
                bossBulletX[i] = bossRect.x + mouthOffsetX;
                bossBulletY[i] = bossRect.y + mouthOffsetY;
                // Aim horizontally towards the player (same y-level so player can jump/dodge)
                float dir = (playerRect.x + playerRect.width / 2f) < bossBulletX[i] ? -1f : 1f;
                bossBulletVX[i] = BOSS_BULLET_SPEED * dir;
                break;
            }
        }
    }

    private void updateEnemies(float delta) {
        for (int i = 0; i < MAX_ENEMIES; i++) {
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

            // Patrol bounds, account for enemy width
            if (enemyX[i] < enemyLeftPatrol[i]) {
                enemyX[i] = enemyLeftPatrol[i];
                enemyVX[i] = Math.abs(enemyVX[i]);
            } else if (enemyX[i] + ENEMY_WIDTH > enemyRightPatrol[i]) {
                enemyX[i] = enemyRightPatrol[i] - ENEMY_WIDTH;
                enemyVX[i] = -Math.abs(enemyVX[i]);
            }
        }
    }

    private void handlePlayerEnemyCollisions() {
        Rectangle playerHit = getPlayerHitRect();
        for (int i = 0; i < MAX_ENEMIES; i++) {
            if (!enemyAlive[i] || enemyFrozen[i]) continue;
            Rectangle enemyRect = getEnemyHitRect(i);
            if (playerHit.overlaps(enemyRect)) {
                // Determine if stomping
                boolean stomping = playerVY < 0 && (playerRect.y - (enemyY[i] + ENEMY_HEIGHT)) > 0.1f;
                if (stomping) {
                    spawnExplosionEffect(enemyX[i], enemyY[i]);
                    enemyAlive[i] = false;
                    score += 100;
                    playerVY = JUMP_VELOCITY * 0.7f; // small bounce
                    if (enemyDeathSound != null) {
                        enemyDeathSound.play(0.6f);  // Play at 60% volume
                    }
                } else {
                    killPlayer();
                    return;
                }
            }
        }
    }

    private void updateBullets(float delta) {
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            bulletX[i] += bulletVX[i] * delta;
            
            // Check if bullet goes off screen
            if (bulletX[i] > levelWidth + 5 || bulletX[i] < -5) {
                bulletActive[i] = false;
                continue;
            }
            
            // Check bullet collision with platforms (ground and floating tiles)
            Rectangle bulletRect = getBulletHitRect(i);
            for (Rectangle platform : platforms) {
                if (bulletRect.overlaps(platform)) {
                    // Bullet hits a platform/tile, deactivate it
                    bulletActive[i] = false;
                    break;
                }
            }
        }

        // Update explosion animations
        if (burningEnemyAnimation != null) {
            for (int i = 0; i < MAX_EXPLOSIONS; i++) {
                if (!explosionActive[i]) continue;
                explosionStateTime[i] += delta;
                if (burningEnemyAnimation.isAnimationFinished(explosionStateTime[i])) {
                    explosionActive[i] = false;
                }
            }
        }
    }

    private void handleBulletEnemyCollisions() {
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            Rectangle bulletRect = getBulletHitRect(i);

            for (int j = 0; j < MAX_ENEMIES; j++) {
                if (!enemyAlive[j]) continue;
                Rectangle enemyRect = getEnemyHitRect(j);
                if (bulletRect.overlaps(enemyRect)) {
                    bulletActive[i] = false;
                    if (bulletType[i] == BULLET_TYPE_ICE) {
                        // Freeze enemy: stop movement and start fade timer
                        enemyFrozen[j] = true;
                        enemyFrozenTimer[j] = 0f;
                        enemyVX[j] = 0f;
                    } else {
                        // Fire bullet: spawn burning explosion and kill enemy
                        spawnExplosionEffect(enemyX[j], enemyY[j]);
                        enemyAlive[j] = false;
                        score += 50;
                        if (enemyDeathSound != null) {
                            enemyDeathSound.play(0.6f);  // Play at 60% volume
                        }
                    }
                    break;
                }
            }
        }
    }

    private void handleBulletBossCollisions() {
        if (!bossAlive || bossRect == null || !bossActivated) return;
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            Rectangle bulletRect = getBulletHitRect(i);
            if (bulletRect.overlaps(bossRect)) {
                bulletActive[i] = false;
                // Fire bullets do more damage than ice
                int damage = (bulletType[i] == BULLET_TYPE_FIRE) ? 2 : 1;
                bossHealth -= damage;
                if (bossHealth <= 0) {
                    bossAlive = false;
                    spawnExplosionEffect(bossRect.x, bossRect.y, true);  // Large boss explosion
                    score += 500;
                    if (enemyDeathSound != null) {
                        enemyDeathSound.play(0.8f);
                    }
                }
            }
        }
    }

    private void updateBossBullets(float delta) {
        if (!bossActivated) return;
        for (int i = 0; i < MAX_BOSS_BULLETS; i++) {
            if (!bossBulletActive[i]) continue;
            bossBulletX[i] += bossBulletVX[i] * delta;
            // Deactivate when off screen/level
            if (bossBulletX[i] < -5 || bossBulletX[i] > levelWidth + 5) {
                bossBulletActive[i] = false;
                continue;
            }

            // Build simple rect for boss bullet
            float bw = 1.4f;
            float bh = 0.6f;
            Rectangle bossBulletRect = new Rectangle(
                    bossBulletX[i],
                    bossBulletY[i] - bh / 2f,
                    bw,
                    bh
            );

            // 1) Boss fire vs player: player dies on hit
            Rectangle playerHit = getPlayerHitRect();
            if (!playerDead && bossBulletRect.overlaps(playerHit)) {
                bossBulletActive[i] = false;
                killPlayer();
                continue;
            }

            // 2) Boss fire vs player ice bullet: both disappear
            for (int j = 0; j < MAX_BULLETS; j++) {
                if (!bulletActive[j]) continue;
                if (bulletType[j] != BULLET_TYPE_ICE) continue;
                Rectangle iceRect = getBulletHitRect(j);
                if (iceRect.overlaps(bossBulletRect)) {
                    bulletActive[j] = false;
                    bossBulletActive[i] = false;
                    break;
                }
            }
        }
    }

    private void handleCoins() {
        Rectangle playerCoinRect = getPlayerHitRect();
        for (int i = 0; i < MAX_COINS; i++) {
            if (!coinActive[i]) continue;
            Rectangle coinRect = getCoinHitRect(i);
            if (playerCoinRect.overlaps(coinRect)) {
                coinActive[i] = false;
                coinsCollected++;
                score += 10;
                if (coinSound != null) {
                    coinSound.play(0.7f);  // Play at 70% volume
                }
            }
        }
    }

    // --- Tighter hitbox helpers -------------------------------------------------

    private Rectangle getPlayerHitRect() {
        // Tighter hitbox to avoid false collisions with transparent pixels
        float insetX = playerRect.width * 0.25f;  // 25% inset on sides
        float insetY = playerRect.height * 0.15f; // 15% inset on top/bottom
        tmpPlayerHitRect.set(
                playerRect.x + insetX,
                playerRect.y + insetY,
                playerRect.width - insetX * 2f,
                playerRect.height - insetY * 2f
        );
        return tmpPlayerHitRect;
    }

    private Rectangle getEnemyHitRect(int index) {
        float ex = enemyX[index];
        float ey = enemyY[index];
        float insetX = ENEMY_WIDTH * 0.15f;
        float insetY = ENEMY_HEIGHT * 0.15f;
        tmpEnemyHitRect.set(
                ex + insetX,
                ey + insetY,
                ENEMY_WIDTH - insetX * 2f,
                ENEMY_HEIGHT - insetY * 2f
        );
        return tmpEnemyHitRect;
    }

    private Rectangle getThornHitRect(Rectangle base) {
        // Much tighter hitbox - thorns have lots of transparent spikes, use 40% inset
        float inset = base.width * 0.4f;  // 40% inset to only hit the solid center
        tmpThornHitRect.set(
                base.x + inset,
                base.y + inset,
                base.width - inset * 2f,
                base.height - inset * 2f
        );
        return tmpThornHitRect;
    }

    private Rectangle getBulletHitRect(int index) {
        // Bullet is drawn as 1.2f x 0.4f, but use tighter hitbox to ignore transparent edges
        float bulletW = 1.2f;
        float bulletH = 0.4f;
        float insetX = bulletW * 0.25f;  // 25% inset on sides
        float insetY = bulletH * 0.2f;   // 20% inset on top/bottom
        return new Rectangle(
                bulletX[index] + insetX,
                bulletY[index] - bulletH / 2f + insetY,
                bulletW - insetX * 2f,
                bulletH - insetY * 2f
        );
    }

    private Rectangle getCoinHitRect(int index) {
        // Coin is drawn with sparkle animation, base size ~1.2f, use tighter hitbox
        float baseSize = 1.2f;  // Updated to match new coin size
        float inset = baseSize * 0.2f;  // 20% inset to ignore transparent edges
        return new Rectangle(
                coinX[index] - baseSize / 2f + inset,
                coinY[index] - baseSize / 2f + inset,
                baseSize - inset * 2f,
                baseSize - inset * 2f
        );
    }

    private void spawnExplosionEffect(float x, float y) {
        spawnExplosionEffect(x, y, false);
    }
    
    private void spawnExplosionEffect(float x, float y, boolean isBoss) {
        if (burningEnemyAnimation == null) return;
        for (int i = 0; i < MAX_EXPLOSIONS; i++) {
            if (!explosionActive[i]) {
                explosionActive[i] = true;
                explosionIsBoss[i] = isBoss;
                explosionStateTime[i] = 0f;
                // Align explosion roughly with enemy position/size
                explosionX[i] = x;
                explosionY[i] = y;
                break;
            }
        }
    }

    private void updateCamera() {
        float halfViewportWidth = worldCamera.viewportWidth / 2f;

        if (bossActivated) {
            // During boss fight, keep camera static over the boss arena like classic Mario.
            float arenaCenterX = (BOSS_ARENA_START_X + BOSS_ARENA_END_X) / 2f;
            worldCamera.position.x = arenaCenterX;
        } else {
            float cameraX = playerRect.x + playerRect.width / 2f;
            float minX = halfViewportWidth;
            float maxX = levelWidth - halfViewportWidth;
            cameraX = MathUtils.clamp(cameraX, minX, maxX);
            worldCamera.position.x = cameraX;
        }

        // Keep vertical offset constant so ground sits a bit lower against the background
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f;
    }

    private void killPlayer() {
        lives--;
        if (lives > 0) {
            resetLevel();
        } else {
            playerDead = true;
        }
    }

    private void renderHud() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        String title = "Mini Platformer – Reach the yellow flag!";
        String controls = "Move: LEFT/RIGHT or A/D   Jump: SPACE/W/UP   Shoot: K";
        String stats = "Score: " + score + "   Coins: " + coinsCollected + "   Lives: " + lives;

        float x = 20;
        float y = Gdx.graphics.getHeight() - 20;

        // Title
        drawShadowedText(title, x, y, Color.WHITE);
        // Controls
        drawShadowedText(controls, x, y - 25, Color.LIGHT_GRAY);
        // Stats
        drawShadowedText(stats, x, y - 50, Color.GOLD);

        // Exit button (top-right)
        float btnX = Gdx.graphics.getWidth() - EXIT_BTN_WIDTH - 10f;
        float btnY = Gdx.graphics.getHeight() - 10f;
        String exitLabel = "Exit";
        // Simple text button: draw background-ish text with shadow
        drawShadowedText(exitLabel, btnX + 15f, btnY - 5f, Color.RED);

        // Boss health bar – fixed at top center of the screen during the boss fight
        if (bossActivated && bossAlive) {
            // Finish HUD text drawing first
            batch.end();

            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();
            float barWidth = 220f;
            float barHeight = 16f;
            float sx = (screenW - barWidth) / 2f;
            float sy = screenH - 80f;  // a bit under the top HUD text
            float healthPercent = Math.max(0f, bossHealth / 25f);
            float filledWidth = barWidth * healthPercent;

            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // Background
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(sx, sy, barWidth, barHeight);
            // Filled portion
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(sx + 2f, sy + 2f, Math.max(0f, filledWidth - 4f), barHeight - 4f);
            shapeRenderer.end();

            // Resume batch for center messages
            batch.begin();
        }

        // Center messages
        if (playerDead) {
            String msg = "You Died! Press R to restart";
            Color color = Color.RED;

            layout.setText(font, msg);
            float msgX = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float msgY = Gdx.graphics.getHeight() / 2f;

            drawShadowedText(msg, msgX, msgY, color);
        } else if (levelComplete) {
            // Draw full screen end.jpg image
            if (endTexture != null) {
                float screenW = Gdx.graphics.getWidth();
                float screenH = Gdx.graphics.getHeight();
                batch.draw(endTexture, 0, 0, screenW, screenH);
            }
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
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (groundTexture != null) {
            groundTexture.dispose();
        }
        if (standingTexture != null) {
            standingTexture.dispose();
        }
        if (jumpTexture != null) {
            jumpTexture.dispose();
        }
        if (walk1Texture != null) {
            walk1Texture.dispose();
        }
        if (walk2Texture != null) {
            walk2Texture.dispose();
        }
        if (walk3Texture != null) {
            walk3Texture.dispose();
        }
        if (enemyTexture != null) {
            enemyTexture.dispose();
        }
        if (coinTexture != null) {
            coinTexture.dispose();
        }
        if (fireBulletTexture != null) {
            fireBulletTexture.dispose();
        }
        if (iceBulletTexture != null) {
            iceBulletTexture.dispose();
        }
        if (thornTexture != null) {
            thornTexture.dispose();
        }
        if (portalTexture != null) {
            portalTexture.dispose();
        }
        if (bossTexture != null) {
            bossTexture.dispose();
        }
        if (bossFireTexture != null) {
            bossFireTexture.dispose();
        }
        // Dispose end texture
        if (endTexture != null) {
            endTexture.dispose();
        }
        if (burningEnemySheet != null) {
            burningEnemySheet.dispose();
        }
        
        // Dispose sounds
        if (jumpSound != null) {
            jumpSound.dispose();
        }
        if (shootSound != null) {
            shootSound.dispose();
        }
        if (fireBulletSound != null) {
            fireBulletSound.dispose();
        }
        if (iceBulletSound != null) {
            iceBulletSound.dispose();
        }
        if (coinSound != null) {
            coinSound.dispose();
        }
        if (enemyDeathSound != null) {
            enemyDeathSound.dispose();
        }
        if (enemyGrowlSound != null) {
            enemyGrowlSound.dispose();
        }

        if (levelCompleteSound != null) {
            levelCompleteSound.dispose();
        }
        if (bossGrowlSound != null) {
            bossGrowlSound.dispose();
        }
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (bossFightMusic != null) {
            bossFightMusic.dispose();
        }
    }
}