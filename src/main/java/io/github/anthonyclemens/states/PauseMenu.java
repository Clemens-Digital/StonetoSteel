package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
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
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.utils.SaveLoadManager;

public class PauseMenu extends BasicGameState {
    private final List<ImageTextButton> menuButtons = new ArrayList<>();
    private Banner titleBanner;
    private static final String TITLE_STRING = "Paused";
    private static final String MAIN_FONT = "MedievalTimes";
    private Image buttonImage;
    private Image bannerImage;
    private SaveLoadManager saveLoadManager = new SaveLoadManager();

    @Override
    public int getID() {
        return GameStates.PAUSE_MENU.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        // Load images and fonts
        buttonImage = new Image("textures/GUI/TextField/UI_Paper_Textfield_01.png", false, Image.FILTER_NEAREST);
        bannerImage = new Image("textures/GUI/TextField/UI_Paper_Banner_01_Downward.png", false, Image.FILTER_NEAREST);
        titleBanner = new Banner(bannerImage, TITLE_STRING, FontManager.getFont(MAIN_FONT, 48), TwoDimensionMath.getMiddleX(720, container.getWidth()), 10, 720, 251);
        titleBanner.changeYOffset(120f);

        // Create buttons
        float midX = (int) TwoDimensionMath.getMiddleX(342, container.getWidth());
        int y = 250;
        int step = 70;
        ImageTextButton saveButton = new ImageTextButton(buttonImage, "Save", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60); y += step;
        ImageTextButton loadButton = new ImageTextButton(buttonImage, "Load", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60); y += step;
        ImageTextButton videoButton = new ImageTextButton(buttonImage, "Video Settings", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60); y += step;
        ImageTextButton soundButton = new ImageTextButton(buttonImage, "Sound Settings", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60); y += step;
        ImageTextButton controlButton = new ImageTextButton(buttonImage, "Control Settings", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60); y += step;
        ImageTextButton exitButton = new ImageTextButton(buttonImage, "Save and Exit", FontManager.getFont(MAIN_FONT, 32), midX, y, 342, 60);
        ImageTextButton resumeButton = new ImageTextButton(buttonImage, "Resume", FontManager.getFont(MAIN_FONT, 40), 10, 10, 240, 80);

        menuButtons.clear();
        menuButtons.addAll(List.of(saveButton, loadButton, videoButton, soundButton, controlButton, exitButton, resumeButton));
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("PauseMenu Initialized");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        // Render the game state behind the menu
        SharedData.getGameState().render(container, game, g);
        // Draw a translucent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, container.getWidth(), container.getHeight());
        // Draw menu
        titleBanner.render(g);
        menuButtons.forEach(itb -> itb.render(g));
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        Input input = container.getInput();
        for (ImageTextButton itb : menuButtons) {
            itb.update(input);
            if (itb.isClicked()) {
                switch (itb.getText()) {
                    case "Save" -> SharedData.getGameState().saveGame(SharedData.getSaveFilePath());
                    case "Load" -> {
                        SharedData.setLoadingSave(true);
                        SharedData.setNewGame(false);
                        SharedData.enterState(GameStates.GAME, game);
                    }
                    case "Video Settings" -> SharedData.enterState(GameStates.VIDEO_SETTINGS, game);
                    case "Sound Settings" -> SharedData.enterState(GameStates.SOUND_SETTINGS, game);
                    case "Control Settings" -> SharedData.enterState(GameStates.CONTROL_SETTINGS, game);
                    case "Save and Exit" -> {
                        SharedData.getGameState().saveGame(SharedData.getSaveFilePath());
                        SharedData.setHotstart(false);
                        Game.stopAllSounds();
                        SharedData.enterState(GameStates.MAIN_MENU, game);
                    }
                    case "Resume" -> SharedData.enterState(GameStates.GAME, game);
                }
            }
        }
        // Resume game if ESC is pressed again
        if (input.isKeyPressed(Input.KEY_ESCAPE)) {
            SharedData.enterState(GameStates.GAME, game);
        }
        SteamAPI.runCallbacks();
    }
}
