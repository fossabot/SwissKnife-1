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

package com.egirlsnation.swissknife;

import com.egirlsnation.swissknife.command.*;
import com.egirlsnation.swissknife.listener.block.onBlockDispense;
import com.egirlsnation.swissknife.listener.block.onBlockPlace;
import com.egirlsnation.swissknife.listener.entity.*;
import com.egirlsnation.swissknife.listener.inventory.onInventoryClick;
import com.egirlsnation.swissknife.listener.inventory.onInventoryClose;
import com.egirlsnation.swissknife.listener.inventory.onInventoryOpen;
import com.egirlsnation.swissknife.listener.player.*;
import com.egirlsnation.swissknife.sql.MySQL;
import com.egirlsnation.swissknife.sql.SqlQuery;
import com.egirlsnation.swissknife.util.LOGGER;
import com.egirlsnation.swissknife.util.ServerUtils;
import com.egirlsnation.swissknife.util.discord.DiscordHandler;
import com.egirlsnation.swissknife.util.player.PingUtil;
import com.egirlsnation.swissknife.util.player.RankUtil;
import me.affanhaq.keeper.Keeper;
import me.affanhaq.keeper.data.ConfigFile;
import me.affanhaq.keeper.data.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.egirlsnation.swissknife.SwissKnife.Config.*;

public class SwissKnife extends JavaPlugin {

    private final PluginManager pluginManager = Bukkit.getPluginManager();

    public MySQL SQL;
    public SqlQuery sqlQuery;

    private final PingUtil pingUtil = new PingUtil();
    private final DiscordHandler discordHandler = new DiscordHandler();
    private final ServerUtils serverUtils = new ServerUtils();
    private final RankUtil rankUtil = new RankUtil();

    private Keeper keeper = null;

    @Override
    public void onEnable() {
        LOGGER.info("Loading config handler.");
        keeper = new Keeper(this).register(new Config()).load();

        if (!webhookURL.isBlank()) {
            if (Bukkit.getServer().getTPS().length == 3) {
                discordHandler.setWebhookURL(webhookURL);
                discordHandler.setTpsArrSize(3);
                initTPSnotifyTask();
            } else if (Bukkit.getServer().getTPS().length == 4) {
                discordHandler.setWebhookURL(webhookURL);
                discordHandler.setTpsArrSize(4);
                initTPSnotifyTask();
            } else {
                LOGGER.warning("You're running server software that's modifiying TPS results that's not supported by this plugin.\nTPS Notifications won't be sent.\nSupported server software for this feature is PaperMC, Tuinity, Airplane, Airplane-Purpur and Purpur");
            }
        }

        correctConfigValues();

        registerEvents();
        registerCommands();

        initSQL();
    }

    @Override
    public void onDisable() {
        if(SQL != null) {
            if (SQL.isConnected()) {
                SQL.disconnect();
            }
        }
        getLogger().info(ChatColor.GREEN + "Swiss Knife plugin disabled.");
    }

