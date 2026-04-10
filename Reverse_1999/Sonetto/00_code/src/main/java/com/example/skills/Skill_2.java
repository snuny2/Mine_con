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

    private static final int BUFF_SEC = 5;
    private static final int BEAM_FRAMES = 22;
    private static final int CIRCLE_FRAMES = 8;
    private static final int BUFF_FRAMES = 7;
    private static final long BEAM_TICKS = 1L;
    private static final long CIRCLE_TICKS = 4L;
    private static final long BUFF_TICKS = 2L; // 7프레임 × 2틱 = 14틱 = 0.7초

    // beam+circle 총 재생 시간: 약 30틱(1.5초) 후 버프 이펙트 시작
    private static final long BUFF_DELAY = 30L;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, BUFF_SEC * 20, 0, false, true, true));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        Location base = player.getLocation().clone();
        base.setYaw(0f);
        base.setPitch(0f);

        Location circlePos = base.clone().add(0, 0.05, 0);
        Location beamPos = base.clone().add(0, 3.0, 0);

        // ── Circle ──────────────────────────────────────────────
        ItemDisplay circleDisplay = circlePos.getWorld()
                .spawn(circlePos, ItemDisplay.class, d -> {
                    d.setBillboard(Display.Billboard.FIXED);
                    d.setTransformation(new Transformation(
                            new Vector3f(0, 0, 0),
                            new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                            new Vector3f(10, 10, 1),
                            new AxisAngle4f(0, 0, 1, 0)));
                });

        // ── Beam ────────────────────────────────────────────────
        ItemDisplay beamDisplay = beamPos.getWorld()
                .spawn(beamPos, ItemDisplay.class, d -> {
                    d.setBillboard(Display.Billboard.FIXED);
                    d.setTransformation(new Transformation(
                            new Vector3f(0, -1.5f, 0),
                            new AxisAngle4f(0, 0, 1, 0),
                            new Vector3f(1.5f, 3f, 1.5f),
                            new AxisAngle4f(0, 0, 1, 0)));
                });

        // Circle 애니메이션
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

        // Beam 애니메이션
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

        // ── 1.5초 후 버프 이펙트 (플레이어 위에 표시) ───────────
        new BukkitRunnable() {
            @Override
            public void run() {
                // 플레이어 현재 위치 기준 (이동했을 수 있으므로)
                Location buffPos = player.getLocation().clone();
                buffPos.setYaw(0f);
                buffPos.setPitch(0f);

                // 플레이어 머리 위 0.5블록
                ItemDisplay buffDisplay = buffPos.getWorld()
                        .spawn(buffPos.clone().add(0, 2.5, 0), ItemDisplay.class, d -> {
                            d.setBillboard(Display.Billboard.VERTICAL); // 항상 카메라 방향 바라봄
                            d.setTransformation(new Transformation(
                                    new Vector3f(0, 0, 0),
                                    new AxisAngle4f(0, 0, 1, 0),
                                    new Vector3f(1.5f, 3f, 1f), // 세로로 표시
                                    new AxisAngle4f(0, 0, 1, 0)));
                        });

                // 버프 이펙트 애니메이션
                new BukkitRunnable() {
                    int frame = 0;

                    @Override
                    public void run() {
                        if (frame >= BUFF_FRAMES) {
                            buffDisplay.remove();
                            cancel();
                            return;
                        }
                        setModel(buffDisplay, String.format("skill2_buff_%02d", frame));
                        frame++;
                    }
                }.runTaskTimer(plugin, 0L, BUFF_TICKS);
            }
        }.runTaskLater(plugin, BUFF_DELAY);

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
