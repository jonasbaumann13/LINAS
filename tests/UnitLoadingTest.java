import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/** Run from the repository root; does not open dialogs or create a network connection. */
public class UnitLoadingTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static class ApprovedChooser extends JFileChooser {
        private final File selection;

        ApprovedChooser(File selection) {
            this.selection = selection;
        }

        @Override
        public int showOpenDialog(Component parent) {
            setSelectedFile(selection);
            return APPROVE_OPTION;
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (boolean german : new boolean[] {false, true}) {
                I18n.setGerman(german);
                File unitsFolder = new File("Gen26/Units").getAbsoluteFile();
                Gameoptions options = new Gameoptions("Test", true, 0, 0);
                try {
                    options.FCUnits = new ApprovedChooser(unitsFolder);
                    options.addFile();
                    check(options.SFile.size() == 6, "Load all six units; German=" + german);
                    check(options.LiMUnits.size() == 6, "Show all six units");
                    check(options.FCUnits.getCurrentDirectory().equals(unitsFolder.getParentFile()),
                        "Remember the selected folder's parent");
                    Unit[] units = options.Op.createBspUA(options.SFile);
                    for (Unit unit : units) {
                        check(unit.type != null, "Parse unit data");
                        check(unit.getDescription() != null && !unit.getDescription().isBlank(),
                            "Load localized description");
                    }
                    options.addFile();
                    check(options.SFile.size() == 6, "Do not add duplicate unit files");
                } finally {
                    options.dispose();
                }

                File tank = new File(unitsFolder, "Tank/Tank.txt");
                options = new Gameoptions("Test", true, 0, 0);
                try {
                    options.FCUnits = new ApprovedChooser(tank);
                    options.addFile();
                    check(options.SFile.size() == 1, "Load individual unit file");
                    check(options.FCUnits.getCurrentDirectory().equals(tank.getParentFile()),
                        "Remember selected file's containing folder");
                } finally {
                    options.dispose();
                }
            }
        });
        System.out.println("PASS: English/German folder and file loading, descriptions, duplicates, and remembered parent folder.");
    }
}
