package me.meerkat.towers.events;

import me.meerkat.towers.Towers;
import me.meerkat.towers.managers.Manager;
import me.meerkat.towers.models.Holder.Section;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {

    private Manager m = Manager.instance;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (m.getInGame().containsKey(p.getUniqueId())) {
            m.exitGame(p, false);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player p = event.getPlayer();
        if (m.getInGame().containsKey(p.getUniqueId())) {
            m.exitGame(p, false);
        }
    }


    @EventHandler
    public void clickearInventario(InventoryClickEvent event) {

        Player p = (Player) event.getWhoClicked();

        if( !m.getInGame().containsKey(p.getUniqueId()) || event.getCurrentItem() == null ) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getSlot();
        Inventory i = event.getView().getTopInventory();

        //Si no coincide el titulo de la ventana con el juego retorna
        /*if( !event.getView().getTitle().equals( Colorize.color( Towers.instance.getConfig().getString("GUI.Tittle")) ) ) {
            return;
        }*/

        if ( !m.isSameInventory(event.getClickedInventory(), p) ){
            p.sendMessage("Retorna");
            return;
        }

        // Si lo que clica es aire o nulo retorna
        if( i.getItem(slot) == null || i.getItem(slot).getType() == Material.AIR  ) {
            return;
        }

        switch ( m.getInGame().get(p.getUniqueId()).getSection() ) {

            //El inventario es el Selector
            case Selector:
                switch (slot) {
                    case 2:
                        m.launchLevels(p, Section.Easy, false);
                        break;

                    case 4:
                        m.launchLevels(p, Section.Medium, false);
                        break;

                    case 6:
                        m.launchLevels(p, Section.Hard, false);
                        break;
                    default:
                        break;
                }
                break;
            //Clica en juego Easy
            case Easy:
                sendClick( Section.Easy , slot, p, event.getClick(), i, i.getItem(slot) );
                break;
            //Clica en juego Medium
            case Medium:
                sendClick( Section.Medium , slot, p, event.getClick(), i, i.getItem(slot));
                break;
            //Clica en juego Hard
            case Hard:
                sendClick( Section.Hard, slot, p, event.getClick(), i, i.getItem(slot));
                break;
            default:
                break;
        }

    }

    private void sendClick(Section type, int slot, Player p, ClickType e, Inventory i, ItemStack item  ) {

        //Flecha Sale del Juego al Selector
        if(slot == 53) {

            m.exitGame(p, true);

        // Pone o bloquea dinero
        }else if ( slot == 49 ) {

            switch( e ) {
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    m.blockMoney( p, i.getItem(slot) );
                    break;
                default:
                    m.depositMoney( p, type );
                    break;
            }

        // Yunque, resetea el juego
        }else if ( slot == 45 ) {
            m.resetGame( p );

        //Comprueba que el item sea hierba y lo manda
        }else {
            if ( item.getType() == Material.valueOf(Towers.instance.getConfig().getString("GUI.Hide.material")) ){
                m.handleLvls( slot, p, i );
            }
        }

    }


    @EventHandler
    public void closeInv(InventoryCloseEvent event) {

        if( !(event.getPlayer() instanceof Player) ) {
            return;
        }

        Player p = (Player) event.getPlayer();

        if( !m.getInGame().containsKey(p.getUniqueId()) || m.getInGame().get(p.getUniqueId()).isChange() ) {
            return;
        }

        m.exitGame(p, false);
    }


}