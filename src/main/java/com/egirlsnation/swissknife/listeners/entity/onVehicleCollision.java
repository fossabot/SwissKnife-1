/*
 * This file is part of the SwissKnife plugin distribution  (https://github.com/EgirlsNationDev/SwissKnife).
 * Copyright (c) 2021 Egirls Nation Development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 *
 * You should have received a copy of the MIT
 * License along with this program.  If not, see
 * <https://opensource.org/licenses/MIT>.
 */

package com.egirlsnation.swissknife.listeners.entity;

import com.egirlsnation.swissknife.utils.Config;
import com.egirlsnation.swissknife.utils.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

import java.util.List;

public class onVehicleCollision implements Listener {

    private final EntityUtil entityUtil = new EntityUtil();

    @EventHandler
    public void VehicleCollision(VehicleEntityCollisionEvent e){
        //Limits the number of vehicles in chunk
        if(Config.instance.limitVehicles){
            List<Entity> vehicles = entityUtil.filterVehicles(e.getVehicle().getLocation().getChunk().getEntities());
            if(vehicles.size() > Config.instance.vehicleLimitChunk){
                entityUtil.removeExcessVehicles(vehicles);
                Bukkit.getLogger().warning("Removed excess vehicles in chunk at: " +  e.getVehicle().getLocation().getBlockX() + " " + e.getVehicle().getLocation().getBlockY() + " " + e.getVehicle().getLocation().getBlockZ());
            }
        }
    }
}
