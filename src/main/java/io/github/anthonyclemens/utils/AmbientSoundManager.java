package io.github.anthonyclemens.utils;

import java.util.Random;

import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.Sound.JukeBox;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.WorldGen.Biome;

public class AmbientSoundManager {
    private IsoRenderer renderer;
    private final JukeBox jukeBox;
    private final SoundBox ambientSoundBox;
    private boolean dayNightSwitch = true;
    private Biome lastBiome = null;
    private double timeSinceLastMusic = 0;
    private double musicDelay = 0;
    private final Random random = new Random();

    public AmbientSoundManager(JukeBox jukeBox, SoundBox ambientSoundBox) {
        this.jukeBox = jukeBox;
        this.ambientSoundBox = ambientSoundBox;
    }

    public void attachRenderer(IsoRenderer renderer) {
        this.renderer = renderer;
    }

    public void playAmbientMusic(DayNightCycle env) {
        boolean isNight = env.isSunDown();
        String musicType = isNight ? "nightMusic" : "dayMusic";

        // Switch music immediately if state changed
        if (isNight != dayNightSwitch) {
            Log.debug("Switching to " + (isNight ? "night" : "day") + " music...");
            jukeBox.stopMusic();
            jukeBox.playRandomSong(musicType);
            jukeBox.setVolume(Settings.getInstance().getMusicVolume()*Settings.getInstance().getMainVolume());
            dayNightSwitch = isNight;
            timeSinceLastMusic = System.currentTimeMillis();
            musicDelay = random.nextDouble(30000) + 30000;
            Log.debug("Next song will play in " + (int)musicDelay/1000 + "s");
            return;
        }

        // Otherwise, wait for current song to finish and delay to expire
        long now = System.currentTimeMillis();
        if (!jukeBox.isPlaying() && (now - timeSinceLastMusic > musicDelay)) {
            Log.debug("Continuing with " + (isNight ? "night" : "day") + " music...");
            jukeBox.playRandomSong(musicType);
            jukeBox.setVolume(Settings.getInstance().getMusicVolume()*Settings.getInstance().getMainVolume());
            timeSinceLastMusic = now;
            musicDelay = random.nextDouble(30000) + 30000;
            Log.debug("Next song will play after a " + (int)musicDelay/1000 + "s delay.");
        }
    }

    public void playAmbientSounds(DayNightCycle env, Player player) {
        if (renderer == null) {
            Log.error("Renderer is not attached to AmbientSoundManager.");
            return;
        }
        if (env.isSunDown() && !ambientSoundBox.isAnySoundPlaying()) {
            ambientSoundBox.setVolume(Settings.getInstance().getAmbientVolume()*Settings.getInstance().getMainVolume());
            ambientSoundBox.playRandomSound("nightSounds");
        }
        Biome currentBiome = player.getBiome();
        if(currentBiome==null) return;
        if (lastBiome != currentBiome) {
            lastBiome = currentBiome;
            ambientSoundBox.stopAllSounds();
        }
        if (env.isSunUp() && !ambientSoundBox.isAnySoundPlaying()) {
            ambientSoundBox.setVolume(Settings.getInstance().getAmbientVolume()*Settings.getInstance().getMainVolume());
            switch (currentBiome) {
                case DESERT -> ambientSoundBox.playRandomSound("desertSounds");
                case PLAINS -> ambientSoundBox.playRandomSound("plainsSounds");
                case WATER -> ambientSoundBox.playRandomSound("waterSounds");
                case BEACH -> ambientSoundBox.playRandomSound("beachSounds");
                /*case MOUNTAIN -> Log.warn("Unimplemented case: " + currentBiome);
                case SWAMP -> Log.warn("Unimplemented case: " + currentBiome);
                case RAINFOREST -> Log.warn("Unimplemented case: " + currentBiome);
                case FOREST -> Log.warn("Unimplemented case: " + currentBiome);
                case SNOWY_PEAK -> Log.warn("Unimplemented case: " + currentBiome);
                case TUNDRA -> Log.warn("Unimplemented case: " + currentBiome);*/
                default -> ambientSoundBox.playRandomSound("plainsSounds");
            }
        }
    }
}
