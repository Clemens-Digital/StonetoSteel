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
import io.github.anthonyclemens.Sound.JukeBox;
import io.github.anthonyclemens.utils.AssetLoader;

public class MainMenu extends BasicGameState{
    //Variables
    private Input input;
    private String texturePack;
    private String soundPack;

    //Constants
    private static final String TITLE_STRING = "Stone to Steel";
    private static final String MAIN_FONT = "MedievalTimes";
    public static JukeBox menuJukeBox;
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();

    @Override
    public int getID() {
        return GameStates.MAIN_MENU.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        input = container.getInput();
        Settings settings = Settings.getInstance();
        texturePack = settings.getTexturePack();
        soundPack = settings.getSoundPack();
        // Load music and play menu music
        menuJukeBox = new JukeBox();
        menuJukeBox.addSongs("menu", AssetLoader.loadListFromFile(soundPack, "menuMusic"));
        menuJukeBox.playRandomSong("menu");
        menuJukeBox.setVolume(settings.getMainVolume()*settings.getMusicVolume());
        // Set background image
        backgroundImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "backgroundImage"), false, Image.FILTER_NEAREST);
        // Create title banner
        Image bannerImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "bannerImage"), false, Image.FILTER_NEAREST);
        titleBanner = new Banner(bannerImage, TITLE_STRING, FontManager.getFont(MAIN_FONT, 60), TwoDimensionMath.getMiddleX(792, container.getWidth()), 10, 820, 280);
        titleBanner.changeYOffset(120f);
        // Load button images
        Image buttonImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "regularButton"), false, Image.FILTER_NEAREST);
        // Create menu buttons
        int startY = 260;
        ImageTextButton startGame = new ImageTextButton(buttonImage, "Singleplayer", FontManager.getFont(MAIN_FONT, 36), TwoDimensionMath.getMiddleX(312, container.getWidth()), startY, 312, 104);
        ImageTextButton options = new ImageTextButton(buttonImage, "Options", FontManager.getFont(MAIN_FONT, 32), TwoDimensionMath.getMiddleX(248, container.getWidth()), startY+140f, 248, 82);
        ImageTextButton exit = new ImageTextButton(buttonImage, "Exit", FontManager.getFont(MAIN_FONT, 32), TwoDimensionMath.getMiddleX(194, container.getWidth()), startY+240f, 194, 64);
        ImageTextButton credits = new ImageTextButton(buttonImage, "Credits", FontManager.getFont(MAIN_FONT, 24), TwoDimensionMath.getMiddleX(168, container.getWidth()), startY+320f, 168, 56);
        menuButtons.clear();
        menuButtons.addAll(List.of(startGame,options,exit,credits));
        Image cursor = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "mainMenuCursor"), false, Image.FILTER_NEAREST);
        container.setMouseCursor(cursor, 0, 0);
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("MainMenu Initialized");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        RenderUtils.drawBackground(backgroundImage,container); // Render the background to fit screen (no stretching)
        titleBanner.render(g); // Render the Title banner
        for(ImageTextButton itb : menuButtons){ // Render all of the buttons
            itb.render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        for(ImageTextButton itb : menuButtons){
            itb.update(input); // Sets the isClicked bool
            if(itb.isClicked()){
                switch(itb.getText()){ // Figure out what button was pressed
                    case "Singleplayer" -> SharedData.enterState(GameStates.NEW_GAME,game);
                    case "Options"-> SharedData.enterState(GameStates.SETTINGS_MENU, game);
                    case "Exit"-> {
                        menuJukeBox.stopMusic();
                        menuJukeBox.clear();
                        container.exit();
                    }
                    case "Credits"-> SharedData.enterState(GameStates.CREDITS, game);
                }
            }
        }
        SteamAPI.runCallbacks();
    }
}
