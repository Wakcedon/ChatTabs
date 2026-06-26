package net.wakcedon.chattabsreloaded.events;

import net.wakcedon.chattabsreloaded.ChatTabs;
import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.wakcedon.chattabsreloaded.config.ProfilesConfig;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Detects when the player changes servers and notifies about profile changes.
 */
public class ServerProfileChangeDetector {
    
    private static String lastServerIp = null;
    private static ServerTabProfile lastProfile = null;
    private static boolean initialized = false;
    
    /**
     * Call this on each client tick to check for server changes.
     */
    public static void tick() {
        if (!initialized) {
            initialized = true;
            lastServerIp = getCurrentServerIp();
            loadCurrentProfile();
            return;
        }
        
        String currentIp = getCurrentServerIp();
        
        // Check if server changed
        if (!ipEquals(lastServerIp, currentIp)) {
            lastServerIp = currentIp;
            onServerChanged(currentIp);
        }
    }
    
    /**
     * Called when the server changes.
     */
    private static void onServerChanged(String newServerIp) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if (!config.enabled) return;
        
        ServerTabProfile newProfile = loadCurrentProfile();
        
        if (newProfile != lastProfile) {
            // Profile changed
            String profileName = newProfile != null ? newProfile.getName() : "Default";
            String message = newServerIp == null 
                ? "Отключились от сервера" 
                : "Подключились к " + newServerIp + " - профиль: " + profileName;
            
            showNotification(message);
            
            ChatTabs.LOGGER.info("Server changed from " + lastServerIp + " to " + newServerIp + 
                               ", profile: " + profileName);
        }
        
        lastProfile = newProfile;
    }
    
    /**
     * Load the current profile based on current server IP.
     */
    private static ServerTabProfile loadCurrentProfile() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        if (!(config instanceof NeoForgeChatTabsConfig)) {
            return null;
        }
        
        NeoForgeChatTabsConfig ncfg = (NeoForgeChatTabsConfig) config;
        ncfg.reloadProfiles();
        ProfilesConfig pc = ncfg.getProfilesConfig();
        String ip = getCurrentServerIp();
        
        if (ip == null) {
            return pc.getDefaultProfile();
        }
        
        return pc.findProfile(ip);
    }
    
    /**
     * Get the current server IP or null if offline.
     */
    private static String getCurrentServerIp() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getCurrentServer() != null) {
                return mc.getCurrentServer().ip;
            }
        } catch (Exception e) {
            ChatTabs.LOGGER.warning("Error getting server IP: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Compare two server IPs safely.
     */
    private static boolean ipEquals(String ip1, String ip2) {
        if (ip1 == null && ip2 == null) return true;
        if (ip1 == null || ip2 == null) return false;
        return ip1.equals(ip2);
    }
    
    /**
     * Show notification to player.
     */
    private static void showNotification(String message) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("§6[ChatTabs] §f").append(Component.literal(message)),
                    false
                );
            }
        } catch (Exception e) {
            ChatTabs.LOGGER.warning("Error showing notification: " + e.getMessage());
        }
    }
    
    /**
     * Reset detector state (call when mod is reloaded).
     */
    public static void reset() {
        lastServerIp = null;
        lastProfile = null;
        initialized = false;
    }
}
