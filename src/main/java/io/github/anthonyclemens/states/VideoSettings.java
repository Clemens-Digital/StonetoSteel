package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
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
import io.github.anthonyclemens.GUI.Buttons.Carousel;
import io.github.anthonyclemens.GUI.Buttons.ImageTextButton;
import io.github.anthonyclemens.GUI.Buttons.ToggleButton;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Main;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.SharedData;

public class VideoSettings extends BasicGameState{
    private List<String> validResolutions;
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();
    private final List<ToggleButton> toggleButtons = new ArrayList<>();
    private final List<Carousel> carousels = new ArrayList<>();
    private final List<Banner> bgBanners = new ArrayList<>();
    private static final String TITLE_STRING = "Video Settings";
    private static final String MAIN_FONT = "MedievalTimes";

    @Override
    public int getID() {
        return GameStates.VIDEO_SETTINGS.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        Settings settings = Settings.getInstance();
        // Set background image
        backgroundImage = new Image("textures/Background.png");
        // Create title banner
        Image bannerImage = new Image("textures/GUI/TextField/UI_Paper_Banner_01_Downward.png");
        bannerImage.setFilter(Image.FILTER_NEAREST);
        titleBanner = new Banner(bannerImage, TITLE_STRING, FontManager.getFont(MAIN_FONT, 48), TwoDimensionMath.getMiddleX(720, container.getWidth()), 10, 720, 251);
        titleBanner.changeYOffset(120f);
        // Load button images
        Image buttonImage = new Image("textures/GUI/TextField/UI_Paper_Textfield_01.png");
        buttonImage.setFilter(Image.FILTER_NEAREST);
        // Load carousel button textures
        Image leftCarouselButton = new Image("textures/GUI/Carousel/leftarrow.png");
        Image rightCarouselButton = new Image("textures/GUI/Carousel/rightarrow.png");
        // Load the background banners
        Image backBanner = new Image("textures/GUI/TextField/UI_Paper_Button_Large_Lock_01a1.png");
        backBanner.setFilter(Image.FILTER_NEAREST);
        // Create menu buttons
        ImageTextButton backButton = new ImageTextButton(buttonImage, "Back", FontManager.getFont(MAIN_FONT, 40), 10, 10, 240, 80);
        ImageTextButton applyButton = new ImageTextButton(buttonImage, "Apply", FontManager.getFont(MAIN_FONT, 24), TwoDimensionMath.getMiddleX(180, container.getWidth()), 564, 180, 64);
        // Create the Carousels and associated borders
        Banner resBanner = new Banner(backBanner, "Res", FontManager.getFont(MAIN_FONT, 32),TwoDimensionMath.getMiddleX(300, container.getWidth())-150, 240, 300, 100);
        resBanner.changeXOffset(32);
        Carousel resolution = new Carousel.Builder()
            .data(validResolutions)
            .font(FontManager.getFont(MAIN_FONT, 28))
            .defaultValue(container.getWidth()+"x"+container.getHeight())
            .leftImage(leftCarouselButton)
            .rightImage(rightCarouselButton)
            .position(TwoDimensionMath.getMiddleX(300, container.getWidth())-70, 272)
            .build();
        Banner refreshRateBanner = new Banner(backBanner, "FPS", FontManager.getFont(MAIN_FONT, 32), TwoDimensionMath.getMiddleX(300, container.getWidth())+150, 240, 300, 100);
        refreshRateBanner.changeXOffset(32);
        Carousel refreshRate = new Carousel.Builder()
            .data(new ArrayList<>(Arrays.asList("60","90","120","144","165","240")))
            .font(FontManager.getFont(MAIN_FONT, 28))
            .defaultValue(String.valueOf(settings.getMaxFPS()))
            .leftImage(leftCarouselButton)
            .rightImage(rightCarouselButton)
            .position(TwoDimensionMath.getMiddleX(300, container.getWidth())+300, 272)
            .build();
        // Create the Toggle buttons and associated borders
        Image enabled = new Image("textures/GUI/ToggleButton/checkmark.png");
        Image disabled = new Image("textures/GUI/ToggleButton/cross.png");
        Banner vsyncBanner = new Banner(backBanner, "VSync", FontManager.getFont(MAIN_FONT, 32),TwoDimensionMath.getMiddleX(300, container.getWidth())-150, 400, 300, 100);
        vsyncBanner.changeXOffset(32);
        ToggleButton vsyncButton = new ToggleButton(enabled, disabled, TwoDimensionMath.getMiddleX(64, container.getWidth())-64, 418, 64, 64);
        vsyncButton.setValue(settings.isVsync());

        Banner fullscreenBanner = new Banner(backBanner, "Fullscreen", FontManager.getFont(MAIN_FONT, 32),TwoDimensionMath.getMiddleX(300, container.getWidth())+150, 400, 300, 100);
        fullscreenBanner.changeXOffset(32);
        ToggleButton fullscreenButton = new ToggleButton(enabled, disabled, TwoDimensionMath.getMiddleX(64, container.getWidth())+240, 418, 64, 64);
        fullscreenButton.setValue(settings.isFullscreen());

        menuButtons.clear();
        menuButtons.addAll(List.of(backButton,applyButton));
        carousels.clear();
        carousels.addAll(List.of(resolution,refreshRate));
        bgBanners.clear();
        bgBanners.addAll(List.of(resBanner,refreshRateBanner,vsyncBanner,fullscreenBanner));
        toggleButtons.clear();
        toggleButtons.addAll(List.of(vsyncButton,fullscreenButton));
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        //Get Valid Screen Resolutions
        Set<String> validRes = new LinkedHashSet<>();
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            Arrays.sort(modes, Comparator.comparingInt(mode -> mode.getWidth() * mode.getHeight()));
            for(DisplayMode m : modes){
                if(m.getHeight()>=768){
                    validRes.add(m.getWidth()+"x"+m.getHeight());
                }
            }
            validResolutions = new ArrayList<>(validRes);
        } catch (LWJGLException e) {
            Log.error("Cannot get display modes");
        }
        Log.debug("VideoSettings Initialized");
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
        List.of(menuButtons, bgBanners, carousels, toggleButtons).forEach(list -> list.forEach(item -> item.render(g)));
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        for(Carousel c : carousels){ c.update(input); }
        for(ToggleButton tb : toggleButtons){ tb.update(input); }
        for(ImageTextButton itb : menuButtons){
            itb.update(input); // Sets the isClicked bool
            if(itb.isClicked()){
                switch(itb.getText()){ // Figure out what button was pressed
                    case "Apply"->{
                        applySettings(container);
                        game.enterState(this.getID());
                    }
                    case "Back"-> {
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

    private void applySettings(GameContainer container) throws SlickException{
        Settings settings = Settings.getInstance();
        Log.debug("\nApplying settings:");
        String resolution = carousels.get(0).getValue();
        String refreshRate = carousels.get(1).getValue();
        Boolean vsyncBool = toggleButtons.get(0).getValue();
        Boolean fullscreenBool = toggleButtons.get(1).getValue();
        Log.debug("Resolution: "+resolution);
        Log.debug("Max FPS: "+refreshRate);
        Log.debug("vsync? "+vsyncBool);
        Log.debug("Fullscreen? "+fullscreenBool);
        settings.setResolution(resolution);
        settings.setMaxFPS(Integer.parseInt(refreshRate));
        settings.setVsync(vsyncBool);
        settings.setFullscreen(fullscreenBool);
        settings.applyToGameContainer(container);
        Main.setSettings(settings);
    }
}
