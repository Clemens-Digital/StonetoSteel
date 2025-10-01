package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import com.codedisaster.steamworks.SteamAPI;

import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.Sound.JukeBox;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.utils.AssetLoader;

public class LoadingScreen extends BasicGameState {

    private Image loadingImage;
    private static final int BAR_WIDTH = 600;
    private static final int BAR_HEIGHT = 64;
    private static final String MAIN_FONT = "Roboto";
    private static final List<String> loadingTips = List.of(
        "Not everything is harvestable... but most things are breakable.",
        "Mobs can die-listen for sound cues and watch their health.",
        "Tools matter: try different ones for different results.",
        "Death isn't the end-some objects drop useful loot.",
        "Exploration gets easier when you move chunk by chunk.",
        "The time of day affects how some things behave.",
        "Saving works best when you're standing still in a quiet zone.",
        "Fire spreads fast - keep it contained, or lose more than you think.",
        "Low on food? Fishing might save you when crops won't grow.",
        "Carry a backup tool - things break at the worst times.",
        "Sometimes it's safer to watch than to fight.",
        "Abandoned structures might hide both loot and danger.",
        "Inventory space is survival - don’t carry what you can’t use.",
        "Nightfall comes quicker than you think; watch the shadows stretch.",
        "Gather wood early; nights are better spent inside.",
        "A roof is more than shelter; it’s a shield from unwanted eyes.",
        "Zombies slow in water… most of the time.",
        "Scorpions strike fast - keep your distance until they lunge.",
        "Skeletons can't hit what they can't see - use cover wisely.",
        "Work in daylight, defend in darkness."
    );
    private static TrueTypeFont mainFont;

    private String currentTip;
    private int tipTimer = 0;
    private static final int TIP_INTERVAL = 5000;
    private boolean waitingForSpace = false;
    private boolean doneLoading = false;
    private int frameCount = 0;
    private int completedSteps;
    private int totalSteps = 0;
    private final Queue<Runnable> loadingSteps = new LinkedList<>();

