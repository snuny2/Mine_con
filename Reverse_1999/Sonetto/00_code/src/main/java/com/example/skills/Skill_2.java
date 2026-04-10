package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;

public class Skill_2 {

    private static final int  BUFF_SEC      = 5;
    private static final int  BEAM_FRAMES   = 22;
    private static final int  CIRCLE_FRAMES = 8;
    private static final long BEAM_TICKS    = 1L;  // 22틱 = 1.1초
    private static final long CIRCLE_TICKS  = 4L;  // 32틱 = 1.6초

    public static void cast(Player player, CustomSkillPlugin plugin) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH, BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.RESISTANCE, BUFF_SEC * 20, 0, false, true, true));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        Location center = player.getLocation();

        // ── Circle: 발밑 바닥 4x4블록 ──────────────────────────
        ItemDisplay circleDisplay = center.getWorld()
            .spawn(center.clone().add(0, 0.05, 0), ItemDisplay.class, d -> {
                // FIXED: 방향 고정 (플레이어 방향 무관)
                d.setBillboard(Display.Billboard.FIXED);
                d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    // X축 -90도 회전 → 바닥에 눕힘
                    new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                    new Vector3f(4, 4, 1),  // 4x4블록, 두께 1
                    new AxisAngle4f(0, 0, 1, 0)
                ));
            });

        // ── Beam: 플레이어 머리 위 2블록, 항상 수직 고정 ────────
        ItemDisplay beamDisplay = center.getWorld()
            .spawn(center.clone().add(0, 2, 0), ItemDisplay.class, d -> {
                // FIXED: 방향 고정, 항상 세로 유지
                d.setBillboard(Display.Billboard.FIXED);
                d.setTransformation(new Transformation(
                    new Vector3f(0, -1.5f, 0),  // Y 중심 보정 (위2→아래)
                    new AxisAngle4f(0, 0, 1, 0), // 회전 없음 (세로)
                    new Vector3f(1.5f, 3f, 1.5f), // 너비1.5, 높이3블록
                    new AxisAngle4f(0, 0, 1, 0)
                ));
            });

        // ── Circle 애니메이션 ────────────────────────────────────
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= CIRCLE_FRAMES) {
                    circleDisplay.remove();
                    cancel();
                    return;
                }
                setModel(circleDisplay, String.format("skill2_circle_%02d", frame));
                frame++;
            }
        }.runTaskTimer(plugin, 0L, CIRCLE_TICKS);

        // ── Beam 애니메이션 ──────────────────────────────────────
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= BEAM_FRAMES) {
                    beamDisplay.remove();
                    cancel();
                    return;
                }
                setModel(beamDisplay, String.format("skill2_beam_%02d", frame));
                frame++;
            }
        }.runTaskTimer(plugin, 0L, BEAM_TICKS);

        player.sendMessage(
            Component.text("✨ 힘 1 + 저항 1 — " + BUFF_SEC + "초 부여!")
                .color(NamedTextColor.GREEN));
    }

    private static void setModel(ItemDisplay display, String modelString) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(modelString));
        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
        display.setItemStack(item);
    }
}
