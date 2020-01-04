package com.sylvcraft;

import org.bukkit.plugin.java.JavaPlugin;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
    updateConfig();
  }
  
  private void updateConfig() {
    String oldGlobal = getConfig().getString("messages.blocked");
    if (oldGlobal == null) return;
    
    getConfig().set("reasons.global", oldGlobal);
    getConfig().set("messages.help", "'&ecidr &7[&eadd &6cidr&7] [&edel &6cidr&7] [&etest &6ip&7] [&elist&7] [&ecalc&7] [&ereason &6cidr|* (reason|*d)&7]'");
    getConfig().set("messages.reason-set", "&eBlock reason set to \"&6%reason%&e\" for CIDR &6%cidr%");
    getConfig().set("messages.reason-removed", "&eCustom block reason removed for CIDR &6%cidr%");
    getConfig().set("messages.reasons-list-none", "&cThere are no reasons set!");
    getConfig().set("messages.reasons-list-header", "&eBlock reasons:%br%");
    getConfig().set("messages.reasons-list-data", "&6%cidr% &7- &6%reason%");
    getConfig().set("messages.blocked", null);
    saveConfig();
  }
  
  public String isBlocked(String ip) {
    for (String cidr : cidrList) {
      try {
        CIDRUtils cu = new CIDRUtils(cidr);
        if (!cu.isInRange(ip)) continue;
        
        String global = getConfig().getString("reasons.global", "");
        return getConfig().getString("reasons." + cidr.replace(".", "_"), global);
      } catch (UnknownHostException ex) {
      }
    }
    return "*clear*";
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
  
  public void listReasons(CommandSender sender, Map<String, String> data) {
    ConfigurationSection cfg = getConfig().getConfigurationSection("reasons");
    if (cfg == null) {
      msg("reasons-list-none", sender);
      return;
    }
    
    msg("reasons-list-header", sender);
    for (String cidr : cfg.getKeys(false)) {
      data.put("%cidr%", cidr.replace("_", "."));
      data.put("%reason%", cfg.getString(cidr));
      msg("reasons-list-data", sender, data);
    }
    msg("reasons-list-footer", sender);
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
