import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Exercises turn selection with incoming multiplayer state, including an eliminated army. */
public final class MainFrameTurnTest {
    private MainFrameTurnTest() {}
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static Unit[] army() {
        Unit[] units = new Unit[4];
        for (int i = 0; i < units.length; i++) {
            units[i] = new Operations().setUnit(new File(AppPaths.units(), "Tank/Tank.txt"),
                i < 2 ? "client" : "host", i >= 2);
            units[i].PosX = 220;
            units[i].PosY = 14 + i * 28;
        }
        return units;
    }
    private static void turns() throws Exception {
        for (boolean host : new boolean[] {false, true}) {
            try (ObjectOutputStream output = new ObjectOutputStream(new ByteArrayOutputStream())) {
                MainFrame frame = new MainFrame(host, output, army());
                try {
                    Unit[] incoming = army();
                    int first = host ? 2 : 0;
                    incoming[first].TP = 0;
                    frame.startTurn(incoming);
                    check(frame.UnitArray == incoming, "Use incoming state before selecting a unit");
                    check(frame.UC == first + 1, "Skip destroyed units of the active player");
                    check(!frame.wait && frame.JBA[1].isEnabled(), "Living army can take its turn");

                    incoming[first + 1].TP = 0;
                    Timer dismiss = new Timer(100, ignored -> {
                        for (Window window : Window.getWindows())
                            if (window instanceof JDialog) window.dispose();
                    });
                    dismiss.start();
                    try {
                        frame.startTurn(incoming);
                        check(frame.end, "Eliminated army ends the game without indexing past its units");
                        check(frame.wait && !frame.JBA[1].isEnabled(), "Eliminated army cannot take further actions");
                    } finally { dismiss.stop(); }
                } finally { frame.dispose(); }
            }
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try { turns(); }
            catch (Exception failure) { throw new AssertionError(failure); }
        });
        System.out.println("MainFrameTurnTest passed: incoming state, destroyed units, eliminated armies");
    }
}
