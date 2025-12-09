package io.github.anthonyclemens.Achievements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Rendering.FontManager;

public class AchievementManager implements Serializable{

    private final List<Achievement> achievements = new ArrayList<>();
    private final transient List<AchievementNotification> notifications = new ArrayList<>();

    public AchievementManager() {
        // Initialize all achievements from the enum
        achievements.addAll(Arrays.asList(Achievement.values()));
    }

    public AchievementManager(List<Achievement> loadedAchievements) {
        // Load achievements from saved data
        this.achievements.addAll(loadedAchievements);
        loadedAchievements.stream().forEach(a -> Log.debug("Loaded achievement: " + a.getName() + " Unlocked: " + a.isUnlocked() + " Progress: " + a.getCurrentStep() + "/" + a.getGoalSteps()));
    }

    /**
     * Record progress for a specific achievement.
     */
    public void recordProgress(Achievement achievement) throws SlickException{
        achievement.incrementStep();
        if (achievement.isUnlocked()) {
            Log.debug("Unlocked achievement: " + achievement.getName());
            notifications.add(new AchievementNotification(achievement, FontManager.getFont("MedievalTimes", 24), FontManager.getFont("Roboto", 24), -50f, 0f));
        }
    }

    /**
     * Record progress for a specific achievement type.
     */
    public void recordProgress(AchievementType achievementType) {
        achievements.stream()
            .filter(a -> a.getAchievementType() == achievementType && !a.isUnlocked())
            .forEach(a -> {
                a.incrementStep();
                if (a.isUnlocked()) {
                    try {
                        Log.debug("Unlocked achievement: " + a.getName());
                        notifications.add(new AchievementNotification(a, FontManager.getFont("MedievalTimes", 24), FontManager.getFont("Roboto", 24), -50f, 0f));
                    } catch (SlickException ex) {
                        Log.error(ex.getMessage());
                    }
                }
            });
    }


    /**
     * Get all achievements.
     */
    public List<Achievement> getAllAchievements() {
        return achievements;
    }

    /**
     * Get all unlocked achievements.
     */
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }

    /**
     * Get all locked achievements.
     */
    public List<Achievement> getLockedAchievements() {
        List<Achievement> locked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (!achievement.isUnlocked()) {
                locked.add(achievement);
            }
        }
        return locked;
    }

    /**
     * Get achievements by type.
     */
    public List<Achievement> getAchievementsByType(AchievementType type) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.getAchievementType() == type) {
                result.add(achievement);
            }
        }
        return result;
    }

    /**
     * Get current notifications.
     */
    public List<AchievementNotification> getNotifications() {
        return notifications;
    }

    /**
     * Reset progress for all achievements.
     */
    public void resetAll() {
        for (Achievement achievement : achievements) {
            achievement.setUnlocked(false);
        }
    }
}
