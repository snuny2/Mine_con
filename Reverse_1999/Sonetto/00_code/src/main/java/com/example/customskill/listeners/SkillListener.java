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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class SkillListener implements Listener {

    private final CustomSkillPlugin plugin;
    private final CooldownManager cdm;
    private final GaugeManager gm;

    public SkillListener(CustomSkillPlugin plugin) {
        this.plugin = plugin;
        this.cdm = plugin.getCooldownManager();
        this.gm = plugin.getGaugeManager();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;

        Player player = e.getPlayer();
        if (!isCustomItem(player.getInventory().getItemInMainHand()))
            return;

        boolean sneak = player.isSneaking();
        boolean rightClick = e.getAction() == Action.RIGHT_CLICK_AIR
                || e.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean leftClick = e.getAction() == Action.LEFT_CLICK_AIR
                || e.getAction() == Action.LEFT_CLICK_BLOCK;

        // 우클릭 → 게이지 충전 후 원거리 발사 (Skill_3)
        if (!sneak && rightClick) {
            if (cdm.isOnCooldown(player, Skill.GAUGE))
                return;
            e.setCancelled(true);
            if (!gm.isCharging(player))
                gm.startCharging(player);
            return;
        }

        // 쉬프트 + 우클릭 → 버프 (Skill_2)
        if (sneak && rightClick) {
            if (cdm.isOnCooldown(player, Skill.BUFF))
                return;
            e.setCancelled(true);
            Skill_2.cast(player, plugin);
            cdm.setCooldown(player, Skill.BUFF, CooldownManager.BUFF_CD);
            return;
        }

        // 쉬프트 + 좌클릭 → 광역 (Skill_1)
        if (sneak && leftClick) {
            if (cdm.isOnCooldown(player, Skill.AOE))
                return;
            e.setCancelled(true);
            Skill_1.cast(player, plugin);
            cdm.setCooldown(player, Skill.AOE, CooldownManager.AOE_CD);
            return;
        }
    }

    @EventHandler
    public void onInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND)
            return;
        Player player = e.getPlayer();
        if (!isCustomItem(player.getInventory().getItemInMainHand()))
            return;

        boolean sneak = player.isSneaking();

        // 쉬프트 + 우클릭 → 버프 (엔티티 위에서도 스킬 발동)
        if (sneak) {
            if (cdm.isOnCooldown(player, Skill.BUFF))
                return; // 쿨타임 중 → 기본 동작
            e.setCancelled(true);
            Skill_2.cast(player, plugin);
            cdm.setCooldown(player, Skill.BUFF, CooldownManager.BUFF_CD);
        } else {
            if (cdm.isOnCooldown(player, Skill.GAUGE))
                return;
            e.setCancelled(true);
            if (!gm.isCharging(player))
                gm.startCharging(player);
        }

        // 우클릭 → 원거리 (엔티티 위에서도 게이지 시작)
        if (cdm.isOnCooldown(player, Skill.GAUGE))
            return;
        e.setCancelled(true);
        if (!gm.isCharging(player))
            gm.startCharging(player);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player))
            return;
        if (!isCustomItem(player.getInventory().getItemInMainHand()))
            return;

        // 쉬프트 + 좌클릭 → 광역 스킬 (기본 공격 차단)
        if (player.isSneaking()) {
            if (cdm.isOnCooldown(player, Skill.AOE))
                return; // 쿨타임 중 → 기본 공격
            e.setCancelled(true);
            Skill_1.cast(player, plugin);
            cdm.setCooldown(player, Skill.AOE, CooldownManager.AOE_CD);
        }
    }

    private void sendCooldownMsg(Player player, String skill, double sec) {
        player.sendMessage(
                Component.text(skill + " 쿨타임: ").color(NamedTextColor.RED)
                        .append(Component.text(String.format("%.1f", sec) + "초")
                                .color(NamedTextColor.YELLOW)));
    }

    private boolean isCustomItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if (!item.hasItemMeta())
            return false;
        ItemMeta meta = item.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        return cmd.getStrings().contains(CustomSkillPlugin.ITEM_MODEL_STRING);
    }
}
