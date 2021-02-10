package me.meerkat.towers.utility;

import me.meerkat.towers.Towers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Creator {

    public static FileConfiguration conf = Towers.instance.getConfig();


    @SuppressWarnings("deprecation")
    public static ItemStack getColoredItem(String mat, String name, String lore) {

        ItemStack fill = null;

        try {

            String mater = conf.getString(mat);
            Material material;

            if( mater.contains(";") ) {

                String a[] = mater.split(";");

                material = Material.valueOf( a[0] );

                int b = Integer.parseInt( a[1] );

                fill = new ItemStack(material, 1, (short) b);

            }else {
                material = Material.valueOf( mater );
                fill = new ItemStack(material);
            }


            if( material == Material.AIR || material == null) {
                return fill;
            }

            ItemMeta meta = fill.getItemMeta();
            assert meta != null;
            meta.setDisplayName( Colorize.color( conf.getString(name)));
            meta.setLore( Colorize.color( conf.getStringList(lore) ) );

            fill.setItemMeta(meta);



        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(Colorize.color(("&4[&6Towers&4] &8>> &4 Error(1) Creating Item, check materials")));
            e.printStackTrace();
        }

        return fill;
    }


    public static void setColoredItem(ItemStack item, String name) {

        try {

            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName( Colorize.color(name) );
            item.setItemMeta(meta);

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(Colorize.color(("&4[&6Towers&4] &8>> &4 Error(3) Setting Item name")));
            e.printStackTrace();
        }

    }

}
