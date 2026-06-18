package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.TabFilterPresets;
import net.minecraft.client.Minecraft;

import net.wakcedon.chattabsreloaded.neoforge.ChatTabsModConfig;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
    private final Path profilesPath;

    private ProfilesConfig profilesConfig;

    public NeoForgeChatTabsConfig(Path configPath) {
        this.configPath = configPath;
        this.profilesPath = configPath.getParent().resolve("chattabs-profiles.json");
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
                    if(getChatTabs().isEmpty()) {
                        createDefaultTabs();
                        save();
                    }
                    return;
                }
            }
        } catch(Throwable t) {
            ChatTabs.LOGGER.warning("Failed to load config: " + t.getMessage());
        }
        ChatTabsConfigBase.setPlatformConfig(this);
        if(getChatTabs().isEmpty()) {
            createDefaultTabs();
        }
    }

    public void loadProfiles() {
        profilesConfig = ProfilesConfig.load(profilesPath);
    }

    @Override
    public void reloadProfiles() {
        loadProfiles();
        ChatTabs.LOGGER.info("Reloaded profiles config");
    }

    public ProfilesConfig getProfilesConfig() {
        if(profilesConfig == null) loadProfiles();
        return profilesConfig;
    }

    public Path getProfilesPath() {
        return profilesPath;
    }

    private void createDefaultTabs() {
        getChatTabs().addAll(TabFilterPresets.createDefaultTabs());
        ChatTabs.LOGGER.info("Created 4 default tabs");
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch(IOException e) {
            ChatTabs.LOGGER.warning("Failed to save config: " + e.getMessage());
        }
    }

    public void syncFromSpec() {
        this.enabled = ChatTabsModConfig.CLIENT.enabled.get();
        this.maxLines = ChatTabsModConfig.CLIENT.maxLines.get();
        this.previewTime = ChatTabsModConfig.CLIENT.previewTime.get().floatValue();
        this.clearHistory = ChatTabsModConfig.CLIENT.clearHistory.get();
        this.textShadow = ChatTabsModConfig.CLIENT.textShadow.get();
        this.autoGenerateMsgTabs = ChatTabsModConfig.CLIENT.autoGenerateMsgTabs.get();
        this.showUnreadCounter = ChatTabsModConfig.CLIENT.showUnreadCounter.get();
        this.tabDragAndDrop = ChatTabsModConfig.CLIENT.tabDragAndDrop.get();
        this.tabAnimationFade = ChatTabsModConfig.CLIENT.tabAnimationFade.get();
        this.tabAppearAnimation = ChatTabsModConfig.CLIENT.tabAppearAnimation.get();
        this.selectedTabColor = parseHexColor(ChatTabsModConfig.CLIENT.selectedTabColor.get());
        this.unreadColor = parseHexColor(ChatTabsModConfig.CLIENT.unreadColor.get());
        this.bgColor = parseHexColor(ChatTabsModConfig.CLIENT.bgColor.get());
        this.bgColorHovered = parseHexColor(ChatTabsModConfig.CLIENT.bgColorHovered.get());
    }

    private static Color parseHexColor(String hex) {
        try {
            return new Color((int) Long.parseLong(hex.replace("#", ""), 16), true);
        } catch(Exception e) {
            return new Color(0x80000000, true);
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
        if(serverIp != null) {
            ServerTabProfile profile = getProfilesConfig().findProfile(serverIp);
            if(profile != null && profile.getTabs() != null && !profile.getTabs().isEmpty()) {
                return profile.getTabs();
            }
        }
        return super.getVisibleChatTabs();
    }

    @Override
    public void addChatTabFirst(ChatTab newTab) {
        super.addChatTabFirst(newTab);
        String serverIp = getCurrentServerIp();
        if(serverIp != null) {
            ServerTabProfile profile = getProfilesConfig().findProfile(serverIp);
            if(profile != null && profile.getTabs() != null) {
                profile.getTabs().add(0, newTab);
                getProfilesConfig().save(profilesPath);
            }
        }
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
        this.showUnreadCounter = other.showUnreadCounter;
        this.tabDragAndDrop = other.tabDragAndDrop;
        this.tabAnimationFade = other.tabAnimationFade;
        this.tabAppearAnimation = other.tabAppearAnimation;
        this.selectedTab = other.selectedTab;
        this.getChatTabs().clear();
        this.getChatTabs().addAll(other.getChatTabs());
    }
}
