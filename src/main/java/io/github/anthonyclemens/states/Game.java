package io.github.anthonyclemens.states;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.Sys;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.InputAdapter;
import org.newdawn.slick.util.Log;

import com.codedisaster.steamworks.SteamAPI;

import io.github.anthonyclemens.Achievements.Achievement;
import io.github.anthonyclemens.Achievements.AchievementType;
import io.github.anthonyclemens.GameObjects.Building.MultiTileObject;
import io.github.anthonyclemens.GameObjects.Mobs.Fish;
import io.github.anthonyclemens.GameObjects.Mobs.Spider;
import io.github.anthonyclemens.GameObjects.Mobs.Zombie;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.SingleTileObject;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Logic.Calender;
import io.github.anthonyclemens.Logic.DateTime;
import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.Camera;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.Sound.JukeBox;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.Utils;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.World;
import io.github.anthonyclemens.utils.AmbientSoundManager;
import io.github.anthonyclemens.utils.CollisionHandler;
import io.github.anthonyclemens.utils.DebugGUI;
import io.github.anthonyclemens.utils.DisplayHUD;
import io.github.anthonyclemens.utils.FrameTimeGraph;
import io.github.anthonyclemens.utils.Profiler;
import io.github.anthonyclemens.utils.ProfilerRenderer;
import io.github.anthonyclemens.utils.SaveLoadManager;

public class Game extends BasicGameState{

    // Game Variables
    private float zoom = 2.0f;
    private float cameraX = 0;
    private float cameraY = 0;
    private boolean dragging = false;
    private float lastMouseX;
    private float lastMouseY;
    private boolean showHUD = true;
    public static boolean paused = false;
    private int updateAccumulator = 0;
    private static final int TARGET_INTERVAL = 1000 / 15;

    // Game Constants
    private Image backgroundImage;
    private static final float MIN_ZOOM = 0.4f;
    private static final float DEBUG_MIN_ZOOM = 0.1f;
    private static final float DAY_LENGTH = 2f; // Length of a day in minutes
    private static final float SUNRISE_TIME = 7f;
    private static final float SUNSET_TIME = 19f;
    private static final DateTime START_DATE_TIME = new DateTime(1, 1, 1462, 8, 0);

    // Game Objects
    private Input input;
    private Camera camera;
    private Player player;
    private DayNightCycle env;
    private Calender calender;
    private IsoRenderer renderer;
    World chunkManager;
    private CollisionHandler collisionHandler;
    private DebugGUI debugGUI;
    private DisplayHUD displayHUD;
    private AmbientSoundManager ambientSoundManager;
    private SaveLoadManager saveLoadManager;

    public static JukeBox jukeBox;
    public static SoundBox ambientSoundBox;
    public static SoundBox passiveMobSoundBox;
    public static SoundBox enemyMobSoundBox;
    public static SoundBox gameObjectSoundBox;

    // Debug Related Variables
    private FrameTimeGraph frameGraph;
    public static boolean showDebug = true;
    public static boolean soundDebug = false;
    public static boolean showCursorLoc = false;
    private Profiler updateProfiler;
    int frameCounter = 0;
    private static final int UPDATE_INTERVAL = 8;
    private long lastFrameTime = System.nanoTime();
    Map<String, String> updateData = null;


