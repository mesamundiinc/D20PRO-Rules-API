package com.d20pro.plugin.rules.fivee;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class FiveERulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
       return FiveERules.GameSystem.FiveE.toString();
    }

    @Override
    public Class getRulesClass() {
        return FiveERules.class;
    }
}
