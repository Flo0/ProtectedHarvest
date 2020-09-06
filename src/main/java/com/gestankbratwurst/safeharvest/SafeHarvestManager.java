package com.gestankbratwurst.safeharvest;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of ProtectedHarvest and was created at the 26.07.2020
 *
 * ProtectedHarvest can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class SafeHarvestManager implements Listener {

  SafeHarvestManager(final SafeHarvest plugin, final SafeHarvestConfig config) {
    this.plugin = plugin;
    this.config = config;
    this.structuralMaterials = EnumSet.of(
        Material.SUGAR_CANE,
        Material.BAMBOO,
        Material.KELP_PLANT,
        Material.CACTUS);
    this.cropMaterials = EnumSet.of(
        Material.POTATOES,
        Material.WHEAT,
        Material.BEETROOTS,
        Material.CARROTS,
        Material.NETHER_WART);
    this.blockCrops = EnumSet.of(
        Material.MELON,
        Material.PUMPKIN,
        Material.COCOA,
        Material.COCOA_BEANS,
        Material.KELP);
    this.messageCooldown = new HashMap<>();
  }

  private final SafeHarvest plugin;
  private final SafeHarvestConfig config;
  private final EnumSet<Material> structuralMaterials;
  private final EnumSet<Material> cropMaterials;
  private final EnumSet<Material> blockCrops;
  private final Map<UUID, Long> messageCooldown;

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    this.messageCooldown.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
  }

  @EventHandler
  public void onLeave(final PlayerQuitEvent event) {
    this.messageCooldown.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockBreak(final BlockBreakEvent event) {
    if (!this.config.isProtectedWorld(event.getPlayer().getWorld().getUID())) {
      return;
    }
    if (event.getPlayer().hasPermission("safeharvest.bypass")) {
      return;
    }
    if (event.isCancelled() == this.config.isTriggerOnSuccess()) {
      return;
    }

    final Block block = event.getBlock();
    final Material material = block.getType();
    final Player player = event.getPlayer();

    if (this.structuralMaterials.contains(material)) {
      this.handleStructural(event, player, block, material);
    } else if (this.cropMaterials.contains(material)) {
      this.handleCrops(event, player, block, material);
    } else if (this.blockCrops.contains(material)) {
      this.handleBlockCrops(event, player);
    }

  }

  private void handleStructural(final BlockBreakEvent event, final Player player, final Block block, final Material material) {
    if (!this.config.isAllowedTool(player.getInventory().getItemInMainHand().getType())) {
      event.setCancelled(true);
      return;
    }
    if (!this.isLowest(block)) {
      event.setCancelled(false);
    } else {
      if (System.currentTimeMillis() - this.messageCooldown.get(player.getUniqueId()) > 500) {
        Msg.send(player, this.config.getMsgNoLowest());
        this.messageCooldown.put(player.getUniqueId(), System.currentTimeMillis());
      }
      final Block up = block.getRelative(BlockFace.UP);
      if (up.getType() == material) {
        final BlockBreakEvent breakEvent = new BlockBreakEvent(event.getBlock().getRelative(BlockFace.UP), event.getPlayer());
        Bukkit.getPluginManager().callEvent(breakEvent);
        up.breakNaturally(player.getInventory().getItemInMainHand());
      }
      event.setCancelled(true);
    }
  }

  private void handleCrops(final BlockBreakEvent event, final Player player, final Block block, final Material material) {
    if (!this.config.isAllowedTool(player.getInventory().getItemInMainHand().getType())) {
      event.setCancelled(true);
      return;
    }
    final BlockData data = block.getBlockData();
    if (data instanceof Ageable) {
      final Ageable ageable = (Ageable) data;
      if (ageable.getAge() == 0) {
        if (System.currentTimeMillis() - this.messageCooldown.get(player.getUniqueId()) > 500) {
          Msg.send(player, this.config.getMsgNoYoung());
          this.messageCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }
        event.setCancelled(true);
        return;
      } else if (ageable.getAge() != ageable.getMaximumAge() && this.config.isOnlyFullyGrown()) {
        if (System.currentTimeMillis() - this.messageCooldown.get(player.getUniqueId()) > 500) {
          Msg.send(player, this.config.getMsgOnlyFull());
          this.messageCooldown.put(player.getUniqueId(), System.currentTimeMillis());
        }
        event.setCancelled(true);
        return;
      }
    }
    event.setCancelled(false);
    Bukkit.getScheduler().runTask(this.plugin, () -> block.setType(material));
  }

  private void handleBlockCrops(final BlockBreakEvent event, final Player player) {
    if (!this.config.isAllowedTool(player.getInventory().getItemInMainHand().getType())) {
      event.setCancelled(true);
      return;
    }
    event.setCancelled(false);
  }

  private boolean isLowest(final Block block) {
    return block.getRelative(BlockFace.DOWN).getType() != block.getType();
  }

}
