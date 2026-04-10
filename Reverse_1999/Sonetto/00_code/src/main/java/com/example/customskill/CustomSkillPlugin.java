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

    // custom_model_data 문자열
    public static final String ITEM_MODEL_STRING  = "sonetto";
    public static final String PROJ_1_MODEL       = "projectile_1"; // 투사체1 모델
    public static final String PROJ_2_MODEL       = "projectile_2"; // 투사체2 모델

    // 액션바 유니코드 (게이지/휘두르기용)
    public static final String SWING_CHAR = "\uE202";

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
