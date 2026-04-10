package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class Skill_2 {

    private static final int BUFF_SEC = 5;

    // beam: \uE300~\uE315 (22프레임)
    private static final String[] BEAM_CHARS = {
        "\uE300", "\uE301", "\uE302", "\uE303", "\uE304",
        "\uE305", "\uE306", "\uE307", "\uE308", "\uE309",
        "\uE30A", "\uE30B", "\uE30C", "\uE30D", "\uE30E",
        "\uE30F", "\uE310", "\uE311", "\uE312", "\uE313",
        "\uE314", "\uE315"
    };

    // circle: \uE400~\uE407 (8프레임)
    private static final String[] CIRCLE_CHARS = {
        "\uE400", "\uE401", "\uE402", "\uE403",
        "\uE404", "\uE405", "\uE406", "\uE407"
    };

    // 1.5초 = 30틱, beam 22프레임 → 약 1~2틱/프레임
    // circle 8프레임 → 약 3~4틱/프레임
    // beam 간격: 30/22 ≈ 1틱
    // circle 간격: 30/8 ≈ 4틱
    private static final long BEAM_INTERVAL   = 1L;
    private static final long CIRCLE_INTERVAL = 4L;

    public static void cast(Player player, CustomSkillPlugin plugin) {
        // 포션 효과
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, BUFF_SEC * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, BUFF_SEC * 20, 0, false, true, true));

        // 사운드
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);

        // beam 이펙트 (타이틀 서브타이틀로 화면 중앙 세로)
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= BEAM_CHARS.length) { cancel(); return; }
                player.showTitle(Title.title(
                    Component.empty(),
                    Component.text(BEAM_CHARS[frame])
                        .font(Key.key("minecraft", "default"))
                        .color(TextColor.color(0xFFFFFF)),
                    Title.Times.times(
                        Duration.ZERO,
                        Duration.ofMillis(BEAM_INTERVAL * 50 + 50),
                        Duration.ZERO)
                ));
                frame++;
            }
        }.runTaskTimer(plugin, 0L, BEAM_INTERVAL);

        // circle 이펙트 (액션바로 바닥 원형)
        new BukkitRunnable() {
            int frame = 0;
            @Override
            public void run() {
                if (frame >= CIRCLE_CHARS.length) {
                    player.sendActionBar(Component.empty());
                    cancel();
                    return;
                }
                player.sendActionBar(
                    Component.text(CIRCLE_CHARS[frame])
                        .font(Key.key("minecraft", "default"))
                        .color(TextColor.color(0xFFFFFF))
                );
                frame++;
            }
        }.runTaskTimer(plugin, 0L, CIRCLE_INTERVAL);

        player.sendMessage(
            Component.text("✨ 힘 1 + 저항 1 — " + BUFF_SEC + "초 부여!")
                .color(NamedTextColor.GREEN));
    }
}
