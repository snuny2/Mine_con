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
    private static final long BEAM_TICKS    = 1L;
    private static final long CIRCLE_TICKS  = 4L;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH, BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.RESISTANCE, BUFF_SEC * 20, 0, false, true, true));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        // yaw=0, pitch=0 으로 고정된 위치 (회전 상속 방지)
        Location base = player.getLocation().clone();
        base.setYaw(0f);
        base.setPitch(0f);

        Location circlePos = base.clone().add(0, 0.05, 0);
        Location beamPos   = base.clone().add(0, 2.0, 0);

        // ── Circle: 발밑 바닥, yaw 고정 ─────────────────────────
        ItemDisplay circleDisplay = circlePos.getWorld()
            .spawn(circlePos, ItemDisplay.class, d -> {
                d.setBillboard(Display.Billboard.FIXED);
                d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                    new Vector3f(4, 4, 1),
                    new AxisAngle4f(0, 0, 1, 0)
                ));
            });

        // ── Beam: 머리 위 2블록, yaw 고정, 항상 수직 ────────────
        ItemDisplay beamDisplay = beamPos.getWorld()
            .spawn(beamPos, ItemDisplay.class, d -> {
                d.setBillboard(Display.Billboard.FIXED);
                d.setTransformation(new Transformation(
                    new Vector3f(0, -1.5f, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(1.5f, 3f, 1.5f),
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
