package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.managers.CooldownManager;
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
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.8f);

        Location start = player.getEyeLocation();
        Vector   dir   = start.getDirection().normalize();

        // ArmorStand 투사체 생성
        ArmorStand stand = player.getWorld()
            .spawn(start, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setSmall(true);
                as.setMarker(true);
                as.setInvulnerable(true);
                as.setCustomNameVisible(false);

                // 투사체 아이템 - 문자열 방식
                ItemStack projItem = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta  meta     = projItem.getItemMeta();
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                cmd.setStrings(List.of(CustomSkillPlugin.PROJECTILE_MODEL_STRING));
                meta.setCustomModelDataComponent(cmd);
                projItem.setItemMeta(meta);
                as.getEquipment().setHelmet(projItem);
            });

        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (traveled >= RANGE || stand.isDead()) {
                    stand.remove();
                    cancel();
                    applyCooldown(player, plugin);
                    return;
                }

                Location pos = start.clone().add(dir.clone().multiply(traveled));
                stand.teleport(pos);

                // 꼬리 파티클
                player.getWorld().spawnParticle(
                    Particle.ENCHANTED_HIT, pos, 3, 0.05, 0.05, 0.05, 0.01);

                // 충돌 검사
                for (Entity entity : player.getWorld()
                        .getNearbyEntities(pos, 0.7, 0.7, 0.7)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    if (entity.equals(player)) continue;
                    if (entity.equals(stand))  continue;

                    LivingEntity target = (LivingEntity) entity;
                    target.damage(DAMAGE, player);
                    target.addPotionEffect(new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        WEAKNESS_SEC * 20, 0, false, true, true));

                    target.getWorld().spawnParticle(
                        Particle.POOF,
                        target.getLocation().add(0, 1, 0),
                        20, 0.3, 0.3, 0.3, 0.15);
                    target.getWorld().playSound(
                        target.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);

                    player.sendMessage(
                        ChatColor.GOLD + "💥 원거리 데미지: "
                        + ChatColor.RED + DAMAGE
                        + ChatColor.GOLD + " → " + target.getName()
                        + " (남은 체력: "
                        + String.format("%.1f", target.getHealth()) + ")");

                    stand.remove();
                    cancel();
                    applyCooldown(player, plugin);
                    return;
                }
                traveled += SPEED;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // 끝까지 못 맞혀도 쿨타임 적용
        new BukkitRunnable() {
            @Override public void run() {
                if (!stand.isDead()) stand.remove();
                applyCooldown(player, plugin);
            }
        }.runTaskLater(plugin, (long)(RANGE / SPEED) + 2L);
    }

    private static void applyCooldown(Player player, CustomSkillPlugin plugin) {
        plugin.getCooldownManager().setCooldown(
            player, CooldownManager.Skill.GAUGE, CooldownManager.GAUGE_CD);
    }
}
