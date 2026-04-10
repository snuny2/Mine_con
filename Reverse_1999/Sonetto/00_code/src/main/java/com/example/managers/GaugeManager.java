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
    // 현재 재생 중인 태스크
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

    // 휘두르기 오버레이 유니코드
    private static final String SWING_CHAR = "\uE200";

    // GIF 100ms = 2틱
    private static final long FRAME_TICKS = 2L;

    public GaugeManager(CustomSkillPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 한 번 누르면 게이지 애니메이션 자동 재생 → 끝나면 스킬 발동
     * 재생 중이면 무시 (중복 실행 방지)
     */
    public void startCharging(Player player) {
        UUID id = player.getUniqueId();
        if (taskMap.containsKey(id)) return; // 이미 재생 중

        final int[] frame = {0};

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (frame[0] >= GAUGE_CHARS.length) {
                    // 모든 프레임 재생 완료
                    cancel();
                    taskMap.remove(id);

                    // 휘두르기 오버레이 표시 + 스킬 발동
                    showSwingOverlay(player);
                    Skill_3.fire(player, plugin);

                    // 0.8초 후 액션바 클리어
                    new BukkitRunnable() {
                        @Override public void run() { clearFrame(player); }
                    }.runTaskLater(plugin, 16L);
                    return;
                }

                showFrame(player, frame[0]);
                frame[0]++;
            }
        }.runTaskTimer(plugin, 0L, FRAME_TICKS);

        taskMap.put(id, task);
    }

    public void stopCharging(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask t = taskMap.remove(id);
        if (t != null) t.cancel();
        clearFrame(player);
    }

    public boolean isCharging(Player player) {
        return taskMap.containsKey(player.getUniqueId());
    }

    public void cancelAll() {
        taskMap.values().forEach(BukkitTask::cancel);
        taskMap.clear();
    }

    // 게이지 프레임 액션바 표시
    private void showFrame(Player player, int frame) {
        player.sendActionBar(
            Component.text(GAUGE_CHARS[frame])
                .font(Key.key("minecraft", "default"))
                .color(TextColor.color(0xFFFFFF))
        );
    }

    // 휘두르기 오버레이 (Title Subtitle로 화면 중앙 표시)
    private void showSwingOverlay(Player player) {
        player.sendActionBar(
            Component.text(SWING_CHAR)
                .font(Key.key("minecraft", "default"))
                .color(TextColor.color(0xFFFFFF))
        );
    }

    private void clearFrame(Player player) {
        player.sendActionBar(Component.empty());
    }
}
