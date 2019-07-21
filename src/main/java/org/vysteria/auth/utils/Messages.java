package org.vysteria.auth.utils;

import org.bukkit.ChatColor;
import org.vysteria.auth.Authentication;

public enum Messages {

    LOGIN(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.login"))),
    FAIL_MESSAGE(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.fail-message"))),
    INVALID_CODE(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.invalid-code"))),
    VALID_CODE(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.valid-code"))),
    SETUP_VALIDATE(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.setup-validate"))),
    SETUP_ALREADY_ENABLED(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.setup-already-enabled"))),
    SETUP_FAIL(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.setup-fail"))),
    SETUP_QRMAP(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.setup-qrmap"))),
    SETUP_CODE(ChatColor.translateAlternateColorCodes('&', Authentication.getInstance().getConfig().getString("messages.setup-code")));

    private String message;

    private Messages(final String string) {
        this.message = string;
    }

    public String getMessage() {
        return this.message;
    }
}
