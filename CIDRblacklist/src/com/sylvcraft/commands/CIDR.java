package com.sylvcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
		/* Adding a new CIDR block to the list */
		case "add":
			if (args.length == 1) {
				plugin.msg("help", sender);
				return true;
			}

			String ip = "";	int bits = 32;
			String[] tmp = args[1].split("/");

			ip = tmp[0];
			data.put("%ip%", tmp[0]);
			data.put("%bits%", (tmp.length == 1)?"32":tmp[1]);

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
			break;

			
		/* Removing a CIDR block from the list */
		case "del":
			if (args.length == 1) {
				plugin.msg("help", sender);
				return true;
			}

			data.put("%cidr%", args[1]);
			plugin.msg("cidr-remove-" + ((plugin.delCIDR(args[1] + ((args[1].indexOf('/') == -1)?"/32":"")))?"good":"fail"), sender, data);
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
			
			plugin.msg("test-" + ((plugin.isBlocked(args[1]))?"good":"fail"), sender, data);
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
		 * Alas, I don't have the motivation to write one here at this time lol	*/
		case "calc":
			plugin.msg("calc-url", sender);
			break;

			
		/* List all blocked CIDR */
		case "reload":
			plugin.reloadConfig();
			plugin.msg("reloaded", sender);
			break;
		}
		return true;
	}
	
	public boolean isIPv4(String ip){ 
		//final Pattern ipAdd = Pattern.compile("b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)b");
		final Pattern validIP = Pattern.compile("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
		return validIP.matcher(ip).matches(); 
	}
}