    private void registerEvents() {
        pluginManager.registerEvents(new CommandPreProcessor(this), this);
        LOGGER.info(ChatColor.AQUA + "Registering block events");
        pluginManager.registerEvents(new onBlockDispense(), this);
        pluginManager.registerEvents(new onBlockPlace(), this);

        LOGGER.info(ChatColor.AQUA + "Registering entity events");
        pluginManager.registerEvents(new onEntityChangeBlock(), this);
        pluginManager.registerEvents(new onEntityChangeBlock(), this);
        pluginManager.registerEvents(new onEntityDamage(), this);
        pluginManager.registerEvents(new onEntityDamageByBlock(), this);
        pluginManager.registerEvents(new onEntityDamageByEntity(), this);
        pluginManager.registerEvents(new onEntityDeath(), this);
        pluginManager.registerEvents(new onEntityPickupItem(), this);
        pluginManager.registerEvents(new onEntityPortalTeleport(), this);
        pluginManager.registerEvents(new onCreatureSpawn(), this);
        pluginManager.registerEvents(new onProjectileHit(), this);

        LOGGER.info(ChatColor.AQUA + "Registering inventory events");
        pluginManager.registerEvents(new onInventoryClick(), this);
        pluginManager.registerEvents(new onInventoryClose(), this);
        pluginManager.registerEvents(new onInventoryOpen(), this);

        LOGGER.info(ChatColor.AQUA + "Registering player events");
        pluginManager.registerEvents(new onGamemodeSwitch(this), this);
        pluginManager.registerEvents(new onJoin(this), this);
        pluginManager.registerEvents(new onLeave(this), this);
        pluginManager.registerEvents(new onPlayerDeath(), this);
        pluginManager.registerEvents(new onPlayerInteract(this), this);
        pluginManager.registerEvents(new onPlayerInteractEntity(), this);
        pluginManager.registerEvents(new onRespawn(), this);
        pluginManager.registerEvents(new onPlayerPlaceCrystal(), this);
        pluginManager.registerEvents(new onSwapHandItems(), this);
        pluginManager.registerEvents(new EnderCrystalListeners(), this);
        pluginManager.registerEvents(new onVehicleCreate(), this);
        pluginManager.registerEvents(new onVehicleCollision(), this);
    }

    private void registerCommands() {
        LOGGER.info("Registering commands.");
        Objects.requireNonNull(this.getCommand("kill")).setExecutor(new KillCommand(this));
        Objects.requireNonNull(this.getCommand("ping")).setExecutor(new PingCommand());
        Objects.requireNonNull(this.getCommand("playtime")).setExecutor(new PlaytimeCommand(this));
        if(enableShitlist) {
            Objects.requireNonNull(this.getCommand("shitlist")).setExecutor(new ShitListCommand(this));
        }
        Objects.requireNonNull(this.getCommand("shrug")).setExecutor(new ShrugCommand());
        Objects.requireNonNull(this.getCommand("rank")).setExecutor(new RankCommand());
        Objects.requireNonNull(this.getCommand("monkey")).setExecutor(new MonkeyCommand());
        Objects.requireNonNull(this.getCommand("tpsalert")).setExecutor(new TpsAlertCommand(this));
    }

