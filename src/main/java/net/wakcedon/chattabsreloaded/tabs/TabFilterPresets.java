package net.wakcedon.chattabsreloaded.tabs;

import java.util.ArrayList;
import java.util.List;

public class TabFilterPresets {

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

    public static String buildGlobalRegex() {
        String tags = String.join("|", GLOBAL_TAGS);
        String circled = String.join("|", GLOBAL_CIRCLED);
        return "^[!?].*|.*(?:" + circled + ").*|.*\\[(?:" + tags + ")\\].*";
    }

    public static String buildLocalRegex() {
        String localTags = String.join("|", LOCAL_TAGS);
        String localCircled = String.join("|", LOCAL_CIRCLED);
        return ".*(?:" + localCircled + ").*|.*\\[(?:" + localTags + ")\\].*|.*: .*|.*<[^>]+>.*";
    }

    public static String buildNotificationRegex() {
        String tags = String.join("|", NOTIFICATION_TAGS);
        return ".*\\[(?:" + tags + ")\\].*|^\\[\\+\\].*|^\\[-\\].*";
    }

    public static List<ChatTab> createDefaultTabs() {
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
}
