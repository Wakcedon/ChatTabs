package net.wakcedon.chattabsreloaded.tabs;

import java.util.List;

public interface ChatMessageList {
    List<ChatLine> getLines();
    void addLine(ChatLine line);
    void clear();
    int size();
}