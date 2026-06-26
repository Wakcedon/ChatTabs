package net.wakcedon.chattabsreloaded.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Manages logging for ChatTabs Reloaded mod.
 * Writes to a separate log file and game logger.
 */
public class LogManager {
    
    private static final String LOG_NAME = "ChatTabs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static Logger logger;
    private static FileHandler fileHandler;
    private static Path logsDirectory;
    
    public enum LogLevel {
        DEBUG(Level.FINE),
        INFO(Level.INFO),
        WARNING(Level.WARNING),
        ERROR(Level.SEVERE);
        
        public final Level javaLevel;
        
        LogLevel(Level javaLevel) {
            this.javaLevel = javaLevel;
        }
    }
    
    /**
     * Initialize the logging system.
     * @param configDirectory The .minecraft/config directory path
     */
    public static void initialize(Path configDirectory) {
        try {
            logsDirectory = configDirectory.resolve("chattabs_logs");
            Files.createDirectories(logsDirectory);
            
            logger = Logger.getLogger(LOG_NAME);
            logger.setLevel(Level.FINE); // Accept all levels, filters will handle it
            
            // Create file handler with rotation
            String logFileName = logsDirectory.resolve("chattabs.log").toString();
            fileHandler = new FileHandler(logFileName, 10 * 1024 * 1024, 10, true); // 10MB, 10 files
            fileHandler.setLevel(Level.FINE);
            
            // Create formatter
            Formatter formatter = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format(
                        "[%s] [%s] %s%n",
                        LocalDateTime.now().format(DATE_FORMAT),
                        record.getLevel().getName(),
                        record.getMessage()
                    );
                }
            };
            fileHandler.setFormatter(formatter);
            
            logger.addHandler(fileHandler);
            
            log(LogLevel.INFO, "ChatTabs logging initialized. Log directory: " + logsDirectory);
            
        } catch (IOException e) {
            System.err.println("Failed to initialize ChatTabs logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Log a message with specified level.
     */
    public static void log(LogLevel level, String message) {
        if (logger != null) {
            logger.log(level.javaLevel, message);
        }
    }
    
    /**
     * Log a message with exception.
     */
    public static void log(LogLevel level, String message, Throwable thrown) {
        if (logger != null) {
            logger.log(level.javaLevel, message, thrown);
        }
    }
    
    /**
     * Log debug message (only if verbose logging enabled).
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    
    /**
     * Log info message.
     */
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * Log warning message.
     */
    public static void warn(String message) {
        log(LogLevel.WARNING, message);
    }
    
    /**
     * Log warning with exception.
     */
    public static void warn(String message, Throwable thrown) {
        log(LogLevel.WARNING, message, thrown);
    }
    
    /**
     * Log error message.
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * Log error with exception.
     */
    public static void error(String message, Throwable thrown) {
        log(LogLevel.ERROR, message, thrown);
    }
    
    /**
     * Cleanup logging resources.
     */
    public static void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
        }
        if (logger != null) {
            for (Handler handler : logger.getHandlers()) {
                handler.close();
            }
        }
    }
    
    /**
     * Get logs directory path.
     */
    public static Path getLogsDirectory() {
        return logsDirectory;
    }
}
