package net.wakcedon.chattabsreloaded.tabs;

import net.minecraft.client.multiplayer.chat.GuiMessage;

import java.util.ArrayList;
import java.util.List;

public class FabricChatMessageList implements ChatMessageList {
    private final List<ChatLine> lines = new ArrayList<>();

    public FabricChatMessageList(List<GuiMessage.Line> guiLines) {
        for (GuiMessage.Line line : guiLines) {
            lines.add(new FabricChatLine(line));
        }
    }

    @Override
    public List<ChatLine> getLines() {
        return lines;
    }

    @Override
    public void addLine(ChatLine line) {
        lines.add(line);
    }

    @Override
    public void clear() {
        lines.clear();
    }

    @Override
    public int size() {
        return lines.size();
    }
}