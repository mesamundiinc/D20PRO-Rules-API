package com.d20pro.plugin.rules.foure;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class FourERulesRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
        return FourERules.GameSystem.FourE.toString();
    }

    @Override
    public Class getRulesClass() {
        return FourERules.class;
    }
}