    @Override
    public int getID() {
        return GameStates.GAME.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        frameGraph = new FrameTimeGraph(400);
        initProfiling(container);
        initSystems();
        logGameState();

        if (handleHotstart()) {
            setVolumes();
            return;
        }

        initSharedData();
        initWorld(container);
        initRenderer();
        initAudio();
        initDebugging();
        zoom = 1f;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        container.getInput().addMouseListener(new InputAdapter() {
            //Drag
            @Override
            public void mousePressed(int button, int x, int y) {
                if (button == Input.MOUSE_RIGHT_BUTTON) {
                    dragging = true;
                    lastMouseX = x;
                    lastMouseY = y;
                }
            }

            @Override
            public void mouseReleased(int button, int x, int y) {
                if (button == Input.MOUSE_RIGHT_BUTTON) {
                    dragging = false;
                }
            }
            //Zoom
            @Override
            public void mouseWheelMoved(int change) {
                zoom += change * 0.001f;
                zoom = Math.min(Math.max((showDebug) ? DEBUG_MIN_ZOOM : MIN_ZOOM, zoom), 8f);
            }
        });
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        updateProfiler.begin();
        updateAccumulator += delta;
        if (updateAccumulator >= TARGET_INTERVAL) {
            SteamAPI.runCallbacks();
            if(!paused){
                ambientSoundManager.playAmbientMusic(env);
                ambientSoundManager.playAmbientSounds(env, player);
            }
            updateAccumulator -= TARGET_INTERVAL;
        }
        updateProfiler.tick("SteamAPI Callback, Sound and Music");
        renderer.update(container, zoom, cameraX, cameraY, env.isSunUp());
        if(showHUD) displayHUD.updateHUD(delta, player);
        updateProfiler.tick("Renderer update");
        int[] playerLoc = renderer.screenToIsometric(player.getRenderX()+player.getHitbox().getWidth()/2, player.getRenderY()+player.getHitbox().getHeight());
        renderer.calculateHitbox(renderer, playerLoc[2], playerLoc[3]);
        updateProfiler.tick("Hitbox Calculation");

        updateKeyboard(game, delta, input);
        updateMouse(input);
        updateProfiler.tick("Input updates");

        Chunk currentChunk = chunkManager.getChunk(playerLoc[2], playerLoc[3]);
        player.update(input, delta, playerLoc, currentChunk, paused);
        updateProfiler.tick("Player update");
        if(!paused) {
            collisionHandler.checkPlayerCollision(player, chunkManager.getChunk(playerLoc[2], playerLoc[3]));
            if(env.getCurrentDateTime().isDaysAfter(START_DATE_TIME, 1)) player.getAchievementManager().recordProgress(AchievementType.SURVIVE);
            //collisionHandler.checkMobCollision(chunkManager);
        }
        updateProfiler.tick("Collision handler");

        camera.update(player, input, cameraX, cameraY);
        cameraX = camera.getX();
        cameraY = camera.getY();
        if(!paused){
            player.interact(input, chunkManager);
            updateProfiler.tick("Player interaction");

            env.updateDayNightCycle(delta);
            renderer.updateChunksAroundPlayer(delta,player,env,playerLoc[2], playerLoc[3]);
            updateProfiler.tick("Update Visible Chunks and GameObjects");
        }
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        long now = System.nanoTime();
        backgroundImage.draw(0, 0, container.getWidth(), container.getHeight());
        renderer.render();
        player.render(container, zoom, camera.getX(), camera.getY(), renderer);
        env.renderOverlay(container, g);
        if (showHUD) displayHUD.renderHUD(container, g, calender, env, player);
        if (showCursorLoc){
            int[] cursorLoc = renderer.screenToIsometric(container.getInput().getMouseX(), container.getInput().getMouseY());
            renderer.drawScaledTile("main", 17, cursorLoc[0]-2, cursorLoc[1]-1, cursorLoc[2], cursorLoc[3]);
        }
        if (showDebug) debugGUI.renderDebugGUI(g, container, renderer, player, zoom, jukeBox, ambientSoundBox);
        frameCounter++;
        if (frameCounter >= UPDATE_INTERVAL && showDebug) {
            updateData = updateProfiler.getAdaptiveTimes();
            frameCounter = 0;
        }
        frameGraph.addSample(now - lastFrameTime);
        lastFrameTime = now;
        if (updateData != null && showDebug) {
            drawLegend(g, updateData, 0, container.getHeight()-(updateData.size()*24f), 20);
            ProfilerRenderer.renderGraph(g, frameGraph, container.getWidth() - 432f, container.getHeight() - 110f, 360, 100);
        }
    }

