package me.meerkat.towers;

import me.meerkat.towers.commands.Comands;
import me.meerkat.towers.events.Events;
import me.meerkat.towers.managers.Manager;
import me.meerkat.towers.statistics.MetricsLite;
import me.meerkat.towers.utility.Colorize;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Towers extends JavaPlugin {

    private final PluginDescriptionFile pdfFile = getDescription();
    private final String version = this.pdfFile.getVersion();
    public static Towers instance;
    private static Economy economy = null;

    // Carga del Plugin
    public void onEnable() {


        registerConfig();
        instance = this;

        setupEconomy();

        if(setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage( Colorize.color( "&4[&6Towers&4] &8>> &eDependencie [Vault]: &2&lFound!") );
        }else {
            Bukkit.getConsoleSender().sendMessage( Colorize.color( "&4[&6Towers&4] &8>> &eDependencie [Vault]: &4Disabled due to no dependency found!") );
            Bukkit.getConsoleSender().sendMessage( Colorize.color( "&4[&6Towers&4] &8>>   &e- &cCheck you have Vault") );
            Bukkit.getConsoleSender().sendMessage( Colorize.color( "&4[&6Towers&4] &8>>   &e- &cCheck you have a Currency plugin") );
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new Manager();

        Colorize.load(this.getDescription().getName(), this.getDescription().getVersion());

        Objects.requireNonNull(this.getCommand("towers")).setExecutor(new Comands(this));

        this.getServer().getPluginManager().registerEvents( new Events(), this);


        updateChecker();

        new MetricsLite(this);

    }

    // Descarga del Plugin
    public void onDisable() {

        Manager.instance.closeGame();

        Colorize.unload(this.getDescription().getName(), this.getDescription().getVersion());
    }

    public void registerConfig() {

        File config = new File(this.getDataFolder(),"config.yml");

        if(!config.exists()) {
            this.getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
    }

    private boolean setupEconomy() {

        if(getServer().getPluginManager().getPlugin("Vault")==null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp==null){
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }


    private void updateChecker() {
        try {
            HttpURLConnection localHttpURLConnection = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=70807").openConnection();
            int i = 1250;
            localHttpURLConnection.setConnectTimeout(i);
            localHttpURLConnection.setReadTimeout(i);
            String latestVersion = new BufferedReader(new InputStreamReader(localHttpURLConnection.getInputStream())).readLine();
            if ((latestVersion.length() <= 7) && (!this.version.equals(latestVersion))) {
                Bukkit.getConsoleSender().sendMessage(Colorize.color(
                        "&4[&6Towers&4] &8>> &cVersion &e(" + latestVersion + "&e) &cis available."));
                Bukkit.getConsoleSender().sendMessage(Colorize.color(
                        "&4[&6Towers&4] &8>> &cYou can download it at: &ehttps://www.spigotmc.org/resources/70807/"));
            }
        } catch (Exception localException) {
            Bukkit.getConsoleSender()
                    .sendMessage(Colorize.color("&4[&6Towers&4] &8>> &cError while checking update."));
        }
    }


    public Economy getEconomy() {
        return economy;
    }



}
