package net.wakcedon.chattabsreloaded.tabs;

import com.google.gson.annotations.Expose;
import net.minecraft.network.chat.Component;
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
    private int messagesAtLastSeen = 0;
    private long firstUnreadTime = 0;
    private static final long BLINK_DURATION_MS = 4000;
    
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

    public Component getDisplayComponent() {
        return Component.translatable(this.name);
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

    public void setFilter(ChatLineFilter filter) {
        this.filter = filter;
    }
    
    public SendModifier getSendModifier() {
        return sendModifier;
    }
    
    public List<ChatMessageList> filterChat(List<ChatMessageList> chatLists) {
        return filter.filterChat(chatLists);
    }
    
    public void addChatLine(ChatLine line) {
        boolean hadUnreads = hasUnreads();
        visibleLines.addFirst(line);
        while(visibleLines.size() > net.wakcedon.chattabsreloaded.ChatTabs.getMaxLines()) {
            visibleLines.removeLast();
        }
        if(!hadUnreads && hasUnreads()) {
            firstUnreadTime = System.currentTimeMillis();
        }
    }
    
    public void setFocused(boolean focused) {
        if(focused) {
            firstMessageUnread = false;
        } else {
            messagesAtLastSeen = visibleLines.size();
            firstMessageUnread = false;
        }
        firstUnreadTime = 0;
    }

    public boolean shouldBlink() {
        if(!hasUnreads()) return false;
        if(firstUnreadTime == 0) return true;
        return System.currentTimeMillis() - firstUnreadTime < BLINK_DURATION_MS;
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
        messagesAtLastSeen = 0;
    }
    
    public int getLastSeenMessage() {
        if(firstMessageUnread) return Math.max(0, visibleLines.size() - 1);
        int unreadCount = Math.max(0, visibleLines.size() - messagesAtLastSeen);
        return Math.max(0, unreadCount - 1);
    }
    
    public boolean hasUnreads() {
        if(visibleLines.isEmpty()) return false;
        if(firstMessageUnread) return true;
        return visibleLines.size() > messagesAtLastSeen;
    }

    public int getUnreadCount() {
        if(visibleLines.isEmpty()) return 0;
        if(firstMessageUnread) return visibleLines.size();
        return Math.max(0, visibleLines.size() - messagesAtLastSeen);
    }
}
