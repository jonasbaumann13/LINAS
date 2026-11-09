import java.io.File;
import java.util.*;

/** Run with -Djava.awt.headless=true. Exercises the rules also used by Swing. */
public class HeadlessBattleTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static Unit load(String name) {
        return new Operations().setUnit(new File(AppPaths.units(), name), "test", false);
    }
    private static void movement() {
        Operations op = new Operations();
        Unit unit = load("Tank/Tank.txt");
        Unit[] units = {unit};
        for (int col = 0; col < 26; col++) for (int row = 0; row < 20; row++) {
            int x = col * 22, y = row * 28 + (col % 2 == 0 ? 14 : 0);
            for (int d = 0; d < 6; d++) {
                unit.PosX = x; unit.PosY = y; unit.ZP[0] = 10;
                // Original GUI boundary conditions, independent of the extracted cell validation.
                boolean blocked = switch (d) {
                    case 0 -> x < 5 || y > 540;
                    case 1 -> y > 520;
                    case 2 -> x > 530 || y > 540;
                    case 3 -> x < 5 || y < 10;
                    case 4 -> y < 20;
                    default -> x > 530 || y < 10;
                };
                check(BattleRules.move(op, units, 0, d) == !blocked, "Legacy boundary parity");
                check(unit.ZP[0] == (blocked ? 10 : 8), "Movement cost");
                check(unit.PosX == x + (blocked ? 0 : BattleRules.DX[d]), "Movement x");
                check(unit.PosY == y + (blocked ? 0 : BattleRules.DY[d]), "Movement y");
            }
        }
        unit.PosX = 220; unit.PosY = 294; unit.ZP[0] = 1;
        check(!BattleRules.move(op, units, 0, 1), "Insufficient action points");
        Unit blocker = load("Tank/Tank.txt"); blocker.PosX = 220; blocker.PosY = 322;
        unit.ZP[0] = 10;
        check(!BattleRules.move(op, new Unit[] {unit, blocker}, 0, 1), "Occupied cell");
    }
    private static void combat() {
        Operations op = new Operations();
        Unit attacker = load("Tank/Tank.txt"), target = load("Tank/Tank.txt");
        attacker.PosX = 220; attacker.PosY = 294; target.PosX = 220; target.PosY = 322;
        Unit[] units = {attacker, target};
        check(BattleRules.fire(op, units, 1, 0, 220, 322, () -> 99), "Legal shot");
        check(attacker.ZP[0] == 6, "Attack costs four points");
        check(target.TP == -25 && target.ZP[1] == 99, "Armour and destroyed unit stats");
        check(target.PosX == 220 && target.PosY == 322, "Tank leaves blocking wreck");
        attacker = load("Artillery/Artillery.txt");
        Unit direct = load("Tank/Tank.txt"), splash = load("Tank/Tank.txt");
        attacker.PosX = 220; attacker.PosY = 294;
        direct.PosX = 220; direct.PosY = 378; // Distance 3, range modifier 4.
        splash.PosX = 220; splash.PosY = 406;
        check(BattleRules.fire(op, new Unit[] {attacker, direct, splash}, 1, 0, 220, 378, () -> 99), "Artillery shot");
        check(direct.TP == 63 && splash.TP == 94, "Direct and splash armour modifiers");
        check(direct.ZP[1] == 4, "Damage changes movement cost");
        int points = attacker.ZP[0];
        check(!BattleRules.fire(op, new Unit[] {attacker}, 1, 0, 220, 322, () -> 99), "Minimum range or AP rejection");
        check(attacker.ZP[0] == points, "Rejected fire preserves points");
    }
    private static void cloning() throws Exception {
        for (var entry : HeadlessBattle.unitFiles().values()) {
            Unit original = new Operations().setUnit(entry.toFile(), "test", false);
            Unit copy = (Unit) original.clone();
            for (var field : Unit.class.getFields()) {
                Object source = field.get(original), cloned = field.get(copy);
                if (source == null || !source.getClass().isArray()) continue;
                check(source != cloned, "Independent cloned array: " + field.getName());
                check(Objects.deepEquals(source, cloned), "Cloned array contents: " + field.getName());
                if (source.getClass().getComponentType().isArray()) {
                    for (int row = 0; row < java.lang.reflect.Array.getLength(source); row++)
                        check(java.lang.reflect.Array.get(source, row) != java.lang.reflect.Array.get(cloned, row),
                            "Independent cloned row: " + field.getName());
                }
            }
        }
        check(java.io.ObjectStreamClass.lookup(Unit.class).getSerialVersionUID() == 8456601742033046694L,
            "Preserve existing multiplayer serialization ID");
    }
    private static void mutualElimination() throws Exception {
        HeadlessBattle battle = new HeadlessBattle(1, Map.of());
        // Arrange the post-combat state where both armies were destroyed in one action.
        var field = HeadlessBattle.class.getDeclaredField("units");
        field.setAccessible(true);
        for (Unit unit : (Unit[]) field.get(battle)) unit.TP = 0;
        battle.step(Map.of("type", "end_turn"));
        Map<String, Object> result = battle.result();
        check(Boolean.TRUE.equals(result.get("terminated")), "Mutual elimination ends the match");
        check(result.get("winner") == null, "Mutual elimination has no winner");
        check(result.get("reason").equals("mutual_elimination"), "Mutual elimination reason");
        check(result.get("payoffs").equals(List.of(0, 0)), "Mutual elimination is a draw");
    }
    private static void json() {
        String source = "{\"a\":[1,true,null,\"line\\n\\u00e4\"],\"b\":-23}";
        check(RlJson.encode(RlJson.parse(source)).contains("line"), "JSON round trip");
        for (String invalid : List.of("{", "[1,]", "01", "1e9999", "{\"x\":1,\"x\":2}", "true false")) {
            try { RlJson.parse(invalid); throw new AssertionError("Accepted invalid JSON: " + invalid); }
            catch (IllegalArgumentException expected) { }
        }
        check(RlJson.integer(RlJson.parse("9223372036854775807")) == Long.MAX_VALUE, "Exact 64-bit seeds");
    }
    public static void main(String[] args) throws Exception {
        movement(); combat(); cloning(); mutualElimination(); json();
        System.out.println("HeadlessBattleTest passed: 3120 movement cases, combat, damage stages, splash, cloning, mutual elimination, JSON");
    }
}
