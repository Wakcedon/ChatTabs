package net.wakcedon.chattabsreloaded.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing and validating command arguments.
 * Replaces fragile substring-based parsing.
 */
public class CommandParser {
    
    private final List<String> args;
    private int position = 0;
    
    public CommandParser(String input) {
        // Parse shell-like arguments respecting quoted strings
        this.args = parseArgs(input);
    }
    
    /**
     * Parse arguments from a string, respecting quoted strings.
     */
    private static List<String> parseArgs(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result;
    }
    
    /**
     * Get the next argument without advancing position.
     */
    public String peek() {
        return position < args.size() ? args.get(position) : null;
    }
    
    /**
     * Get the next argument and advance position.
     */
    public String next() {
        return position < args.size() ? args.get(position++) : null;
    }
    
    /**
     * Get all remaining arguments as a single string.
     */
    public String rest() {
        if (position >= args.size()) return "";
        return String.join(" ", args.subList(position, args.size()));
    }
    
    /**
     * Get argument at specific index.
     */
    public String get(int index) {
        return index < args.size() ? args.get(index) : null;
    }
    
    /**
     * Check if there are more arguments.
     */
    public boolean hasNext() {
        return position < args.size();
    }
    
    /**
     * Get total number of arguments.
     */
    public int size() {
        return args.size();
    }
    
    /**
     * Get current position.
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Reset position to start.
     */
    public void reset() {
        position = 0;
    }
    
    /**
     * Require at least N arguments.
     * @return true if requirement is met
     */
    public boolean requireArguments(int minArgs) {
        return args.size() >= minArgs;
    }
    
    /**
     * Get arguments as a list.
     */
    public List<String> toList() {
        return new ArrayList<>(args);
    }
    
    /**
     * Get remaining arguments as a list.
     */
    public List<String> getRest() {
        if (position >= args.size()) return new ArrayList<>();
        return new ArrayList<>(args.subList(position, args.size()));
    }
    
    @Override
    public String toString() {
        return "CommandParser{" +
                "args=" + args +
                ", position=" + position +
                '}';
    }
}
