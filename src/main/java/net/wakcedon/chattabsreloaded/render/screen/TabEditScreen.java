package net.wakcedon.chattabsreloaded.render.screen;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.tabs.ChatTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TabEditScreen extends Screen {

    private final Screen parent;
    private final ChatTab tab;

    private EditBox nameField;
    private EditBox filterField;
    private Button filterDMsBtn;
    private Button visibleBtn;
    private EditBox prefixField;
    private EditBox suffixField;
    private EditBox hexColorField;

    public TabEditScreen(Screen parent, ChatTab tab) {
        super(Component.translatable("chattabs.tabedit.title"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int fw = Math.min(font.width("W") * 22, width - 80);
        int left = cx - fw / 2;
        int right = cx + fw / 2;
        int halfW = (fw - 10) / 2;
        int y = 50;

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(4, 4, 50, 20).build());

        nameField = new EditBox(font, left, y, fw, 20, Component.translatable("chattabsconfig.chattab.name"));
        nameField.setValue(tab.getName());
        nameField.setMaxLength(64);
        addRenderableWidget(nameField);
        y += 30;

        filterField = new EditBox(font, left, y, fw, 20, Component.translatable("chattabsconfig.chattab.filter.regex"));
        filterField.setValue(tab.getFilter().getRegex());
        filterField.setMaxLength(256);
        addRenderableWidget(filterField);
        y += 28;

        filterDMsBtn = addRenderableWidget(Button.builder(
                Component.translatable("chattabsconfig.chattab.filter.messages").append(": ")
                        .append(Component.literal(tab.getFilter().filtersMessages() ? "§aON" : "§cOFF")),
                b -> {
                    tab.getFilter().filterMessages(!tab.getFilter().filtersMessages());
                    String val = tab.getFilter().filtersMessages() ? "§aON" : "§cOFF";
                    b.setMessage(Component.translatable("chattabsconfig.chattab.filter.messages").append(": ").append(Component.literal(val)));
                })
                .bounds(left, y, fw, 20).build());
        y += 24;

        visibleBtn = addRenderableWidget(Button.builder(
                Component.translatable("chattabsconfig.chattab.visiblebydefault").append(": ")
                        .append(Component.literal(tab.isVisibleByDefault() ? "§aON" : "§cOFF")),
                b -> {
                    tab.setVisibleByDefault(!tab.isVisibleByDefault());
                    String val = tab.isVisibleByDefault() ? "§aON" : "§cOFF";
                    b.setMessage(Component.translatable("chattabsconfig.chattab.visiblebydefault").append(": ").append(Component.literal(val)));
                })
                .bounds(left, y, fw, 20).build());
        y += 28;

        prefixField = new EditBox(font, left, y, halfW, 20, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"));
        prefixField.setValue(tab.getSendModifier().getPrefix());
        prefixField.setMaxLength(32);
        addRenderableWidget(prefixField);

        suffixField = new EditBox(font, left + halfW + 10, y, halfW, 20, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"));
        suffixField.setValue(tab.getSendModifier().getSuffix());
        suffixField.setMaxLength(32);
        addRenderableWidget(suffixField);
        y += 28;

        hexColorField = new EditBox(font, left, y, fw, 20, Component.translatable("chattabsconfig.chattab.filter.color.hex"));
        String hex = String.format("#%06X", tab.getFilter().getHexColor() & 0xFFFFFF);
        hexColorField.setValue(hex);
        hexColorField.setMaxLength(7);
        addRenderableWidget(hexColorField);
        y += 32;

        y = Math.max(y, height - 50);

        addRenderableWidget(Button.builder(Component.translatable("chattabsconfig.chattab.save"), b -> save())
                .bounds(right - 105, y, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(right - 210, y, 100, 20).build());
    }

    private void save() {
        tab.setName(nameField.getValue());
        tab.getFilter().setRegex(filterField.getValue());
        try {
            String hex = hexColorField.getValue().replace("#", "");
            tab.getFilter().setHexColor((int) Long.parseLong(hex, 16));
        } catch(Exception ignored) {}
        ChatTabsConfigBase.getInstance().save();
        onClose();
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);

        int cx = width / 2;
        int fw = Math.min(font.width("W") * 22, width - 80);
        int left = cx - fw / 2;
        int y = 20;

        ctx.drawString(font, Component.translatable("chattabs.tabedit.title"), cx - font.width(Component.translatable("chattabs.tabedit.title")) / 2, y, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
