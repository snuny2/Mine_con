package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class Skill_1 {

    private static final double AOE_SIZE = 4.5;
    private static final double DAMAGE   = 12.0;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        Location center = player.getLocation();

        waveEffect(player, plugin, center);

        player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
        player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<Entity> nearby = center.getWorld()
                        .getNearbyEntities(center, AOE_SIZE, AOE_SIZE, AOE_SIZE);

                int hit = 0;
                for (Entity entity : nearby) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.equals(player)) continue;

                    LivingEntity target = (LivingEntity) entity;
                    target.damage(DAMAGE, player);
                    target.getWorld().spawnParticle(
                            Particle.CRIT,
                            target.getLocation().add(0, 1, 0),
                            20, 0.5, 0.5, 0.5, 0.2);

                    player.sendMessage(
                            Component.text("💥 광역 데미지: ").color(NamedTextColor.GOLD)
                                    .append(Component.text(String.valueOf(DAMAGE)).color(NamedTextColor.RED))
                                    .append(Component.text(" → " + target.getName()
                                            + " (남은 체력: " + String.format("%.1f", target.getHealth()) + ")")
                                            .color(NamedTextColor.YELLOW)));
                    hit++;
                }
                // ✅ 수정: Component.AQUA → Component.text().color()
                player.sendMessage(
                        Component.text("⚡ 광역 공격! 총 " + hit + "마리 적중").color(NamedTextColor.AQUA));
            }
        }.runTaskLater(plugin, 6L);
    }

    private static void waveEffect(Player player, CustomSkillPlugin plugin, Location center) {
        new BukkitRunnable() {
            double radius = 0.5;

            @Override
            public void run() {
                if (radius > AOE_SIZE) { cancel(); return; }
                int points = (int)(radius * 12);
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI / points) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location loc = center.clone().add(x, 0.1, z);
                    player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 2, 0.1, 0.1, 0.1, 0.05);
                }
                radius += 0.4;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
