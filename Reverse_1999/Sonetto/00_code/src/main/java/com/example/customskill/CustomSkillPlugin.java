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

    // 아이템 custom_model_data 문자열
    public static final String ITEM_MODEL_STRING       = "sonetto";
    public static final String PROJECTILE_MODEL_STRING = "projectile";

    // 폰트 유니코드 상수
    public static final String PROJ_1_CHAR  = "\uE200"; // 투사체1
    public static final String PROJ_2_CHAR  = "\uE201"; // 투사체2
    public static final String SWING_CHAR   = "\uE202"; // 휘두르기

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
