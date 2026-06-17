package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;

public class NeoForgeChatTabsConfig extends ChatTabsConfigBase implements PlatformConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(ChatTab.class, new ChatTabTypeAdapter())
            .registerTypeHierarchyAdapter(ChatLineFilter.class, new ChatLineFilterTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setLenient()
            .create();

    private final Path configPath;

    public NeoForgeChatTabsConfig(Path configPath) {
        this.configPath = configPath;
        ChatTabsConfigBase.setPlatformConfig(this);
    }

    @Override
    public void load() {
        try {
            if(Files.exists(configPath)) {
                String json = Files.readString(configPath);
                NeoForgeChatTabsConfig loaded = GSON.fromJson(json, NeoForgeChatTabsConfig.class);
                if(loaded != null) {
                    applyFrom(loaded);
                    ChatTabsConfigBase.setPlatformConfig(this);
                    return;
                }
            }
        } catch(Throwable t) {
            ChatTabs.LOGGER.warning("Failed to load config: " + t.getMessage());
        }
        ChatTabsConfigBase.setPlatformConfig(this);
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch(IOException e) {
            ChatTabs.LOGGER.warning("Failed to save config: " + e.getMessage());
        }
    }

    @Override
    public String getConfigPath() {
        return configPath.toString();
    }

    @Override
    public ChatTabsConfigBase getConfig() {
        return this;
    }

    @Override
    public List<ChatTab> getVisibleChatTabs() {
        String serverIp = getCurrentServerIp();
        if(serverIp == null) return super.getVisibleChatTabs();
        ServerProfile bestProfile = findBestProfile(serverIp);
        if(bestProfile == null) return super.getVisibleChatTabs();
        return bestProfile.getTabs();
    }

    @Override
    public void addChatTabFirst(ChatTab newTab) {
        super.addChatTabFirst(newTab);
        String serverIp = getCurrentServerIp();
        if(serverIp != null) {
            ServerProfile profile = findBestProfile(serverIp);
            if(profile != null) profile.addTabId(newTab.getId());
        }
    }

    private ServerProfile findBestProfile(String serverIp) {
        return serverProfiles.stream()
                .filter(profile -> serverIp.endsWith(profile.getServerAddress()))
                .max(Comparator.comparingInt(p -> p.getServerAddress().length()))
                .orElse(null);
    }

    private static String getCurrentServerIp() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getCurrentServer() == null) return null;
        return mc.getCurrentServer().ip;
    }

    private void applyFrom(NeoForgeChatTabsConfig other) {
        this.enabled = other.enabled;
        this.maxLines = other.maxLines;
        this.previewTime = other.previewTime;
        this.clearHistory = other.clearHistory;
        this.textShadow = other.textShadow;
        this.bgColor = other.bgColor;
        this.bgColorHovered = other.bgColorHovered;
        this.selectedTabColor = other.selectedTabColor;
        this.unreadColor = other.unreadColor;
        this.chatWidth = other.chatWidth;
        this.chatHeightUnfocused = other.chatHeightUnfocused;
        this.chatHeightFocused = other.chatHeightFocused;
        this.autoGenerateMsgTabs = other.autoGenerateMsgTabs;
        this.selectedTab = other.selectedTab;
        this.getChatTabs().clear();
        this.getChatTabs().addAll(other.getChatTabs());
        this.serverProfiles.clear();
        this.serverProfiles.addAll(other.serverProfiles);
    }
}
