package net.wakcedon.chattabsreloaded.commands;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.wakcedon.chattabsreloaded.config.ProfilesConfig;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.wakcedon.chattabsreloaded.tabs.ChatLineFilter;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.wakcedon.chattabsreloaded.tabs.SendModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ChatTabsCommands {

    public static boolean handleCommand(String input) {
        if(input.startsWith("/chattabs")) {
            String args = input.length() > 9 ? input.substring(10).trim() : "";
            return execute(args);
        }
        if(input.startsWith("/ct")) {
            String args = input.length() > 3 ? input.substring(4).trim() : "";
            return execute(args);
        }
        return false;
    }

    private static boolean execute(String args) {
        if(args.isEmpty() || args.equals("help")) {
            sendHelp();
            return true;
        }

        String[] parts = args.split(" ");
        String cmd = parts[0].toLowerCase();

        switch(cmd) {
            case "reload":
                return handleReload();
            case "save":
                return handleSave();
            case "list":
                return handleList();
            case "select":
                return parts.length > 1 ? handleSelect(args.substring(7).trim()) : error("Usage: /chattabs select <name>");
            case "tab":
                return handleTab(args.substring(4).trim());
            case "toggle":
                return parts.length > 1 ? handleToggle(args.substring(7).trim()) : error("Usage: /chattabs toggle <feature>");
            case "filter":
                return parts.length > 2 ? handleFilter(parts[1], args.substring(parts[0].length() + parts[1].length() + 2).trim()) : error("Usage: /chattabs filter <tabName> <regex>");
            case "profile":
                return handleProfile(args.substring(8).trim());
            default:
                return error("Unknown command. Use /chattabs help");
        }
    }

    private static boolean error(String msg) {
        message("§c" + msg);
        return true;
    }

    private static void message(String msg) {
        Minecraft client = Minecraft.getInstance();
        if(client.player != null) {
            client.player.displayClientMessage(Component.literal("§b[ChatTabs]§r " + msg), false);
        }
    }

    private static void sendHelp() {
        message("§eAvailable commands:");
        message(" §7/chattabs help §8- Show this help");
        message(" §7/chattabs reload §8- Reload config from file");
        message(" §7/chattabs save §8- Save config to file");
        message(" §7/chattabs list §8- List all tabs");
        message(" §7/chattabs select <name> §8- Select a tab");
        message(" §7/chattabs tab add <name> §8- Create a new tab");
        message(" §7/chattabs tab remove <name> §8- Remove a tab");
        message(" §7/chattabs toggle <feature> §8- Toggle a feature");
        message(" §7/chattabs filter <tab> <regex> §8- Set tab filter regex");
        message(" §7/chattabs profile list §8- List all server profiles");
        message(" §7/chattabs profile current §8- Show current profile");
        message(" §7/ct ... §8- Alias for /chattabs commands");
    }

    private static boolean handleReload() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        config.load();
        config.reloadProfiles();
        message("§aConfig and profiles reloaded.");
        return true;
    }

    private static boolean handleSave() {
        ChatTabsConfigBase.getInstance().save();
        message("§aConfig saved.");
        return true;
    }

    private static boolean handleList() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        List<ChatTab> tabs = config.getChatTabs();
        if(tabs.isEmpty()) {
            message("§eNo tabs configured.");
            return true;
        }
        message("§eTabs (" + tabs.size() + "):");
        for(int i = 0; i < tabs.size(); i++) {
            ChatTab tab = tabs.get(i);
            String sel = config.selectedTab == i ? " §a◄" : "";
            message(" §7" + i + ". §f" + tab.getDisplayComponent().getString() + sel);
        }
        return true;
    }

    private static boolean handleSelect(String name) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        List<ChatTab> tabs = config.getChatTabs();
        String lower = name.toLowerCase();
        for(int i = 0; i < tabs.size(); i++) {
            if(tabs.get(i).getDisplayComponent().getString().toLowerCase().equals(lower)
                || tabs.get(i).getId().toLowerCase().equals(lower)) {
                config.selectedTab = i;
                message("§aSelected tab: §f" + tabs.get(i).getDisplayComponent().getString());
                return true;
            }
        }
        return error("Tab not found: " + name);
    }

    private static boolean handleTab(String args) {
        String[] parts = args.split(" ", 2);
        if(parts.length < 2) return error("Usage: /chattabs tab add|remove <name>");
        String sub = parts[0].toLowerCase();
        String name = parts[1];

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();

        switch(sub) {
            case "add": {
                ChatTab tab = new ChatTab(name, true);
                config.getChatTabs().add(tab);
                config.save();
                message("§aTab created: §f" + name);
                return true;
            }
            case "remove": {
                List<ChatTab> tabs = config.getChatTabs();
                String lower = name.toLowerCase();
                for(int i = 0; i < tabs.size(); i++) {
                    ChatTab t = tabs.get(i);
                    if(t.getDisplayComponent().getString().toLowerCase().equals(lower)
                        || t.getId().toLowerCase().equals(lower)
                        || t.getName().toLowerCase().equals(lower)) {
                        config.getChatTabs().remove(i);
                        if(config.selectedTab >= i && config.selectedTab > 0) config.selectedTab--;
                        config.save();
                        message("§aTab removed: §f" + t.getDisplayComponent().getString());
                        return true;
                    }
                }
                return error("Tab not found: " + name);
            }
            default:
                return error("Usage: /chattabs tab add|remove <name>");
        }
    }

    private static boolean handleToggle(String feature) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        switch(feature.toLowerCase()) {
            case "unreadcounter":
            case "unread":
                config.showUnreadCounter = !config.showUnreadCounter;
                message("§aUnread counter: " + (config.showUnreadCounter ? "§aon" : "§coff"));
                break;
            case "dragdrop":
            case "drag":
                config.tabDragAndDrop = !config.tabDragAndDrop;
                message("§aDrag & drop: " + (config.tabDragAndDrop ? "§aon" : "§coff"));
                break;
            case "appear":
            case "appearanimation":
            case "tabappearanimation":
                config.tabAppearAnimation = !config.tabAppearAnimation;
                message("§aTab appear animation: " + (config.tabAppearAnimation ? "§aon" : "§coff"));
                break;
            case "animation":
            case "fade":
            case "tabanimationfade":
                config.tabAnimationFade = !config.tabAnimationFade;
                message("§aTab fade animation: " + (config.tabAnimationFade ? "§aon" : "§coff"));
                break;
            default:
                return error("Unknown feature. Available: unreadcounter, dragdrop, appearanimation, animation");
        }
        config.save();
        return true;
    }

    private static boolean handleProfile(String args) {
        if(args.isEmpty() || args.equals("list")) {
            ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
            config.reloadProfiles();
            message("§eAvailable profiles:");
            if(config instanceof NeoForgeChatTabsConfig ncfg) {
                ProfilesConfig pc = ncfg.getProfilesConfig();
                List<ServerTabProfile> profiles = pc.getProfiles();
                if(profiles.isEmpty()) {
                    message(" §7No custom profiles defined.");
                } else {
                    for(ServerTabProfile p : profiles) {
                        int tc = p.getTabs() != null ? p.getTabs().size() : 0;
                        message(" §7" + p.getServerIp() + " §8(" + tc + " tabs)");
                    }
                }
                ServerTabProfile def = pc.getDefaultProfile();
                if(def != null && def.getTabs() != null) {
                    message(" §7default §8(" + def.getTabs().size() + " tabs)");
                }
            } else {
                message(" §7No profile config loaded.");
            }
            return true;
        }
        if(args.equals("current")) {
            String ip = Minecraft.getInstance().getCurrentServer() != null
                ? Minecraft.getInstance().getCurrentServer().ip : null;
            if(ip == null) {
                message("§eNot connected to any server.");
                return true;
            }
            message("§eCurrent server: §f" + ip);
            ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
            if(config instanceof NeoForgeChatTabsConfig ncfg) {
                ServerTabProfile profile = ncfg.getProfilesConfig().findProfile(ip);
                if(profile != null) {
                    message(" §7Profile: §f" + (profile.getName() != null ? profile.getName() : profile.getServerIp()));
                    int tabCount = profile.getTabs() != null ? profile.getTabs().size() : 0;
                    message(" §7Tabs: §f" + tabCount);
                } else {
                    message(" §7No matching profile (using default tabs).");
                }
            }
            return true;
        }
        return error("Usage: /chattabs profile list|current");
    }

    private static boolean handleFilter(String tabName, String regex) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        List<ChatTab> tabs = config.getChatTabs();
        String lower = tabName.toLowerCase();
        for(ChatTab tab : tabs) {
            if(tab.getDisplayComponent().getString().toLowerCase().equals(lower)
                || tab.getId().toLowerCase().equals(lower)) {
                tab.setFilter(new ChatLineFilter(regex, false));
                config.save();
                message("§aFilter set for §f" + tab.getDisplayComponent().getString() + "§a: §7" + regex);
                return true;
            }
        }
        return error("Tab not found: " + tabName);
    }
}
