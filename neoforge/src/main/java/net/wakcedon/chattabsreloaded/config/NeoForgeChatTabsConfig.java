package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.SendModifier;
import net.minecraft.client.Minecraft;

import net.wakcedon.chattabsreloaded.neoforge.ChatTabsModConfig;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

    private static final String[] GLOBAL_TAGS = {
        // English
        "Global", "G", "Server", "Broadcast", "Announcement",
        "Staff", "Admin", "Mod", "Event", "Shout", "World",
        "Trade", "Auction", "Tip", "Notice",
        // Russian
        "Глобальный", "Глобал", "Г", "Сервер", "Объявление",
        "Админ", "Модератор", "Мод", "Ивент", "Мир",
        "Торговля", "Аукцион", "Важно"
    };

    private static final String[] GLOBAL_CIRCLED = {
        "🄶", "Ⓖ", "🅖", "🅶", "🌐"
    };

    private static final String[] LOCAL_TAGS = {
        // English
        "Local", "L",
        // Russian
        "Локальный", "Локал", "Л"
    };

    private static final String[] LOCAL_CIRCLED = {
        "🄻", "Ⓛ", "🅛", "🅻", "📍"
    };

    private static String buildGlobalRegex() {
        String tags = String.join("|", GLOBAL_TAGS);
        String circled = String.join("|", GLOBAL_CIRCLED);
        return "^[!?].*|.*(?:" + circled + ").*|.*\\[(?:" + tags + ")\\].*";
    }

    private static String buildLocalRegex() {
        String globalTags = String.join("|", GLOBAL_TAGS);
        String globalCircled = String.join("|", GLOBAL_CIRCLED);
        String localTags = String.join("|", LOCAL_TAGS);
        String localCircled = String.join("|", LOCAL_CIRCLED);
        return "^(?![!?])(?!.*(?:" + globalCircled + "))(?!.*\\[(?:" + globalTags + ")\\])(?:.*(?:" + localCircled + ").*|.*\\[(?:" + localTags + ")\\].*|.*: .*|.*<[^>]+>.*)";
    }

    private void createDefaultTabs() {
        ArrayList<ChatTab> defaults = new ArrayList<>();
        defaults.add(new ChatTab("tab.all", "chattabs.tab.all", true, true,
                new ChatLineFilter(".*"),
                new SendModifier()));
        defaults.add(new ChatTab("tab.global", "chattabs.tab.global", true, true,
                new ChatLineFilter(buildGlobalRegex(), false),
                new SendModifier("!")));
        defaults.add(new ChatTab("tab.local", "chattabs.tab.local", true, true,
                new ChatLineFilter(buildLocalRegex(), false),
                new SendModifier()));
        getChatTabs().addAll(defaults);
        ChatTabs.LOGGER.info("Created 3 default tabs");
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
        this.showUnreadCounter = other.showUnreadCounter;
        this.tabDragAndDrop = other.tabDragAndDrop;
        this.tabAnimationFade = other.tabAnimationFade;
        this.selectedTab = other.selectedTab;
        this.getChatTabs().clear();
        this.getChatTabs().addAll(other.getChatTabs());
        this.serverProfiles.clear();
        this.serverProfiles.addAll(other.serverProfiles);
    }
}
