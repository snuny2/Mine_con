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

    // 1.21.4+ 문자열 방식 custom_model_data
    public static final String ITEM_MODEL_STRING       = "sonetto";    // 커스텀 무기
    public static final String PROJECTILE_MODEL_STRING = "projectile"; // 투사체

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
