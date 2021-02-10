package me.meerkat.towers.utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Colorize {

    public static void load(String name, String version) {
        Bukkit.getConsoleSender().sendMessage( color("&4[&6Towers&4] &8>> &eEnabled | Running version &a" + version ));
        Bukkit.getConsoleSender().sendMessage( color( "&4[&6Towers&4] &8>> &eThanks for using my plugin! {PablockDA}" ));
    }


    public static void unload(String name, String version) {
        Bukkit.getConsoleSender().sendMessage( color( "&4[&6Towers&4] &8>> &EDisabled | Running version &a" + version) );
        Bukkit.getConsoleSender().sendMessage( color( "&4[&6Towers&4] &8>> &eThanks for using my plugin! {PablockDA}" ));
    }

    public static String color(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> color(List<String> lore) {

        if(lore.isEmpty()) {
            return lore;
        }

        List<String> coloredLore = new ArrayList<>();
        for (String s : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return coloredLore;
    }

    public static String unColor(String string){
        return ChatColor.stripColor(string);
    }



}
