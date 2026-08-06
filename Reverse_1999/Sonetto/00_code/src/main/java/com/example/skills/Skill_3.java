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
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;

public class Skill_3 {

    private static final double RANGE = 7.0;
    private static final double DAMAGE = 7.0;
    private static final int WEAKNESS_SEC = 3;
    private static final double SPEED = 0.5;

    public static void fire(Player player, CustomSkillPlugin plugin) {
        fireProjectile(player, plugin, CustomSkillPlugin.PROJ_1_MODEL, 1, () -> new BukkitRunnable() {
            @Override
            public void run() {
                fireProjectile(player, plugin, CustomSkillPlugin.PROJ_2_MODEL, 2, () -> applyCooldown(player, plugin));
            }
        }.runTaskLater(plugin, 2L));
    }

    private static void fireProjectile(Player player, CustomSkillPlugin plugin,
            String modelString, int projNum,
            Runnable onComplete) {
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 0.8f);

        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection().normalize();

        // ItemDisplay 사용 - 정확히 눈높이에서 시작, 파티클 없음
        Location spawnLoc = eyeLoc.clone();
        spawnLoc.setPitch(0f);
        ItemDisplay proj = eyeLoc.getWorld().spawn(eyeLoc, ItemDisplay.class, d -> {
            d.setBillboard(Display.Billboard.FIXED);
            d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(-2f, 1f, 2f),
                    new AxisAngle4f(0, 0, 1, 0)));
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = item.getItemMeta();
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setStrings(List.of(modelString));
            meta.setCustomModelDataComponent(cmd);
            item.setItemMeta(meta);
            d.setItemStack(item);
        });

        final boolean[] done = { false };

        new BukkitRunnable() {
            double traveled = 0;

            @Override
            public void run() {
                if (traveled >= RANGE || proj.isDead()) {
                    proj.remove();
                    cancel();
                    if (!done[0]) {
                        done[0] = true;
                        onComplete.run();
                    }
                    return;
                }

                // 정확히 눈높이 직선으로 이동
                Location pos = eyeLoc.clone().add(dir.clone().multiply(traveled));
                proj.teleport(pos);

                // 충돌 검사
                for (Entity entity : eyeLoc.getWorld()
                        .getNearbyEntities(pos, 0.8, 0.8, 0.8)) {
                    if (!(entity instanceof LivingEntity))
                        continue;
                    if (entity.equals(player))
                        continue;
                    if (entity instanceof ItemDisplay)
                        continue;

                    LivingEntity target = (LivingEntity) entity;
                    target.setNoDamageTicks(0);
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

                    proj.remove();
                    cancel();
                    if (!done[0]) {
                        done[0] = true;
                        onComplete.run();
                    }
                    return;
                }
                traveled += SPEED;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!proj.isDead())
                    proj.remove();
                if (!done[0]) {
                    done[0] = true;
                    onComplete.run();
                }
            }
        }.runTaskLater(plugin, (long) (RANGE / SPEED) + 2L);
    }

    private static void applyCooldown(Player player, CustomSkillPlugin plugin) {
        plugin.getCooldownManager().setCooldown(
                player, CooldownManager.Skill.GAUGE, CooldownManager.GAUGE_CD);
    }
}
