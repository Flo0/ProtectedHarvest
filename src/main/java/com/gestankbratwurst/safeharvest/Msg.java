package com.gestankbratwurst.safeharvest;

import org.bukkit.command.CommandSender;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of ProtectedHarvest and was created at the 26.07.2020
 *
 * ProtectedHarvest can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
class Msg {

  static void init(final SafeHarvestConfig config) {
    Msg.CONFIG = config;
  }

  private static SafeHarvestConfig CONFIG;

  static void send(final CommandSender receiver, final Object message) {
    receiver.sendMessage(CONFIG.getPrefix() + message.toString());
  }

}
