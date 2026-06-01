package net.wakcedon.chattabsreloaded.tabs;

import net.minecraft.client.multiplayer.chat.GuiMessage;

public class FabricChatLine implements ChatLine {
    private final GuiMessage.Line line;

    public FabricChatLine(GuiMessage.Line line) {
        this.line = line;
    }

    @Override
    public String getContent() {
        return line.content().getString();
    }

    @Override
    public boolean isSystem() {
        return line.content().getStyle().isItalic();
    }

    public GuiMessage.Line getLine() {
        return line;
    }
}