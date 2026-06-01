package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class NeoForgeChatTabsConfig extends ChatTabsConfigBase {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(ChatTab.class, new ChatTabTypeAdapter())
            .registerTypeHierarchyAdapter(ChatLineFilter.class, new ChatLineFilterTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setLenient()
            .create();

    private static NeoForgeChatTabsConfig INSTANCE;

    public static NeoForgeChatTabsConfig getInstance() {
        if (INSTANCE == null) {
            try {
                load();
            } catch (Throwable t) {
                INSTANCE = new NeoForgeChatTabsConfig();
            }
        }
        return INSTANCE;
    }

    public static void load() {
        Path cfg = getConfigFile();
        try {
            if (Files.exists(cfg)) {
                String json = Files.readString(cfg);
                NeoForgeChatTabsConfig cfgObj = GSON.fromJson(json, NeoForgeChatTabsConfig.class);
                if (cfgObj == null) cfgObj = new NeoForgeChatTabsConfig();
                INSTANCE = cfgObj;
                return;
            }
        } catch (Throwable ignored) {}
        INSTANCE = new NeoForgeChatTabsConfig();
    }

    @Override
    public void save() {
        try {
            Path cfg = getConfigFile();
            Files.createDirectories(cfg.getParent());
            String json = GSON.toJson(this);
            Files.writeString(cfg, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException ignored) {}
    }

    private static Path getConfigFile() {
        // Try NeoForge config directory
        try {
            Class<?> neoForgeConfig = Class.forName("net.neoforged.fml.loading.moddiscovery.ModFileInfo");
            Class<?> modContainer = Class.forName("net.neoforged.fml.ModContainer");
            Class<?> configDir = Class.forName("net.neoforged.fml.loading.moddiscovery.ModDirectoryInfo");
            // Simplified fallback for NeoForge
            return Path.of(System.getProperty("user.dir")).resolve("config").resolve("chattabs.json");
        } catch (Throwable ignored) {
        }
        // Fallback to Minecraft run directory /config via reflection
        try {
            Class<?> mc = Class.forName("net.minecraft.client.Minecraft");
            Object instance = mc.getMethod("getInstance").invoke(null);
            if (instance != null) {
                Object gameDir = instance.getClass().getField("gameDirectory").get(instance);
                if (gameDir instanceof java.io.File) {
                    return ((java.io.File) gameDir).toPath().resolve("config").resolve("chattabs.json");
                }
            }
        } catch (Throwable ignored) {
        }
        // Last resort: current working directory
        return Path.of(System.getProperty("user.dir")).resolve("config").resolve("chattabs.json");
    }

    @Override
    public List<ChatTab> getVisibleChatTabs() {
        String serverIp = getCurrentServerIp();
        if (serverIp == null) return super.getVisibleChatTabs();
        List<ServerProfile> matchingProfiles = serverProfiles.stream()
                .filter(profile -> serverIp.endsWith(profile.getServerAddress()))
                .sorted((a, b) -> {
                    int ac = serverIp.compareTo(a.getServerAddress());
                    int bc = serverIp.compareTo(b.getServerAddress());
                    return Integer.compare(ac, bc);
                }).toList();
        if (matchingProfiles.isEmpty()) return super.getVisibleChatTabs();
        return matchingProfiles.get(matchingProfiles.size() - 1).getTabs();
    }

    @Override
    public void addChatTabFirst(ChatTab newTab) {
        super.addChatTabFirst(newTab);
        String serverIp = getCurrentServerIp();
        if (serverIp != null) {
            ServerProfile profile = getCurrentServerProfile();
            if (profile != null) profile.addTabId(newTab.getId());
        }
    }

    private ServerProfile getCurrentServerProfile() {
        String serverIp = getCurrentServerIp();
        if (serverIp == null) return null;
        List<ServerProfile> matchingProfiles = serverProfiles.stream()
                .filter(profile -> serverIp.endsWith(profile.getServerAddress()))
                .sorted((a, b) -> {
                    int ac = serverIp.compareTo(a.getServerAddress());
                    int bc = serverIp.compareTo(b.getServerAddress());
                    return Integer.compare(ac, bc);
                }).toList();
        if (matchingProfiles.isEmpty()) return null;
        return matchingProfiles.get(matchingProfiles.size() - 1);
    }

    private String getCurrentServerIp() {
        try {
            Class<?> mc = Class.forName("net.minecraft.client.Minecraft");
            Object instance = mc.getMethod("getInstance").invoke(null);
            if (instance == null) return null;
            Object serverData = instance.getClass().getMethod("getCurrentServer").invoke(instance);
            if (serverData == null) return null;
            try {
                return (String) serverData.getClass().getField("ip").get(serverData);
            } catch (NoSuchFieldException e) {
                return (String) serverData.getClass().getMethod("address").invoke(serverData);
            }
        } catch (Throwable t) {
            return null;
        }
    }
}