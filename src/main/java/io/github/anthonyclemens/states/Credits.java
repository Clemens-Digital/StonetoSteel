package io.github.anthonyclemens.states;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import io.github.anthonyclemens.GUI.Buttons.ImageTextButton;
import io.github.anthonyclemens.GameStates;
import io.github.anthonyclemens.Rendering.FontManager;
import io.github.anthonyclemens.Rendering.RenderUtils;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.utils.AssetLoader;

public class Credits extends BasicGameState{

    private Input input;
    ImageTextButton backButton;
    TrueTypeFont creditsFont;
    private float gingerX;
    private Image bgImage;

    private float scrollY = 0f;
    private Image gingerImage;
    private static final float SCROLL_SPEED = 50f;


    private static final String MAIN_FONT = "MedievalTimes";
    private static final String CREDITS_FONT = "Roboto";
    private static final int CREDITS_FONT_SIZE = 32;

    private List<String> allCredits;
    Map<String, List<String>> creditSections = new LinkedHashMap<>();


    @Override
    public int getID() {
        return GameStates.CREDITS.getID();
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        input = container.getInput();
        Settings settings = Settings.getInstance();
        String texturePack = settings.getTexturePack();
        String soundPack = settings.getSoundPack();
        // Load button images
        Image buttonImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "regularButton"), false, Image.FILTER_NEAREST);
        backButton = new ImageTextButton(buttonImage, "Back", FontManager.getFont(MAIN_FONT, 32), 10, 10, 240, 80);

        // Load Background Image
        bgImage = new Image(AssetLoader.loadSingleAssetFromFile(texturePack, "backgroundImage"));
        // Credits
        creditSections.put("GAME DEVELOPMENT", List.of(
            "IsoRender Engine - Anthony Clemens",
            "Game Mechanics - Anthony Clemens",
            "UI/UX Design - Marissa Clemens",
            "Game Design - Anthony Clemens"
        ));

        creditSections.put("ASSET CREDITS", List.of(
            "UI Elements - Crusenho",
            "World tileset - Dani Maccari",
            "Grass - Styloo",
            "Character - AxulArt",
            "Zombie - gnomocaqui",
            "Spider - Camacebra Games"
        ));

        creditSections.put("MUSIC CREDITS", List.of(
            "Adrift Among Infinite Stars - Scott Buckley",
            "Forest Walk - Alexander Nakarada",
            "Lovely - Alex-Productions",
            "Monumental - Alex-Productions",
            "Moonlight - Scott Buckley",
            "Morning Light - Keys of Moon",
            "Spring flower - Keys of Moon",
            "Sunset Landscape - Keys of Moon",
            "Walking Home - Alex-Productions",
            "Emotional Ethnic Music - Keys of Moon"
        ));

        creditSections.put("SOUND CREDITS", List.of(
            "NOX Sound Essentials - Nox_Sound_Design"
        ));

        creditSections.put("TOOLS & TECHNOLOGIES", List.of(
            "Basis for Game Engine: Slick2D",
            "Graphics: LWJGL",
            "Powered by Java & Love of Games"
        ));

        creditSections.put("SPECIAL THANKS", List.of(
            "My wife Marissa: For her unwavering support and patience",
            "My friend Kindle: For giving me advice and help with features",
            "My friend Kayle: For his bug reports, suggestions, and testing",
            "Ginger the Cat: For being the best coding companion",
            "The community: For your feedback and support"
        ));

        creditSections.put("GINGER THE CAT",List.of());

        allCredits = new ArrayList<>();
        creditSections.forEach((section, entries) -> {
            allCredits.add("=== " + section + " ===");
            allCredits.addAll(entries);
            allCredits.add("");
        });
        gingerImage = new Image("GingertheCat.jpg", false, Image.FILTER_NEAREST);
        creditsFont = FontManager.getFont(CREDITS_FONT, CREDITS_FONT_SIZE);
        gingerX = (container.getWidth() - gingerImage.getWidth()) / 2f;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        Log.debug("Credits state initialized.");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        RenderUtils.drawBackground(bgImage, container);
        float y = container.getHeight() + scrollY;

        for (String line : allCredits) {
            float x = (container.getWidth() - creditsFont.getWidth(line)) / 2f;
            creditsFont.drawString(x, y, line, Color.black);
            y += CREDITS_FONT_SIZE + 10f;
        }

        if((y+container.getHeight())<=0) game.enterState(GameStates.MAIN_MENU.getID());
        gingerImage.draw(gingerX, y);
        backButton.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        backButton.update(input);
        if (backButton.isClicked()) {
            game.enterState(GameStates.MAIN_MENU.getID());
        }
        float multiplier = 0.001f;
        if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) multiplier = 0.01f;
        scrollY -= SCROLL_SPEED * delta * multiplier;
        SteamAPI.runCallbacks();
    }
}
