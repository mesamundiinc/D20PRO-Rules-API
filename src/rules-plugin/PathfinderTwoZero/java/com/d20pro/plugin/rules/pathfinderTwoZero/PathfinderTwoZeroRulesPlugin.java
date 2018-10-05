package com.d20pro.plugin.rules.pathfinderTwoZero;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class PathfinderTwoZeroRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
        return PathfinderTwoZeroRules.GameSystem.PathfinderTwoZero.toString();
    }

    @Override
    public Class getRulesClass() {
        return PathfinderTwoZeroRules.class;
    }
}
