package net.wakcedon.chattabsreloaded.commands;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.config.NeoForgeChatTabsConfig;
import net.wakcedon.chattabsreloaded.config.ProfilesConfig;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.wakcedon.chattabsreloaded.render.screen.ProfileListScreen;
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
                return parts.length > 1 ? handleSelect(args.substring(7).trim()) : error("chattabs.cmd.usage.select");
            case "tab":
                return handleTab(args.substring(4).trim());
            case "toggle":
                return parts.length > 1 ? handleToggle(args.substring(7).trim()) : error("chattabs.cmd.usage.toggle");
            case "filter":
                return parts.length > 2 ? handleFilter(parts[1], args.substring(parts[0].length() + parts[1].length() + 2).trim()) : error("chattabs.cmd.usage.filter");
            case "profile":
                return handleProfile(args.substring(8).trim());
            default:
                return error("chattabs.cmd.unknown");
        }
    }

    private static boolean error(String key, Object... args) {
        Minecraft client = Minecraft.getInstance();
        if(client.player != null) {
            client.player.displayClientMessage(
                Component.translatable("chattabs.cmd.prefix")
                    .append(Component.literal("§c"))
                    .append(Component.translatable(key, args)),
                false);
        }
        return true;
    }

    private static void message(String key, Object... args) {
        Minecraft client = Minecraft.getInstance();
        if(client.player != null) {
            client.player.displayClientMessage(
                Component.translatable("chattabs.cmd.prefix")
                    .append(Component.translatable(key, args)),
                false);
        }
    }

    private static void sendHelp() {
        message("chattabs.cmd.help.header");
        message("chattabs.cmd.help.entry", "/chattabs help", Component.translatable("chattabs.cmd.help.desc.help"));
        message("chattabs.cmd.help.entry", "/chattabs reload", Component.translatable("chattabs.cmd.help.desc.reload"));
        message("chattabs.cmd.help.entry", "/chattabs save", Component.translatable("chattabs.cmd.help.desc.save"));
        message("chattabs.cmd.help.entry", "/chattabs list", Component.translatable("chattabs.cmd.help.desc.list"));
        message("chattabs.cmd.help.entry", "/chattabs select <name>", Component.translatable("chattabs.cmd.help.desc.select"));
        message("chattabs.cmd.help.entry", "/chattabs tab add <name>", Component.translatable("chattabs.cmd.help.desc.tab.add"));
        message("chattabs.cmd.help.entry", "/chattabs tab remove <name>", Component.translatable("chattabs.cmd.help.desc.tab.remove"));
        message("chattabs.cmd.help.entry", "/chattabs toggle <feature>", Component.translatable("chattabs.cmd.help.desc.toggle"));
        message("chattabs.cmd.help.entry", "/chattabs filter <tab> <regex>", Component.translatable("chattabs.cmd.help.desc.filter"));
        message("chattabs.cmd.help.entry", "/chattabs profile list", Component.translatable("chattabs.cmd.help.desc.profile.list"));
        message("chattabs.cmd.help.entry", "/chattabs profile current", Component.translatable("chattabs.cmd.help.desc.profile.current"));
        message("chattabs.cmd.help.entry", "/chattabs profile gui", Component.translatable("chattabs.cmd.help.desc.profile.gui"));
        message("chattabs.cmd.help.entry", "/ct ...", Component.translatable("chattabs.cmd.help.desc.alias"));
    }

    private static boolean handleReload() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        config.load();
        config.reloadProfiles();
        message("chattabs.cmd.reload");
        return true;
    }

    private static boolean handleSave() {
        ChatTabsConfigBase.getInstance().save();
        message("chattabs.cmd.saved");
        return true;
    }

    private static boolean handleList() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        List<ChatTab> tabs = config.getChatTabs();
        if(tabs.isEmpty()) {
            message("chattabs.cmd.list.empty");
            return true;
        }
        message("chattabs.cmd.list.header", tabs.size());
        for(int i = 0; i < tabs.size(); i++) {
            ChatTab tab = tabs.get(i);
            String sel = config.selectedTab == i ? " §a◄" : "";
            message("chattabs.cmd.list.entry", i, tab.getDisplayComponent().getString(), sel);
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
                message("chattabs.cmd.select", tabs.get(i).getDisplayComponent().getString());
                return true;
            }
        }
        return error("chattabs.cmd.notfound", name);
    }

    private static boolean handleTab(String args) {
        String[] parts = args.split(" ", 2);
        if(parts.length < 2) return error("chattabs.cmd.usage.tab");
        String sub = parts[0].toLowerCase();
        String name = parts[1];

        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();

        switch(sub) {
            case "add": {
                ChatTab tab = new ChatTab(name, true);
                config.getChatTabs().add(tab);
                config.save();
                message("chattabs.cmd.tab.created", name);
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
                        message("chattabs.cmd.tab.removed", t.getDisplayComponent().getString());
                        return true;
                    }
                }
                return error("chattabs.cmd.notfound", name);
            }
            default:
                return error("chattabs.cmd.usage.tab");
        }
    }

    private static boolean handleToggle(String feature) {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        Component on = Component.translatable("chattabs.cmd.value.on");
        Component off = Component.translatable("chattabs.cmd.value.off");
        switch(feature.toLowerCase()) {
            case "unreadcounter":
            case "unread":
                config.showUnreadCounter = !config.showUnreadCounter;
                message("chattabs.cmd.toggle.unread", config.showUnreadCounter ? on : off);
                break;
            case "dragdrop":
            case "drag":
                config.tabDragAndDrop = !config.tabDragAndDrop;
                message("chattabs.cmd.toggle.dragdrop", config.tabDragAndDrop ? on : off);
                break;
            case "appear":
            case "appearanimation":
            case "tabappearanimation":
                config.tabAppearAnimation = !config.tabAppearAnimation;
                message("chattabs.cmd.toggle.appear", config.tabAppearAnimation ? on : off);
                break;
            case "animation":
            case "fade":
            case "tabanimationfade":
                config.tabAnimationFade = !config.tabAnimationFade;
                message("chattabs.cmd.toggle.fade", config.tabAnimationFade ? on : off);
                break;
            default:
                return error("chattabs.cmd.toggle.unknown");
        }
        config.save();
        return true;
    }

    private static boolean handleProfile(String args) {
        if(args.equals("gui")) {
            Minecraft.getInstance().setScreen(new ProfileListScreen(null));
            return true;
        }
        if(args.isEmpty() || args.equals("list")) {
            ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
            config.reloadProfiles();
            message("chattabs.cmd.profile.list.header");
            if(config instanceof NeoForgeChatTabsConfig ncfg) {
                ProfilesConfig pc = ncfg.getProfilesConfig();
                List<ServerTabProfile> profiles = pc.getProfiles();
                if(profiles.isEmpty()) {
                    message("chattabs.cmd.profile.list.empty");
                } else {
                    for(ServerTabProfile p : profiles) {
                        int tc = p.getTabs() != null ? p.getTabs().size() : 0;
                        message("chattabs.cmd.profile.list.entry", p.getServerIp(), tc);
                    }
                }
                ServerTabProfile def = pc.getDefaultProfile();
                if(def != null && def.getTabs() != null) {
                    message("chattabs.cmd.profile.list.default", def.getTabs().size());
                }
            } else {
                message("chattabs.cmd.profile.list.empty");
            }
            return true;
        }
        if(args.equals("current")) {
            String ip = Minecraft.getInstance().getCurrentServer() != null
                ? Minecraft.getInstance().getCurrentServer().ip : null;
            if(ip == null) {
                message("chattabs.cmd.profile.current.offline");
                return true;
            }
            message("chattabs.cmd.profile.current.server", ip);
            ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
            if(config instanceof NeoForgeChatTabsConfig ncfg) {
                ServerTabProfile profile = ncfg.getProfilesConfig().findProfile(ip);
                if(profile != null) {
                    message("chattabs.cmd.profile.current.found", profile.getName() != null ? profile.getName() : profile.getServerIp());
                    int tabCount = profile.getTabs() != null ? profile.getTabs().size() : 0;
                    message("chattabs.cmd.profile.current.tabs", tabCount);
                } else {
                    message("chattabs.cmd.profile.current.none");
                }
            }
            return true;
        }
        return error("chattabs.cmd.usage.profile");
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
                message("chattabs.cmd.filter.set", tab.getDisplayComponent().getString(), regex);
                return true;
            }
        }
        return error("chattabs.cmd.notfound", tabName);
    }
}
