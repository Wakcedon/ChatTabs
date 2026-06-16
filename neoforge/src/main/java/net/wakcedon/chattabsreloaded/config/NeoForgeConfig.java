package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class NeoForgeConfig implements PlatformConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .setLenient()
            .create();

    private final Path configPath;
    private ChatTabsConfigBase config;

    public NeoForgeConfig(String configPath) {
        this.configPath = Path.of(configPath);
        this.config = new ChatTabsConfigBase();
        this.config.platformConfig = this;
    }

    @Override
    public void load() {
        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                ChatTabsConfigBase loadedConfig = GSON.fromJson(json, ChatTabsConfigBase.class);
                if (loadedConfig != null) {
                    this.config = loadedConfig;
                    this.config.platformConfig = this;
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
        this.config = new ChatTabsConfigBase();
        this.config.platformConfig = this;
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(config);
            Files.writeString(configPath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ignored) {
        }
    }

    @Override
    public String getConfigPath() {
        return configPath.toString();
    }

    public ChatTabsConfigBase getConfig() {
        return config;
    }
}
