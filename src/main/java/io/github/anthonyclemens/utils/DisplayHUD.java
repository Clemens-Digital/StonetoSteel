package io.github.anthonyclemens.utils;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import io.github.anthonyclemens.Achievements.AchievementNotification;
import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.Logic.Calender;
import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.SpriteManager;

public class DisplayHUD {
    private final int startY = 256;
    public void renderHUD(GameContainer container, Graphics g, Calender calender, DayNightCycle env, Player player) {
        int width = container.getWidth();
        g.setColor(Color.black);
        g.drawString("Date: " + calender.toString(), width - 200f, 0);
        g.drawString("Time: " + env.toString(), width - 200f, 16);


        SpriteManager.getSpriteSheet("items").getSprite(1, 0).draw(width-70f, startY, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_WOOD)+ "x", width - 70f + 32, startY+8f);
        SpriteManager.getSpriteSheet("main").getSprite(1, 10).draw(width-70f, startY+32f, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_FISH)+ "x", width - 70f + 32, startY+40f);
        SpriteManager.getSpriteSheet("items").getSprite(3, 0).draw(width-70f, startY+64f, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_SEED)+ "x", width - 70f + 32, startY+72f);
        SpriteManager.getSpriteSheet("items").getSprite(0, 0).draw(width-70f, startY+96f, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_STRING)+ "x", width - 70f + 32, startY+104f);
        SpriteManager.getSpriteSheet("items").getSprite(0, 1).draw(width-70f, startY+128f, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_BERRIES)+ "x", width - 70f + 32, startY+136f);
        SpriteManager.getSpriteSheet("items").getSprite(2, 0).draw(width-70f, startY+160f, 2);
        g.drawString(player.getPlayerInventory().getItemCount(Items.ITEM_BONES)+ "x", width - 70f + 32, startY+168f);
        
        for (AchievementNotification n : player.getAchievementManager().getNotifications()) {
            n.setX(width/2f);
            n.render(g);
        }
    }

    public void updateHUD(int delta, Player player){
        List<AchievementNotification> toRemove = new ArrayList<>();
        for (AchievementNotification n : player.getAchievementManager().getNotifications()) {
            n.update(delta);
            if (n.isFinished()) {
                toRemove.add(n);
            }
        }
        player.getAchievementManager().getNotifications().removeAll(toRemove);
    }
}
