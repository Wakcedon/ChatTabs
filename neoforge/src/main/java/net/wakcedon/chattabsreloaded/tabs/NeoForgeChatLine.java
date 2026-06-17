package net.wakcedon.chattabsreloaded.tabs;

import net.minecraft.network.chat.Component;

public class NeoForgeChatLine implements ChatLine {
    private final Component content;
    private final boolean system;

    public NeoForgeChatLine(Object line) {
        Component comp = extractComponent(line);
        this.content = comp != null ? comp : Component.literal(line.toString());
        this.system = comp == null;
    }

    public NeoForgeChatLine(Component content, boolean system) {
        this.content = content;
        this.system = system;
    }

    @Override
    public String getContent() {
        return content.getString();
    }

    @Override
    public boolean isSystem() {
        return system;
    }

    public Component getComponent() {
        return content;
    }

    private static Component extractComponent(Object line) {
        try {
            Object result = line.getClass().getMethod("content").invoke(line);
            if(result instanceof Component c) return c;
        } catch(Exception ignored) {}
        return null;
    }
}
