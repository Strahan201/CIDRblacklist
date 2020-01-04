package com.sylvcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.sylvcraft.CIDRblacklist;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLogin implements Listener {
  CIDRblacklist plugin;
  
  public PlayerLogin(CIDRblacklist instance) {
    plugin = instance;
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent e) {
    if (!plugin.getConfig().getBoolean("config.active", true)) return;
    
    String blockMessage = plugin.isBlocked(e.getRealAddress().getHostAddress());
    if (blockMessage.equals("*clear*")) return;
 
    e.setKickMessage(blockMessage);
    e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
  }
}