package com.d20pro.plugin.rules.starfinder;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class StarfinderRulesRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
        return StarfinderRules.GameSystem.Starfinder.toString();
    }

    @Override
    public Class getRulesClass() {
        return StarfinderRules.class;
    }
}
