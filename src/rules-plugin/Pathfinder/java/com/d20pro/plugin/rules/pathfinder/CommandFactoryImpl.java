package com.d20pro.plugin.rules.pathfinder;
import java.util.*;

import com.d20pro.plugin.api.rules.RulesPlugin;

import com.mindgene.common.plugin.Factory;


public class CommandFactoryImpl implements Factory<RulesPlugin>
{
  public List<RulesPlugin> getPlugins()
  {
    return Collections.singletonList(new PathfinderRulesRulesPlugin());
  }
}
