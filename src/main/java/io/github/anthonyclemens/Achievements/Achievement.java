package io.github.anthonyclemens.Achievements;

import org.newdawn.slick.util.Log;

public enum Achievement {
    // Mob Achievements
    SLAY_1_ZOMBIE(AchievementType.ZOMBIE_SLAYING, "First Blood", "Slay 1 zombie", 1),
    SLAY_10_ZOMBIES(AchievementType.ZOMBIE_SLAYING, "Zombie Slayer", "Slay 10 zombies", 10),
    SLAY_50_ZOMBIES(AchievementType.ZOMBIE_SLAYING, "Zombie Hunter", "Slay 50 zombies", 50),
    SLAY_100_ZOMBIES(AchievementType.ZOMBIE_SLAYING, "Zombie Exterminator", "Slay 100 zombies", 100),
    // Resource Gathering Achievements
    CHOP_1_TREE(AchievementType.TREE_CHOPPING, "I've played these games before", "Chop down 1 tree", 1),
    CHOP_10_TREES(AchievementType.TREE_CHOPPING, "Lumberjack Apprentice", "Chop down 10 trees", 10),
    CHOP_50_TREES(AchievementType.TREE_CHOPPING, "Lumberjack Journeyman", "Chop down 50 trees", 50),
    // Survival Achievements
    SURVIVE_1_DAY(AchievementType.SURVIVE, "This wasn't a dream?", "Survive 1 day");



    private final String name;
    private final AchievementType achievementType;
    private final String description;
    private final int goalSteps;
    private int currentStep = 0;
    private boolean isUnlocked = false;

    Achievement(AchievementType achievementType, String name, String description) {
        this.name = name;
        this.description = description;
        this.goalSteps = 1;
        this.achievementType = achievementType;
    }

    Achievement(AchievementType achievementType, String name, String description, int goalSteps) {
        this.name = name;
        this.description = description;
        this.goalSteps = goalSteps;
        this.achievementType = achievementType;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public int getGoalSteps() {
        return goalSteps;
    }
    public int getCurrentStep() {
        return currentStep;
    }
    public boolean isUnlocked() {
        return isUnlocked;
    }
    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }
    public AchievementType getAchievementType(){
        return this.achievementType;
    }

    public void incrementStep() {
        if(this.currentStep<this.goalSteps){
            currentStep++;
        }else{
            return;
        }
        Log.debug("Incrementing "+this.name+", progress "+this.currentStep+"/"+this.goalSteps);
        if(currentStep >= goalSteps) {
            isUnlocked = true;
        }
    }
}
