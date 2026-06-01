package net.grilledham.chattabs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.annotations.Expose;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.grilledham.chattabs.profiles.ServerProfile;
import net.grilledham.chattabs.tabs.ChatLineFilter;
import net.grilledham.chattabs.tabs.ChatTab;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public final class FabricConfigScreens {

    private FabricConfigScreens() {}

    public static ConfigBuilder create() {
        ChatTabsConfig cfg = ChatTabsConfig.getInstance();
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable("chattabsconfig.title"))
                .setSavingRunnable(ChatTabsConfig::save)
                .transparentBackground();
        builder.setGlobalizedExpanded(true);
        builder.setGlobalized(true);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        SubCategoryBuilder chatSubCategory = entryBuilder
                .startSubCategory(Component.translatable("chattabsconfig.category.general.chat"))
                .setExpanded(true);
        chatSubCategory.add(entryBuilder
                .startIntField(Component.translatable("chattabsconfig.maxlines"), cfg.maxLines)
                .setTooltip(Component.translatable("chattabsconfig.maxlines.tooltip"))
                .setDefaultValue(100)
                .setMin(100)
                .setSaveConsumer(i -> cfg.maxLines = i)
                .build());
        chatSubCategory.add(entryBuilder
                .startFloatField(Component.translatable("chattabsconfig.previewtime"), cfg.previewTime)
                .setTooltip(Component.translatable("chattabsconfig.previewtime.tooltip"))
                .setDefaultValue(10.0F)
                .setMin(0.0F)
                .setSaveConsumer(f -> cfg.previewTime = f)
                .build());
        chatSubCategory.add(entryBuilder
                .startBooleanToggle(Component.translatable("chattabsconfig.clearhistory"), cfg.clearHistory)
                .setTooltip(Component.translatable("chattabsconfig.clearhistory.tooltip"))
                .setDefaultValue(true)
                .setSaveConsumer(i -> cfg.clearHistory = i)
                .build());
        NestedListListEntry<ChatTab, MultiElementListEntry<ChatTab>> chatTabsList = new NestedListListEntry<>(
                Component.translatable("chattabsconfig.chattabs"),
                cfg.getChatTabs(),
                true,
                Optional::empty,
                l -> {
                    // replace internal list
                    // ChatTabsConfig manages internals
                },
                List::of,
                Component.translatable("chattabsconfig.reset"),
                true,
                false,
                (value, entry) -> {
                    ChatTab tab = value != null ? value : new ChatTab();
                    return new MultiElementListEntry<>(Component.literal(tab.getName()), tab, List.of(
                            entryBuilder.startStrField(Component.translatable("chattabsconfig.chattab.id"), tab.getId())
                                    .setDefaultValue("new_tab")
                                    .setSaveConsumer(tab::setId)
                                    .build(),
                            entryBuilder.startStrField(Component.translatable("chattabsconfig.chattab.name"), tab.getName())
                                    .setDefaultValue("New Tab")
                                    .setSaveConsumer(tab::setName)
                                    .build(),
                            entryBuilder.startBooleanToggle(Component.translatable("chattabsconfig.chattab.save"), tab.shouldSave())
                                    .setDefaultValue(true)
                                    .setSaveConsumer(tab::setSave)
                                    .build(),
                            entryBuilder.startBooleanToggle(Component.translatable("chattabsconfig.chattab.visiblebydefault"), tab.isVisibleByDefault())
                                    .setTooltip(Component.translatable("chattabsconfig.chattab.visiblebydefault.tooltip"))
                                    .setDefaultValue(true)
                                    .setSaveConsumer(tab::setVisibleByDefault)
                                    .build(),
                            new MultiElementListEntry<>(Component.translatable("chattabsconfig.chattab.filter"), tab.getFilter(), List.of(
                                    entryBuilder.startBooleanToggle(Component.translatable("chattabsconfig.chattab.filter.messages"), tab.getFilter().filtersMessages())
                                            .setDefaultValue(false)
                                            .setSaveConsumer(tab.getFilter()::filterMessages)
                                            .build(),
                                    entryBuilder.startStrField(Component.translatable("chattabsconfig.chattab.filter.regex"), tab.getFilter().getRegex())
                                            .setTooltip(Component.translatable("chattabsconfig.chattab.filter.regex.tooltip"))
                                            .setDefaultValue(".*")
                                            .setSaveConsumer(tab.getFilter()::setRegex)
                                            .build(),
                                    entryBuilder.startEnumSelector(Component.translatable("chattabsconfig.chattab.filter.color"), ChatLineFilter.ColorFilter.class, tab.getFilter().getColorFilter())
                                            .setTooltip(Component.translatable("chattabsconfig.chattab.filter.color.tooltip"))
                                            .setDefaultValue(ChatLineFilter.ColorFilter.DISABLED)
                                            .setSaveConsumer(tab.getFilter()::setColorFilter)
                                            .build(),
                                    entryBuilder.startColorField(Component.translatable("chattabsconfig.chattab.filter.color.hex"), tab.getFilter().getHexColor())
                                            .setDefaultValue(0xFFFFFF)
                                            .setSaveConsumer(tab.getFilter()::setHexColor)
                                            .build()
                            ), true),
                            new MultiElementListEntry<>(Component.translatable("chattabsconfig.chattab.sendmodifier"), tab.getFilter(), List.of(
                                    entryBuilder.startStrField(Component.translatable("chattabsconfig.chattab.sendmodifier.prefix"), tab.getSendModifier().getPrefix())
                                            .setDefaultValue("")
                                            .setSaveConsumer(tab.getSendModifier()::setPrefix)
                                            .build(),
                                    entryBuilder.startStrField(Component.translatable("chattabsconfig.chattab.sendmodifier.suffix"), tab.getSendModifier().getSuffix())
                                            .setDefaultValue("")
                                            .setSaveConsumer(tab.getSendModifier()::setSuffix)
                                            .build()
                            ), true)
                    ), true);
                }
        );
        NestedListListEntry<ServerProfile, MultiElementListEntry<ServerProfile>> serverProfilesList = new NestedListListEntry<>(
                Component.translatable("chattabsconfig.serverprofiles"),
                cfg.serverProfiles,
                true,
                Optional::empty,
                l -> cfg.serverProfiles = l,
                List::of,
                Component.translatable("chattabsconfig.reset"),
                true,
                false,
                (value, entry) -> {
                    ServerProfile profile = value != null ? value : new ServerProfile();
                    NestedListListEntry<String, StringListEntry> tabs = new NestedListListEntry<>(
                            Component.translatable("chattabsconfig.serverprofile.tabs"),
                            profile.getTabIds(),
                            true,
                            Optional::empty,
                            profile::setTabIds,
                            List::of,
                            Component.translatable("chattabsconfig.reset"),
                            true,
                            false,
                            (v, listEntry) -> {
                                String tabId = v != null ? v : "";
                                return entryBuilder.startStrField(Component.translatable("chattabsconfig.serverprofile.tabs.tabId"), tabId)
                                        .setDefaultValue("")
                                        .build();
                            }
                    );
                    return new MultiElementListEntry<>(Component.literal(profile.getServerAddress()), profile, List.of(
                            entryBuilder.startStrField(Component.translatable("chattabsconfig.serverprofile.serveraddress"), profile.getServerAddress())
                                    .setDefaultValue("")
                                    .setSaveConsumer(profile::setServerAddress)
                                    .build(),
                            tabs
                    ), true);
                }
        );
        builder.getOrCreateCategory(Component.translatable("chattabsconfig.category.general"))
                .addEntry(entryBuilder
                        .startBooleanToggle(Component.translatable("chattabsconfig.enabled"), cfg.enabled)
                        .setTooltip(Component.translatable("chattabsconfig.enabled.tooltip"))
                        .setDefaultValue(true)
                        .setSaveConsumer(b -> cfg.enabled = b)
                        .build())
                .addEntry(chatSubCategory.build());
        builder.getOrCreateCategory(Component.translatable("chattabsconfig.category.tabs"))
                .addEntry(entryBuilder
                        .startBooleanToggle(Component.translatable("chattabsconfig.autogeneratemsgtabs"), cfg.autoGenerateMsgTabs)
                        .setTooltip(Component.translatable("chattabsconfig.autogeneratemsgtabs.tooltip"))
                        .setDefaultValue(true)
                        .setSaveConsumer(b -> cfg.autoGenerateMsgTabs = b)
                        .build())
                .addEntry(entryBuilder
                        .startBooleanToggle(Component.translatable("chattabsconfig.savegenerated"), cfg.saveGenerated)
                        .setDefaultValue(false)
                        .setSaveConsumer(b -> cfg.saveGenerated = b)
                        .build())
                .addEntry(chatTabsList)
                .addEntry(serverProfilesList);
        builder.getOrCreateCategory(Component.translatable("chattabsconfig.category.appearance"))
                .addEntry(entryBuilder
                        .startBooleanToggle(Component.translatable("chattabsconfig.textshadow"), cfg.textShadow)
                        .setTooltip(Component.translatable("chattabsconfig.textshadow.tooltip"))
                        .setDefaultValue(true)
                        .setSaveConsumer(b -> cfg.textShadow = b)
                        .build())
                .addEntry(entryBuilder
                        .startAlphaColorField(Component.translatable("chattabsconfig.bgcolor"), cfg.bgColor.getRGB())
                        .setTooltip(Component.translatable("chattabsconfig.bgcolor.tooltip"))
                        .setDefaultValue(0x80000000)
                        .setSaveConsumer(i -> cfg.bgColor = new Color(i, true))
                        .build())
                .addEntry(entryBuilder
                        .startAlphaColorField(Component.translatable("chattabsconfig.bgcolorhovered"), cfg.bgColorHovered.getRGB())
                        .setTooltip(Component.translatable("chattabsconfig.bgcolorhovered.tooltip"))
                        .setDefaultValue(0x80FFFFFF)
                        .setSaveConsumer(i -> cfg.bgColorHovered = new Color(i, true))
                        .build())
                .addEntry(entryBuilder
                        .startAlphaColorField(Component.translatable("chattabsconfig.selectedtabcolor"), cfg.selectedTabColor.getRGB())
                        .setTooltip(Component.translatable("chattabsconfig.selectedtabcolor.tooltip"))
                        .setDefaultValue(0xFFFFFFFF)
                        .setSaveConsumer(i -> cfg.selectedTabColor = new Color(i, true))
                        .build())
                .addEntry(entryBuilder
                        .startAlphaColorField(Component.translatable("chattabsconfig.unreadcolor"), cfg.unreadColor.getRGB())
                        .setTooltip(Component.translatable("chattabsconfig.unreadcolor.tooltip"))
                        .setDefaultValue(0xFF00AAAA)
                        .setSaveConsumer(i -> cfg.unreadColor = new Color(i, true))
                        .build())
                .addEntry(entryBuilder
                        .startIntField(Component.translatable("chattabsconfig.chatwidth"), cfg.chatWidth)
                        .setDefaultValue(320)
                        .setMin(40)
                        .setSaveConsumer(i -> cfg.chatWidth = i)
                        .build())
                .addEntry(entryBuilder
                        .startIntField(Component.translatable("chattabsconfig.chatheightfocused"), cfg.chatHeightFocused)
                        .setDefaultValue(180)
                        .setMin(20)
                        .setSaveConsumer(i -> cfg.chatHeightFocused = i)
                        .build())
                .addEntry(entryBuilder
                        .startIntField(Component.translatable("chattabsconfig.chatheightunfocused"), cfg.chatHeightUnfocused)
                        .setDefaultValue(90)
                        .setMin(20)
                        .setSaveConsumer(i -> cfg.chatHeightUnfocused = i)
                        .build())
        ;
        return builder;
    }
}
