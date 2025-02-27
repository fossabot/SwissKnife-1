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

package com.egirlsnation.swissknife.utils.player;

import com.egirlsnation.swissknife.systems.hooks.votingPlugin.UserUtils;
import com.egirlsnation.swissknife.systems.hooks.votingPlugin.VotingPluginHook;
import com.egirlsnation.swissknife.utils.Config;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RankUtil {

    private final UserUtils userUtils = new UserUtils();
    private VotingPluginHook votingPluginHook = new VotingPluginHook();

    public void promoteIfEligible(@NotNull Player player){

        if(!Config.instance.ranksEnabled) return;

        if(!player.hasPlayedBefore()) return;

        //Name "PLAY_ONE_MINUTE" is missleading. It's actually in ticks.
        int pt = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        final ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if(pt >= getTicksFromHours(Config.instance.newfagHours) && !player.hasPermission("egirls.rank.newfag")){ //Hours to ticks
            String command = "lp user " + player.getName() + " parent add newfag";
            Bukkit.dispatchCommand(console, command);
        }

        if(pt >= getTicksFromHours(Config.instance.midfagHours) && !player.hasPermission("egirls.rank.vet")){ //Hours to ticks
            String command = "lp user " + player.getName() + " parent add veteran";
            Bukkit.dispatchCommand(console, command);
            Bukkit.getServer().broadcastMessage(player.getDisplayName() + ChatColor.GREEN + " reached " + ChatColor.YELLOW + "MidFag" + ChatColor.GREEN + "!");
        }

        if(pt >= getTicksFromHours(Config.instance.oldfagHours) && !player.hasPermission("egirls.rank.oldfag")){ //Hours to ticks
            String command = "lp user " + player.getName() + " parent add oldfag";
            Bukkit.dispatchCommand(console, command);
            Bukkit.getServer().broadcastMessage(player.getDisplayName() + ChatColor.GREEN + " reached " + ChatColor.RED + "OldFag" + ChatColor.GREEN + "!");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 100, 0);
        }

        if(!votingPluginHook.isVotingPluginHookActive()) return;
        if(pt >= getTicksFromHours(Config.instance.elderfagHours) && userUtils.getVotes(player) >= Config.instance.elderfagVotes && !player.hasPermission("egirls.rank.legend")){ //Hours to ticks
            String command = "lp user " + player.getName() + " parent add legend";
            Bukkit.dispatchCommand(console, command);
            Bukkit.getServer().broadcastMessage(player.getDisplayName() + ChatColor.GOLD + " reached " + ChatColor.DARK_AQUA + "ElderFag" + ChatColor.GOLD + "!");
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 100, 0);
            }
        }

        if(pt >= getTicksFromHours(Config.instance.boomerfagHours) && userUtils.getVotes(player) >= Config.instance.boomerfagVotes && !player.hasPermission("egirls.rank.boomerfag")){ //Hours to ticks
            String command = "lp user " + player.getName() + " parent add boomerfag";
            Bukkit.dispatchCommand(console, command);
            Bukkit.getServer().broadcastMessage(player.getDisplayName() + ChatColor.GOLD + " reached " + ChatColor.AQUA + "BoomerFag" + ChatColor.GOLD + "!");
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 100, 0);
            }
        }
    }

    public int getTicksFromHours(int hours){
        int ticks = hours * 20;
        ticks = ticks * 60;
        ticks = ticks * 60;
        return ticks;
    }

    public List<String> getOnlinePlayerRankList(){
        List<String> playerRankList = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()){
            playerRankList.add(p.getDisplayName());
        }
        return playerRankList;
    }

    public List<String> getOnlinePlayerNamesUnderPlaytime(int playtimeHours){
        List<String> playernames = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getStatistic(Statistic.PLAY_ONE_MINUTE) < getTicksFromHours(playtimeHours)){
                playernames.add(p.getName());
            }
        }
        return playernames;
    }
}
