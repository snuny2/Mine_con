package com.example.customskill.listeners;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.managers.CooldownManager;
import com.example.customskill.managers.CooldownManager.Skill;
import com.example.customskill.managers.GaugeManager;
import com.example.customskill.skills.Skill_1;
import com.example.customskill.skills.Skill_2;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class SkillListener implements Listener {

    private final CustomSkillPlugin plugin;
    private final CooldownManager   cdm;
    private final GaugeManager      gm;

    public SkillListener(CustomSkillPlugin plugin) {
        this.plugin = plugin;
        this.cdm    = plugin.getCooldownManager();
        this.gm     = plugin.getGaugeManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        if (!isCustomItem(player.getInventory().getItemInMainHand())) return;
        e.setCancelled(true);

        boolean sneak      = player.isSneaking();
        boolean rightClick = e.getAction() == Action.RIGHT_CLICK_AIR
                          || e.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean leftClick  = e.getAction() == Action.LEFT_CLICK_AIR
                          || e.getAction() == Action.LEFT_CLICK_BLOCK;

        // 쉬프트 + 우클릭 → Skill_2 버프
        if (sneak && rightClick) {
            if (cdm.isOnCooldown(player, Skill.BUFF)) {
                sendCooldownMsg(player, "버프", cdm.getRemaining(player, Skill.BUFF));
                return;
            }
            Skill_2.cast(player, plugin);
            cdm.setCooldown(player, Skill.BUFF, CooldownManager.BUFF_CD);
            return;
        }

        // 쉬프트 + 좌클릭 → Skill_1 광역
        if (sneak && leftClick) {
            if (cdm.isOnCooldown(player, Skill.AOE)) {
                sendCooldownMsg(player, "광역", cdm.getRemaining(player, Skill.AOE));
                return;
            }
            Skill_1.cast(player, plugin);
            cdm.setCooldown(player, Skill.AOE, CooldownManager.AOE_CD);
            return;
        }

        // 우클릭 → 게이지 충전 → Skill_3 원거리
        if (!sneak && rightClick) {
            if (cdm.isOnCooldown(player, Skill.GAUGE)) {
                sendCooldownMsg(player, "원거리", cdm.getRemaining(player, Skill.GAUGE));
                return;
            }
            if (!gm.isCharging(player)) gm.startCharging(player);
        }
    }

    private void sendCooldownMsg(Player player, String skill, double sec) {
        player.sendMessage(
            Component.text(skill + " 쿨타임: ").color(NamedTextColor.RED)
                .append(Component.text(String.format("%.1f", sec) + "초")
                    .color(NamedTextColor.YELLOW))
        );
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        // 1.21.4+ 문자열 방식으로 확인
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        return cmd.getStrings().contains(CustomSkillPlugin.ITEM_MODEL_STRING);
    }
}
