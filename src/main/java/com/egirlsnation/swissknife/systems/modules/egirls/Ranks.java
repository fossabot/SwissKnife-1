/*
 * This file is part of the SwissKnife plugin distribution  (https://github.com/EgirlsNationDev/SwissKnife).
 * Copyright (c) 2022 Egirls Nation Development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GPL-3.0 License.
 *
 * You should have received a copy of the GPL-3.0
 * License along with this program.  If not, see
 * <https://opensource.org/licenses/GPL-3.0>.
 */

package com.egirlsnation.swissknife.systems.modules.egirls;

import com.egirlsnation.swissknife.SwissKnife;
import com.egirlsnation.swissknife.settings.BoolSetting;
import com.egirlsnation.swissknife.settings.IntSetting;
import com.egirlsnation.swissknife.settings.Setting;
import com.egirlsnation.swissknife.settings.SettingGroup;
import com.egirlsnation.swissknife.systems.modules.Categories;
import com.egirlsnation.swissknife.systems.modules.Module;
import com.egirlsnation.swissknife.utils.entity.player.RankUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Ranks extends Module {
    public Ranks() {
        super(Categories.EgirlsNation, "ranks", "Egirls Nation rank system");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> checkPeriodically = sgGeneral.add(new BoolSetting.Builder()
            .name("periodic-check")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> period = sgGeneral.add(new IntSetting.Builder()
            .name("period")
            .defaultValue(10)
            .range(5,120)
            .build()
    );

    @Override
    public void onEnable(){
        if(!isEnabled()) return;
        if(!checkPeriodically.get()) return;

        Bukkit.getScheduler().runTaskTimer(SwissKnife.INSTANCE, () -> {
            for(Player p : Bukkit.getOnlinePlayers()){
                if(p.hasPlayedBefore()){
                    RankUtil.promoteIfEligible(p);
                }
            }
        }, 6000, period.get() * 60 * 20);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e){
        if(!isEnabled()) return;
        if(!e.getPlayer().hasPlayedBefore()) return;
        RankUtil.promoteIfEligible(e.getPlayer());
    }
}
