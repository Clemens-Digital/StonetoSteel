package io.github.anthonyclemens.Achievements;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class AchievementNotification {
    private final Achievement achievement;
    private float x;
    private float y;
    private final float targetY;
    private boolean slidingUp = false;
    private int timer = 0; // how long to stay visible

    public AchievementNotification(Achievement achievement, float startY, float targetY) {
        this.achievement = achievement;
        this.y = startY; // start above the screen (e.g., -50)
        this.targetY = targetY; // visible position (e.g., 50)
    }

    public void update(int delta) {
        if (!slidingUp) {
            // Slide down into view
            if (y < targetY) {
                y += 0.5f * delta; // slide speed
                if (y >= targetY) {
                    y = targetY;
                }
            } else {
                // Stay visible for 3 seconds
                timer += delta;
                if (timer > 5000) {
                    slidingUp = true;
                }
            }
        } else {
            // Slide back up out of view
            y -= 0.5f * delta;
        }
    }

    public void render(Graphics g) {
        g.setColor(Color.blue);
        g.drawRoundRect(this.x-(g.getFont().getWidth(this.achievement.getDescription())/2), this.y, g.getFont().getWidth(this.achievement.getDescription()), 60, 4);
        g.setColor(Color.white);
        g.drawString("Unlocked: " + this.achievement.getName(), this.x-(g.getFont().getWidth(this.achievement.getName())/2f), this.y);
        g.drawString(this.achievement.getDescription(), this.x-(g.getFont().getWidth(this.achievement.getName())/2f), this.y+20f);
    }

    public boolean isFinished() {
        return slidingUp && y < -50; // off-screen
    }

    public void setX(float x) {
        this.x = x;
    }
}
