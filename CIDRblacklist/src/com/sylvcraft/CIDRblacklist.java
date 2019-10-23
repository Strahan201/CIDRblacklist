package com.sylvcraft;

import org.bukkit.plugin.java.JavaPlugin;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import com.sylvcraft.commands.CIDR;
import com.sylvcraft.events.PlayerLogin;

public class CIDRblacklist extends JavaPlugin {
	public List<String> cidrList = new ArrayList<String>();
	
  @Override
  public void onEnable() {
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new PlayerLogin(this), this);
    getCommand("cidr").setExecutor(new CIDR(this));
    
    saveDefaultConfig();
    cidrList = getConfig().getStringList("blocked");
  }
  
  public boolean isBlocked(String ip) {
  	for (String cidr : cidrList) {
  		try {
  			CIDRUtils cu = new CIDRUtils(cidr);
  			if (cu.isInRange(ip)) return true;
  		} catch (UnknownHostException ex) {
  		}
  	}
  	return false;
  }

  public boolean addCIDR(String cidr) {
  	if (cidrList.contains(cidr)) return false;
  	cidrList.add(cidr);
  	getConfig().set("blocked", cidrList);
  	saveConfig();
  	return true;
  }
  
  public boolean delCIDR(String cidr) {
  	if (!cidrList.contains(cidr)) return false;
  	cidrList.remove(cidr);
  	getConfig().set("blocked", cidrList);
  	saveConfig();
  	return true;
  }
  

  public void msg(String msgCode, CommandSender sender) {
    if (getConfig().getString("messages." + msgCode) == null) return;
    msgTransmit(getConfig().getString("messages." + msgCode), sender);
  }

  public void msg(String msgCode, CommandSender sender, Map<String, String> data) {
    if (getConfig().getString("messages." + msgCode) == null) return;
    String tmp = getConfig().getString("messages." + msgCode, msgCode);
    for (Map.Entry<String, String> mapData : data.entrySet()) {
      tmp = tmp.replace(mapData.getKey(), mapData.getValue());
    }
    msgTransmit(tmp, sender);
  }

  public void msgTransmit(String msg, CommandSender sender) {
    for (String m : (msg + " ").split("%br%")) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
    }
  }
}
