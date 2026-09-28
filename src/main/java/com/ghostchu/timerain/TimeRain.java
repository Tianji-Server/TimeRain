package com.ghostchu.timerain;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class TimeRain extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        Bukkit.getScheduler().runTaskTimer(this, () -> Bukkit.getWorlds().forEach(world -> {
            if (!world.hasStorm()) {
                world.setGameRule(GameRules.RANDOM_TICK_SPEED, 3);
            } else {
                world.setGameRule(GameRules.RANDOM_TICK_SPEED, 15);
                checkPlayers(world);
            }
        }), 10, 30);
    }

    private void checkPlayers(World world) {
        world.getPlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL)
                return;
            String biomeName = player.getLocation().getBlock().getBiome().getKey().getKey();
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
        if (player.getSaturation() > 0) {
            player.setSaturation(Math.max(player.getSaturation() - 1, 0f));
        } else {
            player.setFoodLevel(Math.max(player.getFoodLevel() - 1, 0));
        }
        player.sendActionBar("您感觉自己正在光速掉SAN中，请火速找个地方避雨消除效果！");
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
