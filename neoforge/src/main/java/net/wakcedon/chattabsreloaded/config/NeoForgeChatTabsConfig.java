package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
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

    private static final String[] GLOBAL_TAGS = {
        "Global", "G", "Server", "Broadcast", "Announcement",
        "Staff", "Admin", "Mod", "Event", "Shout", "World",
        "Trade", "Auction", "Tip", "Notice",
        "Глобальный", "Глобал", "Г", "Сервер", "Объявление",
        "Админ", "Модератор", "Мод", "Ивент", "Мир",
        "Торговля", "Аукцион", "Важно",
        "Global", "Servidor", "Anuncio", "Staff", "Admin",
        "Evento", "Mundo", "Subasta", "Consejo", "Aviso",
        "全球", "服务器", "公告", "工作人员", "管理员",
        "活动", "世界", "交易", "拍卖", "提示",
        "信息", "系统", "通知"
    };

    private static final String[] GLOBAL_CIRCLED = {
        "🄶", "Ⓖ", "🅖", "🅶", "🌐"
    };

    private static final String[] LOCAL_TAGS = {
        "Local", "L", "Локальный", "Локал", "Л", "Local", "本地"
    };

    private static final String[] LOCAL_CIRCLED = {
        "🄻", "Ⓛ", "🅛", "🅻", "📍"
    };

    private static final String[] NOTIFICATION_TAGS = {
        "!", "Server", "Info", "INF", "Notice", "Alert",
        "System", "Notification", "PSA", "Warning",
        "!", "Сервер", "ИНФО", "Информация", "Система",
        "Уведомление", "Важно", "Объявление", "Предупреждение",
        "!", "Servidor", "Info", "Aviso", "Sistema",
        "Notificación", "Importante", "Anuncio", "Advertencia",
        "!", "服务器", "信息", "通知", "系统",
        "公告", "重要", "警告"
    };

    private static String buildGlobalRegex() {
        String tags = String.join("|", GLOBAL_TAGS);
        String circled = String.join("|", GLOBAL_CIRCLED);
        return "^[!?].*|.*(?:" + circled + ").*|.*\\[(?:" + tags + ")\\].*";
    }

    private static String buildLocalRegex() {
        String localTags = String.join("|", LOCAL_TAGS);
        String localCircled = String.join("|", LOCAL_CIRCLED);
        return ".*(?:" + localCircled + ").*|.*\\[(?:" + localTags + ")\\].*|.*: .*|.*<[^>]+>.*";
    }

    private static String buildNotificationRegex() {
        String tags = String.join("|", NOTIFICATION_TAGS);
        return ".*\\[(?:" + tags + ")\\].*";
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
        defaults.add(new ChatTab("tab.notifications", "chattabs.tab.notifications", true, true,
                new ChatLineFilter(buildNotificationRegex(), false),
                new SendModifier()));
        getChatTabs().addAll(defaults);
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
