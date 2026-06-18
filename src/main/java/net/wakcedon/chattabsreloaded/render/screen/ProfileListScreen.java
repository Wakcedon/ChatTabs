package net.wakcedon.chattabsreloaded.render.screen;

import net.wakcedon.chattabsreloaded.config.ChatTabsConfigBase;
import net.wakcedon.chattabsreloaded.config.ProfilesConfig;
import net.wakcedon.chattabsreloaded.profiles.ServerTabProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ProfileListScreen extends Screen {

    private final Screen parent;
    private ProfilesConfig profilesConfig;
    private List<ServerTabProfile> profiles;
    private ServerTabProfile defaultProfile;

    public ProfileListScreen(Screen parent) {
        super(Component.translatable("chattabsconfig.serverprofiles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ChatTabsConfigBase config = ChatTabsConfigBase.getInstance();
        profilesConfig = config.getProfilesConfig();
        profiles = new ArrayList<>();
        defaultProfile = null;
        if(profilesConfig != null) {
            if(profilesConfig.getProfiles() != null) profiles.addAll(profilesConfig.getProfiles());
            defaultProfile = profilesConfig.getDefaultProfile();
        }

        int cx = width / 2;
        int fw = Math.min(340, width - 40);
        int left = cx - fw / 2;
        int rowH = 20;
        int y = 36;

        if(defaultProfile != null) {
            y += rowH;
        }
        for(int i = 0; i < profiles.size(); i++) {
            ServerTabProfile p = profiles.get(i);
            int rowY = y;

            int btnW = 40;
            int gap = 4;

            Button editBtn = Button.builder(Component.literal("✎"), b -> {
                    minecraft.setScreen(new ProfileEditScreen(this, p, () -> saveAndRebuild()));
                })
                .bounds(cx + fw / 2 - (btnW * 2 + gap), rowY, btnW, 16).build();
            addRenderableWidget(editBtn);

            Button delBtn = Button.builder(Component.literal("×"), b -> {
                    profiles.remove(p);
                    saveAndRebuild();
                })
                .bounds(cx + fw / 2 - btnW, rowY, btnW, 16).build();
            addRenderableWidget(delBtn);

            y += rowH;
        }

        y = Math.max(y + 10, height - 36);
        int btnW = 90;
        int gap = 10;
        int btnsW = btnW * 2 + gap;
        int btnsLeft = cx - btnsW / 2;

        addRenderableWidget(Button.builder(Component.translatable("chattabsconfig.serverprofiles"), b -> {
                ServerTabProfile newProf = new ServerTabProfile("newserver.example.com", "New Profile", new ArrayList<>());
                minecraft.setScreen(new ProfileEditScreen(this, newProf, () -> {
                    profiles.add(newProf);
                    saveAndRebuild();
                }));
            })
            .bounds(btnsLeft, y, btnW, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(btnsLeft + btnW + gap, y, btnW, 16).build());
    }

    private void saveAndRebuild() {
        if(profilesConfig != null) {
            profilesConfig.setProfiles(profiles);
            ChatTabsConfigBase.getInstance().save();
        }
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);
        super.render(ctx, mx, my, delta);

        int cx = width / 2;
        ctx.drawString(font, title, cx - font.width(title) / 2, 14, 0xFFFFFF);

        int fw = Math.min(340, width - 40);
        int left = cx - fw / 2;
        int rowH = 20;
        int y = 36;

        if(defaultProfile != null) {
            String ip = defaultProfile.getServerIp() != null ? defaultProfile.getServerIp() : "";
            int tc = defaultProfile.getTabs() != null ? defaultProfile.getTabs().size() : 0;
            String name = defaultProfile.getName() != null ? defaultProfile.getName() : defaultProfile.getServerIp();
            ctx.drawString(font, "§e" + name + " §7(" + ip + ", " + tc + " tabs) §8[default]",
                left + 2, y + 2, 0xAAAAAA);
            y += rowH;
        }

        for(ServerTabProfile p : profiles) {
            String ip = p.getServerIp() != null ? p.getServerIp() : "";
            int tc = p.getTabs() != null ? p.getTabs().size() : 0;
            String name = p.getName() != null ? p.getName() : ip;
            ctx.drawString(font, name + " §7(" + ip + ", " + tc + " tabs)",
                left + 2, y + 2, 0xAAAAAA);
            y += rowH;
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
