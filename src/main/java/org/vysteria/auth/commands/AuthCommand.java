package org.vysteria.auth.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.vysteria.auth.Authentication;
import org.vysteria.auth.utils.AuthenticationDetails;
import org.vysteria.auth.utils.Messages;
import org.vysteria.auth.utils.TOTP;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class AuthCommand implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender.hasPermission("vysteria.auth") && sender instanceof Player) {
            final Player player = (Player)sender;
            Authentication.getInstance().attemptDataLoad(player.getUniqueId());
            if (Authentication.getInstance().hasTwofactorauth(player.getUniqueId())) {
                Authentication.getInstance().unloadAuthenticationDetails(player.getUniqueId());
                player.sendMessage(Messages.SETUP_ALREADY_ENABLED.getMessage());
                return true;
            }
            final AuthenticationDetails authenticationDetails = new AuthenticationDetails(player.getUniqueId().toString(), TOTP.generateBase32Secret(), true);
            Authentication.getInstance().addAuthenticationDetauls(player.getUniqueId(), authenticationDetails);
            try {
                final URL url = new URL(TOTP.qrImageUrl("minecraftserver", authenticationDetails.getKey()));
                final BufferedImage image = ImageIO.read(url);
                final ItemStack i = new ItemStack(Material.MAP, 1);
                final MapView view = Bukkit.createMap(player.getWorld());
                view.getRenderers().clear();
                view.addRenderer((MapRenderer)new QRMap(image));
                i.setDurability(view.getId());
                player.setItemInHand(i);
                player.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), 90.0f));
                sender.sendMessage(Messages.SETUP_QRMAP.getMessage().replace("%code%", authenticationDetails.getKey()));
            }
            catch (IOException e) {
                sender.sendMessage(Messages.SETUP_CODE.getMessage().replace("%code%", authenticationDetails.getKey()));
                e.printStackTrace();
            }
            sender.sendMessage(Messages.SETUP_VALIDATE.getMessage());
        }
        return true;
    }
}
