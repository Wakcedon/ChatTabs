package net.wakcedon.chattabsreloaded.tabs;

// Для NeoForge нам нужно будет использовать соответствующие классы
// Это будет реализация для NeoForge платформы
public class NeoForgeChatLine implements ChatLine {
    private final Object line; // Будет содержать NeoForge-специфичный объект

    public NeoForgeChatLine(Object line) {
        this.line = line;
    }

    @Override
    public String getContent() {
        // Здесь будет логика для извлечения контента из NeoForge объекта
        return line.toString();
    }

    @Override
    public boolean isSystem() {
        // Здесь будет логика для определения системного сообщения в NeoForge
        return false;
    }

    public Object getLine() {
        return line;
    }
}