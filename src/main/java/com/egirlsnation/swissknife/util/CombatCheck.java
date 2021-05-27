package com.egirlsnation.swissknife.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.egirlsnation.swissknife.SwissKnife.Config.combatTimeout;

public class CombatCheck {

    private static final Map<UUID, Long> combatMap = new HashMap<>();

    public void addToCombatMap(Player player){
        UUID playerUUID = player.getUniqueId();
        combatMap.put(playerUUID, System.currentTimeMillis());
    }

    public boolean isInCombat(Player player){
        UUID playerUUID = player.getUniqueId();
        if(!combatMap.containsKey(playerUUID)) return false;

        long timeDifference = System.currentTimeMillis() - combatMap.get(playerUUID);
        if(timeDifference >= combatTimeout){
            combatMap.remove(playerUUID);
            return false;
        }else{
            return true;
        }
    }

    public long getRemainingTime(Player player){
        UUID playerUUID = player.getUniqueId();
        if(!combatMap.containsKey(playerUUID)) return 0;

        long timeDifference = System.currentTimeMillis() - combatMap.get(playerUUID);
        if(timeDifference >= combatTimeout) return 0;

        return TimeUnit.MILLISECONDS.toSeconds(combatTimeout -timeDifference);
    }

    public void removePlayer(Player player){
        combatMap.remove(player.getUniqueId());
    }
}
