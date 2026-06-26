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
    
    // Unread tracking - stores how many messages existed when tab was last viewed
    private int messagesWhenLastFocused = 0;
    
    // Animation timers
    private long lastUnreadTime = 0;
    private long blinkDuration = 3000;
    private static final long FIRST_BLINK_MS = 3000;
    private static final long SUBSEQUENT_BLINK_MS = 1500;
    private long badgeAppearStart = 0;
    
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
        this("chattabs.tab.new", true);
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
        if (this.name != null && this.name.startsWith("chattabs.")) {
            return Component.translatable(this.name);
        }
        return Component.literal(this.name != null ? this.name : "");
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
        
        // Trim old messages
        while(visibleLines.size() > net.wakcedon.chattabsreloaded.ChatTabs.getMaxLines()) {
            visibleLines.removeLast();
        }
        
        // Update unread state - new message means we have unreads now
        if(!hadUnreads) {
            // Transitioning from no unreads to having unreads
            lastUnreadTime = System.currentTimeMillis();
            blinkDuration = FIRST_BLINK_MS;
            badgeAppearStart = System.currentTimeMillis();
        } else {
            // Already had unreads, update animation timer
            lastUnreadTime = System.currentTimeMillis();
            blinkDuration = SUBSEQUENT_BLINK_MS;
        }
    }
    
    /**
     * Called when this tab becomes focused/selected.
     * Marks all current messages as read.
     */
    public void setFocused(boolean focused) {
        if(focused) {
            // Tab is now focused - mark all current messages as read
            messagesWhenLastFocused = visibleLines.size();
            lastUnreadTime = 0;
            badgeAppearStart = 0;
        } else {
            // Tab is unfocused - keep current unread state for animation
            lastUnreadTime = 0;
            badgeAppearStart = 0;
        }
    }

    public float getBadgeAlpha() {
        if(!hasUnreads()) return 0;
        if(badgeAppearStart == 0) return 1.0f;
        long elapsed = System.currentTimeMillis() - badgeAppearStart;
        float alpha = Math.min(elapsed / 200.0f, 1.0f);
        if(alpha >= 1.0f) badgeAppearStart = 0;
        return alpha;
    }

    public boolean shouldBlink() {
        if(!hasUnreads()) return false;
        if(lastUnreadTime == 0) return false;
        return System.currentTimeMillis() - lastUnreadTime < blinkDuration;
    }
    
    public List<ChatLine> getVisibleChatLines() {
        return List.copyOf(visibleLines);
    }
    
    public String modifySend(String msg) {
        return sendModifier.apply(msg);
    }
    
    public void clear(boolean totalClear) {
        visibleLines.clear();
        messagesWhenLastFocused = 0;
    }
    
    /**
     * Returns the number of unread messages in this tab.
     * Only called when tab is NOT focused.
     */
    public int getUnreadCount() {
        if(visibleLines.isEmpty()) return 0;
        return Math.max(0, visibleLines.size() - messagesWhenLastFocused);
    }
    
    /**
     * Returns true if this tab has any unread messages.
     */
    public boolean hasUnreads() {
        return getUnreadCount() > 0;
    }
}
