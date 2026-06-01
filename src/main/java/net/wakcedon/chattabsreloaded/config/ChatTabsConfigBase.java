package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
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

public class ChatTabsConfigBase {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(ChatTab.class, new ChatTabTypeAdapter())
            .registerTypeHierarchyAdapter(ChatLineFilter.class, new ChatLineFilterTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setLenient()
            .create();

    @Expose
    public boolean enabled = true;

    @Expose
    public int maxLines = 100;

    @Expose
    public float previewTime = 10;

    @Expose
    public boolean clearHistory = true;

    @Expose
    public boolean textShadow = true;

    @Expose
    public Color bgColor = new Color(0x80000000, true);

    @Expose
    public Color bgColorHovered = new Color(0x80FFFFFF, true);

    @Expose
    public Color selectedTabColor = new Color(0xFFFFFFFF, true);

    @Expose
    public Color unreadColor = new Color(0xFF00AAAA, true);

    @Expose
    public int chatWidth = 320;

    @Expose
    public int chatHeightUnfocused = 90;

    @Expose
    public int chatHeightFocused = 180;

    @Expose
    public boolean autoGenerateMsgTabs = true;

    @Expose
    private boolean saveGenerated = false;

    public int selectedTab = 0;

    @Expose
    private List<ChatTab> chatTabs = new ArrayList<>();

    @Expose
    public List<ServerProfile> serverProfiles = new ArrayList<>();

    public List<ChatTab> getVisibleChatTabs() {
        // Базовая реализация без платформо-специфичной логики
        return chatTabs.stream().filter(ChatTab::isVisibleByDefault).toList();
    }

    public List<ChatTab> getChatTabs() {
        return chatTabs;
    }

    public ChatTab getSelectedChatTab() {
        List<ChatTab> chatTabs = getVisibleChatTabs();
        if (chatTabs.isEmpty()) return null;
        while (selectedTab - 1 >= chatTabs.size()) selectedTab--;
        return chatTabs.get(selectedTab - 1);
    }

    public void addChatTabFirst(ChatTab newTab) {
        chatTabs.add(0, newTab);
    }

    public void save() {
        // Сохранение будет реализовано в платформо-специфичных классах
    }

    public void load() {
        // Загрузка будет реализована в платформо-специфичных классах
    }
}