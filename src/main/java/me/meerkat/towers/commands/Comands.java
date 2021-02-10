package me.meerkat.towers.commands;

import me.meerkat.towers.Towers;
import me.meerkat.towers.managers.Manager;
import me.meerkat.towers.utility.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



public class Comands implements CommandExecutor {

	private Towers plugin;
	private Manager m = Manager.instance;

	public Comands(Towers plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command comando, String label, String[] args) {

		if ( !(sender instanceof Player)  ) {

			Bukkit.getConsoleSender().sendMessage((Colorize.color( "&4[&6Towers&4] &8>> &4&lYou cant execute commands from the console")));
			return false;
			
		} else {
			
			Player p = (Player) sender;
			
			if( args.length == 0 ) {

				m.openSelector(p, true);

			}else if ( args.length > 0 && p.hasPermission("tw.admin") ) {
				
				String subCommando = args[0].toLowerCase();

				switch (subCommando) {

					case "reload":

						plugin.reloadConfig();
						m.onReload();

						p.sendMessage(Colorize.color( "&6Configuration succecfully reloaded" ) );
						break;

					default:
						p.sendMessage(Colorize.color( plugin.getConfig().getString("Lang.Msg_Help") ) );
						break;
				}
				
			}else {
				p.sendMessage(Colorize.color(  plugin.getConfig().getString("Lang.Msg_Help")  ));
				return false;
			} 
		}
		return false;
	}
}
