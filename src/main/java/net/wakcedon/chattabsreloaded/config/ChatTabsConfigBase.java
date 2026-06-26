package net.wakcedon.chattabsreloaded.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;

import java.awt.*;
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
    public boolean showUnreadCounter = true;

    @Expose
    public boolean tabDragAndDrop = true;

    @Expose
    public boolean tabAnimationFade = true;

    @Expose
    public boolean tabAppearAnimation = true;

    @Expose
    private boolean saveGenerated = false;

    public int selectedTab = 0;

    @Expose
    private List<ChatTab> chatTabs = new ArrayList<>();

    private static PlatformConfig platformConfig;

    public static void setPlatformConfig(PlatformConfig config) {
        if (config == null) {
            ChatTabs.LOGGER.warning("Attempted to set null platformConfig!");
            return;
        }
        platformConfig = config;
    }

    public static ChatTabsConfigBase getInstance() {
        if (platformConfig != null) {
            ChatTabsConfigBase config = platformConfig.getConfig();
            if (config != null) {
                return config;
            }
        }
        throw new IllegalStateException("ChatTabs config not properly initialized! PlatformConfig must be set before accessing config.");
    }

    public List<ChatTab> getVisibleChatTabs() {
        return chatTabs.stream().filter(ChatTab::isVisibleByDefault).toList();
    }

    public List<ChatTab> getChatTabs() {
        return chatTabs;
    }

    public ChatTab getSelectedChatTab() {
        List<ChatTab> chatTabs = getVisibleChatTabs();
        if (chatTabs.isEmpty()) return null;
        if (selectedTab < 0) selectedTab = 0;
        while (selectedTab >= chatTabs.size()) selectedTab--;
        return chatTabs.get(selectedTab);
    }

    public boolean shouldSaveGenerated() {
        return saveGenerated;
    }

    public void setSaveGenerated(boolean saveGenerated) {
        this.saveGenerated = saveGenerated;
    }

    public void addChatTabFirst(ChatTab newTab) {
        chatTabs.add(0, newTab);
    }

    /**
     * Save configuration to disk.
     * Must be implemented by platform-specific subclasses.
     */
    public void save() {
        throw new UnsupportedOperationException("save() must be implemented by platform-specific subclass");
    }

    /**
     * Load configuration from disk.
     * Must be implemented by platform-specific subclasses.
     */
    public void load() {
        throw new UnsupportedOperationException("load() must be implemented by platform-specific subclass");
    }

    /**
     * Reload server profiles from disk.
     * Must be implemented by platform-specific subclasses.
     */
    public void reloadProfiles() {
        throw new UnsupportedOperationException("reloadProfiles() must be implemented by platform-specific subclass");
    }

    /**
     * Get the profiles configuration.
     * Must be implemented by platform-specific subclasses.
     * @return ProfilesConfig instance, never null
     */
    public ProfilesConfig getProfilesConfig() {
        throw new UnsupportedOperationException("getProfilesConfig() must be implemented by platform-specific subclass");
    }
}