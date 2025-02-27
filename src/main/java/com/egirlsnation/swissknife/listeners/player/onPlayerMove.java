/*
 *
 * This file is part of the SwissKnife plugin distribution  (https://github.com/EgirlsNationDev/SwissKnife).
 * Copyright (c) 2021 Egirls Nation Development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 *
 * You should have received a copy of the MIT
 * License along with this program.  If not, see
 * <https://opensource.org/licenses/MIT>.
 *
 */

package com.egirlsnation.swissknife.listeners.player;

import com.egirlsnation.swissknife.utils.Config;
import com.egirlsnation.swissknife.utils.SwissLogger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class onPlayerMove implements Listener {

    @EventHandler
    public void PlayerMove(PlayerMoveEvent e) {
        if (Config.instance.preventPlayerBellowOw || Config.instance.preventPlayerBellowNether) {
            if (e.getTo().getY() < 0) {
                World.Environment env = e.getTo().getWorld().getEnvironment();
                if (Config.instance.preventPlayerBellowOw && env.equals(World.Environment.NORMAL)) {
                    handlePlayerBellowFloor(e);
                } else if (Config.instance.preventPlayerBellowNether && env.equals(World.Environment.NETHER)) {
                    handlePlayerBellowFloor(e);
                }
            }
        }

        if (Config.instance.preventPlayersOnNether) {
            Location l = e.getTo();
            if (!l.getWorld().getEnvironment().equals(World.Environment.NETHER)) return;
            if (l.getY() >= Config.instance.netherRoofHeight) {
                SwissLogger.info("Player " + e.getPlayer().getName() + " attempted to go above the nether roof");
                e.setCancelled(true);
                if (Config.instance.teleportPlayersDown) {
                    e.getPlayer().teleport(e.getPlayer().getLocation().subtract(0, 3, 0));
                }
                if (Config.instance.dmgPlayersOnNether) {
                    e.getPlayer().damage(Config.instance.dmgToDealNether);
                }
            }
        }
    }

    private void handlePlayerBellowFloor(PlayerMoveEvent e) {
        if (Config.instance.placeBedrockBellow) {
            e.getPlayer().getWorld().getBlockAt(e.getTo().getBlockX(), 0, e.getTo().getBlockZ()).setType(Material.BEDROCK);
        }
        e.setTo(e.getFrom().add(0, 2, 0));
        SwissLogger.info("Player " + e.getPlayer().getName() + " attempted to go bellow the bedrock floor");
    }
}
