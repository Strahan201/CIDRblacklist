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
		if (!plugin.isBlocked(e.getRealAddress().getHostAddress())) return;
		
		e.setKickMessage(plugin.getConfig().getString("messages.blocked", "I'm sorry, but connections from your IP address are disallowed!"));
		e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
  }
}