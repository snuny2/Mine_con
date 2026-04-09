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
    private final Map<UUID, Integer>    gaugeMap = new HashMap<>();
    private final Map<UUID, BukkitTask> taskMap  = new HashMap<>();

    public static final int MAX_GAUGE = 10;

    // 리소스팩 font/default.json 에서 매핑한 게이지 프레임 유니코드
    // GIF 프레임 수에 맞게 수정하세요
    private static final String[] GAUGE_CHARS = {
        "\uE100", "\uE101", "\uE102", "\uE103", "\uE104",
        "\uE105", "\uE106", "\uE107", "\uE108", "\uE109", "\uE10A"
    };

    public GaugeManager(CustomSkillPlugin plugin) {
        this.plugin = plugin;
    }

    public void startCharging(Player player) {
        UUID id = player.getUniqueId();
        if (taskMap.containsKey(id)) return;

        gaugeMap.put(id, 0);
        showFrame(player, 0);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                int cur = gaugeMap.getOrDefault(id, 0) + 1;
                gaugeMap.put(id, cur);
                showFrame(player, cur);

                if (cur >= MAX_GAUGE) {
                    cancel();
                    taskMap.remove(id);
                    // 게이지 MAX → 원거리 스킬 발동
                    Skill_3.fire(player, plugin);
                    new BukkitRunnable() {
                        @Override public void run() {
                            gaugeMap.remove(id);
                            clearFrame(player);
                        }
                    }.runTaskLater(plugin, 10L);
                }
            }
        }.runTaskTimer(plugin, 0L, 4L); // 4틱 = 0.2초마다 프레임 전환

        taskMap.put(id, task);
    }

    public void stopCharging(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask t = taskMap.remove(id);
        if (t != null) t.cancel();
        gaugeMap.remove(id);
        clearFrame(player);
    }

    public boolean isCharging(Player player) {
        return taskMap.containsKey(player.getUniqueId());
    }

    public void cancelAll() {
        taskMap.values().forEach(BukkitTask::cancel);
        taskMap.clear();
        gaugeMap.clear();
    }

    private void showFrame(Player player, int frame) {
        int idx = Math.min(frame, GAUGE_CHARS.length - 1);
        player.sendActionBar(
            Component.text(GAUGE_CHARS[idx])
                .font(Key.key("minecraft", "default"))
                .color(TextColor.color(0xFFFFFF))
        );
    }

    private void clearFrame(Player player) {
        player.sendActionBar(Component.empty());
    }
}
