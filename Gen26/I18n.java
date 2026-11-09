import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;

/** Local presentation preference; never changes network commands or unit identifiers. */
public final class I18n {
    private static boolean german;
    private static final Map<String, String> DE = new HashMap<>();
    static {
        DE.put("Internet IP:", "Internet-IP:");
        DE.put("Retry", "Erneut");
        DE.put("Looking up public IP...", "Ermittle Internet-IP...");
        DE.put("Unavailable - check internet and retry", "Nicht verfuegbar - Internet pruefen");
        DE.put("Share this IP with your opponent. Forward TCP port 1111.", "Diese IP weitergeben. TCP-Port 1111 im Router freigeben.");
        DE.put("Public IPv4 provided by api.ipify.org. Select the address to copy it.", "Oeffentliche IPv4 von api.ipify.org. Adresse zum Kopieren markieren.");
        DE.put("Create Game", "Spiel erstellen");
        DE.put("Join Game", "Spiel beitreten");
        DE.put("Keypad Configuration", "Tastenbelegung");
        DE.put("Exit", "Beenden");
        DE.put("Please enter your name.", "Bitte gib deinen Namen ein.");
        DE.put("Please enter the server IP.", "Bitte gib die Server-IP ein.");
        DE.put("Fog of war", "Kriegsnebel");
        DE.put("Transfer own units", "Eigene Einheiten senden");
        DE.put("Transfer own pictures", "Eigene Bilder senden");
        DE.put("Accept", "Bestätigen");
        DE.put("Add", "Hinzufügen");
        DE.put("Credits available", "Verfügbare Credits");
        DE.put("Units for this game:", "Einheiten für dieses Spiel:");
        DE.put("Waiting", "Warten");
        DE.put("Start", "Starten");
        DE.put("Unit files or folders", "Einheitendateien oder Ordner");
        DE.put("Cannot read unit file: ", "Einheitendatei nicht lesbar: ");
        DE.put("Please enter a number for the available credits.", "Bitte gib eine Zahl für die verfügbaren Credits ein.");
        DE.put("\nConection created\n", "\nVerbindung hergestellt\n");
        DE.put("Do you want to quit the game?", "Möchtest du das Spiel beenden?");
        DE.put("Cost:", "Kosten:");
        DE.put("Hitpoints:", "Trefferpunkte:");
        DE.put("Armour for ", "Panzerung gegen ");
        DE.put("Maximum Damage:", "Maximaler Schaden:");
        DE.put("Damage type:", "Schadenstyp:");
        DE.put("Factor for area damage:", "Flächenschadensfaktor:");
        DE.put("range:", "Reichweite:");
        DE.put("Area damage radius:", "Radius des Flächenschadens:");
        DE.put("Sight range:", "Sichtweite:");
        DE.put("Camouflage:", "Tarnung:");
        DE.put("Points per round:", "Zugpunkte pro Runde:");
        DE.put("Points per move:", "Zugpunkte pro Feld:");
        DE.put("Points per attack:", "Zugpunkte pro Angriff:");
        DE.put("Obstacle after death?", "Hindernis nach Zerstörung?");
        DE.put("Back", "Zurück");
        DE.put("Linas descriptions", "Linas Beschreibungen");
        DE.put("Yes", "Ja");
        DE.put("No", "Nein");
        DE.put("Linas preparations", "Linas Vorbereitungen");
        DE.put("Select units for battle!", "Du musst Einheiten für den Kampf auswählen!");
        DE.put("Waiting for client", "Warte auf Client");
        DE.put("Waiting for host", "Warte auf Host");
        DE.put("Start game", "Spiel beginnen");
        DE.put(" Credits", " Credits");
        DE.put("Player:", "Spieler:");
        DE.put("Unit:", "Einheit:");
        DE.put("Available units:", "Verfügbare Einheiten:");
        DE.put("End game", "Spiel beenden");
        DE.put("Next", "Weiter");
        DE.put("Linas deployment", "Linas Einheiten setzen");
        DE.put("Ready", "Fertig");
        DE.put("Please place units on empty spaces.", "Bitte auf den freien Flächen setzen.");
        DE.put("Waiting for host...", "Warte auf Host...");
        DE.put("Selected enemy", "Markierter Gegner");
        DE.put("Firepower:", "Feuerkraft:");
        DE.put("Range:", "Reichweite:");
        DE.put("Action points:", "Zugpunkte:");
        DE.put("Points/move", "Zugpunkte/Feld");
        DE.put("Points/shot", "Zugpunkte/Schuss");
        DE.put("HP:", "TP:");
        DE.put("End turn", "Zug beenden");
        DE.put("Waiting for client...", "Warte auf Client...");
        DE.put("Waiting for server", "Warte auf Server");
        DE.put("You are victorious!", "Du hast gewonnen!");
        DE.put("You have been defeated!", "Du wurdest besiegt!");
        DE.put("The enemy runs away!", "Der Gegner flieht!");
        DE.put("Error", "Fehler");
        DE.put("Input", "Eingabe");
        DE.put("Confirmation", "Bestätigung");
        DE.put("Information", "Information");
        DE.put("Selected enemy (", "Markierter Gegner (");
    }
    private I18n() {}
    public static boolean isGerman() { return german; }
    public static void setGerman(boolean value) {
        german = value;
        UIManager.put("OptionPane.yesButtonText", text("Yes"));
        UIManager.put("OptionPane.noButtonText", text("No"));
        UIManager.put("OptionPane.cancelButtonText", german ? "Abbrechen" : "Cancel");
    }
    public static String text(String english) {
        return german ? DE.getOrDefault(english, english) : english;
    }
    public static String tooltip(String description) {
        if (description == null || description.trim().isEmpty()) return null;
        return "<html><body style='width: 340px'>" + description.replace("&", "&amp;")
            .replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") + "</body></html>";
    }
}
