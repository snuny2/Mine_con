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
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Skill_2 {

    private static final int  BUFF_SEC       = 5;
    private static final int  BEAM_FRAMES    = 22;
    private static final int  CIRCLE_FRAMES  = 8;
    // 1.5초 = 30틱 총 재생
    // beam: 30틱/22프레임 ≈ 1틱/프레임
    // circle: 30틱/8프레임 ≈ 4틱/프레임
    private static final long BEAM_TICKS     = 1L;
    private static final long CIRCLE_TICKS   = 4L;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        // 포션 효과
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH, BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.RESISTANCE, BUFF_SEC * 20, 0, false, true, true));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        Location center = player.getLocation();

        // ── Circle 이펙트 (발밑 바닥, 4x4블록) ──────────────────
        // ItemDisplay 엔티티로 바닥에 평평하게 배치
        ItemDisplay circleDisplay = (ItemDisplay) center.getWorld()
            .spawnEntity(center.clone().add(0, 0.05, 0), EntityType.ITEM_DISPLAY);

        // 4x4블록 = scale 4
        circleDisplay.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),           // translation
            new AxisAngle4f((float)Math.toRadians(90), 1, 0, 0), // X축 90도 회전 → 바닥에 눕힘
            new Vector3f(4, 4, 4),           // scale (4블록 크기)
            new AxisAngle4f(0, 0, 1, 0)      // 우회전 없음
        ));
        circleDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);

        // ── Beam 이펙트 (머리 위 2블록, 아래로 뻗음) ────────────
        // 머리 위 2블록 위치, 세로로 세움
        ItemDisplay beamDisplay = (ItemDisplay) center.getWorld()
            .spawnEntity(center.clone().add(0, 2, 0), EntityType.ITEM_DISPLAY);

        beamDisplay.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f(0, 0, 1, 0),     // 회전 없음 (세로)
            new Vector3f(1.5f, 3f, 1.5f),    // X,Z=1.5블록 너비, Y=3블록 높이(위2→바닥)
            new AxisAngle4f(0, 0, 1, 0)
        ));
        beamDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

        List<ItemDisplay> displays = List.of(circleDisplay, beamDisplay);

        // ── Circle 애니메이션 ─────────────────────────────────────
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= CIRCLE_FRAMES) {
                    circleDisplay.remove();
                    cancel();
                    return;
                }
                setDisplayModel(circleDisplay,
                    String.format("skill2_circle_%02d", frame));
                frame++;
            }
        }.runTaskTimer(plugin, 0L, CIRCLE_TICKS);

        // ── Beam 애니메이션 ───────────────────────────────────────
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= BEAM_FRAMES) {
                    beamDisplay.remove();
                    cancel();
                    return;
                }
                setDisplayModel(beamDisplay,
                    String.format("skill2_beam_%02d", frame));
                frame++;
            }
        }.runTaskTimer(plugin, 0L, BEAM_TICKS);

        player.sendMessage(
            Component.text("✨ 힘 1 + 저항 1 — " + BUFF_SEC + "초 부여!")
                .color(NamedTextColor.GREEN));
    }

    private static void setDisplayModel(ItemDisplay display, String modelString) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(modelString));
        meta.setCustomModelDataComponent(cmd);
        item.setItemMeta(meta);
        display.setItemStack(item);
    }
}
