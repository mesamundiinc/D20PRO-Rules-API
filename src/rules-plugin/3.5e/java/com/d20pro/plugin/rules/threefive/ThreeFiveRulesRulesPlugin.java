package com.d20pro.plugin.rules.threefive;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class ThreeFiveRulesRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
        return ThreeFiveRules.GameSystem.ThreeFive.toString();
    }

    @Override
    public Class getRulesClass() {
        return ThreeFiveRules.class;
    }
}
