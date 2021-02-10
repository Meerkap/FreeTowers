package me.meerkat.towers.managers;

import me.meerkat.towers.Towers;
import me.meerkat.towers.models.Holder;
import me.meerkat.towers.models.Holder.Section;
import me.meerkat.towers.utility.Colorize;
import me.meerkat.towers.utility.Creator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Manager {

    public static Manager instance;

    private FileConfiguration conf;

    private final HashMap<UUID, Holder> inGame = new HashMap<>();

    private Inventory selector;

    private final Economy econ = Towers.instance.getEconomy();

    private int maxLevels = 3;

    public Manager() {
        instance = this;
        conf = Towers.instance.getConfig();
        fillSelector();
        maxLevels = getMaxLevels();
    }

    public void openSelector(Player p, boolean isStart ) {

        if(isStart)
            inGame.put( p.getUniqueId(), new Holder() );

        p.openInventory(selector);

    }

    //Close the game for all players
    public void closeGame() {

        if( !inGame.isEmpty() ) {

            Object[] mapas = inGame.keySet().toArray();

            Player p;

            for (Object user : mapas) {
                p = Bukkit.getPlayer((UUID)user);
                assert p != null;
                exitGame(p, false);
                p.closeInventory();
            }
        }
    }


    public void launchLevels(Player p, Section type, boolean isReseting ) {

        Holder h = inGame.get(p.getUniqueId());

        Inventory inv;

        if( isReseting ) {
            inv = h.getInv();
        }else {
            //inv = Bukkit.createInventory(null, 99 , Mensajes.ChatOnColor( conf.getString("GUI.Tittle") ));
            inv = Bukkit.createInventory(null, 54 , Colorize.color( conf.getString("GUI.Tittle") ));
        }

        fillGameBasic(inv);

        if( type == Section.Easy ) {
            fill3Rows( inv, p, new String[]{"110", "101", "011"} );
        }else if ( type == Section.Medium ) {
            fill2Rows(inv,p);
        }else {
            fill3Rows( inv, p, new String[]{"100","010","001"} );
        }

        h.setChange(true);

        h.setSection( type );
        h.setInv(inv);

        if(!isReseting) {
            p.openInventory(inv);
        }

        h.setChange(false);

    }


    //Maneja los niveles
    public void handleLvls(int slot, Player p, Inventory i){

        Holder h = inGame.get(p.getUniqueId());

        if( h.getBudget() < 1 ) { return; }

        if( !canRank( slot, p, h.getSection() ) || h.getRank() == -1 ) { return; }

        //Si acierta
        if ( h.getMapa().get( String.valueOf(slot) ).equals("1") ) {
            i.setItem( slot , getSucces() );
            h.setRank(1);
            changeItem( h );
            playSound(p, 1, 1, "Sounds.Success");
            return;
        }

        //Si falla
        i.setItem( slot , getFail() );
        playSound(p, 1, 1, "Sounds.Fail");

        Set<Map.Entry<String, String>> set = h.getMapa().entrySet();

        for (Map.Entry<String, String> entry : set) {

            if( entry.getValue().equals("1")  ) {
                i.setItem( Integer.parseInt(entry.getKey()) , getSucces() );
            }else {
                i.setItem( Integer.parseInt(entry.getKey()) , getFail() );
            }

        }
        changeItem( h );
        h.failRank();

    }


    // ========= Seccion de Depositar  =========
    // =========================================

    // Le quitamos el dinero y lo metemos en la carteda del juego
    public void depositMoney( Player p, Section type ) {

        Holder h = inGame.get(p.getUniqueId());

        if( h.getRank() != 0 || h.getBlocked() ) { return; }

        double increment = conf.getDouble("Levels." + getTipos( type ) +".increment");

        if( econ.getBalance(p) - increment >= 0 ) {

            econ.withdrawPlayer( p, increment );
            h.setBudget( h.getBudget() + increment );

        }else {
            p.sendMessage(Colorize.color("&4[&6Towers&4] " + conf.getString("Lang.No_Money")));
        }

        changeItem( h );
    }


    // Se bloquea - desbloquea el dinero
    public void blockMoney( Player p, ItemStack i ) {

        Holder h = 	inGame.get(p.getUniqueId());

        if( h.getBudget() == 0 /*|| h.getRank() != 0*/  ) { return; }

        if( h.getBlocked() ) {

            h.setBlocked(false);
            changeItem( h );
            return;
        }

        h.setBlocked(true);
        changeItem(h);
    }


    // ========= Seccion de Cualquier Cerrado =========
    // ================================================

    public void exitGame( Player p, boolean isArrow ) {

        Holder h = 	inGame.get(p.getUniqueId());

        //Sale del juego desde seleccion
        if( h.getSection() == Section.Selector ) {
            inGame.remove(p.getUniqueId());
            return;
        }

        //Sale del juego cerrando ventana
        if( ( h.getRank() == -1 || h.getBudget() == 0 ) && !isArrow ) {
            inGame.remove(p.getUniqueId());
            //h.setSection(Section.S);
            Bukkit.getConsoleSender().sendMessage("Retorna sin dar dinero");
            return;
        }

        //Si sale al menú de selección
        h.setChange(true);

        Bukkit.getConsoleSender().sendMessage("Pasa a dar el dinero");
        //Si clica en la flecha a partir de aqui
        econ.depositPlayer(p , getWorth(h, false) );

        h.setSection( Section.Selector );
        h.setBudget(0);
        h.setBlocked(false);
        h.resetRank();
        h.getMapa().clear();

        if(isArrow) {
            openSelector(p, false);
            h.setChange(false);
        }else {
            inGame.remove(p.getUniqueId());
        }

    }


    // ========= Seccion de Reseteo  =========
    // =======================================

    public void resetGame( Player p ) {

        Holder h = inGame.get(p.getUniqueId());

        //Si no ha fallado
        if( h.getRank() != -1 ) {
            econ.depositPlayer(p , getWorth( h, false ) );
        }

        launchLevels(p,  h.getSection() , true);

        if( h.getBlocked() ) {

            if(econ.getBalance(p) - h.getBudget() >= 0) {
                econ.withdrawPlayer(p, h.getBudget() );
            }else {
                //p.sendMessage("No tienes suficiente dinero para jugar con lo bloqueado");
                p.sendMessage(Colorize.color("&4[&6Towers&4] " + conf.getString("Lang.No_Money")));
                h.setBudget(0);
                h.setBlocked(false);
            }

        }else {
            h.setBudget(0);
        }

        h.resetRank();
        changeItem( h );
    }


    // ========= Seccion de Utiles  =========
    private String getTipos( Section type ) {
        return  type.toString();
    }


    private String getFormat(double number) {
        return String.format("%.2f", number);
    }


    private Double getWorth( Holder h, boolean isNext ) {

        double multiplier = conf.getDouble("Levels." + getTipos( h.getSection() ) +".multiplier");

        if (isNext){
            return h.getBudget() * Math.pow( multiplier, h.getRank()+1 ) ;
        }else{
            return h.getBudget() * Math.pow( multiplier, h.getRank() ) ;
        }

    }


    private void changeItem(Holder h) {

        if( h.getBlocked() ) {
            Creator.setColoredItem( h.getInv().getItem(49) , "&5[#]&6 " + h.getBudget() + " " + Colorize.color((conf.getString("GUI.Worth")) + getFormat(getWorth(h, true))) );
            return;
        }

        Creator.setColoredItem( h.getInv().getItem(49) , "&6" + h.getBudget() + " " + Colorize.color((conf.getString("GUI.Worth")) + getFormat(getWorth(h, true))) );

    }


    //Dice si el jugador puede jugar el siguiente nivel
    private boolean canRank( int slot, Player p, Section s ) {

        int up = 35 - ( inGame.get(p.getUniqueId()).getRank()  * 9 );

        if( s == Section.Easy || s == Section.Hard) {
            for (int i = 0; i < 45 ; i++) {
                if( up + 3 == slot || up + 5 == slot || up + 7 == slot ) { return true;	}
            }

        }else {
            for (int i = 0; i < 45 ; i++) {
                if( up + 4 == slot || up + 6 == slot ) { return true; }
            }
        }
        return false;
    }


    //Rellena el selector de Modalidades
    private void fillSelector() {

        selector = Bukkit.createInventory(null, 9 , Colorize.color( conf.getString("GUI.Tittle") ));

        ItemStack aux = Creator.getColoredItem("GUI.Fill.material", "GUI.Fill.name", "GUI.Fill.lore");
        for (int j = 0; j < selector.getSize(); j++) {
            selector.setItem(j, aux);
        }

        aux = Creator.getColoredItem("Levels.Easy.material", "Levels.Easy.name", "Levels.Easy.lore");
        selector.setItem(2, aux);
        aux = Creator.getColoredItem("Levels.Medium.material", "Levels.Medium.name", "Levels.Medium.lore");
        selector.setItem(4, aux);
        aux = Creator.getColoredItem("Levels.Hard.material", "Levels.Hard.name", "Levels.Hard.lore");
        selector.setItem(6, aux);

    }

    //Rellena el inventario de juego con la base
    private void fillGameBasic( Inventory inv ) {

        ItemStack b = getFill();

        for (int j = 0; j < inv.getSize(); j++) {
            inv.setItem( j, b );
        }

        inv.setItem(45, getReset() );
        inv.setItem(49, getMoney() );
        inv.setItem(53, getExit() );

    }


    private void fill2Rows(Inventory inv, Player p) {

        String[] sequence = new String[] {"01","10"};

        Holder h = inGame.get(p.getUniqueId());

        int cont = 0;
        int ran = new Random().nextInt(1+1);

        for (int j = 0; j < 44; j++) {

            if(cont == 3) {
                inv.setItem( j, getHide() );
                h.getMapa().put( String.valueOf(j) , Character.toString(sequence[ran].charAt(0)) );
            }else if(cont == 5) {
                h.getMapa().put( String.valueOf(j) , Character.toString(sequence[ran].charAt(1))  );
                inv.setItem(j, getHide() );
            }

            if(cont == 9 && j < 53 ) {
                cont = 0;
                ran = new Random().nextInt(1 + 1);
            }

            cont++;
        }
    }



    private void fill3Rows(Inventory inv, Player p, String[] chain) {

        Holder h = inGame.get(p.getUniqueId());

        int cont = 0;
        int ran = new Random().nextInt(2 + 1);

        ItemStack hide = getHide();

        //recorre el array y coloca en los lugares donde sea un random
        for (int j = 0; j < 44; j++) {

            if(cont==2) {

                h.getMapa().put( String.valueOf(j) , Character.toString( chain[ran].charAt(0) ) );
                inv.setItem(j, hide );

            }else if(cont==4) {

                h.getMapa().put( String.valueOf(j) , Character.toString(chain[ran].charAt(1))  );
                inv.setItem(j, hide );

            }else if(cont==6) {

                h.getMapa().put( String.valueOf(j) , Character.toString(chain[ran].charAt(2))  );
                inv.setItem(j, hide );

            }

            if(cont == 9 && j < 44 ) {
                cont = 0;
                ran = new Random().nextInt(2 + 1);
            }
            cont++;
        }
    }



    public void playSound(Player p , float f1, float f2, String s) {

        if( !conf.getBoolean("Sounds.Enable")  ) {
            return;
        }

        Sound sound;
        String c = Objects.requireNonNull( conf.getString(s) ).toUpperCase();

        if( c.isEmpty() ) {
            Bukkit.getConsoleSender().sendMessage( Colorize.color("&4[Towers] A sound is empty ") );
            return;
        }

        try {
            sound = Sound.valueOf(c);
            p.playSound(p.getLocation(), sound, f1, f2);

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage( Colorize.color("&4[&6Towers&4] Error playing sound to " + p.getName() + ", check sounds fits with the version" ) );
            e.printStackTrace();
        }

    }


    public boolean isSameInventory(Inventory inv, Player p){
        return Objects.equals(inv, inGame.get(p.getUniqueId()).getInv()) || Objects.equals(inv, selector);
    }


    private ItemStack getFill() {
        return Creator.getColoredItem("GUI.Fill.material", "GUI.Fill.name", "GUI.Fill.lore");
    }

    private ItemStack getHide() {
        return Creator.getColoredItem("GUI.Hide.material", "GUI.Hide.name", "GUI.Hide.lore");
    }

    private ItemStack getFail() {
        return Creator.getColoredItem("GUI.Fail.material", "GUI.Fail.name", "GUI.Fail.lore");
    }

    private ItemStack getSucces() {
        return Creator.getColoredItem("GUI.Succes.material", "GUI.Succes.name", "GUI.Succes.lore");
    }

    private ItemStack getExit() {
        return Creator.getColoredItem("GUI.Exit.material", "GUI.Exit.name", "GUI.Exit.lore");
    }

    private ItemStack getReset() {
        return Creator.getColoredItem("GUI.Reset.material", "GUI.Reset.name", "GUI.Reset.lore");
    }

    private ItemStack getMoney() {
        return Creator.getColoredItem("GUI.Money.material", "GUI.Money.name", "GUI.Money.lore") ;
    }

    private int getMaxLevels(){
        return conf.getInt("Levels.Max_Levels");
    }

    public void onReload() {
        Creator.conf = Towers.instance.getConfig();
        conf = Towers.instance.getConfig();
        maxLevels = conf.getInt("Levels.Max_Levels");
        fillSelector();
    }

    public HashMap<UUID, Holder> getInGame() {
        return inGame;
    }


}
