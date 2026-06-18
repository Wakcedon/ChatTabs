package net.wakcedon.chattabsreloaded.render.screen;

import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ProfileEditScreen extends Screen {

    private final Screen parent;
    private final ServerTabProfile profile;
    private final Runnable onSave;

    private EditBox ipField;
    private EditBox nameField;
    private int fw, left;

    public ProfileEditScreen(Screen parent, ServerTabProfile profile, Runnable onSave) {
        super(Component.translatable("chattabsconfig.serverprofiles"));
        this.parent = parent;
        this.profile = profile;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        fw = Math.min(260, width - 40);
        left = cx - fw / 2;
        int gap = 4;
        int rowH = 18;
        int y = 40;

        nameField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.chattab.name"));
        nameField.setValue(profile.getName() != null ? profile.getName() : "");
        nameField.setMaxLength(64);
        addRenderableWidget(nameField);
        y += rowH + 12;

        ipField = new EditBox(font, left, y, fw, rowH, Component.translatable("chattabsconfig.serverprofile.serveraddress"));
        ipField.setValue(profile.getServerIp() != null ? profile.getServerIp() : "");
        ipField.setMaxLength(128);
        addRenderableWidget(ipField);
        y += rowH + 12;

        if(profile.getTabs() != null && !profile.getTabs().isEmpty()) {
            y += 4;
            y += 20;
        }

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
        profile.setName(nameField.getValue());
        profile.setServerIp(ipField.getValue());
        if(onSave != null) onSave.run();
        onClose();
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);

        int cx = width / 2;
        int y = 26;

        ctx.drawString(font, title, cx - font.width(title) / 2, y, 0xFFFFFF);
        y += 18;

        ctx.drawString(font, Component.translatable("chattabsconfig.chattab.name"),
            cx - font.width(Component.translatable("chattabsconfig.chattab.name")) / 2, y, 0xAAAAAA);
        y += 18 + 12;

        ctx.drawString(font, Component.translatable("chattabsconfig.serverprofile.serveraddress"),
            cx - font.width(Component.translatable("chattabsconfig.serverprofile.serveraddress")) / 2, y, 0xAAAAAA);
        y += 18 + 12;

        if(profile.getTabs() != null && !profile.getTabs().isEmpty()) {
            y += 4;
            ctx.drawString(font, "§7" + profile.getTabs().size() + " "
                + Component.translatable("chattabsconfig.serverprofile.tabs").getString(),
                left + 2, y, 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
