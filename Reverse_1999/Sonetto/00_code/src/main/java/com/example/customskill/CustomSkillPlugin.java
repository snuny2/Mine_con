package com.example.customskill;

import com.example.customskill.commands.DebugCommand;
import com.example.customskill.listeners.SkillListener;
import com.example.customskill.managers.CooldownManager;
import com.example.customskill.managers.GaugeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomSkillPlugin extends JavaPlugin {

    private static CustomSkillPlugin instance;
    private CooldownManager cooldownManager;
    private GaugeManager gaugeManager;

    public static final String ITEM_MODEL_STRING       = "sonetto";
    public static final String PROJECTILE_MODEL_STRING = "projectile";

    // ── 폰트 유니코드 배치 ──────────────────────────────
    // 게이지 프레임: \uE100 ~ \uE11C (29개, GaugeManager에서 직접 사용)

    // 스킬1 (원거리) 투사체
    public static final String PROJ_1_CHAR  = "\uE200"; // 투사체1 이미지
    public static final String PROJ_2_CHAR  = "\uE201"; // 투사체2 이미지
    public static final String SWING_CHAR   = "\uE202"; // 휘두르기 이펙트

    // 스킬2 (버프) - 이미지 미정, 나중에 추가
    // public static final String BUFF_OVERLAY = "\uE300";

    // 스킬3 (광역) - 이미지 미정, 나중에 추가
    // public static final String AOE_OVERLAY  = "\uE400";

    @Override
    public void onEnable() {
        instance        = this;
        cooldownManager = new CooldownManager();
        gaugeManager    = new GaugeManager(this);

        getServer().getPluginManager()
            .registerEvents(new SkillListener(this), this);
        getCommand("skilltest")
            .setExecutor(new DebugCommand(this));

        getLogger().info("CustomSkillItem 활성화!");
    }

    @Override
    public void onDisable() {
        gaugeManager.cancelAll();
        getLogger().info("CustomSkillItem 비활성화!");
    }

    public static CustomSkillPlugin getInstance() { return instance; }
    public CooldownManager getCooldownManager()   { return cooldownManager; }
    public GaugeManager    getGaugeManager()      { return gaugeManager; }
}
