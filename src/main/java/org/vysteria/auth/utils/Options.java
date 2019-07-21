package org.vysteria.auth.utils;

import org.vysteria.auth.Authentication;

public enum Options {

    MAX_TRIES(Authentication.getInstance().getConfig().getInt("options.max-tries")),
    DENY_COMMANDS(Authentication.getInstance().getConfig().getBoolean("options.deny-commands")),
    DENY_MOVEMENT(Authentication.getInstance().getConfig().getBoolean("options.deny-movement")),
    DENY_INTERACTION(Authentication.getInstance().getConfig().getBoolean("options.deny-interaction")),
    DENY_DAMAGE(Authentication.getInstance().getConfig().getBoolean("options.deny-damage"));

    private int intValue;
    private String stringValue;
    private boolean booleanValue;

    private Options(final int intValue, final String stringValue, final boolean booleanValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
        this.booleanValue = booleanValue;
    }

    private Options(final int intValue) {
        this.intValue = intValue;
    }

    private Options(final String stringValue) {
        this.stringValue = stringValue;
    }

    private Options(final boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public int getIntValue() {
        return this.intValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public boolean getBooleanValue() {
        return this.booleanValue;
    }
}