    private void updateKeyboard(StateBasedGame game, int delta, Input input) throws SlickException{
        if (input.isKeyDown(Input.KEY_LEFT)) cameraX -= delta * 0.1f * zoom;
        if (input.isKeyDown(Input.KEY_RIGHT)) cameraX += delta * 0.1f * zoom;
        if (input.isKeyDown(Input.KEY_UP)) cameraY -= delta * 0.1f * zoom;
        if (input.isKeyDown(Input.KEY_DOWN)) cameraY += delta * 0.1f * zoom;
        if (input.isKeyPressed(Input.KEY_ESCAPE)) SharedData.enterState(GameStates.PAUSE_MENU, game);
        if (input.isKeyPressed(Input.KEY_F1)) showHUD=!showHUD;
        if (input.isKeyPressed(Input.KEY_F2)) {
            boolean oldShowDebug = showDebug;
            boolean oldShowHUD = showHUD;
            showDebug = false;
            showHUD = false;
            this.render(game.getContainer(),game,game.getContainer().getGraphics());
            Utils.takeScreenShot(game.getContainer().getGraphics(), game.getContainer());
            showDebug = oldShowDebug;
            showHUD = oldShowHUD;
        }
        if (input.isKeyPressed(Input.KEY_F3)) showDebug=!showDebug;
        if (input.isKeyPressed(Input.KEY_F4)) showCursorLoc=!showCursorLoc;
        if (input.isKeyPressed(Input.KEY_F11)||(input.isKeyDown(Input.KEY_LALT) && input.isKeyPressed(Input.KEY_ENTER))){
            boolean toggleFullscreen = !game.getContainer().isFullscreen();
            game.getContainer().setFullscreen(toggleFullscreen);
        }
        if (input.isKeyPressed(Input.KEY_F)){
            int[] clickedLoc = renderer.screenToIsometric(input.getMouseX(), input.getMouseY());
            Chunk clickedChunk = chunkManager.getChunk(clickedLoc[2], clickedLoc[3]);
            Fish nFish = new Fish(clickedLoc[0], clickedLoc[1], clickedLoc[2], clickedLoc[3]);
            clickedChunk.addGameObject(nFish);
        }
        if (input.isKeyPressed(Input.KEY_Z)){
            int[] clickedLoc = renderer.screenToIsometric(input.getMouseX(), input.getMouseY());
            Chunk clickedChunk = chunkManager.getChunk(clickedLoc[2], clickedLoc[3]);
            Zombie nZomb = new Zombie(clickedLoc[0], clickedLoc[1], clickedLoc[2], clickedLoc[3]);
            //nZomb.setDestinationByGlobalPosition(player.getPlayerLocation());
            clickedChunk.addGameObject(nZomb);
        }
        if (input.isKeyPressed(Input.KEY_T)){
            int[] clickedLoc = renderer.screenToIsometric(input.getMouseX(), input.getMouseY());
            Chunk clickedChunk = chunkManager.getChunk(clickedLoc[2], clickedLoc[3]);
            Spider nSpider = new Spider(clickedLoc[0], clickedLoc[1], clickedLoc[2], clickedLoc[3]);
            //nSpider.setDestinationByGlobalPosition(player.getPlayerLocation());
            clickedChunk.addGameObject(nSpider);
        }
        if (input.isKeyPressed(Input.KEY_P)) paused = !paused;
        if (input.isKeyPressed(Input.KEY_B)){
            int[] clickedLoc = renderer.screenToIsometric(input.getMouseX(), input.getMouseY());
            Chunk clickedChunk = chunkManager.getChunk(clickedLoc[2], clickedLoc[3]);
            MultiTileObject test = new MultiTileObject("mtos/tree.mto", clickedLoc[0], clickedLoc[1], clickedLoc[2], clickedLoc[3]);
            clickedChunk.addGameObject(test);
        }
        if (input.isKeyPressed(Input.KEY_L)){
            int[] clickedLoc = renderer.screenToIsometric(input.getMouseX(), input.getMouseY());
            Chunk clickedChunk = chunkManager.getChunk(clickedLoc[2], clickedLoc[3]);
            SingleTileObject log = new SingleTileObject("main", "log", 30, clickedLoc[0], clickedLoc[1], clickedLoc[2], clickedLoc[3]);
            clickedChunk.addGameObject(log);
        }
        if(input.isKeyPressed(Input.KEY_N)){
            env.setTime(19,30,0);
        }
    }

    private void updateMouse(Input input){
        // Handle mouse dragging
        if (dragging) {
            float currentMouseX = input.getMouseX();
            float currentMouseY = input.getMouseY();

            // Calculate the delta movement
            float deltaX = (currentMouseX - lastMouseX) / zoom;
            float deltaY = (currentMouseY - lastMouseY) / zoom;

            // Update the camera position
            cameraX -= deltaX;
            cameraY -= deltaY;

            // Update the last mouse position
            lastMouseX = currentMouseX;
            lastMouseY = currentMouseY;
        }
    }

    public IsoRenderer getRenderer() {
        return renderer;
    }

    public Player getPlayer() {
        return player;
    }

    public DayNightCycle getEnv() {
        return env;
    }

    public World getChunkManager() {
        return chunkManager;
    }

    public Camera getCamera() {
        return camera;
    }

    public void saveGame(String filepath) {
        saveLoadManager.saveGame(filepath, env, chunkManager, camera, player);
    }

