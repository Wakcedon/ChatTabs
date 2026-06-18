package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.SendModifier;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ProfilesConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(ChatTab.class, new ChatTabTypeAdapter())
            .registerTypeHierarchyAdapter(ChatLineFilter.class, new ChatLineFilterTypeAdapter())
            .setLenient()
            .create();

    private List<ServerTabProfile> profiles = new ArrayList<>();
    private ServerTabProfile defaultProfile;

    public static ProfilesConfig load(Path path) {
        if(Files.exists(path)) {
            try {
                String json = Files.readString(path);
                ProfilesConfig loaded = GSON.fromJson(json, ProfilesConfig.class);
                if(loaded != null) {
                    if(loaded.profiles == null) loaded.profiles = new ArrayList<>();
                    return loaded;
                }
            } catch(Throwable t) {
                ChatTabs.LOGGER.warning("Failed to load profiles config: " + t.getMessage());
            }
        }
        return createDefault();
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            String json = GSON.toJson(this);
            Files.writeString(path, json, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch(IOException e) {
            ChatTabs.LOGGER.warning("Failed to save profiles config: " + e.getMessage());
        }
    }

    public static ProfilesConfig createDefault() {
        ProfilesConfig cfg = new ProfilesConfig();

        ServerTabProfile def = new ServerTabProfile(
                "default",
                "Default",
                createDefaultTabs()
        );
        cfg.defaultProfile = def;
        cfg.profiles = new ArrayList<>();
        return cfg;
    }

    private static List<ChatTab> createDefaultTabs() {
        List<ChatTab> list = new ArrayList<>();
        list.add(new ChatTab("tab.all", "chattabs.tab.all", true, true,
                new ChatLineFilter(".*"), new SendModifier()));
        list.add(new ChatTab("tab.global", "chattabs.tab.global", true, true,
                new ChatLineFilter(buildGlobalRegex(), false), new SendModifier("!")));
        list.add(new ChatTab("tab.local", "chattabs.tab.local", true, true,
                new ChatLineFilter(buildLocalRegex(), false), new SendModifier()));
        list.add(new ChatTab("tab.notifications", "chattabs.tab.notifications", true, true,
                new ChatLineFilter(buildNotificationRegex(), false), new SendModifier()));
        return list;
    }

    public ServerTabProfile findProfile(String serverIp) {
        if(serverIp == null || profiles == null) return defaultProfile;
        ServerTabProfile best = null;
        int bestLen = 0;
        for(ServerTabProfile p : profiles) {
            String pattern = p.getServerIp();
            if(pattern != null && serverIp.endsWith(pattern)) {
                if(pattern.length() > bestLen) {
                    bestLen = pattern.length();
                    best = p;
                }
            }
        }
        return best != null ? best : defaultProfile;
    }

    public List<ServerTabProfile> getProfiles() { return profiles; }
    public void setProfiles(List<ServerTabProfile> profiles) { this.profiles = profiles; }

    public ServerTabProfile getDefaultProfile() { return defaultProfile; }
    public void setDefaultProfile(ServerTabProfile defaultProfile) { this.defaultProfile = defaultProfile; }

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
}
