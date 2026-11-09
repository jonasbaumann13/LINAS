import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/** Tests actual button, configured keyboard and mouse selection with unequal armies. */
public final class MainFrameSelectionTest {
    private MainFrameSelectionTest() {}
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static Unit[] army(int clients, int hosts) {
        Unit[] units = new Unit[clients + hosts];
        for (int i = 0; i < units.length; i++) {
            units[i] = new Operations().setUnit(new File(AppPaths.units(), "Tank/Tank.txt"),
                i < clients ? "client" : "host", i >= clients);
            units[i].PosX = 220;
            units[i].PosY = 14 + 56 * i;
        }
        return units;
    }
    private static void next(MainFrame frame, boolean keyboard) {
        if (keyboard) {
            int key = KeyBindings.snapshot()[KeyBindings.Command.NEXT.ordinal()];
            Object action = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(key, 0));
            frame.getRootPane().getActionMap().get(action)
                .actionPerformed(new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, "test"));
        } else frame.JBA[0].doClick(0);
    }
    private static void mouse(MainFrame frame, int index) {
        Unit unit = frame.UnitArray[index];
        frame.leftButton(new MouseEvent(frame.Brett, MouseEvent.MOUSE_CLICKED, 0, 0,
            unit.PosX + 9, unit.PosY + 8, 1, false, MouseEvent.BUTTON1));
        check(frame.UC == index, "Mouse selects own living unit " + index);
    }
    private static void scenario(int clients, int hosts, boolean host, boolean keyboard) throws Exception {
        Unit[] units = army(clients, hosts);
        int first = host ? clients : 0, limit = host ? units.length : clients;
        int count = limit - first;
        String context = "client=" + clients + ", host=" + hosts + ", isHost=" + host + ", keyboard=" + keyboard;
        try (ObjectOutputStream output = new ObjectOutputStream(new ByteArrayOutputStream())) {
            MainFrame frame = new MainFrame(host, output, units);
            try {
                frame.startTurn(units);
                for (int step = 1; step <= count * 2; step++) {
                    next(frame, keyboard);
                    check(frame.UC == first + step % count, "Stable circular order: " + context);
                }
                mouse(frame, limit - 1);
                next(frame, keyboard);
                check(frame.UC == first, "Wrap after manual selection: " + context);

                for (int i = first; i < limit; i++) units[i].ZP[0] = 0;
                units[limit - 1].ZP[0] = 20;
                frame.UC = first;
                next(frame, keyboard);
                check(frame.UC == limit - 1, "Reach last actionable unit across exhausted units: " + context);
                check(frame.keyNext && frame.JBA[0].isEnabled(), "Do not disable a valid choice: " + context);
                next(frame, keyboard);
                check(frame.UC == limit - 1, "Single actionable unit wraps to itself: " + context);

                // Enough for movement, but not an attack.
                units[limit - 1].ZP[1] = 2; units[limit - 1].ZP[2] = 4;
                units[limit - 1].ZP[0] = 2;
                next(frame, keyboard);
                check(frame.UC == limit - 1 && frame.keyNext, "Movement-only unit stays selectable: " + context);
                // Enough for an attack, but not movement.
                units[limit - 1].ZP[1] = 5; units[limit - 1].ZP[2] = 2;
                next(frame, keyboard);
                check(frame.UC == limit - 1 && frame.keyNext, "Attack-only unit stays selectable: " + context);

                units[limit - 1].TP = 0;
                next(frame, keyboard);
                check(!frame.keyNext && !frame.JBA[0].isEnabled(), "Destroyed and exhausted units are skipped: " + context);
                check(frame.JBA[1].isEnabled() && !frame.wait, "No actions does not end the turn automatically: " + context);
                int selected = frame.UC;
                next(frame, keyboard);
                check(frame.UC == selected, "No eligible unit is a safe no-op: " + context);
                if (count > 1) {
                    mouse(frame, first);
                    check(!frame.keyNext, "Manual selection does not revive exhausted choices: " + context);
                }

                units[limit - 1].TP = units[limit - 1].basicTP;
                frame.startTurn(units);
                check(frame.keyNext && frame.JBA[0].isEnabled(), "New turn restores cycling: " + context);
                next(frame, keyboard);
                check(frame.UC == first + 1 % count, "Restored cycle starts in stable order: " + context);
            } finally { frame.dispose(); }
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                for (int[] sizes : new int[][] {{1, 4}, {4, 1}, {3, 5}, {5, 3}})
                    for (boolean host : new boolean[] {false, true})
                        for (boolean keyboard : new boolean[] {false, true})
                            scenario(sizes[0], sizes[1], host, keyboard);
            } catch (Exception failure) { throw new AssertionError(failure); }
        });
        System.out.println("MainFrameSelectionTest passed: 16 button/key scenarios, unequal armies, order, mouse selection, exhaustion, new turns");
    }
}
