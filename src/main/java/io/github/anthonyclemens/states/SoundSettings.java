package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.List;

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

import io.github.anthonyclemens.GUI.Banner;
import io.github.anthonyclemens.GUI.Buttons.ImageTextButton;
import io.github.anthonyclemens.GUI.Slider;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.SharedData;

public class SoundSettings extends BasicGameState{
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();
    private final List<Slider> sliders = new ArrayList<>();
    private final List<Banner> banners = new ArrayList<>();

    private static final String TITLE_STRING = "Sound Settings";
    private static final String MAIN_FONT = "MedievalTimes";
    private static final int SLIDER_WIDTH = 280;
    private static final int SLIDER_X_OFFSET = 138;
    private static final int BANNER_LABEL_OFFSET = 36;
    private static final int SLIDER_BANNER_HEIGHT = 76;

    @Override
    public int getID() {
        return GameStates.SOUND_SETTINGS.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException{
        Settings settings = Settings.getInstance();
        // Set background image
        backgroundImage = new Image("textures/Background.png");
        // Create title banner
        Image bannerImage = new Image("textures/GUI/TextField/UI_Paper_Banner_01_Downward.png", false, Image.FILTER_NEAREST);
        titleBanner = new Banner(bannerImage, TITLE_STRING, FontManager.getFont(MAIN_FONT, 48), TwoDimensionMath.getMiddleX(720, container.getWidth()), 10, 720, 251);
        titleBanner.changeYOffset(120f);
        // Load button images
        Image buttonImage = new Image("textures/GUI/TextField/UI_Paper_Textfield_01.png", false, Image.FILTER_NEAREST);
        // Create menu buttons
        ImageTextButton backButton = new ImageTextButton(buttonImage, "Back", FontManager.getFont(MAIN_FONT, 40), 10, 10, 240, 80);
        ImageTextButton applyButton = new ImageTextButton(buttonImage, "Apply", FontManager.getFont(MAIN_FONT, 24), TwoDimensionMath.getMiddleX(180, container.getWidth()), 564, 180, 64);
        //Create Sliders and their Banners
        Image sliderNub = new Image("textures/GUI/Slider/UI_Paper_Scroll_Bar.png", false, Image.FILTER_NEAREST);
        Image sliderBanner = new Image("textures/GUI/TextField/UI_Paper_Button_Large_Lock_02a1.png", false, Image.FILTER_NEAREST);
        TrueTypeFont sliderFont = FontManager.getFont(MAIN_FONT, 26);
        //Left side
        Banner mainBanner = createBanner("Main", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) - 260, 240, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider mainSlider = createSlider(sliderNub, Color.black, sliderFont, mainBanner.getX() + SLIDER_X_OFFSET, mainBanner.getY() + mainBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getMainVolume());

        Banner musicBanner = createBanner("Music", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) - 260, 340, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider musicSlider = createSlider(sliderNub, Color.black, sliderFont, musicBanner.getX() + SLIDER_X_OFFSET, musicBanner.getY() + musicBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getMusicVolume());

        Banner ambientBanner = createBanner("Ambient", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) - 260, 440, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider ambientSlider = createSlider(sliderNub, Color.black, sliderFont, ambientBanner.getX() + SLIDER_X_OFFSET, ambientBanner.getY() + ambientBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getAmbientVolume());

        Banner playerBanner = createBanner("Player", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) + 260, 240, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider playerSlider = createSlider(sliderNub, Color.black, sliderFont, playerBanner.getX() + SLIDER_X_OFFSET, playerBanner.getY() + playerBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getPlayerVolume());

        Banner friendlyBanner = createBanner("Friendly", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) + 260, 340, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider friendlySlider = createSlider(sliderNub, Color.black, sliderFont, friendlyBanner.getX() + SLIDER_X_OFFSET, friendlyBanner.getY() + friendlyBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getFriendlyVolume());

        Banner enemyBanner = createBanner("Enemy", sliderFont, sliderBanner, (int)TwoDimensionMath.getMiddleX(520, container.getWidth()) + 260, 440, 520, SLIDER_BANNER_HEIGHT, BANNER_LABEL_OFFSET);
        Slider enemySlider = createSlider(sliderNub, Color.black, sliderFont, enemyBanner.getX() + SLIDER_X_OFFSET, enemyBanner.getY() + enemyBanner.getHeight() / 2 - 5, SLIDER_WIDTH, settings.getEnemyVolume());

        banners.clear();
        banners.addAll(List.of(mainBanner, musicBanner, ambientBanner, playerBanner, friendlyBanner, enemyBanner));
        sliders.clear();
        sliders.addAll(List.of(mainSlider, musicSlider, ambientSlider, playerSlider, friendlySlider, enemySlider));
        menuButtons.clear();
        menuButtons.addAll(List.of(backButton,applyButton));
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("SoundSettings Initialized");
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
        titleBanner.render(g); // Render the Title banner
        for(Banner b : banners){ b.render(g); }
        for(ImageTextButton itb : menuButtons){ itb.render(g); }
        for(Slider s : sliders){ s.render(g); }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        for(Slider s : sliders){ s.update(input); }
        for(ImageTextButton itb : menuButtons){
            itb.update(input); // Sets the isClicked bool
            if(itb.isClicked()){
                switch(itb.getText()){ // Figure out what button was pressed
                    case "Apply"->applySoundSettings();
                    case "Back"->{
                        if(SharedData.getLastState() == GameStates.PAUSE_MENU) {
                            SharedData.enterState(GameStates.PAUSE_MENU, game);
                        } else {
                            SharedData.enterState(GameStates.SETTINGS_MENU, game);
                        }
                    }
                }
            }
        }
        SteamAPI.runCallbacks();
    }

    private void applySoundSettings() {
        Settings settings = Settings.getInstance();
        Log.info("Applying Sound Settings...");
        float mainVol = sliders.get(0).getValue();
        float musicVol = sliders.get(1).getValue();
        float ambientVol = sliders.get(2).getValue();
        float playerVol = sliders.get(3).getValue();
        float friendlyVol = sliders.get(4).getValue();
        float enemyVol = sliders.get(5).getValue();
        Log.debug("Main volume: "+mainVol);
        Log.debug("Music volume: "+musicVol);
        Log.debug("Ambient volume: "+ambientVol);
        Log.debug("Player volume: "+playerVol);
        Log.debug("Friendly volume: "+friendlyVol);
        Log.debug("Enemy volume: "+enemyVol);
        settings.setMainVolume(mainVol);
        settings.setMusicVolume(musicVol);
        settings.setAmbientVolume(ambientVol);
        settings.setPlayerVolume(playerVol);
        settings.setFriendlyVolume(friendlyVol);
        settings.setEnemyVolume(enemyVol);
        MainMenu.menuJukeBox.setVolume(Math.min(musicVol,mainVol));
    }

    private Banner createBanner(String label, TrueTypeFont font, Image image, int x, int y, int width, int height, int labelOffsetX) {
        Banner banner = new Banner(image, label, font, x, y, width, height);
        banner.changeXOffset(labelOffsetX);
        return banner;
    }

    private Slider createSlider(Image image, Color color, TrueTypeFont font, float x, float y, int width, float initialValue) {
        Slider slider = new Slider(image, color, font, x, y, width, 10);
        slider.setValue(initialValue);
        return slider;
    }
}
