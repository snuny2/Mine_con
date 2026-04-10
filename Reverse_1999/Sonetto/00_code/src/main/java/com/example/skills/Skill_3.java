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

    /** 게이지 완료 → 투사체1 발사 → 즉시 투사체2 발사 */
    public static void fire(Player player, CustomSkillPlugin plugin) {
        // 투사체1 (projectile_1 모델)
        fireProjectile(player, plugin, CustomSkillPlugin.PROJ_1_MODEL, 1, () -> {
            // 투사체1 끝나면 바로 투사체2
            new BukkitRunnable() {
                @Override public void run() {
                    fireProjectile(player, plugin, CustomSkillPlugin.PROJ_2_MODEL, 2, () -> {
                        applyCooldown(player, plugin);
                    });
                }
            }.runTaskLater(plugin, 2L);
        });
    }

    private static void fireProjectile(Player player, CustomSkillPlugin plugin,
                                        String modelString, int projNum,
                                        Runnable onComplete) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.8f);

        Location start = player.getEyeLocation();
        Vector   dir   = start.getDirection().normalize();

        // ArmorStand 생성 - 투사체 모델 장착
        ArmorStand stand = player.getWorld()
            .spawn(start, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setSmall(true);
                as.setMarker(true);
                as.setInvulnerable(true);
                as.setCustomNameVisible(false);

                // 각 투사체 모델 문자열로 아이템 생성
                ItemStack projItem = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta  meta     = projItem.getItemMeta();
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                cmd.setStrings(List.of(modelString)); // "projectile_1" or "projectile_2"
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

                Location pos = start.clone().add(dir.clone().multiply(traveled));
                stand.teleport(pos);

                // 꼬리 파티클 (가볍게)
                player.getWorld().spawnParticle(
                    Particle.ENCHANTED_HIT, pos, 2, 0.05, 0.05, 0.05, 0.01);

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

        // 사거리 초과 시 콜백
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
