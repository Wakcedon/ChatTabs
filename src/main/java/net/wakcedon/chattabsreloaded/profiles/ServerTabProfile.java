package net.wakcedon.chattabsreloaded.profiles;

import net.wakcedon.chattabsreloaded.tabs.ChatTab;

import java.util.List;
import java.util.Map;

public class ServerTabProfile {

    private String serverIp;
    private String name;
    private List<ChatTab> tabs;
    private Map<String, String> overrides;

    public ServerTabProfile() {}

    public ServerTabProfile(String serverIp, String name, List<ChatTab> tabs) {
        this.serverIp = serverIp;
        this.name = name;
        this.tabs = tabs;
    }

    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ChatTab> getTabs() { return tabs; }
    public void setTabs(List<ChatTab> tabs) { this.tabs = tabs; }

    public Map<String, String> getOverrides() { return overrides; }
    public void setOverrides(Map<String, String> overrides) { this.overrides = overrides; }
}
