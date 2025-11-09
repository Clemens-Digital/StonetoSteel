package io.github.anthonyclemens.states;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Math.TwoDimensionMath;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.Sound.SoundBox;

public class BootScreen extends BasicGameState{

    private final Color nameColor = new Color(255, 255, 255, 255);
    private float logoAlpha = 0f;
    private Image cdLogo;
    private int containerWidth = 0;
    private int logoTimer = 0;
    private int nameIndex = 1;
    private int nameTimer = 0;
    private int startTimer = 0;
    private Sound bootSound;
    private SoundBox bootBox;
    private static final int FADE_INTERVAL = 5;
    private static final int LOGO_REVEAL_INTERVAL = 10;
    private static final int NAME_REVEAL_INTERVAL = 650;
    private static final int START_DELAY = 850;
    private static final String CD = "Clemens Digital";
    private String nameString = "";
    private TrueTypeFont nameFont;

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        Settings settings = Settings.getInstance();
        containerWidth = container.getWidth();
        nameFont = FontManager.getFont("Basis33", 64);
        cdLogo = new Image("textures/cd-logo.png");
        bootSound = new Sound("sounds/boot.ogg");
        bootSound.play(1f, settings.getMusicVolume());
        bootBox = new SoundBox();
        bootBox.setVolume(settings.getMusicVolume());
        bootBox.addSound("key", "sounds/key1.ogg");
        bootBox.addSound("key", "sounds/key2.ogg");
        bootBox.addSound("key", "sounds/key3.ogg");
        bootBox.addSound("space", "sounds/space.ogg");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        cdLogo.setAlpha(logoAlpha);
        cdLogo.draw(TwoDimensionMath.getMiddleX(400, containerWidth), 64,400,400);
        nameFont.drawString(TwoDimensionMath.getMiddleX(nameFont.getWidth(CD), containerWidth), 500, nameString, nameColor);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if(container.getInput().isKeyPressed(Input.KEY_SPACE)) {
            bootSound.stop();
            game.enterState(GameStates.MAIN_MENU.getID());
        }
        // Start Delay
        startTimer += delta;
        if(startTimer >= START_DELAY) logoTimer += delta;
        // Logo reveal logic
        if (logoTimer >= LOGO_REVEAL_INTERVAL && logoAlpha<=1f && bootSound.playing()) {
            logoTimer = 0;
            logoAlpha += 0.01f;
        }
        // Text reveal logic after Logo Reveal
        if(logoAlpha>=1f) nameTimer += delta;
        if(nameTimer >= NAME_REVEAL_INTERVAL && nameIndex <= CD.length()){
            nameTimer = 0;
            if(CD.toCharArray()[nameIndex-1] == ' '){
                bootBox.playRandomSound("space");
            }else{
                bootBox.playRandomSound("key");
            }
            nameString = CD.substring(0, nameIndex++);
        }
        if(bootSound.playing()) return;
        // When sound is done playing, fade to black and go to main menu
        if (logoTimer >= FADE_INTERVAL && logoAlpha>=0f) {
            logoTimer = 0;
            logoAlpha -= 0.01f;
            nameColor.a -=0.01f;
        }
        if(logoAlpha<0.005f) game.enterState(GameStates.MAIN_MENU.getID());

    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("BootScreen state initialized.");
    }

    @Override
    public int getID() {
        return GameStates.BOOT_SCREEN.getID();
    }
}
