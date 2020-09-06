package com.gestankbratwurst.safeharvest;

import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of ProtectedHarvest and was created at the 26.07.2020
 *
 * ProtectedHarvest can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
class SafeHarvestConfig {

  SafeHarvestConfig(final SafeHarvest plugin) {
    plugin.getDataFolder().mkdirs();
    final File configFile = new File(plugin.getDataFolder(), "configuration.yml");
    if (!configFile.exists()) {
      plugin.saveResource("configuration.yml", false);
    }
    final FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
    this.enabledWorlds = new HashSet<>();
    this.allowedTools = EnumSet.noneOf(Material.class);

    for (final String worldName : configuration.getStringList("enabled_worlds")) {
      final World world = Bukkit.getWorld(worldName);
      if (world == null) {
        plugin.getLogger().warning("World does not exist and will be ignored: " + worldName);
      } else {
        this.enabledWorlds.add(world.getUID());
      }
    }

    for (final String materialName : configuration.getStringList("harvest_tools")) {
      try {
        final Material material = Material.valueOf(materialName);
        this.allowedTools.add(material);
      } catch (final Exception e) {
        plugin.getLogger().warning("Material does not exist and will be ignored: " + materialName);
      }
    }

    this.prefix = configuration.getString("messages.prefix");
    this.onlyFullyGrown = configuration.getBoolean("allow_only_grown");
    this.msgNoLowest = configuration.getString("messages.cant_break_lowest");
    this.msgOnlyFull = configuration.getString("messages.only_grown");
    this.msgNoYoung = configuration.getString("messages.no_young");
    this.triggerOnSuccess = configuration.getBoolean("trigger_on_success", false);
  }

  @Getter
  private final String prefix;
  @Getter
  private final String msgNoLowest;
  @Getter
  private final String msgOnlyFull;
  @Getter
  private final String msgNoYoung;
  @Getter
  private final boolean triggerOnSuccess;

  @Getter
  private final boolean onlyFullyGrown;
  private final Set<UUID> enabledWorlds;
  private final EnumSet<Material> allowedTools;


  boolean isProtectedWorld(final UUID worldID) {
    return this.enabledWorlds.contains(worldID);
  }

  boolean isAllowedTool(final Material material) {
    return this.allowedTools.contains(material);
  }

}
