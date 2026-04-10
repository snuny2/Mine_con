package com.example.customskill.managers;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.skills.Skill_3;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GaugeManager {

    private final CustomSkillPlugin plugin;
    private final Map<UUID, BukkitTask> taskMap = new HashMap<>();

    // GIF 29프레임 유니코드 \uE100 ~ \uE11C
    private static final String[] GAUGE_CHARS = {
        "\uE100", "\uE101", "\uE102", "\uE103", "\uE104",
        "\uE105", "\uE106", "\uE107", "\uE108", "\uE109",
        "\uE10A", "\uE10B", "\uE10C", "\uE10D", "\uE10E",
        "\uE10F", "\uE110", "\uE111", "\uE112", "\uE113",
        "\uE114", "\uE115", "\uE116", "\uE117", "\uE118",
        "\uE119", "\uE11A", "\uE11B", "\uE11C"
    };

    private static final long FRAME_TICKS = 1L;

    public GaugeManager(CustomSkillPlugin plugin) {
        this.plugin = plugin;
    }

    public void startCharging(Player player) {
        UUID id = player.getUniqueId();
        if (taskMap.containsKey(id)) return;

        final int[] frame = {0};

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (frame[0] >= GAUGE_CHARS.length) {
                    cancel();
                    taskMap.remove(id);
                    // 게이지 완료 → 휘두르기 이미지 표시 후 스킬 발동
                    showActionBar(player, CustomSkillPlugin.SWING_CHAR);
                    Skill_3.fire(player, plugin);
                    // 투사체 발사 후 액션바 클리어
                    new BukkitRunnable() {
                        @Override public void run() { clearActionBar(player); }
                    }.runTaskLater(plugin, 20L);
                    return;
                }
                showActionBar(player, GAUGE_CHARS[frame[0]]);
                frame[0]++;
            }
        }.runTaskTimer(plugin, 0L, FRAME_TICKS);

        taskMap.put(id, task);
    }

    public void stopCharging(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask t = taskMap.remove(id);
        if (t != null) t.cancel();
        clearActionBar(player);
    }

    public boolean isCharging(Player player) {
        return taskMap.containsKey(player.getUniqueId());
    }

    public void cancelAll() {
        taskMap.values().forEach(BukkitTask::cancel);
        taskMap.clear();
    }

    private void showActionBar(Player player, String text) {
        player.sendActionBar(
            Component.text(text)
                .font(Key.key("minecraft", "default"))
                .color(TextColor.color(0xFFFFFF))
        );
    }

    private void clearActionBar(Player player) {
        player.sendActionBar(Component.empty());
    }
}
