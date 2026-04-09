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

    public static final int ITEM_MODEL_DATA = 1001;
    public static final int PROJECTILE_MODEL_DATA = 1002;

    @Override
    public void onEnable() {
        instance = this;
        cooldownManager = new CooldownManager();
        gaugeManager = new GaugeManager(this);

        // plugin 인스턴스를 직접 넘겨야 함
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

    public static CustomSkillPlugin getInstance() {
        return instance;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public GaugeManager getGaugeManager() {
        return gaugeManager;
    }
}
