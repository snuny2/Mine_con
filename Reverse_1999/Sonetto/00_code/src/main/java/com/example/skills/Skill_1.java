package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Skill_1 {

    private static final double AOE_SIZE = 4.5;
    private static final double DAMAGE = 12.0;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        Location base = player.getLocation().clone();
        base.setYaw(0f);
        base.setPitch(0f);

        player.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
        player.playSound(base, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

        List<ItemDisplay> displays = new ArrayList<>();

        // 0009: 바닥 마법진 (스킬 범위)
        displays.add(spawnDisplay(base.clone().add(0, 0.05, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                new Vector3f(9, 9, 1), "skill1_0009"));

        // 0011: 4블록 위 (0009와 대칭)
        displays.add(spawnDisplay(base.clone().add(0, 4.0, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(9, 9, 1), "skill1_0011"));

        // 0012: 0011보다 0.8블록 아래, 1/3 크기
        displays.add(spawnDisplay(base.clone().add(0, 3.2, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(3, 3, 1), "skill1_0012"));

        // 0010: 0012보다 0.3블록 위, 0012 감싸는 크기
        displays.add(spawnDisplay(base.clone().add(0, 3.5, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(4, 4, 1), "skill1_0010"));

        // 0.3초 후 데미지
        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<Entity> nearby = base.getWorld()
                        .getNearbyEntities(base, AOE_SIZE, AOE_SIZE, AOE_SIZE);
                int hit = 0;
                for (Entity entity : nearby) {
                    if (!(entity instanceof LivingEntity))
                        continue;
                    if (entity.equals(player))
                        continue;
                    if (entity instanceof ItemDisplay)
                        continue;

                    LivingEntity target = (LivingEntity) entity;
                    target.setNoDamageTicks(0);
                    target.damage(DAMAGE, player);

                    player.sendMessage(
                            Component.text("💥 광역 데미지: ").color(NamedTextColor.GOLD)
                                    .append(Component.text(String.valueOf(DAMAGE)).color(NamedTextColor.RED))
                                    .append(Component.text(" → " + target.getName()
                                            + " (남은 체력: " + String.format("%.1f", target.getHealth()) + ")")
                                            .color(NamedTextColor.YELLOW)));
                    hit++;
                }
                player.sendMessage(
                        Component.text("⚡ 광역 공격! 총 " + hit + "마리 적중").color(NamedTextColor.AQUA));
            }
        }.runTaskLater(plugin, 6L);

        // 1.5초 후 마법진 제거
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ItemDisplay d : displays) {
                    if (!d.isDead())
                        d.remove();
                }
            }
        }.runTaskLater(plugin, 30L);
    }

    private static ItemDisplay spawnDisplay(Location loc, World world,
            AxisAngle4f rotation, Vector3f scale,
            String modelString) {
        return world.spawn(loc, ItemDisplay.class, d -> {
            d.setBillboard(Display.Billboard.FIXED);
            d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), rotation, scale,
                    new AxisAngle4f(0, 0, 1, 0)));
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = item.getItemMeta();
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setStrings(List.of(modelString));
            meta.setCustomModelDataComponent(cmd);
            item.setItemMeta(meta);
            d.setItemStack(item);
        });
    }
}
