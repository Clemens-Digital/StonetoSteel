package io.github.anthonyclemens.Achievements;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;

import io.github.anthonyclemens.GUI.Banner;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.utils.AssetLoader;

public class AchievementNotification extends Banner{
    private final float startY;
    private final float targetY;
    private boolean slidingUp = false;
    private int timer = 0; // how long to stay visible

    public AchievementNotification(Achievement achievement, TrueTypeFont titleTTF, TrueTypeFont descriptionTTF, float startY, float targetY) throws SlickException {
        super(new Image(AssetLoader.loadSingleAssetFromFile(Settings.getInstance().getTexturePack(), "bannerImage"), false, Image.FILTER_NEAREST),
            achievement.getName(), achievement.getDescription(), titleTTF, descriptionTTF, 0, startY-280, 820, 280);
        this.targetY = targetY;
        this.startY = startY-280;
    }

    public void update(int delta) {
        float y = getY();
        if (!slidingUp) {
            if (y < targetY) {
                y += 0.2f * delta;
                if (y >= targetY) {
                    y = targetY;
                }
                setY(y);
            } else {
                timer += delta;
                if (timer > 5000) { // 5 seconds
                    slidingUp = true;
                }
            }
        } else {
            y -= 0.2f * delta;
            setY(y);
        }
    }


    public boolean isFinished() {
        return slidingUp && getY() <= startY-280;
    }

}
