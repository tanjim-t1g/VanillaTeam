package com.vanillateams.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private MessageUtil() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        String legacyConverted = convertLegacyToMiniMessage(input);
        return MINI_MESSAGE.deserialize(legacyConverted)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component button(String label, String command, NamedTextColor color) {
        return Component.text("[" + label + "]")
                .color(color)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to " + label.toLowerCase()).color(NamedTextColor.GRAY)
                ));
    }

    public static Component acceptDenyButtons(String acceptCmd, String denyCmd) {
        return button("Accept", acceptCmd, NamedTextColor.GREEN)
                .append(Component.text(" "))
                .append(button("Deny", denyCmd, NamedTextColor.RED));
    }

    public static Component yesNoButtons(String yesCmd, String noCmd) {
        return button("Yes", yesCmd, NamedTextColor.GREEN)
                .append(Component.text(" "))
                .append(button("No", noCmd, NamedTextColor.RED));
    }

    private static String convertLegacyToMiniMessage(String text) {
        if (!text.contains("&") && !text.contains("§")) {
            return text;
        }
        String normalized = text.replace('§', '&');
        Component legacyComp = LEGACY_AMPERSAND.deserialize(normalized);
        return MINI_MESSAGE.serialize(legacyComp).replace("\\<", "<");
    }
}
