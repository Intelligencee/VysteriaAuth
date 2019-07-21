package org.vysteria.auth.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.vysteria.auth.Authentication;
import org.vysteria.auth.utils.AuthenticationDetails;
import org.vysteria.auth.utils.Messages;
import org.vysteria.auth.utils.Options;
import org.vysteria.auth.utils.TOTP;

import java.security.GeneralSecurityException;

public class AuthEvents implements Listener {

    @EventHandler
    public void onEvent(final AsyncPlayerChatEvent event) {
        if (Authentication.getInstance().hasTwofactorauth(event.getPlayer().getUniqueId())) {
            final AuthenticationDetails authenticationDetails = Authentication.getInstance().getAuthenticationDetails(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            new BukkitRunnable() {
                public void run() {
                    String validCode;
                    try {
                        validCode = TOTP.generateCurrentNumberString(authenticationDetails.getKey());
                    }
                    catch (GeneralSecurityException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (validCode.equals(event.getMessage())) {
                        if (authenticationDetails.isSetup()) {
                            Authentication.getInstance().saveAuthenticationDetails(event.getPlayer().getUniqueId(), authenticationDetails);
                        }
                        Authentication.getInstance().unloadAuthenticationDetails(event.getPlayer().getUniqueId());
                        event.getPlayer().sendMessage(Messages.VALID_CODE.getMessage());
                    }
                    else {
                        final AuthenticationDetails val$authenticationDetails = authenticationDetails;
                        ++val$authenticationDetails.attempts;
                        event.getPlayer().sendMessage(Messages.INVALID_CODE.getMessage());
                        if (authenticationDetails.attempts > Options.MAX_TRIES.getIntValue()) {
                            if (!authenticationDetails.isSetup()) {
                                new BukkitRunnable() {
                                    public void run() {
                                        event.getPlayer().kickPlayer(Messages.FAIL_MESSAGE.getMessage());
                                    }
                                }.runTask((Plugin)Authentication.getInstance());
                            }
                            else {
                                Authentication.getInstance().unloadAuthenticationDetails(event.getPlayer().getUniqueId());
                                event.getPlayer().sendMessage(Messages.SETUP_FAIL.getMessage());
                            }
                        }
                    }
                }
            }.runTaskAsynchronously((Plugin)Authentication.getInstance());
        }
    }

    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent event) {
        if (!Options.DENY_COMMANDS.getBooleanValue()) {
            return;
        }
        if (Authentication.getInstance().hasTwofactorauth(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEvent(final PlayerMoveEvent event) {
        if (!Options.DENY_MOVEMENT.getBooleanValue()) {
            return;
        }
        if ((event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) && Authentication.getInstance().hasTwofactorauth(event.getPlayer().getUniqueId())) {
            event.getPlayer().teleport(event.getFrom());
        }
    }

    @EventHandler
    public void onEvent(final PlayerInteractEvent event) {
        if (!Options.DENY_INTERACTION.getBooleanValue()) {
            return;
        }
        if (Authentication.getInstance().hasTwofactorauth(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEvent(final EntityDamageEvent event) {
        if (!Options.DENY_DAMAGE.getBooleanValue()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (Authentication.getInstance().hasTwofactorauth(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