    public static void stopAllSounds() {
        if (jukeBox != null) jukeBox.stopMusic();
        if (ambientSoundBox != null) ambientSoundBox.stopAllSounds();
        if (passiveMobSoundBox != null) passiveMobSoundBox.stopAllSounds();
        if (enemyMobSoundBox != null) enemyMobSoundBox.stopAllSounds();
        if (gameObjectSoundBox != null) gameObjectSoundBox.stopAllSounds();
    }

    public void drawLegend(Graphics g, Map<String, String> data, float startX, float startY, int spacingY) {
        int index = 0;
        g.setColor(Color.black);
        for (var entry : data.entrySet()) {
            String label = entry.getKey();
            String time = entry.getValue();
            g.drawString(label + " " + time, startX + 16, startY + (index * spacingY));

            index++;
        }
    }

    private void initProfiling(GameContainer container) throws SlickException {
        updateProfiler = new Profiler();
        input = container.getInput();
        backgroundImage = new Image("textures/MissingTexture.png");
    }

    private void initSystems() {
        collisionHandler = new CollisionHandler();
        debugGUI = new DebugGUI();
        displayHUD = new DisplayHUD();
        saveLoadManager = new SaveLoadManager();
    }

    private void logGameState() {
        Log.debug(String.format(
            "Entering Game State with hotstart: %b, loading save: %b, new game: %b",
            SharedData.isHotstart(),
            SharedData.isLoadingSave(),
            SharedData.isNewGame()
        ));
    }

    private boolean handleHotstart() {
        if (SharedData.isHotstart() && !SharedData.isLoadingSave()) {
            setVolumes();
            return true;
        }
        return false;
    }

    private void initSharedData() {
        SharedData.setHotstart(true);
        SharedData.setGameState(this);
    }

    private void initWorld(GameContainer container) {
        if (SharedData.isNewGame() || !SaveLoadManager.exists(SharedData.getSaveFilePath())) {
            chunkManager = new World(new Random(Sys.getTime()).nextInt());
            createNewPlayer(container.getWidth() / 2f, container.getHeight() / 2f, 0.075f, 100, null);
            calender = new Calender(START_DATE_TIME.getDay(), START_DATE_TIME.getMonth(), START_DATE_TIME.getYear());
            env = new DayNightCycle(DAY_LENGTH, SUNRISE_TIME, SUNSET_TIME, calender);
        } else {
            saveLoadManager.loadGame(SharedData.getSaveFilePath());
            chunkManager = saveLoadManager.getRenderer().getChunkManager();
            createNewPlayer(
                saveLoadManager.getPlayerX(),
                saveLoadManager.getPlayerY(),
                saveLoadManager.getPlayerSpeed(),
                saveLoadManager.getPlayerHealth(),
                saveLoadManager.getPlayerAchievements()
            );
            env = saveLoadManager.getDayNightCycle();
            calender = saveLoadManager.getDayNightCycle().getCalender();
            player.setPlayerInventory(saveLoadManager.getPlayerInventory());
        }
        camera = new Camera(player.getX(), player.getY());
        SharedData.setLoadingSave(false);
    }

    private void initRenderer() {
        renderer = new IsoRenderer(zoom, "main", chunkManager);
    }

    private void initAudio() {
        ambientSoundManager = new AmbientSoundManager(jukeBox, ambientSoundBox);
        ambientSoundManager.attachRenderer(renderer);
        setVolumes();
    }

    private void initDebugging() {
        if (soundDebug) {
            passiveMobSoundBox.setDebug(true);
            enemyMobSoundBox.setDebug(true);
            gameObjectSoundBox.setDebug(true);
        }
    }

    private void setVolumes(){
        Settings settings = Settings.getInstance();
        ambientSoundBox.setVolume(settings.getMainVolume()*settings.getAmbientVolume());
        jukeBox.setVolume(settings.getMainVolume()*settings.getMusicVolume());
        passiveMobSoundBox.setVolume(settings.getMainVolume()*settings.getFriendlyVolume());
        enemyMobSoundBox.setVolume(settings.getMainVolume()*settings.getEnemyVolume());
        gameObjectSoundBox.setVolume(settings.getMainVolume()*settings.getFriendlyVolume());
        player.setVolume(settings.getMainVolume()*settings.getPlayerVolume());
    }

    private void createNewPlayer(float x, float y, float speed, int health, List<Achievement> achievements) {
        player = new Player(x, y, speed, achievements);
        player.setHealth(health);
    }
}
