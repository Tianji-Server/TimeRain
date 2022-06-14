package com.ghostchu.timerain;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.UUID;

public final class TimeRain extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getWorlds().forEach(world -> {
            if (!world.hasStorm()) {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
            } else {
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 15);
                checkPlayers(world);
            }
        }), 10, 30);
    }

    private void checkPlayers(World world) {
        world.getPlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL)
                return;
            String biomeName = player.getLocation().getBlock().getBiome().name().toLowerCase(Locale.ROOT);
            boolean rain = !biomeName.contains("desert");
            if (rain && biomeName.contains("savana"))
                rain = false;
            if (rain && biomeName.contains("savanna"))
                rain = false;
            if (rain && biomeName.contains("badland"))
                rain = false;
            if (rain) {
                Block block = player.getWorld().getHighestBlockAt(player.getLocation());
                if (player.getLocation().getY() >= block.getLocation().getY()) {
                    applyRainEffectToPlayer(player);
                }
            }
        });
    }

    private void applyRainEffectToPlayer(Player player) {
        player.damage(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
        if (player.getSaturation() > 0) {
            player.setSaturation(Math.max(player.getSaturation() - 1, 0f));
        } else {
            player.setFoodLevel(Math.max(player.getFoodLevel() - 1, 0));
        }
        // Reduce player items durability
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType().getMaxDurability() > 0) {
                short maxuses = item.getType().getMaxDurability();
                short durability = (short) (maxuses + 5 - item.getDurability());
                if (durability <= item.getType().getMaxDurability()) {
                    item.setDurability(durability);
                } else {
                    item.setType(Material.AIR);
                }
            }
        }
        if (player.getInventory().getItemInMainHand().getType().getMaxDurability() > 0) {
            player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() - 2));
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new UUID(0, 0), new TextComponent("找地方避雨！"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("timerain.toggle")) {
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        getConfig().set("enabled", Boolean.parseBoolean(args[0]));
        saveConfig();
        sender.sendMessage("Time Rain is now " + (Boolean.parseBoolean(args[0]) ? "enabled" : "disabled"));
        return true;
    }
}
