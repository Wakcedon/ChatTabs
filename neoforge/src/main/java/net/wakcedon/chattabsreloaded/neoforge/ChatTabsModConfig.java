package net.wakcedon.chattabsreloaded.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ChatTabsModConfig {

    public static class Client {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue maxLines;
        public final ModConfigSpec.DoubleValue previewTime;
        public final ModConfigSpec.BooleanValue clearHistory;
        public final ModConfigSpec.BooleanValue textShadow;
        public final ModConfigSpec.BooleanValue autoGenerateMsgTabs;

        Client(ModConfigSpec.Builder builder) {
            builder.comment("General settings").push("general");

            enabled = builder
                    .comment("Enable or disable ChatTabs Reloaded")
                    .translation("chattabsconfig.enabled")
                    .define("enabled", true);

            maxLines = builder
                    .comment("How many lines are saved in the chat history. Minecraft default is 100.")
                    .translation("chattabsconfig.maxlines")
                    .defineInRange("maxLines", 100, 10, 1000);

            previewTime = builder
                    .comment("How long message previews are displayed (in seconds).")
                    .translation("chattabsconfig.previewtime")
                    .defineInRange("previewTime", 10.0, 0.0, 60.0);

            clearHistory = builder
                    .comment("Clear chat history when switching servers.")
                    .translation("chattabsconfig.clearhistory")
                    .define("clearHistory", true);

            autoGenerateMsgTabs = builder
                    .comment("Automatically create tabs for received DMs.")
                    .translation("chattabsconfig.autogeneratemsgtabs")
                    .define("autoGenerateMsgTabs", true);

            builder.pop();

            builder.comment("Appearance settings").push("appearance");

            textShadow = builder
                    .comment("Should the chat text have a shadow. Minecraft default is true.")
                    .translation("chattabsconfig.textshadow")
                    .define("textShadow", true);

            builder.pop();
        }
    }

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = pair.getRight();
        CLIENT = pair.getLeft();
    }
}
