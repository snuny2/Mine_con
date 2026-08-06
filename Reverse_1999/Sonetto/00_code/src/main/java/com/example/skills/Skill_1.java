package com.example.customskill.skills;

import com.example.customskill.CustomSkillPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.ActiveModel;

public class Skill_1 {

    private static final double AOE_SIZE = 4.5;
    // 모델 이름 (meg list에서 확인한 이름)
    private static final String CRYSTAL_MODEL = "skillcrystal_mc";

    public static void cast(Player player, CustomSkillPlugin plugin) {
        Location base = player.getLocation().clone();
        base.setYaw(0f);
        base.setPitch(0f);

        List<ItemDisplay> displays = new ArrayList<>();

        // ── 마법진 4개 ──────────────────────────────────
        displays.add(spawnDisplay(base.clone().add(0, 0.05, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(-90), 1, 0, 0),
                new Vector3f(9, 9, 1), "skill1_0009"));
        displays.add(spawnDisplay(base.clone().add(0, 4.0, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(9, 9, 1), "skill1_0011"));
        displays.add(spawnDisplay(base.clone().add(0, 3.2, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(3, 3, 1), "skill1_0012"));
        displays.add(spawnDisplay(base.clone().add(0, 3.5, 0), base.getWorld(),
                new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
                new Vector3f(4, 4, 1), "skill1_0010"));

        // ── 크리스탈 소환 (try-catch로 감싸서 실패해도 나머지 진행) ──
        final Entity[] holderRef = { null };
        try {
            Entity holder = base.getWorld().spawn(base, ArmorStand.class, as -> {
                as.setInvisible(true);
                as.setGravity(false);
                as.setMarker(true); // 콜리전 없음 → 데미지 안 맞음
                as.setInvulnerable(true);
            });
            holderRef[0] = holder;

            ModeledEntity modeled = ModelEngineAPI.createModeledEntity(holder);
            ActiveModel active = ModelEngineAPI.createActiveModel(CRYSTAL_MODEL);
            modeled.addModel(active, true);
        } catch (Exception ex) {
            // 크리스탈 실패해도 스킬은 계속 진행
            if (holderRef[0] != null)
                holderRef[0].remove();
            holderRef[0] = null;
            plugin.getLogger().warning("크리스탈 소환 실패: " + ex.getMessage());
        }

        // ── 데미지 5번 ──────────────────────────────────
        long[] damageTiming = { 12L, 16L, 18L, 20L, 32L };
        double[] damageAmount = { 4.0, 4.0, 4.0, 4.0, 10.0 };

        for (int i = 0; i < damageTiming.length; i++) {
            final double dmg = damageAmount[i];
            final boolean isLast = (i == damageTiming.length - 1);
            final int hitNum = i + 1;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Collection<Entity> nearby = base.getWorld()
                            .getNearbyEntities(base, AOE_SIZE, AOE_SIZE, AOE_SIZE);
                    int hit = 0;
                    for (Entity entity : nearby) {
                        if (!(entity instanceof LivingEntity))
                            continue;
                        if (entity.equals(player))
                            continue;
                        if (entity instanceof ItemDisplay)
                            continue;
                        if (entity instanceof ArmorStand)
                            continue; // 크리스탈 홀더 제외

                        LivingEntity target = (LivingEntity) entity;

                        if (!isLast) {
                            org.bukkit.util.Vector beforeVel = target.getVelocity().clone();
                            target.setNoDamageTicks(0);
                            target.damage(dmg, player);
                            target.setVelocity(beforeVel);
                            player.playSound(base, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1.2f);
                        } else {
                            target.setNoDamageTicks(0);
                            target.damage(dmg, player);
                        }

                        player.sendMessage(
                                Component.text("💥 " + hitNum + "타 데미지: ").color(NamedTextColor.GOLD)
                                        .append(Component.text(String.valueOf(dmg)).color(NamedTextColor.RED))
                                        .append(Component.text(" → " + target.getName()
                                                + " (남은 체력: " + String.format("%.1f", target.getHealth()) + ")")
                                                .color(NamedTextColor.YELLOW)));
                        hit++;
                    }

                    if (isLast) {
                        player.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
                        player.playSound(base, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);

                        base.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,
                                base.clone().add(0, -1, 0), 1, 0, 0, 0, 0);
                        base.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                                base.clone().add(0, 1, 0), 40, 2.0, 1.0, 2.0, 0.05);
                        base.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                                base.clone().add(0, 0.5, 0), 15, 1.5, 0.3, 1.5, 0.02);

                        player.sendMessage(
                                Component.text("⚡ 광역 공격 완료! (" + hit + "마리)").color(NamedTextColor.AQUA));
                    }
                }
            }.runTaskLater(plugin, damageTiming[i]);
        }

        // ── 1.6초 후 마법진 + 크리스탈 제거 ──────────────
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ItemDisplay d : displays) {
                    if (!d.isDead())
                        d.remove();
                }
                if (holderRef[0] != null && !holderRef[0].isDead()) {
                    holderRef[0].remove();
                }
            }
        }.runTaskLater(plugin, 32L);
    }

    private static ItemDisplay spawnDisplay(Location loc, World world,
            AxisAngle4f rotation, Vector3f scale, String modelString) {
        return world.spawn(loc, ItemDisplay.class, d -> {
            d.setBillboard(Display.Billboard.FIXED);
            d.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), rotation, scale,
                    new AxisAngle4f(0, 0, 1, 0)));
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = item.getItemMeta();
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            cmd.setStrings(List.of(modelString));
            meta.setCustomModelDataComponent(cmd);
            item.setItemMeta(meta);
            d.setItemStack(item);
        });
    }
}
