import java.awt.*;
import java.awt.event.*;
import java.util.prefs.BackingStoreException;
import javax.swing.*;

/** Edits a private draft; only Save changes the active bindings. */
public final class KeypadConfiguration extends JDialog {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private int[] draft = KeyBindings.snapshot();
    private final JButton[] buttons = new JButton[draft.length];
    private final JLabel status = new JLabel(" ");
    private int listening = -1;
    private String text(String en, String de) { return I18n.isGerman() ? de : en; }

    public KeypadConfiguration(JFrame owner) {
        super(owner, I18n.text("Keypad Configuration"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(content);
        content.add(new JLabel(text("Click a key, then press its replacement (without modifiers).",
            "Taste anklicken, dann neue Taste ohne Zusatztasten drücken.")), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 2, 12, 8));
        for (KeyBindings.Command command : KeyBindings.Command.values()) {
            int index = command.ordinal();
            rows.add(new JLabel(command.label()));
            buttons[index] = new JButton();
            buttons[index].addActionListener(ignored -> {
                listening = index;
                refresh();
                status.setText(text("Press a key. Escape cancels capture.", "Taste drücken. Escape bricht die Eingabe ab."));
                buttons[index].requestFocusInWindow();
            });
            rows.add(buttons[index]);
        }
        content.add(rows, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(new JLabel(text("Mouse: left-click selects a unit; right-click attacks. Numpad defaults require Num Lock.",
            "Maus: Linksklick wählt, Rechtsklick greift an. Standardbelegung benötigt Num Lock.")), BorderLayout.NORTH);
        footer.add(status, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reset = new JButton(text("Restore defaults", "Standard wiederherstellen"));
        reset.addActionListener(ignored -> { listening = -1; draft = KeyBindings.defaults(); refresh(); status.setText(" "); });
        JButton cancel = new JButton(text("Cancel", "Abbrechen"));
        cancel.addActionListener(ignored -> dispose());
        JButton save = new JButton(text("Save", "Speichern"));
        save.addActionListener(ignored -> {
            if (listening >= 0) return;
            try { KeyBindings.save(draft); dispose(); }
            catch (BackingStoreException | SecurityException ex) {
                JOptionPane.showMessageDialog(this, text("Could not save key settings: ", "Tasten konnten nicht gespeichert werden: ") + ex.getMessage(),
                    I18n.text("Error"), JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(reset); actions.add(cancel); actions.add(save);
        footer.add(actions, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);
        KeyEventDispatcher capture = event -> {
            if (listening < 0 || !isActive()) return false;
            event.consume();
            if (event.getID() != KeyEvent.KEY_PRESSED) return true;
            int code = event.getKeyCode();
            if (code == KeyEvent.VK_ESCAPE) { listening = -1; refresh(); status.setText(" "); return true; }
            if (event.getModifiersEx() != 0 || code == KeyEvent.VK_UNDEFINED) {
                status.setText(text("Use a single key without Shift, Ctrl, Alt or Windows.", "Eine Taste ohne Umschalt, Strg, Alt oder Windows verwenden."));
                return true;
            }
            for (int i = 0; i < draft.length; i++) {
                if (i != listening && draft[i] == code) {
                    status.setText(text("Already assigned to: ", "Bereits belegt: ") + KeyBindings.Command.values()[i].label());
                    return true;
                }
            }
            draft[listening] = code;
            listening = -1;
            refresh();
            status.setText(" ");
            return true;
        };
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent event) { manager.removeKeyEventDispatcher(capture); }
        });
        manager.addKeyEventDispatcher(capture);
        refresh();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }
    private void refresh() {
        for (int i = 0; i < buttons.length; i++)
            buttons[i].setText(i == listening ? text("Press a key...", "Taste drücken...") : KeyEvent.getKeyText(draft[i]));
    }
}
