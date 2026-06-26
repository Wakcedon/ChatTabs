package net.wakcedon.chattabsreloaded.tabs;

import com.google.gson.annotations.Expose;
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
        this.regex = validateRegex(filter);
        this.colorFilter = colorFilter;
        this.hexColor = validateHexColor(hexColor);
        this.filter = line -> {
            try {
                boolean colorMatches = matchesLineColor(line);
                if(filterMessages) {
                    return line.getContent().toLowerCase(java.util.Locale.ROOT).contains(regex.toLowerCase(java.util.Locale.ROOT)) && colorMatches;
                }
                return line.getContent().matches(regex) && colorMatches;
            } catch(PatternSyntaxException e) {
                return true;
            }
        };
        this.filterMessages = filterMessages;
    }
    
    /**
     * Validates regex pattern. Falls back to ".*" if invalid.
     */
    private static String validateRegex(String regex) {
        if(regex == null || regex.isEmpty()) {
            return ".*";
        }
        try {
            java.util.regex.Pattern.compile(regex);
            return regex;
        } catch(PatternSyntaxException e) {
            // Log warning and return permissive pattern
            System.err.println("[ChatTabs] Invalid regex pattern '" + regex + "': " + e.getMessage());
            return ".*";
        }
    }
    
    /**
     * Validates HEX color value. Ensures it's in valid RGB range (0x000000 to 0xFFFFFF).
     */
    private static int validateHexColor(int hexColor) {
        if((hexColor & 0xFF000000) == 0) {
            // Valid - no alpha channel, just RGB
            return hexColor & 0xFFFFFF;
        }
        // Strip alpha channel if present
        return hexColor & 0xFFFFFF;
    }
    
    private boolean matchesLineColor(ChatLine line) {
        if(colorFilter == ColorFilter.DISABLED) return true;
        int lineColor = extractFirstColor(line.getContent());
        if(lineColor == -1) return colorFilter == ColorFilter.DISABLED;
        return switch(colorFilter) {
            case HEX -> lineColor == hexColor;
            case DISABLED -> true;
            default -> lineColor == colorFilter.getColor();
        };
    }
    
    private int extractFirstColor(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\u00A7([0-9a-fklmnor])").matcher(text);
        if(!matcher.find()) return -1;
        String code = matcher.group(1);
        return switch(code) {
            case "0" -> 0x000000;
            case "1" -> 0x0000AA;
            case "2" -> 0x00AA00;
            case "3" -> 0x00AAAA;
            case "4" -> 0xAA0000;
            case "5" -> 0xAA00AA;
            case "6" -> 0xFFAA00;
            case "7" -> 0xAAAAAA;
            case "8" -> 0x555555;
            case "9" -> 0x5555FF;
            case "a" -> 0x55FF55;
            case "b" -> 0x55FFFF;
            case "c" -> 0xFF5555;
            case "d" -> 0xFF55FF;
            case "e" -> 0xFFFF55;
            case "f" -> 0xFFFFFF;
            default -> -1;
        };
    }
    
    public ChatLineFilter(String filter, boolean filterMessages) {
        this(filter, ColorFilter.DISABLED, 0xFFFFFF, filterMessages);
    }
    
    public ChatLineFilter(String filter) {
        this(filter, false);
    }
    
    public List<ChatMessageList> filterChat(List<ChatMessageList> chatLists) {
        return chatLists.stream().filter(list -> list.getLines().stream().anyMatch(filter)).toList();
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