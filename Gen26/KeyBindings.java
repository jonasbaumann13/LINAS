import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import java.awt.event.ActionEvent;

/** Local keyboard preferences. Legacy command codes keep game rules unchanged. */
public final class KeyBindings {
    public enum Command {
        SOUTHWEST("Move down-left", "Nach links unten bewegen", 97),
        SOUTH("Move down", "Nach unten bewegen", 98),
        SOUTHEAST("Move down-right", "Nach rechts unten bewegen", 99),
        NORTHWEST("Move up-left", "Nach links oben bewegen", 100),
        NORTH("Move up", "Nach oben bewegen", 101),
        NORTHEAST("Move up-right", "Nach rechts oben bewegen", 102),
        NEXT("Next unit (deployment and battle)", "Nächste Einheit (Aufstellung und Kampf)", 107),
        END_TURN("End turn", "Zug beenden", 10);
        final String english, german;
        final int code;
        Command(String english, String german, int code) {
            this.english = english; this.german = german; this.code = code;
        }
        String label() { return I18n.isGerman() ? german : english; }
    }
    private static final Preferences PREFS = Preferences.userNodeForPackage(KeyBindings.class).node("keypad");
    private static int[] keys = load();
    private KeyBindings() {}
    public static int[] defaults() {
        Command[] commands = Command.values();
        int[] result = new int[commands.length];
        for (int i = 0; i < result.length; i++) result[i] = commands[i].code;
        return result;
    }
    private static int[] load() {
        int[] result = defaults();
        for (Command command : Command.values())
            result[command.ordinal()] = PREFS.getInt(command.name(), command.code);
        return valid(result) ? result : defaults();
    }
    public static int[] snapshot() { return keys.clone(); }
    public static boolean valid(int[] values) {
        if (values == null || values.length != Command.values().length) return false;
        Set<Integer> used = new HashSet<>();
        for (int value : values)
            if (value <= 0 || KeyEvent.getKeyText(value).startsWith("Unknown") || !used.add(value)) return false;
        return true;
    }
    public static void save(int[] values) throws BackingStoreException {
        if (!valid(values)) throw new IllegalArgumentException("Every action needs a different key.");
        for (Command command : Command.values()) PREFS.putInt(command.name(), values[command.ordinal()]);
        PREFS.flush();
        keys = values.clone();
    }
    public static int commandFor(int keyCode) {
        for (Command command : Command.values())
            if (keys[command.ordinal()] == keyCode) return command.code;
        return -1;
    }
    /** Window bindings work even when the board has keyboard focus. */
    public static void install(JRootPane root, boolean deployment, java.util.function.IntConsumer handler) {
        for (Command command : Command.values()) {
            if (deployment && command != Command.NEXT) continue;
            String name = "linas." + command.name();
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(keys[command.ordinal()], 0), name);
            root.getActionMap().put(name, new AbstractAction() {
                public void actionPerformed(ActionEvent event) { handler.accept(command.code); }
            });
        }
    }
}
