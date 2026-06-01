package net.wakcedon.chattabsreloaded.config;

public interface PlatformConfig {
    void load();
    void save();
    String getConfigPath();
}