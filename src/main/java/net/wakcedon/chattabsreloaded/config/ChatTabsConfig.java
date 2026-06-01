package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
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
}
