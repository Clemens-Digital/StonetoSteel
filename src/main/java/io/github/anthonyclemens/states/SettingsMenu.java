package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import com.codedisaster.steamworks.SteamAPI;

import io.github.anthonyclemens.GUI.Banner;
import io.github.anthonyclemens.GUI.Buttons.ImageTextButton;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.utils.AssetLoader;

public class SettingsMenu extends BasicGameState{
    //Variables
    private Input input;
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();
    private Settings settings;
    private String texturePack;
    private String soundPack;

    //Constants
    private static final String TITLE_STRING = "Options";
    private static final String MAIN_FONT = "MedievalTimes";
    private static final int MAIN_FONT_SIZE = 48;
    private static final int BUTTON_FONT_SIZE = 32;
    private static final int BUTTON_WIDTH = 342;
    private static final int BUTTON_HEIGHT = 114;

    @Override
    public int getID() {
        return GameStates.SETTINGS_MENU.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        input = container.getInput();
        settings = Settings.getInstance();
        texturePack = settings.getTexturePack();
        soundPack = settings.getSoundPack();
        // Set background image
        backgroundImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "backgroundImage"));
        // Create title banner
        Image bannerImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "bannerImage"), false, Image.FILTER_NEAREST);
        titleBanner = new Banner(bannerImage, TITLE_STRING, FontManager.getFont(MAIN_FONT, MAIN_FONT_SIZE), TwoDimensionMath.getMiddleX(720, container.getWidth()), 10, 720, 251);
        titleBanner.changeYOffset(120f);
        // Load button images
        Image buttonImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "regularButton"), false, Image.FILTER_NEAREST);
        // Create menu buttons
        ImageTextButton videoSettings = new ImageTextButton(buttonImage, "Video Settings", FontManager.getFont(MAIN_FONT, BUTTON_FONT_SIZE), TwoDimensionMath.getMiddleX(BUTTON_WIDTH, container.getWidth()), 300, BUTTON_WIDTH, BUTTON_HEIGHT);
        ImageTextButton soundSettings = new ImageTextButton(buttonImage, "Sound Settings", FontManager.getFont(MAIN_FONT, BUTTON_FONT_SIZE), TwoDimensionMath.getMiddleX(BUTTON_WIDTH, container.getWidth()), 450, BUTTON_WIDTH, BUTTON_HEIGHT);
        ImageTextButton controlSettings = new ImageTextButton(buttonImage, "Control Settings", FontManager.getFont(MAIN_FONT, BUTTON_FONT_SIZE), TwoDimensionMath.getMiddleX(BUTTON_WIDTH, container.getWidth()), 600, BUTTON_WIDTH, BUTTON_HEIGHT);
        // Navigation Buttons
        ImageTextButton backButton = new ImageTextButton(buttonImage, "Back", FontManager.getFont(MAIN_FONT, BUTTON_FONT_SIZE), 10, 10, 240, 80);
        ImageTextButton resetButton = new ImageTextButton(buttonImage, "Reset Settings", FontManager.getFont(MAIN_FONT, BUTTON_FONT_SIZE), container.getWidth()-250f, 10, 240, 80);
        menuButtons.clear();
        menuButtons.addAll(List.of(videoSettings,soundSettings,controlSettings,backButton,resetButton));
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("SettingsMenu initialized.");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        if(SharedData.getLastState() == GameStates.PAUSE_MENU){
            // Render the game state behind the menu
            SharedData.getGameState().render(container, game, g);
            // Draw a translucent overlay
            g.setColor(new org.newdawn.slick.Color(0, 0, 0, 180));
            g.fillRect(0, 0, container.getWidth(), container.getHeight());
        } else {
            RenderUtils.drawBackground(backgroundImage,container);
        }
        
        titleBanner.render(g);
        for(ImageTextButton itb : menuButtons){
            itb.render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        for(ImageTextButton itb : menuButtons){
            itb.update(input); // Sets the isClicked bool
            if(itb.isClicked()){
                switch(itb.getText()){ // Figure out what button was pressed
                    case "Video Settings"-> SharedData.enterState(GameStates.VIDEO_SETTINGS,game);
                    case "Sound Settings"-> SharedData.enterState(GameStates.SOUND_SETTINGS,game);
                    case "Control Settings"-> SharedData.enterState(GameStates.CONTROL_SETTINGS,game);
                    case "Back"-> SharedData.enterState(GameStates.MAIN_MENU, game);
                    case "Reset Settings" -> {
                        settings.writeDefaultOptions();
                        Log.info("Settings reset to default.");
                        settings.applyToGameContainer(container);
                    }
                }
            }
        }
        SteamAPI.runCallbacks();
    }

}
