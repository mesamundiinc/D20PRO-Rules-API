package com.d20pro.plugin.rules.espergenesis;

import com.d20pro.plugin.api.rules.RulesPlugin;
import com.mindgene.common.plugin.Factory;

import java.util.Collections;
import java.util.List;


public class CommandFactoryImpl implements Factory<RulesPlugin> {
    public List<RulesPlugin> getPlugins() {
        return Collections.singletonList(new EsperGenesisRulesPlugin());
    }


}
