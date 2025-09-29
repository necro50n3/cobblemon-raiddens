package com.necro.raid.dens.common.util;

public interface IHealthSetter {
    void setMaxHealth(int maxHealth, boolean setCurrent);

    default void setMaxHealth(int maxHealth) {
        this.setMaxHealth(maxHealth, true);
    }
}
