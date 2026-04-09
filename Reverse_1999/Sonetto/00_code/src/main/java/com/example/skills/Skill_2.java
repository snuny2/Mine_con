package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class Skill_2 {

    private static final int BUFF_SEC = 5;
    private static final String OVERLAY_CHAR = "\uE200"; // 버프 오버레이

    public static void cast(Player player, CustomSkillPlugin plugin) {
        // 포션 효과
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                BUFF_SEC * 20, 0, false, true, true));

        // 화면 오버레이 이미지 (Subtitle 활용)
        player.showTitle(Title.title(
                Component.empty(),
                Component.text(OVERLAY_CHAR)
                        .font(Key.key("minecraft", "default"))
                        .color(TextColor.color(0xFFFFFF)),
                Title.Times.times(
                        Duration.ofMillis(100),
                        Duration.ofMillis(600),
                        Duration.ofMillis(400))));

        // 사운드
        player.playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        // 파티클 (나선형)
        new BukkitRunnable() {
            double angle = 0;
            int tick = 0;

            @Override
            public void run() {
                if (tick++ >= 25) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 3; i++) {
                    double a = angle + (Math.PI * 2 / 3 * i);
                    double x = Math.cos(a) * 0.9;
                    double z = Math.sin(a) * 0.9;
                    double y = (tick / 25.0) * 2.2;
                    Location loc = player.getLocation().add(x, y, z);
                    player.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(
                            Particle.END_ROD, loc, 1,
                            0.05, 0.05, 0.05, 0.02);
                }
                angle += Math.PI / 7;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.sendMessage(ChatColor.GREEN
                + "✨ 힘 1 + 저항 1 — " + BUFF_SEC + "초 부여!");
    }
}