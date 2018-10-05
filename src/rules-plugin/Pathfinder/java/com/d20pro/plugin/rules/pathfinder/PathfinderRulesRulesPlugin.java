package com.d20pro.plugin.rules.pathfinder;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class PathfinderRulesRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
        return PathfinderRules.GameSystem.Pathfinder.toString();
    }

    @Override
    public Class getRulesClass() {
        return PathfinderRules.class;
    }
}
