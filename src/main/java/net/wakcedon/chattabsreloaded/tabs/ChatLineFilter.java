package net.wakcedon.chattabsreloaded.tabs;

import com.google.gson.annotations.Expose;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.TextColor;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

public class ChatLineFilter {
    
    @Expose
    private boolean filterMessages;
    
    @Expose
    private String regex;
    
    @Expose
    private ColorFilter colorFilter;
    @Expose
    private int hexColor;
    
    private final Predicate<ChatLine> filter;
    
    public ChatLineFilter() {
        this(".*");
    }
    
    public ChatLineFilter(String filter, ColorFilter colorFilter, int hexColor, boolean filterMessages) {
        this.regex = filter;
        this.colorFilter = colorFilter;
        this.hexColor = hexColor;
        this.filter = line -> {
            try {
                boolean colorMatches = matchesLineColor(line);
                if(filterMessages) {
                    // Для платформо-специфичной логики фильтрации сообщений
                    // нужно использовать реализации в Fabric/Neo версиях
                    return false;
                }
                return line.getContent().matches(regex) && colorMatches;
            } catch(PatternSyntaxException e) {
                return true;
            }
        };
        this.filterMessages = filterMessages;
    }
    
    private boolean matchesLineColor(ChatLine line) {
        boolean colorMatches = true;
        // Для платформо-специфичной логики цвета
        // нужно использовать реализации в Fabric/Neo версиях
        return colorMatches;
    }
    
    public ChatLineFilter(String filter, boolean filterMessages) {
        this(filter, ColorFilter.DISABLED, 0xFFFFFF, filterMessages);
    }
    
    public ChatLineFilter(String filter) {
        this(filter, false);
    }
    
    public List<ChatMessageList> filterChat(List<ChatMessageList> chatLists) {
        return chatLists.stream().filter(filter).toList();
    }
    
    public boolean test(ChatLine message) {
        return filter.test(message);
    }
    
    public String getRegex() {
        return regex;
    }
    
    public ColorFilter getColorFilter() {
        return colorFilter;
    }
    
    public int getHexColor() {
        return this.hexColor;
    }
    
    public void setRegex(String regex) {
        this.regex = regex;
    }
    
    public void setColorFilter(ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }
    
    public void setHexColor(int hexColor) {
        this.hexColor = hexColor;
    }
    
    public boolean filtersMessages() {
        return filterMessages;
    }
    
    public void filterMessages(boolean filterMessages) {
        this.filterMessages = filterMessages;
    }
    
    public enum ColorFilter {
        DISABLED,
        HEX,
        BLACK(0x000000),
        DARK_BLUE(0x0000AA),
        DARK_GREEN(0x00AA00),
        DARK_AQUA(0x00AAAA),
        DARK_RED(0xAA0000),
        DARK_PURPLE(0xAA00AA),
        GOLD(0xFFAA00),
        GRAY(0xAAAAAA),
        DARK_GRAY(0x555555),
        BLUE(0x5555FF),
        GREEN(0x55FF55),
        AQUA(0x55FFFF),
        RED(0xFF5555),
        LIGHT_PURPLE(0xFF55FF),
        YELLOW(0xFFFF55),
        WHITE(0xFFFFFF);
        
        private final int color;
        
        ColorFilter(int color) {
            this.color = color;
        }
        
        ColorFilter() {
            this(-1);
        }
        
        public int getColor() {
            return color;
        }
    }
}