import java.util.function.IntSupplier;

/** Rules shared by Swing and headless battles; no UI or networking. */
public final class BattleRules {
    public static final int[] DX = {-22, 0, 22, -22, 0, 22};
    public static final int[] DY = {14, 28, 14, -14, -28, -14};
    private BattleRules() {}
    public static boolean cell(int x, int y) {
        if (x < 0 || x > 550 || x % 22 != 0) return false;
        int offset = (x / 22) % 2 == 0 ? 14 : 0;
        return y >= offset && y <= offset + 19 * 28 && (y - offset) % 28 == 0;
    }
    public static boolean canMove(Unit u, int direction) {
        return direction >= 0 && direction < 6 && u.TP > 0 && u.ZP[0] >= u.ZP[1]
            && cell(u.PosX + DX[direction], u.PosY + DY[direction]);
    }
    public static boolean move(Operations op, Unit[] units, int id, int direction) {
        Unit u = units[id];
        if (canMove(u, direction)) {
            int x = u.PosX + DX[direction], y = u.PosY + DY[direction];
            if (op.occupied(x, y, units) != -1) return false;
            u.PosX = x; u.PosY = y; u.ZP[0] -= u.ZP[1];
            return true;
        }
        return false;
    }
    public static boolean canFire(Operations op, Unit[] units, int id, int x, int y) {
        Unit u = units[id];
        return u.TP > 0 && cell(x, y) && u.ZP[0] >= u.ZP[2]
            && op.rwMod(op.distance(x, y, units, id), units, id) != 0;
    }
    /** Blind fire, friendly fire, splash, wrecks and health-dependent stats follow GUI rules. */
    public static boolean fire(Operations op, Unit[] units, int ignoredSplit, int id, int x, int y,
            IntSupplier percent) {
        if (!canFire(op, units, id, x, y)) return false;
        int distance = op.distance(x, y, units, id);
        int damage = op.damage(op.rwMod(distance, units, id), units, id, percent.getAsInt());
        units[id].ZP[0] -= units[id].ZP[2];
        int target = op.occupied(x, y, units);
        if (target != -1 && units[target].TP > 0) {
            units[target].TP -= op.ArmourMod(units, damage, id, target);
            op.DamMod(units, target);
        }
        // Preserve original update order, including self damage.
        if (units[id].RW[2] != 0) {
            int[] victims = op.AreaDamageMod(units, id, x, y);
            for (int i = 0; i < units.length; i++) {
                if (victims[i] != 0 && units[i].TP > 0) {
                    int splash = (int) (damage * Math.pow(units[id].AreaDamageMod, victims[i]));
                    units[i].TP -= op.ArmourMod(units, splash, id, i);
                    op.DamMod(units, i);
                }
            }
        }
        return true;
    }
}
