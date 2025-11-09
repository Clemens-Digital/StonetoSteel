package io.github.anthonyclemens;

/**
 * Enum for all game state IDs for easy reference.
 */
public enum GameStates {
    MAIN_MENU(0),
    SETTINGS_MENU(1),
    NEW_GAME(2),
    CREDITS(3),
    MULTIPLAYER_MENU(4),
    GAME(99),
    VIDEO_SETTINGS(10),
    SOUND_SETTINGS(11),
    CONTROL_SETTINGS(12),
    PAUSE_MENU(101),
    LOADING_SCREEN(98),
    BOOT_SCREEN(123);


    private final int id;

    GameStates(int id) {
        this.id = id;
    }

    /**
     * Returns the integer ID associated with this state.
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the GameStates enum for a given ID, or null if not found.
     */
    public static GameStates fromID(int id) {
        for (GameStates state : values()) {
            if (state.id == id) return state;
        }
        return null;
    }
}