    private String soundPack;
    private String texturePack;
    private String lastSoundPack;
    private String lastTexturePack;

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("Loading Screen entered.");
        mainFont = FontManager.getFont(MAIN_FONT, 48);
        currentTip = loadingTips.get(new Random().nextInt(loadingTips.size()));
        if(!Settings.getInstance().getSoundPack().equals(lastSoundPack)){
            Log.debug("Sound pack changed from " + lastSoundPack + " to " + Settings.getInstance().getSoundPack());
            soundPack = Settings.getInstance().getSoundPack();
            lastSoundPack = soundPack;
            reset(container, game);
        }else if(!Settings.getInstance().getTexturePack().equals(lastTexturePack)){
            Log.debug("Texture pack changed from " + lastTexturePack + " to " + Settings.getInstance().getTexturePack());
            texturePack = Settings.getInstance().getTexturePack();
            lastTexturePack = texturePack;
            reset(container, game);
        }else{
            Log.debug("Sound pack and texture pack unchanged.");
            if(doneLoading){
                Log.debug("Already loaded, skipping loading steps.");
                SharedData.enterState(GameStates.GAME, game);
            }
        }
    }

    private void reset(GameContainer container, StateBasedGame game) throws SlickException {
        loadingSteps.clear();
        totalSteps = 0;
        init(container, game);
        completedSteps = 0;
        doneLoading=false;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        texturePack = Settings.getInstance().getTexturePack();
        soundPack = Settings.getInstance().getSoundPack();
        loadingImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "loadingImage"));
        loadingSteps.add(() -> Game.jukeBox = new JukeBox());
        loadingSteps.add(() -> Game.jukeBox.addSongs("dayMusic", AssetLoader.loadListFromFile(soundPack, "dayMusic")));
        loadingSteps.add(() -> Game.jukeBox.addSongs("nightMusic", AssetLoader.loadListFromFile(soundPack, "nightMusic")));
        loadingSteps.add(() -> Game.ambientSoundBox = new SoundBox());
        loadingSteps.add(() -> Game.ambientSoundBox.addSounds("plainsSounds", AssetLoader.loadListFromFile(soundPack, "plainsSounds")));
        loadingSteps.add(() -> Game.ambientSoundBox.addSounds("desertSounds", AssetLoader.loadListFromFile(soundPack, "desertSounds")));
        loadingSteps.add(() -> Game.ambientSoundBox.addSounds("waterSounds", AssetLoader.loadListFromFile(soundPack, "waterSounds")));
        loadingSteps.add(() -> Game.ambientSoundBox.addSounds("beachSounds", AssetLoader.loadListFromFile(soundPack, "beachSounds")));
        loadingSteps.add(() -> Game.ambientSoundBox.addSounds("nightSounds", AssetLoader.loadListFromFile(soundPack, "nightSounds")));
        loadingSteps.add(() -> Game.passiveMobSoundBox = new SoundBox());
        loadingSteps.add(() -> Game.passiveMobSoundBox.addSounds("fishHurt", AssetLoader.loadListFromFile(soundPack, "fishHurtSounds")));
        loadingSteps.add(() -> Game.enemyMobSoundBox = new SoundBox());
        loadingSteps.add(() -> Game.gameObjectSoundBox = new SoundBox());
        loadingSteps.add(() -> Game.gameObjectSoundBox.addSounds("bigTreeHitSounds", AssetLoader.loadListFromFile(soundPack, "bigTreeHitSounds")));
        loadingSteps.add(() -> Game.gameObjectSoundBox.addSounds("smallTreeHitSounds", AssetLoader.loadListFromFile(soundPack, "smallTreeHitSounds")));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("main", AssetLoader.loadSingleAssetFromFile(texturePack, "mainTiles"), 18, 18));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("fishes", AssetLoader.loadSingleAssetFromFile(texturePack, "fishes"), 16, 16));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("bigtrees", AssetLoader.loadSingleAssetFromFile(texturePack, "bigTrees"), 48, 48));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("smalltrees", AssetLoader.loadSingleAssetFromFile(texturePack, "smallTrees"), 16, 32));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("grass", AssetLoader.loadSingleAssetFromFile(texturePack, "grass"), 16, 16));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("specialtrees", AssetLoader.loadSingleAssetFromFile(texturePack, "specialTrees"), 56, 56));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("zombies", AssetLoader.loadSingleAssetFromFile(texturePack, "zombies"), 16, 16));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("spiders", AssetLoader.loadSingleAssetFromFile(texturePack, "spiders"), 16, 16));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("items", AssetLoader.loadSingleAssetFromFile(texturePack, "items"), 16, 16));
        loadingSteps.add(() -> SpriteManager.addSpriteSheet("player", AssetLoader.loadSingleAssetFromFile(texturePack, "player"), 16, 17));
        totalSteps = loadingSteps.size(); // Save initial size
        Log.debug("LoadingScreen Initialized");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        RenderUtils.drawBackground(loadingImage,container);
        // Dimensions and position
        int barX = (container.getWidth() - BAR_WIDTH) / 2;
        int barY = container.getHeight() - 80;
        // Colors and progress
        g.setColor(Color.darkGray);
        g.fillRect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
        float percent = (float) completedSteps / totalSteps;
        int filledWidth = (int) (BAR_WIDTH * percent);
        g.setColor(Color.green);
        g.fillRect(barX, barY, filledWidth, BAR_HEIGHT);
        String loadingText = "Loading " + (int)(percent * 100) + "%";
        if (waitingForSpace) loadingText = "Press SPACE to begin your journey...";
        float textX = TwoDimensionMath.getMiddleX(mainFont.getWidth(loadingText), container.getWidth());
        float textY = barY - 64f;
        mainFont.drawString(textX, textY, loadingText, Color.black);
        List<String> wrappedTip = wrapText(mainFont, currentTip, container.getWidth() - 40);
        float startY = 32;
        for (int i = 0; i < wrappedTip.size(); i++) {
            String line = wrappedTip.get(i);
            float lineX = TwoDimensionMath.getMiddleX(mainFont.getWidth(line), container.getWidth());
            mainFont.drawString(lineX, startY + i * mainFont.getLineHeight(), line, Color.black);
        }
        frameCount++;
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if (!doneLoading && frameCount % 2 == 0 && !loadingSteps.isEmpty()) {
            loadingSteps.poll().run();
            completedSteps++;
        } else if (!doneLoading && loadingSteps.isEmpty()) {
            Log.debug("All resources loaded. Waiting for SPACE press...");
            doneLoading = true;
            waitingForSpace = true;
            render(container, game, container.getGraphics());
        }
        if (waitingForSpace && container.getInput().isKeyPressed(Input.KEY_SPACE)) {
            SharedData.enterState(GameStates.GAME, game);
            waitingForSpace = false;
        }
        // Tip rotation logic
        tipTimer += delta;
        if (tipTimer >= TIP_INTERVAL) {
            tipTimer = 0;
            // Pick a new tip that is different from the current one
            String newTip;
            do {
                newTip = loadingTips.get(new Random().nextInt(loadingTips.size()));
            } while (newTip.equals(currentTip));
            currentTip = newTip;
        }

        SteamAPI.runCallbacks();
    }

    @Override
    public int getID() {
        return GameStates.LOADING_SCREEN.getID();
    }

    private List<String> wrapText(TrueTypeFont font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            if (font.getWidth(testLine) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
