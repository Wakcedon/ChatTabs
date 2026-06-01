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

    public NeoForgeConfig() {
        this.configPath = Path.of(getConfigPath());
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
        try {
            Class<?> mc = Class.forName("net.minecraft.client.Minecraft");
            Object instance = mc.getMethod("getInstance").invoke(null);
            if (instance != null) {
                Object gameDir = instance.getClass().getField("gameDirectory").get(instance);
                if (gameDir instanceof java.io.File) {
                    return ((java.io.File) gameDir).toPath().resolve("config").resolve("chattabs.json").toString();
                }
            }
        } catch (Throwable ignored) {
        }
        return Path.of(System.getProperty("user.dir")).resolve("config").resolve("chattabs.json").toString();
    }

    public ChatTabsConfigBase getConfig() {
        return config;
    }
}