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

package com.egirlsnation.swissknife.systems.handlers.commandCooldown;

public class CommandInfo {
    private final CommandType command;
    private final long cooldown;

    public CommandInfo(long cd, CommandType cmd) {
        cooldown = cd;
        command = cmd;
    }


    public CommandType getCommand() {
        return command;
    }

    public long getCooldown(){
        return cooldown;
    }
}
