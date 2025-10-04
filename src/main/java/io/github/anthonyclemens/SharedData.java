package io.github.anthonyclemens;

import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.Camera;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.WorldGen.World;
import io.github.anthonyclemens.states.Game;


public class SharedData {
    // Private constructor to prevent instantiation
    private SharedData() {}

    private static int seed = 0;
    private static boolean hotstart = false;
    private static GameStates lastState = null;
    private static String saveFilePath = null;
    private static boolean newGame = true;
    private static boolean loadingSave = false;
    private static String ipAddress = "";

    private static Game gameState;

    public static boolean isHotstart() {
        return hotstart;
    }

    public static void setHotstart(boolean value) {
        SharedData.hotstart = value;
    }

    public static void setSeed(int newSeed) {
        SharedData.seed = newSeed;
    }

    public static int getSeed() {
        return seed;
    }

    public static void setSaveFilePath(String path) {
        SharedData.saveFilePath = path;
    }

    public static String getSaveFilePath() {
        return saveFilePath;
    }

    public static boolean isNewGame() {
        return newGame;
    }

    public static void setNewGame(boolean isNew) {
        SharedData.newGame = isNew;
    }

    public static boolean isLoadingSave() {
        return loadingSave;
    }

    public static void setLoadingSave(boolean loading) {
        SharedData.loadingSave = loading;
    }

    public static void enterState(GameStates state, StateBasedGame game) {
        // Assign lastState to the current state before switching
        GameStates currentState = getCurrentState(game);
        SharedData.lastState = currentState;
        Log.debug("Entering state: " + state + " from " + (lastState != null ? lastState : "none"));
        game.enterState(state.getID());
    }

    /**
     * Utility to get the current GameStates enum from the StateBasedGame.
     */
    private static GameStates getCurrentState(StateBasedGame game) {
        int currentId = game.getCurrentState().getID();
        return GameStates.fromID(currentId);
    }

    public static GameStates getLastState() {
        return lastState;
    }

    public static void setGameState(Game g) { gameState = g; }
    public static Game getGameState() { return gameState; }

    public static IsoRenderer getRenderer() { return gameState != null ? gameState.getRenderer() : null; }
    public static Player getPlayer() { return gameState != null ? gameState.getPlayer() : null; }
    public static DayNightCycle getDayNightCycle() { return gameState != null ? gameState.getEnv() : null; }
    public static World getChunkManager() { return gameState != null ? gameState.getChunkManager() : null; }
    public static Camera getCamera() { return gameState != null ? gameState.getCamera() : null; }
    public static String getIPAddress() { return ipAddress; }
    public static void setIPAddress(String newIP) { ipAddress=newIP; }
}
