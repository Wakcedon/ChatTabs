package net.wakcedon.chattabsreloaded.tabs;

import com.google.gson.annotations.Expose;
import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ChatTab {
    
    @Expose
    private String id;
    
    @Expose
    private String name;
    @Expose
    private boolean save;
    @Expose
    private boolean visibleByDefault;
    @Expose
    private ChatLineFilter filter;
    @Expose
    private SendModifier sendModifier;
    
    private final Deque<ChatLine> visibleLines = new LinkedList<>();
    
    private boolean firstMessageUnread = true;
    private ChatLine lastSeenLine;
    
    public ChatTab(String id, String name, boolean save, boolean visibleByDefault, ChatLineFilter filter, SendModifier sendModifier) {
        this.id = id;
        this.name = name;
        this.save = save;
        this.visibleByDefault = visibleByDefault;
        this.filter = filter;
        this.sendModifier = sendModifier;
    }
    
    public ChatTab(String id, String name, boolean save, ChatLineFilter filter, SendModifier sendModifier) {
        this(id, name, save, true, filter, sendModifier);
    }
    
    public ChatTab(String name, boolean save, ChatLineFilter filter, SendModifier sendModifier) {
        this("new_tab", name, save, filter, sendModifier);
    }
    
    public ChatTab(String name, boolean save) {
        this(name, save, new ChatLineFilter(), new SendModifier());
    }
    
    public ChatTab() {
        this("New Tab", true);
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setSave(boolean save) {
        this.save = save;
    }
    
    public void setVisibleByDefault(boolean visibleByDefault) {
        this.visibleByDefault = visibleByDefault;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean shouldSave() {
        return save;
    }
    
    public boolean isVisibleByDefault() {
        return visibleByDefault;
    }
    
    public ChatLineFilter getFilter() {
        return filter;
    }
    
    public SendModifier getSendModifier() {
        return sendModifier;
    }
    
    public List<ChatMessageList> filterChat(List<ChatMessageList> chatLists) {
        return filter.filterChat(chatLists);
    }
    
    public void addChatLine(ChatLine line) {
        visibleLines.addFirst(line);
        while(visibleLines.size() > net.wakcedon.chattabsreloaded.ChatTabs.getMaxLines()) {
            visibleLines.removeLast();
        }
    }
    
    public void setFocused(boolean focused) {
        if(focused) {
            lastSeenLine = null;
            firstMessageUnread = false;
        } else if(!visibleLines.isEmpty()) {
            lastSeenLine = visibleLines.getFirst();
            firstMessageUnread = false;
        } else {
            firstMessageUnread = true;
        }
    }
    
    public List<ChatLine> getVisibleChatLines() {
        return List.copyOf(visibleLines);
    }
    
    public String modifySend(String msg) {
        return sendModifier.apply(msg);
    }
    
    public void clear(boolean totalClear) {
        visibleLines.clear();
        firstMessageUnread = true;
    }
    
    public int getLastSeenMessage() {
        if(firstMessageUnread) return visibleLines.size() - 1;
        if(lastSeenLine == null) return 0;
        int index = 0;
        for(ChatLine line : visibleLines) {
            if(line.equals(lastSeenLine)) {
                return index;
            }
            index++;
        }
        return 0;
    }
    
    public boolean hasUnreads() {
        if(lastSeenLine != null) {
            int seenIndex = 0;
            for(ChatLine line : visibleLines) {
                if(line.equals(lastSeenLine)) {
                    return seenIndex > 0;
                }
                seenIndex++;
            }
        }
        return !visibleLines.isEmpty() && firstMessageUnread;
    }
}