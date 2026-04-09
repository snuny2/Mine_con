package com.example.customskill.commands;

import com.example.customskill.CustomSkillPlugin;
import com.example.customskill.managers.CooldownManager.Skill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DebugCommand implements CommandExecutor {

        private final CustomSkillPlugin plugin;

        public DebugCommand(CustomSkillPlugin plugin) {
                this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd,
                        String label, String[] args) {
                if (!(sender instanceof Player player)) {
                        sender.sendMessage("플레이어만 사용 가능합니다.");
                        return true;
                }

                if (args.length == 0) {
                        sendHelp(player);
                        return true;
                }

                var cdm = plugin.getCooldownManager();

                switch (args[0].toLowerCase()) {
                        case "reset" -> {
                                cdm.resetAll(player);
                                plugin.getGaugeManager().stopCharging(player);
                                player.sendMessage(Component.text(
                                                "✅ 모든 쿨타임 초기화!").color(NamedTextColor.GREEN));
                        }
                        case "reset1" -> {
                                cdm.resetCooldown(player, Skill.GAUGE);
                                player.sendMessage(Component.text(
                                                "✅ 1스킬 쿨타임 초기화!").color(NamedTextColor.GREEN));
                        }
                        case "reset2" -> {
                                cdm.resetCooldown(player, Skill.BUFF);
                                player.sendMessage(Component.text(
                                                "✅ 2스킬 쿨타임 초기화!").color(NamedTextColor.GREEN));
                        }
                        case "reset3" -> {
                                cdm.resetCooldown(player, Skill.AOE);
                                player.sendMessage(Component.text(
                                                "✅ 3스킬 쿨타임 초기화!").color(NamedTextColor.GREEN));
                        }
                        case "status" -> {
                                player.sendMessage(Component.text(
                                                "=== 쿨타임 상태 ===").color(NamedTextColor.AQUA));
                                player.sendMessage(Component.text(
                                                "원거리: " + fmt(cdm.getRemaining(player, Skill.GAUGE)))
                                                .color(NamedTextColor.YELLOW));
                                player.sendMessage(Component.text(
                                                "버프:   " + fmt(cdm.getRemaining(player, Skill.BUFF)))
                                                .color(NamedTextColor.YELLOW));
                                player.sendMessage(Component.text(
                                                "광역:   " + fmt(cdm.getRemaining(player, Skill.AOE)))
                                                .color(NamedTextColor.YELLOW));
                        }
                        case "damage" -> {
                                double dmg = args.length >= 2
                                                ? Double.parseDouble(args[1])
                                                : 7.0;
                                player.damage(dmg);
                                player.sendMessage(Component.text(
                                                "💉 " + dmg + " 데미지. 현재 체력: "
                                                                + String.format("%.1f", player.getHealth()))
                                                .color(NamedTextColor.RED));
                        }
                        case "give" -> {
                                player.getInventory().addItem(makeItem());
                                player.sendMessage(Component.text(
                                                "✅ 커스텀 아이템 지급!").color(NamedTextColor.GREEN));
                        }
                        default -> sendHelp(player);
                }
                return true;
        }

        private String fmt(double sec) {
                return sec <= 0 ? "준비됨" : String.format("%.1f", sec) + "초";
        }

        private void sendHelp(Player player) {
                player.sendMessage(Component.text(
                                "=== /skilltest 명령어 ===").color(NamedTextColor.GOLD));
                String[][] cmds = {
                                { "reset", "모든 쿨타임 초기화" },
                                { "reset1", "원거리 쿨타임 초기화" },
                                { "reset2", "버프 쿨타임 초기화" },
                                { "reset3", "광역 쿨타임 초기화" },
                                { "status", "쿨타임 상태 확인" },
                                { "damage [n]", "데미지 테스트" },
                                { "give", "커스텀 아이템 지급" },
                };
                for (String[] c : cmds) {
                        player.sendMessage(
                                        Component.text("/skilltest " + c[0] + "  ")
                                                        .color(NamedTextColor.YELLOW)
                                                        .append(Component.text(c[1])
                                                                        .color(NamedTextColor.WHITE)));
                }
        }

        private ItemStack makeItem() {
                ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text("유리 만연필")
                                .color(NamedTextColor.GOLD));
                meta.setCustomModelData(1001);
                item.setItemMeta(meta);
                return item;
        }
}