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

    public TabEditScreen(Screen parent, ChatTab tab) {
        super(Component.translatable("chattabs.tabedit.title"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40;
        int fw = font.width("W") * 18;

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(4, 4, 40, 20).build());

        nameField = new EditBox(font, cx - fw/2, y + 12, fw, 20, Component.translatable("chattabsconfig.chattab.name"));
        nameField.setValue(tab.getName());
        nameField.setMaxLength(64);
        addRenderableWidget(nameField);
        y += 45;

        filterField = new EditBox(font, cx - fw/2, y + 12, fw, 20, Component.translatable("chattabsconfig.chattab.filter.regex"));
        filterField.setValue(tab.getFilter().getRegex());
        filterField.setMaxLength(256);
        addRenderableWidget(filterField);
        y += 40;

        filterDMsBtn = addRenderableWidget(Button.builder(
                Component.translatable("chattabsconfig.chattab.filter.messages").append(": ")
                        .append(Component.literal(tab.getFilter().filtersMessages() ? "ON" : "OFF")),
                b -> {
                    tab.getFilter().filterMessages(!tab.getFilter().filtersMessages());
                    b.setMessage(Component.translatable("chattabsconfig.chattab.filter.messages").append(": ")
                            .append(Component.literal(tab.getFilter().filtersMessages() ? "ON" : "OFF")));
                })
                .bounds(cx - fw/2, y, fw, 20).build());
        y += 25;

        visibleBtn = addRenderableWidget(Button.builder(
                Component.translatable("chattabsconfig.chattab.visiblebydefault").append(": ")
                        .append(Component.literal(tab.isVisibleByDefault() ? "ON" : "OFF")),
                b -> {
                    tab.setVisibleByDefault(!tab.isVisibleByDefault());
                    b.setMessage(Component.translatable("chattabsconfig.chattab.visiblebydefault").append(": ")
                            .append(Component.literal(tab.isVisibleByDefault() ? "ON" : "OFF")));
                })
                .bounds(cx - fw/2, y, fw, 20).build());
        y += 30;

        prefixField = new EditBox(font, cx - fw/2, y + 12, fw/2 - 5, 20, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"));
        prefixField.setValue(tab.getSendModifier().getPrefix());
        prefixField.setMaxLength(32);
        addRenderableWidget(prefixField);

        suffixField = new EditBox(font, cx + 5, y + 12, fw/2 - 5, 20, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"));
        suffixField.setValue(tab.getSendModifier().getSuffix());
        suffixField.setMaxLength(32);
        addRenderableWidget(suffixField);
        y += 40;

        addRenderableWidget(Button.builder(Component.translatable("chattabsconfig.chattab.save"), b -> save())
                .bounds(cx - 55, y, 50, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(cx + 5, y, 50, 20).build());
    }

    private void save() {
        tab.setName(nameField.getValue());
        tab.getFilter().setRegex(filterField.getValue());
        ChatTabsConfigBase.getInstance().save();
        onClose();
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);

        int cx = width / 2;
        int fw = font.width("W") * 18;
        int y = 40;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.name"), cx - fw/2, y, 0xFFFFFF);
        y += 45;
        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.filter.regex"), cx - fw/2, y, 0xFFFFFF);
        y += 40;
        y += 55;
        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"), cx - fw/2, y, 0xFFFFFF);
        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"), cx + 5, y, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
