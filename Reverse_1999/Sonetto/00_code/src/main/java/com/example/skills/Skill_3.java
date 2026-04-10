package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.managers.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Skill_3 {

    private static final double RANGE        = 7.0;
    private static final double DAMAGE       = 7.0;
    private static final int    WEAKNESS_SEC = 3;
    private static final double SPEED        = 0.5;

    public static void fire(Player player, CustomSkillPlugin plugin) {
        fireProjectile(player, plugin, CustomSkillPlugin.PROJ_1_MODEL, 1, () ->
            new BukkitRunnable() {
                @Override public void run() {
                    fireProjectile(player, plugin, CustomSkillPlugin.PROJ_2_MODEL, 2, () ->
                        applyCooldown(player, plugin));
                }
            }.runTaskLater(plugin, 2L)
        );
    }

    private static void fireProjectile(Player player, CustomSkillPlugin plugin,
                                        String modelString, int projNum,
                                        Runnable onComplete) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.8f);

        Location eyeLoc = player.getEyeLocation();
        Vector   dir    = eyeLoc.getDirection().normalize();

        // ArmorStand - small(true) + marker로 head가 eyeLoc에 바로 위치
        ArmorStand stand = player.getWorld()
            .spawn(eyeLoc, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setSmall(true);
                as.setMarker(true);
                as.setInvulnerable(true);
                as.setCustomNameVisible(false);

                ItemStack projItem = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta  meta     = projItem.getItemMeta();
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                cmd.setStrings(List.of(modelString));
                meta.setCustomModelDataComponent(cmd);
                projItem.setItemMeta(meta);
                as.getEquipment().setHelmet(projItem);
            });

        final boolean[] done = {false};

        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (traveled >= RANGE || stand.isDead()) {
                    stand.remove();
                    cancel();
                    if (!done[0]) { done[0] = true; onComplete.run(); }
                    return;
                }

                // 투사체 위치 - 눈높이 그대로
                Location pos = eyeLoc.clone().add(dir.clone().multiply(traveled));
                stand.teleport(pos);

                // 파티클 없음 (이미지로 대체)

                // 충돌 검사 - head 위치 기준
                for (Entity entity : player.getWorld()
                        .getNearbyEntities(pos, 0.8, 0.8, 0.8)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.equals(player)) continue;
                    if (entity.equals(stand))  continue;

                    LivingEntity target = (LivingEntity) entity;
                    target.damage(DAMAGE, player);
                    target.addPotionEffect(new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        WEAKNESS_SEC * 20, 0, false, true, true));

                    // 피격 파티클
                    target.getWorld().spawnParticle(
                        Particle.POOF, target.getLocation().add(0, 1, 0),
                        15, 0.3, 0.3, 0.3, 0.1);
                    target.getWorld().playSound(
                        target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);

                    player.sendMessage(
                        Component.text("💥 투사체" + projNum + " 데미지: ").color(NamedTextColor.GOLD)
                            .append(Component.text(String.valueOf(DAMAGE)).color(NamedTextColor.RED))
                            .append(Component.text(" → " + target.getName()
                                + " (남은 체력: " + String.format("%.1f", target.getHealth()) + ")")
                                .color(NamedTextColor.YELLOW)));

                    stand.remove();
                    cancel();
                    if (!done[0]) { done[0] = true; onComplete.run(); }
                    return;
                }
                traveled += SPEED;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            @Override public void run() {
                if (!stand.isDead()) stand.remove();
                if (!done[0]) { done[0] = true; onComplete.run(); }
            }
        }.runTaskLater(plugin, (long)(RANGE / SPEED) + 2L);
    }

    private static void applyCooldown(Player player, CustomSkillPlugin plugin) {
        plugin.getCooldownManager().setCooldown(
            player, CooldownManager.Skill.GAUGE, CooldownManager.GAUGE_CD);
    }
}
