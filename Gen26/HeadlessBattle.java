import java.io.*;
import java.nio.file.*;
import java.util.*;

/** A single seeded battle. One controller can drive both players, without Swing or PvP sockets. */
public final class HeadlessBattle {
    private final Operations op = new Operations();
    private final Random random;
    private final Unit[] units;
    private final String[] types;
    private final int split, maxTurns, maxSteps;
    private final boolean fog;
    private int currentPlayer, turn = 1, steps;
    private boolean terminated, truncated;
    private Integer winner;
    private String reason;

    public static Map<String, Path> unitFiles() throws IOException {
        Path root = AppPaths.units().toPath();
        Map<String, Path> files = new TreeMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".txt")).toList())
                files.put(root.relativize(path).toString().replace('\\', '/'), path);
        }
        if (files.isEmpty()) throw new IOException("No unit definitions found");
        return files;
    }
    public static List<Object> catalogue() throws IOException {
        List<Object> result = new ArrayList<>();
        Operations op = new Operations();
        for (var file : unitFiles().entrySet()) {
            Unit unit = load(op, file.getValue(), 0);
            Map<String, Object> info = stats(unit);
            info.put("unitType", file.getKey());
            result.add(info);
        }
        return result;
    }
    private static Unit load(Operations op, Path file, int player) {
        Unit unit = op.setUnit(file.toFile(), "player" + player, player == 1);
        if (unit.type == null || unit.TP <= 0 || unit.ZP[1] <= 0 || unit.ZP[2] <= 0)
            throw new IllegalArgumentException("Invalid unit definition: " + file);
        return unit;
    }
    public HeadlessBattle(long seed, Map<String, Object> config) throws IOException {
        Set<String> allowed = Set.of("armies", "positions", "fog", "maxTurns", "maxSteps");
        if (!allowed.containsAll(config.keySet())) throw new IllegalArgumentException("Unknown match configuration field");
        random = new Random(seed);
        Map<String, Path> catalogue = unitFiles();
        maxTurns = RlJson.bounded(config.getOrDefault("maxTurns", 200), 1, 10000);
        maxSteps = RlJson.bounded(config.getOrDefault("maxSteps", 5000), 1, 1000000);
        Object fogValue = config.getOrDefault("fog", false);
        if (!(fogValue instanceof Boolean)) throw new IllegalArgumentException("fog must be boolean");
        fog = (Boolean) fogValue;
        List<?> armies = RlJson.asList(config.getOrDefault("armies", List.of(
            List.of("Tank/Tank.txt", "Jeep/Jeep.txt", "Soldier/AT.txt"),
            List.of("Tank/Tank.txt", "Jeep/Jeep.txt", "Soldier/AT.txt"))));
        if (armies.size() != 2) throw new IllegalArgumentException("armies must contain exactly two arrays");
        List<?> a = RlJson.asList(armies.get(0)), b = RlJson.asList(armies.get(1));
        if (a.isEmpty() || b.isEmpty() || a.size() > 32 || b.size() > 32)
            throw new IllegalArgumentException("Each army needs 1..32 units");
        split = a.size();
        units = new Unit[a.size() + b.size()];
        types = new String[units.length];
        List<?> positions = config.containsKey("positions") ? RlJson.asList(config.get("positions")) : null;
        if (positions != null && positions.size() != 2) throw new IllegalArgumentException("positions needs two arrays");
        Set<String> occupied = new HashSet<>();
        for (int player = 0; player < 2; player++) {
            List<?> army = player == 0 ? a : b;
            List<?> placements = positions == null ? null : RlJson.asList(positions.get(player));
            if (placements != null && placements.size() != army.size())
                throw new IllegalArgumentException("One position is required for each unit");
            List<int[]> cells = new ArrayList<>();
            for (int col = player == 0 ? 0 : 22; col < (player == 0 ? 4 : 26); col++)
                for (int row = 0; row < 20; row++) cells.add(new int[] {col * 22, row * 28 + (col % 2 == 0 ? 14 : 0)});
            Collections.shuffle(cells, random);
            for (int i = 0; i < army.size(); i++) {
                String type = RlJson.string(army.get(i));
                Path path = catalogue.get(type);
                if (path == null) throw new IllegalArgumentException("Unknown unitType: " + type);
                int id = (player == 0 ? 0 : split) + i;
                Unit unit = load(op, path, player);
                int x = cells.get(i)[0], y = cells.get(i)[1];
                if (placements != null) {
                    List<?> pair = RlJson.asList(placements.get(i));
                    if (pair.size() != 2) throw new IllegalArgumentException("Position must be [x,y]");
                    x = RlJson.bounded(pair.get(0), 0, 550);
                    y = RlJson.bounded(pair.get(1), 0, 546);
                }
                if (!BattleRules.cell(x, y) || !occupied.add(x + "," + y))
                    throw new IllegalArgumentException("Positions must be distinct board cells");
                unit.PosX = x; unit.PosY = y;
                units[id] = unit; types[id] = type;
            }
        }
    }
    private int owner(int id) { return id < split ? 0 : 1; }
    private boolean hidden(int id, int player) {
        return owner(id) != player && fog && !op.PointVisibility(units, split, player == 1,
            units[id].PosX, units[id].PosY, units[id].camouflage);
    }
    private static List<Integer> ints(int[] values) {
        List<Integer> list = new ArrayList<>();
        for (int v : values) list.add(v);
        return list;
    }
    private static Map<String, Object> stats(Unit u) {
        List<Double> armour = new ArrayList<>();
        for (double v : u.Armour) armour.add(v);
        return RlJson.object("name", u.type, "price", u.price, "hp", u.TP, "maxHp", u.basicTP,
            "actionPoints", u.ZP[0], "pointsPerTurn", u.basicZP, "moveCost", u.ZP[1], "attackCost", u.ZP[2],
            "damage", u.Dam, "damageType", u.DamType, "minRange", u.RW[0], "maxRange", u.RW[1],
            "splashRadius", u.RW[2], "splashFactor", u.AreaDamageMod, "rangeModifiers", ints(u.RWtype),
            "sight", u.sight, "camouflage", u.camouflage, "armourTypes", Arrays.asList(u.ArmourType),
            "armourMultipliers", armour, "leavesWreck", u.obstacleAD);
    }
    public Map<String, Object> observe(int player) {
        if (player < 0 || player > 1) throw new IllegalArgumentException("player must be 0 or 1");
        List<Object> seen = new ArrayList<>();
        for (int id = 0; id < units.length; id++) {
            if (hidden(id, player)) continue;
            Unit u = units[id];
            Map<String, Object> info = stats(u);
            info.putAll(RlJson.object("id", id, "player", owner(id), "unitType", types[id],
                "x", u.PosX, "y", u.PosY, "alive", u.TP > 0, "onBoard", BattleRules.cell(u.PosX, u.PosY)));
            seen.add(info);
        }
        return RlJson.object("player", player, "currentPlayer", currentPlayer, "turn", turn, "steps", steps,
            "fog", fog, "maxTurns", maxTurns, "maxSteps", maxSteps, "board", RlJson.object("columns", 26, "rows", 20), "units", seen,
            "terminated", terminated, "truncated", truncated);
    }
    private boolean knownMove(int id, int direction) {
        Unit u = units[id];
        if (!BattleRules.canMove(u, direction)) return false;
        int occupant = op.occupied(u.PosX + BattleRules.DX[direction], u.PosY + BattleRules.DY[direction], units);
        // A mask must not disclose an unseen enemy's position. A blocked attempt is a no-op.
        return occupant == -1 || hidden(occupant, currentPlayer);
    }
    public List<Map<String, Object>> legalActions() {
        List<Map<String, Object>> actions = new ArrayList<>();
        if (terminated || truncated) return actions;
        for (int id = 0; id < units.length; id++) {
            if (owner(id) != currentPlayer || units[id].TP <= 0) continue;
            for (int d = 0; d < 6; d++) if (knownMove(id, d))
                actions.add(RlJson.object("type", "move", "unit", id, "direction", d));
            // Empty cells and hidden targets are allowed, just like mouse-based blind fire.
            if (units[id].ZP[0] >= units[id].ZP[2]) {
                for (int col = 0; col < 26; col++) for (int row = 0; row < 20; row++) {
                    int x = col * 22, y = row * 28 + (col % 2 == 0 ? 14 : 0);
                    if (BattleRules.canFire(op, units, id, x, y))
                        actions.add(RlJson.object("type", "attack", "unit", id, "x", x, "y", y));
                }
            }
        }
        actions.add(RlJson.object("type", "end_turn"));
        return actions;
    }
    public Map<String, Object> result() {
        List<Integer> payoff = winner == null ? List.of(0, 0) : winner == 0 ? List.of(1, -1) : List.of(-1, 1);
        return RlJson.object("terminated", terminated, "truncated", truncated, "winner", winner,
            "reason", reason, "turn", turn, "steps", steps, "payoffs", payoff);
    }
    public Map<String, Object> view() {
        return RlJson.object("observation", observe(currentPlayer), "legalActions", legalActions(), "result", result());
    }
    public Map<String, Object> step(Map<String, Object> action) {
        if (terminated || truncated) throw new IllegalArgumentException("Match ended; reset before stepping");
        String type = RlJson.string(action.get("type"));
        int actor = currentPlayer;
        if (type.equals("end_turn")) {
            if (!action.keySet().equals(Set.of("type"))) throw new IllegalArgumentException("Unexpected action field");
            if (turn >= maxTurns) { truncated = true; reason = "turn_limit"; }
            else { currentPlayer = 1 - currentPlayer; turn++; op.newRound(units); }
        } else {
            Set<String> fields = type.equals("move") ? Set.of("type", "unit", "direction") : Set.of("type", "unit", "x", "y");
            if (!type.equals("move") && !type.equals("attack")) throw new IllegalArgumentException("Unknown action type");
            if (!action.keySet().equals(fields)) throw new IllegalArgumentException("Unexpected or missing action field");
            int id = RlJson.bounded(action.get("unit"), 0, units.length - 1);
            if (owner(id) != currentPlayer || units[id].TP <= 0) throw new IllegalArgumentException("Not a living unit of the active player");
            if (type.equals("move")) {
                int direction = RlJson.bounded(action.get("direction"), 0, 5);
                if (!knownMove(id, direction)) throw new IllegalArgumentException("Illegal move");
                BattleRules.move(op, units, id, direction);
            } else {
                int x = RlJson.bounded(action.get("x"), 0, 550), y = RlJson.bounded(action.get("y"), 0, 546);
                if (!BattleRules.canFire(op, units, id, x, y)) throw new IllegalArgumentException("Illegal attack");
                BattleRules.fire(op, units, split, id, x, y, () -> random.nextInt(100));
            }
        }
        steps++;
        boolean[] alive = new boolean[2];
        for (int id = 0; id < units.length; id++) if (units[id].TP > 0) alive[owner(id)] = true;
        if (!alive[0] || !alive[1]) {
            terminated = true; truncated = false;
            if (alive[0]) winner = 0;
            else if (alive[1]) winner = 1;
            else winner = null;
            reason = winner == null ? "mutual_elimination" : "elimination";
        } else if (!truncated && steps >= maxSteps) { truncated = true; reason = "step_limit"; }
        Map<String, Object> response = view();
        response.put("actor", actor);
        response.put("rewards", result().get("payoffs"));
        return response;
    }
}
