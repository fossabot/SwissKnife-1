package com.egirlsnation.swissknife.listener.player;

import com.egirlsnation.swissknife.heads.HeadsHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class onPlayerDeath implements Listener {

    private final HeadsHandler headsHandler = new HeadsHandler();

    @EventHandler
    private void PlayerDeath(PlayerDeathEvent e){
        if(e.getEntity().getKiller() == null) return;
        if(headsHandler.isAncientOrDraconite(e.getEntity().getKiller().getInventory().getItemInMainHand())){
            headsHandler.dropHeadIfLucky(e.getEntity(), e.getEntity().getKiller(), e.getEntity().getKiller().getInventory().getItemInMainHand());
        }
    }
}
