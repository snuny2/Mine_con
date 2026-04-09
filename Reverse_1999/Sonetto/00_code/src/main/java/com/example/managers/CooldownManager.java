package com.example.customskill.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // 스킬 enum
    public enum Skill {
        GAUGE, // 우클릭 원거리 (Skill_3)
        BUFF, // 쉬프트+우클릭 버프 (Skill_2)
        AOE // 쉬프트+좌클릭 광역 (Skill_1)
    }

    // 쿨타임 상수 (밀리초)
    public static final long GAUGE_CD = 5_000L;
    public static final long BUFF_CD = 15_000L;
    public static final long AOE_CD = 30_000L;

    private final Map<UUID, Map<Skill, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(Player p, Skill skill) {
        var map = cooldowns.get(p.getUniqueId());
        if (map == null || !map.containsKey(skill))
            return false;
        return System.currentTimeMillis() < map.get(skill);
    }

    public double getRemaining(Player p, Skill skill) {
        var map = cooldowns.get(p.getUniqueId());
        if (map == null || !map.containsKey(skill))
            return 0;
        return Math.max(0,
                (map.get(skill) - System.currentTimeMillis()) / 1000.0);
    }

    public void setCooldown(Player p, Skill skill, long ms) {
        cooldowns
                .computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .put(skill, System.currentTimeMillis() + ms);
    }

    public void resetCooldown(Player p, Skill skill) {
        var map = cooldowns.get(p.getUniqueId());
        if (map != null)
            map.remove(skill);
    }

    public void resetAll(Player p) {
        cooldowns.remove(p.getUniqueId());
    }
}
