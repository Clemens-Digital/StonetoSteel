package io.github.anthonyclemens.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.Sys;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

/**
 * JukeBox manages categorized music tracks, allowing random playback and resource cleanup.
 */
public class JukeBox {

    private final HashMap<String, List<String>> recentlyPlayed = new HashMap<>();
    private final HashMap<String, HashMap<String, Music>> songs;

    private Music currentMusic;
    private final Random random;
    private float volume;

    public JukeBox() {
        this.songs = new HashMap<>();
        this.random = new Random(Sys.getTime());
    }

    /**
     * Adds a single song to a category.
     * @param category Category name.
     * @param path     Path to the music file.
     */
    public void addSong(String category, String path) throws SlickException {
        Log.debug("Loading song ("+category+","+path+")");
        this.songs.computeIfAbsent(category, k -> new HashMap<>()).put(path, new Music(path, true));
    }

    /**
     * Adds multiple songs to a category.
     * @param category Category name.
     * @param paths    List of music file paths.
     */
    public void addSongs(String category, List<String> paths) {
        paths.forEach(path -> {
            try {
                this.songs
                    .computeIfAbsent(category, k -> new HashMap<>())
                    .put(path, new Music(path, true));
                Log.debug("Loading song (" + category + "," + path + ")");
            } catch (SlickException e) {
                Log.error(e);
            }
        });
    }

    /**
     * Plays a random song from the specified category.
     * @param category Category name.
     */
    public void playRandomSong(String category) {
        stopMusic();
        HashMap<String, Music> categorySongs = this.songs.get(category);
        if (categorySongs == null || categorySongs.isEmpty()) {
            Log.warn("No songs available in category: " + category);
            return;
        }

        List<String> allPaths = new ArrayList<>(categorySongs.keySet());
        List<String> history = recentlyPlayed.computeIfAbsent(category, k -> new ArrayList<>());

        // Reset history if all songs have been played
        if (history.size() == allPaths.size()) {
            history.clear();
        }

        // Filter out already played songs
        List<String> unplayed = new ArrayList<>();
        for (String path : allPaths) {
            if (!history.contains(path)) {
                unplayed.add(path);
            }
        }

        // Select and play a random unplayed song
        if (!unplayed.isEmpty()) {
            String randomPath = unplayed.get(random.nextInt(unplayed.size()));
            Music randomSong = categorySongs.get(randomPath);
            playSong(randomSong);
            history.add(randomPath); // Track the song
        }
    }

    /**
     * Loops a specific song.
     * @param song Music object to play.
     */
    public void playSong(Music song){
        stopMusic();
        currentMusic=song;
        currentMusic.setVolume(volume);
        Log.debug("Playing song at "+findSongPath(song));
        currentMusic.loop();
    }

    /**
     * Stops the currently playing music.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null; // Dereference to help GC
        }
    }

    /**
     * Checks if music is currently playing.
     * @return true if music is playing, false otherwise.
     */
    public boolean isPlaying() {
        return currentMusic != null && currentMusic.playing();
    }

    /**
     * Sets the playback volume for music.
     * @param volume Volume (0.0 to 1.0).
     */
    public void setVolume(float volume){
        this.volume=volume;
        if(currentMusic!=null) currentMusic.setVolume(volume);
    }

    /**
     * Finds the file path for a given Music object.
     * @param song Music object.
     * @return File path string or null if not found.
     */
    public String findSongPath(Music song) {
        if (song == null) {
            Log.warn("Provided song is null.");
            return null;
        }

        // Iterate through all categories
        for (Map.Entry<String, HashMap<String, Music>> categoryEntry : this.songs.entrySet()) {
            HashMap<String, Music> categorySongs = categoryEntry.getValue();

            // Search through the songs in each category
            for (Map.Entry<String, Music> songEntry : categorySongs.entrySet()) {
                if (songEntry.getValue().equals(song)) {
                    return songEntry.getKey(); // Return the path if a match is found
                }
            }
        }

        Log.warn("Song not found.");
        return null; // Return null if the song isn't found
    }

    /**
     * Returns the file path of the currently playing song, or null if none is playing.
     * @return The path of the current song as a String.
     */
    public String getCurrentSong() {
        return findSongPath(currentMusic);
    }

    /**
     * Releases all loaded music and clears resources.
     */
    public void clear() {
        stopMusic();
        for (HashMap<String, Music> category : songs.values()) {
            for (Music music : category.values()) {
                if (music != null) {
                    music.stop();
                    music = null;
                }
            }
            category.clear();
        }
        songs.clear();
    }
}

