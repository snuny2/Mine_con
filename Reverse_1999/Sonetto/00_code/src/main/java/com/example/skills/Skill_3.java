package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.managers.CooldownManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

    /** 게이지 완료 후 호출 - 투사체1 발사 후 즉시 투사체2 발사 */
    public static void fire(Player player, CustomSkillPlugin plugin) {
        // 투사체1 발사
        fireProjectile(player, plugin, 1, () -> {
            // 투사체1 끝나면 바로 투사체2 발사 (0.1초 딜레이)
            new BukkitRunnable() {
                @Override public void run() {
                    fireProjectile(player, plugin, 2, () -> {
                        // 투사체2 끝나면 쿨타임 적용
                        applyCooldown(player, plugin);
                    });
                }
            }.runTaskLater(plugin, 2L);
        });
    }

    private static void fireProjectile(Player player, CustomSkillPlugin plugin,
                                        int projNum, Runnable onComplete) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.8f);

        Location start = player.getEyeLocation();
        Vector   dir   = start.getDirection().normalize();

        // 투사체 번호에 따라 액션바 문자 선택
        String projChar = projNum == 1
            ? CustomSkillPlugin.PROJ_1_CHAR
            : CustomSkillPlugin.PROJ_2_CHAR;

        // ArmorStand 투사체 생성
        ArmorStand stand = player.getWorld()
            .spawn(start, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setGravity(false);
                as.setSmall(true);
                as.setMarker(true);
                as.setInvulnerable(true);
                as.setCustomNameVisible(false);

                ItemStack projItem = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta  meta     = projItem.getItemMeta();
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                cmd.setStrings(List.of(CustomSkillPlugin.PROJECTILE_MODEL_STRING));
                meta.setCustomModelDataComponent(cmd);
                projItem.setItemMeta(meta);
                as.getEquipment().setHelmet(projItem);
            });

        final boolean[] completed = {false};

        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (traveled >= RANGE || stand.isDead()) {
                    stand.remove();
                    cancel();
                    if (!completed[0]) {
                        completed[0] = true;
                        onComplete.run();
                    }
                    return;
                }

                Location pos = start.clone().add(dir.clone().multiply(traveled));
                stand.teleport(pos);

                // 투사체 액션바 이미지 표시
                player.sendActionBar(
                    Component.text(projChar)
                        .font(Key.key("minecraft", "default"))
                        .color(TextColor.color(0xFFFFFF))
                );

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
                        Component.text("💥 투사체" + projNum + " 데미지: ").color(NamedTextColor.GOLD)
                            .append(Component.text(String.valueOf(DAMAGE)).color(NamedTextColor.RED))
                            .append(Component.text(" → " + target.getName()
                                + " (남은 체력: " + String.format("%.1f", target.getHealth()) + ")")
                                .color(NamedTextColor.YELLOW)));

                    stand.remove();
                    cancel();
                    if (!completed[0]) {
                        completed[0] = true;
                        onComplete.run();
                    }
                    return;
                }
                traveled += SPEED;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // 끝까지 못 맞혀도 콜백 실행
        new BukkitRunnable() {
            @Override public void run() {
                if (!stand.isDead()) stand.remove();
                if (!completed[0]) {
                    completed[0] = true;
                    onComplete.run();
                }
            }
        }.runTaskLater(plugin, (long)(RANGE / SPEED) + 2L);
    }

    private static void applyCooldown(Player player, CustomSkillPlugin plugin) {
        plugin.getCooldownManager().setCooldown(
            player, CooldownManager.Skill.GAUGE, CooldownManager.GAUGE_CD);
    }
}
