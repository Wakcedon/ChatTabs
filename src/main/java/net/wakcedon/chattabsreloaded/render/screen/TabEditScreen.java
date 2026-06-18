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

    private int fw, left, halfW, rowH, gap, labelGap;

    @Override
    protected void init() {
        int cx = width / 2;
        fw = Math.min(320, width - 40);
        left = cx - fw / 2;
        halfW = (fw - 8) / 2;
        rowH = 18;
        gap = 4;
        labelGap = 10;
        int y = 40;

        nameField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.name"));
        nameField.setValue(tab.getName());
        nameField.setMaxLength(64);
        addRenderableWidget(nameField);
        y += rowH + labelGap;

        filterField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.filter.regex"));
        filterField.setValue(tab.getFilter().getRegex());
        filterField.setMaxLength(256);
        addRenderableWidget(filterField);
        y += rowH + labelGap;

        filterDMsBtn = addRenderableWidget(Button.builder(
                makeToggleLabel("chattabsconfig.chattab.filter.messages", tab.getFilter().filtersMessages()),
                b -> {
                    tab.getFilter().filterMessages(!tab.getFilter().filtersMessages());
                    b.setMessage(makeToggleLabel("chattabsconfig.chattab.filter.messages", tab.getFilter().filtersMessages()));
                })
                .bounds(left, y, fw, 16).build());
        y += 20;

        visibleBtn = addRenderableWidget(Button.builder(
                makeToggleLabel("chattabsconfig.chattab.visiblebydefault", tab.isVisibleByDefault()),
                b -> {
                    tab.setVisibleByDefault(!tab.isVisibleByDefault());
                    b.setMessage(makeToggleLabel("chattabsconfig.chattab.visiblebydefault", tab.isVisibleByDefault()));
                })
                .bounds(left, y, fw, 16).build());
        y += 22;

        prefixField = new EditBox(font, left, y, halfW, rowH, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"));
        prefixField.setValue(tab.getSendModifier().getPrefix());
        prefixField.setMaxLength(32);
        addRenderableWidget(prefixField);

        suffixField = new EditBox(font, left + halfW + 8, y, halfW, rowH, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"));
        suffixField.setValue(tab.getSendModifier().getSuffix());
        suffixField.setMaxLength(32);
        addRenderableWidget(suffixField);
        y += rowH + labelGap;

        hexColorField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.filter.color.hex"));
        String hex = String.format("#%06X", tab.getFilter().getHexColor() & 0xFFFFFF);
        hexColorField.setValue(hex);
        hexColorField.setMaxLength(7);
        addRenderableWidget(hexColorField);
        y += rowH + labelGap;

        int btnW = 90;
        int btnGap = 10;
        int btnsW = btnW * 2 + btnGap;
        int btnsLeft = cx - btnsW / 2;
        y = Math.max(y + 6, height - 36);

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(btnsLeft, y, btnW, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("chattabsconfig.chattab.save"), b -> save())
                .bounds(btnsLeft + btnW + btnGap, y, btnW, 16).build());
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
        int y = 26;

        ctx.drawString(font, Component.translatable("chattabs.tabedit.title"),
                cx - font.width(Component.translatable("chattabs.tabedit.title")) / 2, y, 0xFFFFFF);
        y += labelGap + 4;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.name"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.name")) / 2, y, 0xAAAAAA);
        y += rowH + labelGap;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.filter.regex"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.filter.regex")) / 2, y, 0xAAAAAA);
        y += rowH + labelGap;

        y += 20 + 22;
        y -= 4;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"),
                left + halfW / 2 - font.width(Component.translatable("chattabsconfig.chattab.sendmodifier.prefix")) / 2, y, 0xAAAAAA);
        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"),
                left + halfW + 8 + halfW / 2 - font.width(Component.translatable("chattabsconfig.chattab.sendmodifier.suffix")) / 2, y, 0xAAAAAA);
        y += rowH + labelGap;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.filter.color.hex"),
                cx - font.width(Component.translatable("chattabsconfig.chattab.filter.color.hex")) / 2, y, 0xAAAAAA);
    }

    private Component makeToggleLabel(String key, boolean value) {
        return Component.translatable(key).append(": ").append(
                value ? Component.translatable("chattabs.value.on") : Component.translatable("chattabs.value.off"));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
