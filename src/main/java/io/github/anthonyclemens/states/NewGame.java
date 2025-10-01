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
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.utils.SaveLoadManager;

public class NewGame extends BasicGameState {
    // Variables
    private Input input;
    private Image backgroundImage;
    private Banner titleBanner;
    private final List<ImageTextButton> menuButtons = new ArrayList<>();
    private ImageTextButton backButton;
    private ImageTextButton cancelButton;
    private ImageTextButton deleteWorldButton;
    private boolean delete = false;

    // Constants
    private static final String MAIN_FONT = "MedievalTimes";
    private static final String TITLE_STRING = "World Selection";
    private static final String EMPTY = "- empty -";
    private static final String[] SAVES = {
        "saves/save1",
        "saves/save2",
        "saves/save3",
        "saves/save4",
        "saves/save5"
    };

    @Override
    public int getID() {
        return GameStates.NEW_GAME.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        input = container.getInput();
        // Set background image
        backgroundImage = new Image("textures/Background.png");
        // Create title banner
        Image bannerImage = new Image("textures/GUI/TextField/UI_Paper_Banner_01_Downward.png", false, Image.FILTER_NEAREST);
        titleBanner = new Banner(
            bannerImage,
            TITLE_STRING,
            FontManager.getFont(MAIN_FONT, 48),
            TwoDimensionMath.getMiddleX(720, container.getWidth()),
            10,
            720,
            251
        );
        titleBanner.changeYOffset(120f);
        // Load button images
        Image buttonImage = new Image("textures/GUI/TextField/UI_Paper_Textfield_01.png", false, Image.FILTER_NEAREST);

        // Create menu buttons
        backButton = new ImageTextButton(
            buttonImage, "Back", FontManager.getFont(MAIN_FONT, 40),
            10, 10, 240, 80
        );
        cancelButton = new ImageTextButton(
            buttonImage, "Cancel", FontManager.getFont(MAIN_FONT, 40),
            10, 10, 240, 80
        );
        cancelButton.setRender(false);
        deleteWorldButton = new ImageTextButton(
            buttonImage, "Delete World", FontManager.getFont(MAIN_FONT, 32),
            TwoDimensionMath.getMiddleX(342, container.getWidth()), container.getHeight()-120f, 342, 60
        );
        ImageTextButton[] slots = new ImageTextButton[5];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ImageTextButton(
            buttonImage, "", FontManager.getFont(MAIN_FONT, 32),
            TwoDimensionMath.getMiddleX(342, container.getWidth()), 220 + i * 80, 342, 60
            );
            slots[i].setName("Slot" + (i + 1));
        }
        menuButtons.clear();
        menuButtons.addAll(List.of(slots[0],slots[1],slots[2],slots[3],slots[4],backButton,deleteWorldButton,cancelButton));
        initButtons();
    }

    private void initButtons(){
        for(int i = 0; i < SAVES.length; i++){
            menuButtons.get(i).setText((!SaveLoadManager.getSize(SAVES[i]).equals("0")) ? "World "+(i+1)+" (" + SaveLoadManager.getSize(SAVES[i]) +")" : EMPTY);
        }
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("NewGame Initialized");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        RenderUtils.drawBackground(backgroundImage, container);
        titleBanner.render(g);
        for (ImageTextButton itb : menuButtons) {
            itb.render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if(delete){
            updateDeleteMenu();
            return;
        }
        updateMenu(game);
        SteamAPI.runCallbacks();
    }

    private void updateDeleteMenu(){
        for (ImageTextButton button : menuButtons) {
                button.update(input);
                if (button.isClicked()) {
                    String name = button.getName();
                    if (name.startsWith("Slot")) {
                        switch (name) {
                            case "Slot1" -> SaveLoadManager.deleteSave(SAVES[0]);
                            case "Slot2" -> SaveLoadManager.deleteSave(SAVES[1]);
                            case "Slot3" -> SaveLoadManager.deleteSave(SAVES[2]);
                            case "Slot4" -> SaveLoadManager.deleteSave(SAVES[3]);
                            case "Slot5" -> SaveLoadManager.deleteSave(SAVES[4]);
                        }
                        delete = false;
                        initButtons();
                        return;
                    } else if (name.equals("Cancel")) {
                        delete = false;
                        titleBanner.setText(TITLE_STRING);
                        backButton.setRender(true);
                        deleteWorldButton.setRender(true);
                        cancelButton.setRender(false);
                        Log.debug("Exiting world delete mode.");
                        return;
                    }
                }
            }
    }

    private void updateMenu(StateBasedGame game){
        for (ImageTextButton button : menuButtons) {
            button.update(input);
            if (button.isClicked()) {
                String name = button.getName();
                if (name.startsWith("Slot")) {
                    switch (name) {
                        case "Slot1" -> {
                            SharedData.setSaveFilePath(SAVES[0]);
                        }
                        case "Slot2" -> {
                            SharedData.setSaveFilePath(SAVES[1]);
                        }
                        case "Slot3" -> {
                            SharedData.setSaveFilePath(SAVES[2]);
                        }
                        case "Slot4" -> {
                            SharedData.setSaveFilePath(SAVES[3]);
                        }
                        case "Slot5" -> {
                            SharedData.setSaveFilePath(SAVES[4]);
                        }
                    }
                    if (!SaveLoadManager.getSize(SharedData.getSaveFilePath()).equals("0")) {
                        SharedData.setNewGame(false);
                        SharedData.setLoadingSave(true);
                        Log.debug("Loading existing game from: " + SharedData.getSaveFilePath() + " Size: " + SaveLoadManager.getSize(SharedData.getSaveFilePath()));
                    } else {
                        SharedData.setNewGame(true);
                        SharedData.setLoadingSave(false);
                        Log.debug("Starting new game with save file: " + SharedData.getSaveFilePath());
                    }
                    MainMenu.menuJukeBox.stopMusic();
                    SharedData.enterState(GameStates.LOADING_SCREEN, game);
                }else if (name.equals("Delete World")) {
                    Log.debug("Entering world delete mode.");
                    delete = true;
                    titleBanner.setText("Delete World?");
                    backButton.setRender(false);
                    deleteWorldButton.setRender(false);
                    cancelButton.setRender(true);
                }else if (name.equals("Back")) {
                    SharedData.enterState(GameStates.MAIN_MENU,game);
                }
            }
        }
    }
}
