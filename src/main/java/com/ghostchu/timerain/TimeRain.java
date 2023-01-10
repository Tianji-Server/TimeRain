package com.ghostchu.timerain;

import java.util.Locale;
import java.util.UUID;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class TimeRain extends JavaPlugin {
  public void onEnable() {
    saveDefaultConfig();
    Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> Bukkit.getWorlds().forEach(()), 10L, 30L);
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
            if (player.getLocation().getY() >= block.getLocation().getY())
              applyRainEffectToPlayer(player); 
          } 
        });
  }
  
  private void applyRainEffectToPlayer(Player player) {
    player.damage(0.5D);
    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
    if (player.getSaturation() > 0.0F) {
      player.setSaturation(Math.max(player.getSaturation() - 1.0F, 0.0F));
    } else {
      player.setFoodLevel(Math.max(player.getFoodLevel() - 1, 0));
    } 
    for (ItemStack item : player.getInventory().getArmorContents()) {
      if (item != null && item.getType().getMaxDurability() > 0) {
        short maxuses = item.getType().getMaxDurability();
        short durability = (short)(maxuses + 5 - item.getDurability());
        if (durability <= item.getType().getMaxDurability()) {
          item.setDurability(durability);
        } else {
          item.setType(Material.AIR);
        } 
      } 
    } 
    if (player.getInventory().getItemInMainHand().getType().getMaxDurability() > 0)
      player.getInventory().getItemInMainHand().setDurability((short)(player.getInventory().getItemInMainHand().getDurability() - 2)); 
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new UUID(0L, 0L), (BaseComponent)new TextComponent("));
  }
  
  public void onDisable() {}
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("timerain.toggle"))
      return false; 
    if (args.length == 0)
      return false; 
    getConfig().set("enabled", Boolean.valueOf(Boolean.parseBoolean(args[0])));
    saveConfig();
    sender.sendMessage("Time Rain is now " + (Boolean.parseBoolean(args[0]) ? "enabled" : "disabled"));
    return true;
  }
}

