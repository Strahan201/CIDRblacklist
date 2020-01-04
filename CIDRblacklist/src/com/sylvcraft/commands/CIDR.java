package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import com.sylvcraft.CIDRblacklist;

public class CIDR implements TabExecutor {
  CIDRblacklist plugin;
  
  public CIDR(CIDRblacklist plugin) {
    this.plugin = plugin;
  }
  
  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (args.length == 1) return getMatchedAsType(args[0], new ArrayList<String>(Arrays.asList("add", "del", "list", "calc", "test", "reload")));
    return null;
  }
  
  List<String> getMatchedAsType(String typed, List<String> values) {
    List<String> ret = new ArrayList<String>();
    for (String element : values) if (element.startsWith(typed)) ret.add(element);
    return ret;
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!sender.hasPermission("cidrblacklist.admin")) {
      plugin.msg("access-denied", sender);
      return true;
    }
    
    if (args.length == 0) {
      plugin.msg("help", sender);
      return true;
    }
    
    Map<String, String> data = new HashMap<String, String>();
    switch (args[0].toLowerCase()) {

    /* Adding new CIDR block(s) to the list */
    case "add":
      if (args.length == 1) {
        plugin.msg("help", sender);
        return true;
      }

      for (int x=1; x<args.length; x++) {
        String ip = ""; int bits = 32;
        String[] tmp = args[x].split("/");
  
        ip = tmp[0];
        data.put("%ip%", tmp[0]);
        data.put("%bits%", (tmp.length == 1)?"32":tmp[1]);
        data.put("%cidr%", args[x]);
  
        try {
          if (tmp.length == 2) bits = Integer.valueOf(tmp[1]);
        } catch (NumberFormatException ex) {
          plugin.msg("invalid-bits", sender, data);
          return true;
        }
  
        if (bits > 32 || bits < 1) {
          plugin.msg("invalid-bits", sender, data);
          return true;
        }
        if (!isIPv4(ip)) {
          plugin.msg("invalid-ip", sender, data);
          return true;
        }
        
        plugin.msg("cidr-add-" + ((plugin.addCIDR(ip + "/" + String.valueOf(bits)))?"good":"fail"), sender, data);
      }
      break;

      
    /* Removing CIDR block(s) from the list */
    case "del":
      if (args.length == 1) {
        plugin.msg("help", sender);
        return true;
      }

      for (int x=1; x<args.length; x++) {
        data.put("%cidr%", args[x]);
        plugin.msg("cidr-remove-" + ((plugin.delCIDR(args[x] + ((args[x].indexOf('/') == -1)?"/32":"")))?"good":"fail"), sender, data);
      }
      break;

      
    /* Test an IP against the blacklist */
    case "test":
      if (args.length == 1) {
        plugin.msg("help", sender);
        return true;
      }

      data.put("%ip%", args[1]);
      if (!isIPv4(args[1])) {
        plugin.msg("invalid-ip", sender, data);
        return true;
      }
      
      plugin.msg("test-" + ((!plugin.isBlocked(args[1]).equals("*clear*"))?"good":"fail"), sender, data);
      break;
      
      
    /* List all blocked CIDR */
    case "list":
      if (plugin.cidrList.size() == 0) {
        plugin.msg("list-none", sender);
        return true;
      }
      
      plugin.msg("list-header", sender);
      for (String cidr : plugin.cidrList) {
        data.put("%cidr%", cidr);
        plugin.msg("list-data", sender, data);
      }
      
      plugin.msg("list-footer", sender);
      break;

      
    /* Give user path to an online CIDR calculator
     * Alas, I don't have the motivation to write one here at this time lol */
    case "calc":
      plugin.msg("calc-url", sender);
      break;

      
    /* List all blocked CIDR */
    case "reload":
      plugin.reloadConfig();
      plugin.msg("reloaded", sender);
      break;
      
      
    /* Set the message sent when an IP is blocked */
    case "reason":
      if (args.length == 1) {
        plugin.listReasons(sender, data);
        return true;
      }
      
      String cidr = (args[1].equals("*")?"global":args[1]);
      if (cidr.indexOf("/") == -1 && !cidr.equalsIgnoreCase("global")) cidr += "/32";
      data.put("%cidr%", cidr);

      if (args.length == 2) {
        if (!cidr.equals("global") && !plugin.getConfig().getStringList("blocked").contains(cidr)) {
          plugin.msg("cidr-remove-fail", sender, data);
          return true;
        }
        
        plugin.getConfig().set("reasons." + cidr.replace(".", "_"), null);
        plugin.saveConfig();
        plugin.msg("reason-removed", sender, data);
        return true;
      }
      
      String reason = StringUtils.join(args, " ", 2, args.length);
      data.put("%reason%", reason);

      if (!cidr.equals("global") && !plugin.getConfig().getStringList("blocked").contains(cidr)) {
        plugin.msg("cidr-remove-fail", sender, data);
        return true;
      }
      
      plugin.getConfig().set("reasons." + cidr.replace(".", "_"), reason);
      plugin.saveConfig();
      plugin.msg("reason-set", sender, data);
      break;
    }
    return true;
  }
  
  public boolean isIPv4(String ip){ 
    final Pattern validIP = Pattern.compile("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
    return validIP.matcher(ip).matches(); 
  }
}
