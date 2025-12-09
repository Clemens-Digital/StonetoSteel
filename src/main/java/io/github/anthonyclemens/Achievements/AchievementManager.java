package io.github.anthonyclemens.Achievements;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.util.Log;

public class AchievementManager {

    private final List<Achievement> achievements = new ArrayList<>();
    private final List<AchievementNotification> notifications = new ArrayList<>();

    public AchievementManager() {
        // Initialize all achievements from the enum
        for (Achievement achievement : Achievement.values()) {
            achievements.add(achievement);
        }
    }

    public AchievementManager(List<Achievement> loadedAchievements) {
        // Load achievements from saved data
        this.achievements.addAll(loadedAchievements);
        loadedAchievements.stream().forEach(a -> Log.debug("Loaded achievement: " + a.getName() + " Unlocked: " + a.isUnlocked() + " Progress: " + a.getCurrentStep() + "/" + a.getGoalSteps()));
    }

    /**
     * Record progress for a specific achievement.
     */
    public void recordProgress(Achievement achievement) {
        achievement.incrementStep();
        if (achievement.isUnlocked()) {
            Log.debug("Unlocked achievement: " + achievement.getName());
            notifications.add(new AchievementNotification(achievement, -50f, 500f));
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
                    Log.debug("Unlocked achievement: " + a.getName());
                    notifications.add(new AchievementNotification(a, -50f, 50f));
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
