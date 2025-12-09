package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import com.codedisaster.steamworks.SteamAPI;

import io.github.anthonyclemens.GUI.Banner;
import io.github.anthonyclemens.GUI.Buttons.Button;
import io.github.anthonyclemens.GUI.Buttons.ImageTextButton;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.SharedData;

public class ControlSettings extends BasicGameState{
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();

    private static final String TITLE_STRING = "Controls";
    private static final String MAIN_FONT = "MedievalTimes";

    @Override
    public int getID() {
        return GameStates.CONTROL_SETTINGS.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
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
        // Create menu buttons
        ImageTextButton backButton = new ImageTextButton(buttonImage, "Back", FontManager.getFont(MAIN_FONT, 40), 10, 10, 240, 80);
        menuButtons.clear();
        menuButtons.addAll(List.of(backButton));
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("ControlSettings Initialized");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        if(SharedData.getLastState() == GameStates.PAUSE_MENU){
            // Render the game state behind the menu
            SharedData.getGameState().render(container, game, g);
            // Draw a translucent overlay
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, container.getWidth(), container.getHeight());
        } else {
            RenderUtils.drawBackground(backgroundImage,container);
        }
        titleBanner.render(g); // Render the Title banner
        for(ImageTextButton itb : menuButtons){
            itb.render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        SteamAPI.runCallbacks();
        /*for(ImageTextButton itb : menuButtons){
            itb.update(container.getInput()); // Sets the isClicked bool
            if(itb.isClicked()){
                switch(itb.getText()){ // Figure out what button was pressed
                    case "Back"-> {
                        if(SharedData.getLastState() == GameStates.PAUSE_MENU) {
                            SharedData.enterState(GameStates.PAUSE_MENU, game);
                        } else {
                            SharedData.enterState(GameStates.SETTINGS_MENU, game);
                        }
                    }
                }
            }
        }*/
        menuButtons.stream().forEach(itb -> itb.update(container.getInput()));
        ImageTextButton clickedItb = menuButtons.stream()
            .filter(Button::isClicked)
            .findFirst()
            .orElse(null);
        if(clickedItb==null) return;
        switch(clickedItb.getText()){
            case "Back"-> {
                if(SharedData.getLastState() == GameStates.PAUSE_MENU) {
                    SharedData.enterState(GameStates.PAUSE_MENU, game);
                } else {
                    SharedData.enterState(GameStates.SETTINGS_MENU, game);
                }
            }
            default -> {}
        }
    }
}
