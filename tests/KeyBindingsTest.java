import javax.swing.*;
import java.awt.event.*;

/** No preferences are changed by this test. */
public class KeyBindingsTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        int[] defaults = KeyBindings.defaults();
        check(KeyBindings.valid(defaults), "Defaults must be valid");
        int[] duplicate = defaults.clone();
        duplicate[0] = duplicate[1];
        check(!KeyBindings.valid(duplicate), "Reject duplicate keys");
        duplicate[0] = KeyEvent.VK_UNDEFINED;
        check(!KeyBindings.valid(duplicate), "Reject undefined keys");
        check(!KeyBindings.valid(new int[0]), "Reject missing bindings");
        int[] custom = defaults.clone();
        custom[0] = KeyEvent.VK_A;
        check(KeyBindings.valid(custom), "Allow letter keys");
        int[] active = KeyBindings.snapshot();
        for (KeyBindings.Command command : KeyBindings.Command.values())
            check(KeyBindings.commandFor(active[command.ordinal()]) == command.code, "Resolve " + command);
        SwingUtilities.invokeAndWait(() -> {
            JRootPane root = new JRootPane();
            int[] received = {-1};
            KeyBindings.install(root, false, code -> received[0] = code);
            for (KeyBindings.Command command : KeyBindings.Command.values()) {
                Object action = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .get(KeyStroke.getKeyStroke(active[command.ordinal()], 0));
                check(action != null, "Window shortcut for " + command);
                root.getActionMap().get(action).actionPerformed(new ActionEvent(root, 0, "test"));
                check(received[0] == command.code, "Dispatch " + command);
            }
            JRootPane deployment = new JRootPane();
            KeyBindings.install(deployment, true, code -> received[0] = code);
            check(deployment.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).allKeys().length == 1,
                "Deployment binds only next unit");
            check(deployment.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(
                KeyStroke.getKeyStroke(active[KeyBindings.Command.NEXT.ordinal()], 0)) != null,
                "Deployment uses configured next unit key");
        });
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeAndWait(() -> {
                JFrame owner = new JFrame();
                try {
                    for (boolean german : new boolean[] {false, true}) {
                        I18n.setGerman(german);
                        KeypadConfiguration dialog = new KeypadConfiguration(owner);
                        try {
                            check(dialog.isModal(), "Settings stay separate from game input");
                            check(dialog.getTitle().equals(I18n.text("Keypad Configuration")), "Localized title");
                            check(dialog.getWidth() > 0 && dialog.getHeight() > 0, "Dialog layout");
                        } finally { dialog.dispose(); }
                    }
                } finally { owner.dispose(); I18n.setGerman(false); }
            });
        }
        System.out.println("KeyBindingsTest passed");
    }
}
