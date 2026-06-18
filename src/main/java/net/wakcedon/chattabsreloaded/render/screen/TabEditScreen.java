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
        int fw = Math.min(300, width - 60);
        int left = cx - fw / 2;
        int halfW = (fw - 12) / 2;
        int rowH = 22;
        int gap = 10;
        int y = 50;

        nameField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.name"));
        nameField.setValue(tab.getName());
        nameField.setMaxLength(64);
        addRenderableWidget(nameField);
        y += rowH + gap + 14;

        filterField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.filter.regex"));
        filterField.setValue(tab.getFilter().getRegex());
        filterField.setMaxLength(256);
        addRenderableWidget(filterField);
        y += rowH + gap + 14;

        filterDMsBtn = addRenderableWidget(Button.builder(
                makeToggleLabel("chattabsconfig.chattab.filter.messages", tab.getFilter().filtersMessages()),
                b -> {
                    tab.getFilter().filterMessages(!tab.getFilter().filtersMessages());
                    b.setMessage(makeToggleLabel("chattabsconfig.chattab.filter.messages", tab.getFilter().filtersMessages()));
                })
                .bounds(left, y, fw, 20).build());
        y += 26;

        visibleBtn = addRenderableWidget(Button.builder(
                makeToggleLabel("chattabsconfig.chattab.visiblebydefault", tab.isVisibleByDefault()),
                b -> {
                    tab.setVisibleByDefault(!tab.isVisibleByDefault());
                    b.setMessage(makeToggleLabel("chattabsconfig.chattab.visiblebydefault", tab.isVisibleByDefault()));
                })
                .bounds(left, y, fw, 20).build());
        y += 30;

        prefixField = new EditBox(font, left, y, halfW, rowH, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"));
        prefixField.setValue(tab.getSendModifier().getPrefix());
        prefixField.setMaxLength(32);
        addRenderableWidget(prefixField);

        suffixField = new EditBox(font, left + halfW + 12, y, halfW, rowH, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"));
        suffixField.setValue(tab.getSendModifier().getSuffix());
        suffixField.setMaxLength(32);
        addRenderableWidget(suffixField);
        y += rowH + gap + 14;

        hexColorField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.filter.color.hex"));
        String hex = String.format("#%06X", tab.getFilter().getHexColor() & 0xFFFFFF);
        hexColorField.setValue(hex);
        hexColorField.setMaxLength(7);
        addRenderableWidget(hexColorField);

        int btnW = 110;
        int btnGap = 14;
        int btnsW = btnW * 2 + btnGap;
        int btnsLeft = cx - btnsW / 2;
        y = Math.max(y + rowH + gap + 16, height - 40);

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(btnsLeft, y, btnW, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("chattabsconfig.chattab.save"), b -> save())
                .bounds(btnsLeft + btnW + btnGap, y, btnW, 20).build());
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
        int fw = Math.min(300, width - 60);
        int left = cx - fw / 2;
        int halfW = (fw - 12) / 2;
        int rowH = 22;
        int gap = 10;
        int y = 36;

        ctx.drawString(font, Component.translatable("chattabs.tabedit.title"),
                cx - font.width(Component.translatable("chattabs.tabedit.title")) / 2, 20, 0xFFFFFF);

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.name"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.name")) / 2, y, 0xAAAAAA);
        y += rowH + gap + 14;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.filter.regex"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.filter.regex")) / 2, y, 0xAAAAAA);
        y += rowH + gap + 14;

        y += 26 + 30;
        y -= 14;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"),
                left + halfW / 2 - font.width(Component.translatable("chattabsconfig.chattab.sendmodifier.prefix")) / 2, y, 0xAAAAAA);
        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"),
                left + halfW + 12 + halfW / 2 - font.width(Component.translatable("chattabsconfig.chattab.sendmodifier.suffix")) / 2, y, 0xAAAAAA);
        y += rowH + gap + 14;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.filter.color.hex"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.filter.color.hex")) / 2, y, 0xAAAAAA);
    }

    private Component makeToggleLabel(String key, boolean value) {
        return Component.translatable(key).append(": ").append(
                Component.literal(value ? "§aON" : "§cOFF"));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
