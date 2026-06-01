package net.wakcedon.chattabsreloaded.tabs;

import net.minecraft.client.multiplayer.chat.GuiMessage;

public class FabricChatLine implements ChatLine {
    private final GuiMessage.Line line;

    public FabricChatLine(GuiMessage.Line line) {
        this.line = line;
    }

    @Override
    public String getContent() {
        return line.content().toString();
    }

    @Override
    public boolean isSystem() {
        return false; // В Fabric нет прямого способа определить системное сообщение
    }

    public GuiMessage.Line getLine() {
        return line;
    }
}