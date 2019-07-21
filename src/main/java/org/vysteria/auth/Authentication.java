package org.vysteria.auth;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.vysteria.auth.commands.AuthCommand;
import org.vysteria.auth.events.AuthEvents;
import org.vysteria.auth.utils.AuthenticationDetails;
import org.vysteria.auth.utils.Messages;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Authentication extends JavaPlugin implements Listener {

    private static Authentication instance;
    private static HashMap<UUID, AuthenticationDetails> loadedAuthenticationDetails;

    public void onEnable() {
        Authentication.instance = this;
        Authentication.loadedAuthenticationDetails = new HashMap<UUID, AuthenticationDetails>();
        this.dataGenerator();
        Bukkit.getPluginCommand("auth").setExecutor((CommandExecutor)new AuthCommand());
        Bukkit.getPluginManager().registerEvents((Listener)new AuthEvents(), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
    }

    public static Authentication getInstance() {
        return Authentication.instance;
    }

    private void dataGenerator() {
        this.saveDefaultConfig();
        final File dataDir = new File(this.getDataFolder() + File.separator + "data");
        if (!dataDir.isDirectory()) {
            dataDir.mkdir();
        }
    }

    public void attemptDataLoad(final UUID uuid) {
        final File userPath = new File(this.getDataFolder() + File.separator + "data" + File.separator + uuid.toString() + ".yml");
        if (!userPath.exists()) {
            return;
        }
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(userPath);
        final String name = yaml.getString("name");
        final String key = yaml.getString("key");
        Authentication.loadedAuthenticationDetails.put(uuid, new AuthenticationDetails(name, key, false));
    }

    public void addAuthenticationDetauls(final UUID uuid, final AuthenticationDetails authenticationDetails) {
        Authentication.loadedAuthenticationDetails.put(uuid, authenticationDetails);
    }

    public void saveAuthenticationDetails(final UUID uuid, final AuthenticationDetails authenticationDetails) {
        final File userPath = new File(this.getDataFolder() + File.separator + "data" + File.separator + uuid.toString() + ".yml");
        if (!userPath.exists()) {
            try {
                userPath.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(userPath);
        yaml.set("name", (Object)authenticationDetails.getName());
        yaml.set("key", (Object)authenticationDetails.getKey());
        try {
            yaml.save(userPath);
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.attemptDataLoad(event.getPlayer().getUniqueId());
        if (this.hasTwofactorauth(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(Messages.LOGIN.getMessage());
        }
    }

    public boolean hasTwofactorauth(final UUID uuid) {
        return Authentication.loadedAuthenticationDetails.containsKey(uuid);
    }

    public AuthenticationDetails getAuthenticationDetails(final UUID uuid) {
        return Authentication.loadedAuthenticationDetails.get(uuid);
    }

    public void unloadAuthenticationDetails(final UUID uuid) {
        Authentication.loadedAuthenticationDetails.remove(uuid);
    }
}
