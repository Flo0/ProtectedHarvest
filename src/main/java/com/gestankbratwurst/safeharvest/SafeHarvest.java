package com.gestankbratwurst.safeharvest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SafeHarvest extends JavaPlugin {

  @Override
  public void onEnable() {
    final SafeHarvestConfig config = new SafeHarvestConfig(this);
    Msg.init(config);

    Bukkit.getPluginManager().registerEvents(new SafeHarvestManager(this, config), this);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

}
