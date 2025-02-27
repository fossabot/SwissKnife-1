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

package com.egirlsnation.swissknife.systems.hooks.votingPlugin;

public class VotingPluginHook {

    private UserUtils userUtils = new UserUtils();

    public void initVotingPluginHook(){
        userUtils.setUserManager();
    }

    public void removeVotingPluginHook(){
        userUtils.removeUserManager();
    }

    public boolean isVotingPluginHookActive(){
        return userUtils.getUserManager() != null;
    }
}