    private void initSQL() {
        LOGGER.info(ChatColor.AQUA + "Starting up SQL driver.");

        this.SQL = new MySQL();
        this.sqlQuery = new SqlQuery(this);

        if (Config.databaseName.equals("name") && Config.databaseUsername.equals("username") && Config.databasePassword.equals("password")) {
            LOGGER.warning("Default SQL config values detected. SQL driver won't be initiated.");
            return;
        }

        try {
            SQL.connect();
        } catch (SQLException | ClassNotFoundException throwables) {
            LOGGER.error("Something went wrong while initiating SQL\nStack trace will follow.");
            throwables.printStackTrace();
        }

        if (SQL.isConnected()) {
            LOGGER.info(ChatColor.GREEN + "Sucessfully connected to SwissKnife database.");
            sqlQuery.createStatsTable();
            //sqlQuery.createPingTable();
            LOGGER.info(ChatColor.GREEN + "Finished SQL initialization.");
        }
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    private void initPingLogTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> pingUtil.uploadPingMap(pingUtil.getAllPings(), SQL, sqlQuery), 6000, 12000);
    }

    private void initTPSnotifyTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            List<Double> tps = serverUtils.getTPS();
            if (discordHandler.shouldPostAlert(tps)) {
                List<String> rankNames = null;
                if(listOnlinePlayers){
                    rankNames = rankUtil.getOnlinePlayerRankList();
                }
                List<String> namesUnderPt = null;
                if(listLowPtPlayers){
                    namesUnderPt = rankUtil.getOnlinePlayerNamesUnderPlaytime(lowPtThreshold);
                }

                List<String> finalRankNames = rankNames;
                List<String> finalNamesUnderPt = namesUnderPt;
                int playercount = Bukkit.getServer().getOnlinePlayers().size();
                int maxSlots = Bukkit.getServer().getMaxPlayers();
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        discordHandler.postDiscordTPSNotif(tps, playercount, maxSlots, finalRankNames, finalNamesUnderPt);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 6000, tpsTaskTime);
    }

    private void correctConfigValues(){
        if(replaceChance > 100){
            replaceChance = 100;
        }
        if(replaceChance < 0){
            replaceChance = 0;
        }

    }

    @ConfigFile("config.yml")
    public static class Config {

        /*
         * Combat check config options
         */

        @ConfigValue("combatCheck.timeout")
        public static long combatTimeout = 20000;

        /*
         * Jihads config options
         */

        @ConfigValue("jihads.enabled")
        public static boolean jihadsEnabled = true;

        @ConfigValue("jihads.limitRadius")
        public static boolean limitJihadRadius = true;

        @ConfigValue("jihads.radius")
        public static int jihadsRadius = 10000;

        @ConfigValue("jihads.power")
        public static double jihadsPower = 6.0;

        /*
         * Illegals config options
         */

        @ConfigValue("illegals.maxEnchant")
        public static int maxEnchantLevel = 100;

        @ConfigValue("illegals.checkLores")
        public static List<String> illegalLoreList = Arrays.asList("§9§lBig Dick Energy X", "§cCurse of Simping");

        @ConfigValue("illegals.maxTotemStack")
        public static int maxTotemStack = 2;

        @ConfigValue("illegals.illegalBlockList")
        public static List<String> illegalBlockList = Arrays.asList("BEDROCK", "END_PORTAL_FRAME", "BARRIER", "STRUCTURE_BLOCK", "STRUCTURE_VOID");

        @ConfigValue("illegals.enable1kPicks")
        public static boolean enable1kPicks = false;

        /*
         * Patches config options
         */

        @ConfigValue("patches.limitVehiclesInChunk")
        public static boolean limitVehicles = true;

        @ConfigValue("patches.maxVehicleInChunk")
        public static int vehicleLimitChunk = 26;

        /*
         * Preventions config options
         */

        @ConfigValue("preventions.preventHighDamage")
        public static boolean preventHighDmg = true;

        @ConfigValue("preventions.kickOnHighDmg")
        public static boolean kickOnHighDamage = true;

        @ConfigValue("preventions.highDamageThreshold")
        public static int highDmgThreshold = 1000;

        @ConfigValue("preventions.preventWitherSpawningAtSpawn")
        public static boolean preventWithersAtSpawn = false;

        @ConfigValue("preventions.spawnRadius")
        public static int spawnRadius = 2000;

        /*
         * SQL config options
         */

        @ConfigValue("sql.host")
        public static String databaseHost = "172.18.0.1";

        @ConfigValue("sql.port")
        public static String databasePort = "3306";

        @ConfigValue("sql.dbName")
        public static String databaseName = "name";

        @ConfigValue("sql.dbUserName")
        public static String databaseUsername = "username";

        @ConfigValue("sql.dbPassword")
        public static String databasePassword = "password";

        /*
         * Misc config options
         */

        @ConfigValue("misc.mainWorldName")
        public static String mainWorldName = "world";

        @ConfigValue("misc.netherWorldName")
        public static String netherWorldName = "world_nether";

        @ConfigValue("misc.endWorldName")
        public static String endWorldName = "world_the_end";

        @ConfigValue("misc.petsUseTotems")
        public static boolean petsUseTotems = false;

        @ConfigValue("misc.maxItemNameLength")
        public static int maxItemNameLength = 50;

        @ConfigValue("misc.disableEndermenGriefInEnd")
        public static boolean disableEndermanGrief = false;

        @ConfigValue("misc.disableEntityPortalTP")
        public static boolean disableEntityPortal = true;

        @ConfigValue("misc.disableEntityPortalTpList")
        public static List<String> entityTypeDisablePortal = Arrays.asList(EntityType.BEE.name(), EntityType.ENDER_CRYSTAL.name());

        /*
         * Disable Commands at spawn config options
         */

        @ConfigValue("disableCommandsAtSpawn.enabled")
        public static boolean disableCommandsAtSpawn = true;

        @ConfigValue("disableCommandsAtSpawn.radius")
        public static int disableCommandsRadius = 2000;

        @ConfigValue("disableCommandsAtSpawn.commands")
        public static List<String> radiusLimitedCmds = Arrays.asList("tpa", "tpahere", "tpayes", "tpaccept", "tpaaccept", "tpno", "tpano", "tpdeny", "tpadeny", "tpyes");

        /*
         * Discord TPS Notifier config options
         */

        @ConfigValue("discordTPSnotifier.webhookURL")
        public static String webhookURL = "";

        @ConfigValue("discordTPSnotifier.webhookName")
        public static String webhookName = "TPS Alert";

        @ConfigValue("discordTPSnotifier.webhookAvatarURL")
        public static String webhookAvatarURL = "";

        @ConfigValue("discordTPSnotifier.pingRolesIDs")
        public static List<String> roleIDs = Arrays.asList("719274790795346031");

        @ConfigValue("discordTPSnotifier.taskRepeatTimeTicks")
        public static int tpsTaskTime = 1200;

        @ConfigValue("discordTPSnotifier.secondsBetweenNotify")
        public static int notifyDelay = 600;

        @ConfigValue("discordTPSnotifier.tpsAverageThreshold")
        public static int tpsAvgThreshold = 18;

        @ConfigValue("discordTPSnotifier.useLongerAverageTPS")
        public static boolean longerTPSavg = false;

        @ConfigValue("discordTPSnotifier.tpsNotifyAlways")
        public static int tpsNotifyAlways = 13;

        @ConfigValue("discordTPSnotifier.listOnlinePlayers")
        public static boolean listOnlinePlayers = true;

        @ConfigValue("discordTPSnotifier.listLowPlaytimePlayers")
        public static boolean listLowPtPlayers = true;

        @ConfigValue("discordTPSnotifier.lowPlaytimeThresholdInHours")
        public static int lowPtThreshold = 30;

        /*
         * Shitlist config options
         */

        @ConfigValue("shitlist.enable")
        public static boolean enableShitlist = true;

        @ConfigValue("shitlist.blacklistedCommands")
        public static List<String> blacklistedCommands = Arrays.asList("tpa", "tpahere", "tpayes", "tpaccept", "tpaaccept", "tpno", "tpano", "tpdeny", "tpadeny", "tpyes");

        @ConfigValue("shitlist.leakCoords")
        public static boolean leakCoords = false;

        @ConfigValue("shitlist.swapWordsRandomly")
        public static boolean swapWordsRandomly = true;

        @ConfigValue("shitlist.wordsToReplaceWith")
        public static List<String> replacementWords = Arrays.asList("titty", "pickle", "canoodle", "mitten", "badger", "doodlesack");

        @ConfigValue("shitlist.replaceChance")
        public static int replaceChance = 30;

        /*
         * Draconite Items config options
         */

        @ConfigValue("draconiteItems.enablePickaxe")
        public static boolean enablePickaxe = false;

        @ConfigValue("draconiteItems.pickaxeXpToDrain")
        public static float xpToDrain = 0.5f;

        /*
         * EgirlsNation config options
         */

        @ConfigValue("egirlsnation.enableAnniversaryItems")
        public static boolean anniversaryItems = true;

        @ConfigValue("egirlsnation.ranksEnabled")
        public static boolean ranksEnabled = false;

        @ConfigValue("egirlsnation.midfagHours")
        public static int midfagHours = 48;

        @ConfigValue("egirlsnation.oldfagHours")
        public static int oldfagHours = 408;

        @ConfigValue("egirlsnation.elderfagHours")
        public static int elderfagHours = 2400;

        @ConfigValue("egirlsnation.elderfagVotes")
        public static int elderfagVotes = 300;
    }
}
