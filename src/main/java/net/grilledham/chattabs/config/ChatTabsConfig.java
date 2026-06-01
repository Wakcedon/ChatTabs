package net.grilledham.chattabs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.annotations.Expose;
import net.grilledham.chattabs.ChatTabs;
import net.grilledham.chattabs.profiles.ServerProfile;
import net.grilledham.chattabs.tabs.ChatLineFilter;
import net.grilledham.chattabs.tabs.ChatTab;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ChatTabsConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeHierarchyAdapter(ChatTab.class, new ChatTabTypeAdapter())
            .registerTypeHierarchyAdapter(ChatLineFilter.class, new ChatLineFilterTypeAdapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setStrictness(Strictness.LENIENT)
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

    private static ChatTabsConfig INSTANCE;

    private static Path getConfigFile() {
        // Try FabricLoader via reflection when available
        try {
            Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object inst = fabricLoader.getMethod("getInstance").invoke(null);
            Path dir = (Path) fabricLoader.getMethod("getConfigDir").invoke(inst);
            return dir.resolve("chattabs.json");
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

    public ServerProfile getCurrentServerProfile() {
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

    public List<ChatTab> getVisibleChatTabs() {
        ServerProfile profile = getCurrentServerProfile();
        if (profile == null) return chatTabs.stream().filter(ChatTab::isVisibleByDefault).toList();
        return profile.getTabs();
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
        ServerProfile profile = getCurrentServerProfile();
        if (profile != null) profile.addTabId(newTab.getId());
    }

    public void addChatTabLast(ChatTab newTab) {
        chatTabs.add(newTab);
        ServerProfile profile = getCurrentServerProfile();
        if (profile != null) profile.addTabId(newTab.getId());
    }

    public void addChatTabLeft(ChatTab tabToRight, ChatTab newTab) {
        chatTabs.add(chatTabs.indexOf(tabToRight), newTab);
        ServerProfile profile = getCurrentServerProfile();
        if (profile != null) profile.addTabId(newTab.getId());
    }

    public void addChatTabLeft(int tabToRight, ChatTab newTab) {
        ChatTab ttr = getVisibleChatTabs().get(tabToRight);
        addChatTabLeft(ttr, newTab);
    }

    public void addChatTabRight(ChatTab tabToLeft, ChatTab newTab) {
        chatTabs.add(chatTabs.indexOf(tabToLeft) + 1, newTab);
        ServerProfile profile = getCurrentServerProfile();
        if (profile != null) profile.addTabId(newTab.getId());
    }

    public void addChatTabRight(int tabToLeft, ChatTab newTab) {
        ChatTab ttl = getVisibleChatTabs().get(tabToLeft);
        addChatTabRight(ttl, newTab);
    }

    public void removeChatTab(ChatTab tab) {
        chatTabs.remove(tab);
        ServerProfile profile = getCurrentServerProfile();
        if (profile != null && chatTabs.stream().filter(t -> t.getId().equals(tab.getId())).toList().isEmpty()) profile.removeTabId(tab.getId());
    }

    public void removeVisibleChatTab(int i) {
        removeChatTab(getVisibleChatTabs().get(i));
    }

    public void moveChatTabLeft(ChatTab tab) {
        int ttli = getVisibleChatTabs().indexOf(tab) - 1;
        if (ttli < 0) return;
        ChatTab ttl = getVisibleChatTabs().get(ttli);
        chatTabs.remove(tab);
        chatTabs.add(chatTabs.indexOf(ttl), tab);
    }

    public void moveChatTabLeft(int tab) {
        moveChatTabLeft(getVisibleChatTabs().get(tab));
    }

    public void moveChatTabRight(ChatTab tab) {
        int ttri = getVisibleChatTabs().indexOf(tab) + 1;
        if (ttri >= getVisibleChatTabs().size()) return;
        ChatTab ttr = getVisibleChatTabs().get(ttri);
        chatTabs.remove(tab);
        chatTabs.add(chatTabs.indexOf(ttr) + 1, tab);
    }

    public void moveChatTabRight(int tab) {
        moveChatTabRight(getVisibleChatTabs().get(tab));
    }

    public static ChatTabsConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatTabsConfig();
        return INSTANCE;
    }

    public static void load() {
        Path configFile = getConfigFile();
        if (Files.notExists(configFile)) {
            save();
            return;
        }
        try {
            INSTANCE = GSON.fromJson(Files.newBufferedReader(configFile), ChatTabsConfig.class);
            if (!INSTANCE.saveGenerated) {
                INSTANCE.chatTabs.removeIf(tab -> !tab.shouldSave());
            }
        } catch (IOException e) {
            ChatTabs.LOGGER.error("Failed to load config file!", e);
        }
    }

    public static void save() {
        try {
            Path configFile = getConfigFile();
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, GSON.toJson(getInstance()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            ChatTabs.LOGGER.error("Failed to save config file!", e);
        }
    }
}
