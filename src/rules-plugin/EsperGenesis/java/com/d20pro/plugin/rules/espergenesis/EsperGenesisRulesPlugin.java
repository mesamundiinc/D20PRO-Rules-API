package com.d20pro.plugin.rules.espergenesis;

import com.d20pro.plugin.api.rules.RulesPlugin;

public class EsperGenesisRulesPlugin implements RulesPlugin {
    @Override
    public String getGameSystemName() {
       return EsperGenesisRules.GameSystem.EsperGenesis.toString();
    }

    @Override
    public Class getRulesClass() {
        return EsperGenesisRules.class;
    }
}
