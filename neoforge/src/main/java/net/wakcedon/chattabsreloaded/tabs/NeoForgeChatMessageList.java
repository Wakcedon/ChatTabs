package net.wakcedon.chattabsreloaded.tabs;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeChatMessageList implements ChatMessageList {
    private final List<ChatLine> lines = new ArrayList<>();

    public NeoForgeChatMessageList(List<Object> neoForgeLines) {
        for (Object line : neoForgeLines) {
            lines.add(new NeoForgeChatLine(line));
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