# Python / reinforcement-learning interface (protocol version 1)

LINAS can run complete battles in a persistent Java process without creating Swing
windows or connecting two copies of the multiplayer game. Python controls both
players and can choose a different policy for each. Movement and attack execution
are shared with `MainFrame` through `BattleRules`; unit loading, distance, damage,
armour, health-dependent stats, visibility and point refresh use `Operations`.
The normal GUI remains the default when starting `Linas` without arguments.

## Build and run

Requires a JDK 17+ and Python 3.10+. No external Java or Python libraries are needed.
From the repository root in PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build-rl.ps1
python examples/python/linas_client.py
```

The build script finds a JDK through `JAVA_HOME`, PATH, or IntelliJ's `%USERPROFILE%/.jdks`.
You can specify `-JdkHome "C:\path\to\jdk"`. It compiles to `dist/rl/classes`.
The Python example launches Java on an ephemeral local port, plays two automated
matches and prints their results, then stops its Java process. It checks for a
Java 17+ runtime, including IntelliJ's JDKs, so an old Java 8 on PATH is skipped.
Use `--java "C:\path\to\jdk\bin\java.exe"` to select the runtime explicitly.
`--episodes`, `--seed` and `--classpath` are also available.

Alternatively start Java yourself (using a Java 17+ executable):

```powershell
java -cp dist/rl/classes Linas --rl-server 8765
python examples/python/linas_client.py --port 8765
```

The default port is 8765; port 0 requests an ephemeral port. On readiness the server
prints exactly one UTF-8 JSON line on stdout, then flushes it:

```json
{"event":"ready","version":1,"host":"127.0.0.1","port":8765}
```

Diagnostics go to stderr. The server binds only to IPv4 loopback. A connection owns
one environment and can reset it repeatedly. It serves one connection at a time;
closing/disconnecting discards that match and allows a fresh connection. Use one
Java process per worker for parallel training. The example's `launch_server`
context manager handles startup, readiness, ephemeral ports and process cleanup.

## Wire format

Send one complete JSON object per line over TCP, encoded as UTF-8 and terminated
with LF (`\n`). Responses also end with LF; read complete lines rather than
assuming one socket read equals one response. CRLF requests are accepted.
Each request is limited to 65,536 characters and JSON nesting depth 32. Send
requests sequentially, waiting for each response. Oversized or incomplete lines
close the connection. Other malformed requests return errors; the connection
remains usable.

```json
{"version":1,"id":1,"method":"reset","params":{"seed":42,"config":{"fog":false}}}
```

A successful response has `id`, `version`, `ok:true` and `result`. An error has
`ok:false` and an `error` object instead:

```json
{"id":2,"version":1,"ok":false,"error":{"code":"invalid_request","message":"Call reset first"}}
```

`id` is echoed and can be an integer, string or null; it is optional. The optional
`version` defaults to 1, and `params` defaults to `{}`. Unknown methods/fields,
incorrect types, unsupported versions and invalid actions are rejected. Error
codes are `invalid_request` and `io_error`. A rejected action does not advance the
state or consume randomness. A rejected reset preserves the previous match.
Seeds are signed 64-bit integers; use Python integers to preserve precision.

## Methods

| Method | Parameters | Result |
| --- | --- | --- |
| `ping` | None | Name and protocol version |
| `catalog` | None | Available unit types and their initial stats |
| `reset` | Optional `seed` (default 0), optional `config` | Current view (observation, actions, result) |
| `observe` | `player`: 0 or 1 | That player's observation |
| `legalActions` | None | Actions for the active player |
| `step` | `action`: one action object | Updated view, `actor` and `rewards` |
| `matchResult` | None | Termination flags, winner, reason, counters and payoffs |
| `close` | None | `{"closed":true}`, then connection closes |

Except `ping`, `catalog`, `reset` and `close`, methods require a successful reset.
Neither observation nor querying actions changes the match or its random stream.
Calling `step` after a match ends is an error; actions are then an empty list.

### Match configuration

```json
{
  "armies": [["Tank/Tank.txt", "Soldier/AT.txt"], ["Tank/Tank.txt", "Jeep/Jeep.txt"]],
  "positions": [[[220,294], [198,308]], [[308,294], [330,308]]],
  "fog": false,
  "maxTurns": 200,
  "maxSteps": 5000
}
```

Every field is optional. Each army has 1..32 units, with repetitions allowed.
`unitType` identifiers come from `catalog` and are paths relative to `Units` using
forward slashes. Arbitrary paths outside the catalogue cannot be loaded.
The default armies each contain one Tank, Jeep and AT team.

Positions, when supplied, must match both army lengths and contain distinct valid
board cells. They allow controlled scenarios anywhere on the board. When omitted,
the seeded generator places player 0 in columns 0..3 and player 1 in columns 22..25.
The default `fog` is false for initial fully observed experiments.
`maxTurns` is 1..10,000; `maxSteps` is 1..1,000,000.

Player 0 acts first. A turn is one player's entire turn, not a pair of player turns.
The counter begins at 1 and advances on `end_turn`. Points refresh through the same
`Operations.newRound` as the GUI (all units refresh at each turn transition).
Every accepted action, including `end_turn`, increments `steps`. Reaching a limit
truncates the episode with a draw. Elimination on that action takes precedence.
The same seed, configuration, unit files and action sequence reproduce placements
and damage rolls. Different seeds may occasionally produce the same outcome.

### Observations and coordinates

Observations contain `player`, `currentPlayer`, `turn`, `steps`, `fog`, `maxTurns`,
`maxSteps`, `board`, `units`, `terminated` and `truncated`.
The board has 26 columns and 20 rows. Coordinates are the original game's cell
positions: `x = column * 22`, `y = row * 28 + (14 if column is even else 0)`.
These are cell origins, not mouse-click coordinates or axial hex coordinates.

Each unit exposes a stable match-local `id`, owner (`player`), `unitType`, name,
`x`, `y`, `alive`, `onBoard`, HP, action points, costs, damage, range modifiers,
armour multipliers/types, sight, camouflage and splash properties. `minRange` and
`maxRange` have their usual meanings, despite some reversed legacy comments.
IDs are not renumbered when a unit dies. `onBoard:false` describes removed wrecks;
the legacy rules move nonblocking destroyed units off their cell by one pixel.

With fog enabled, each observation includes own units and only currently visible
enemies/wrecks, using the same visibility calculation as the GUI. No hidden-unit
positions, HP or global enemy counts are included. Memory of previously seen units
belongs in the agent. `observe(player)` supports both viewpoints because the
controller manages both agents; it must route each policy only its own observation.

### Actions

Select an acting unit directly; keyboard focus and the GUI's Next button are not
part of the learning task. A step does not automatically end the player's turn.

```json
{"type":"move","unit":0,"direction":2}
```

| Direction | Meaning | Coordinate change |
| --- | --- | --- |
| 0 | Down-left | -22, +14 |
| 1 | Down | 0, +28 |
| 2 | Down-right | +22, +14 |
| 3 | Up-left | -22, -14 |
| 4 | Up | 0, -28 |
| 5 | Up-right | +22, -14 |

```json
{"type":"attack","unit":0,"x":308,"y":294}
```

Attacks target cells. Blind fire, empty cells and friendly fire are supported,
including splash damage to either army. Range, armour, AP costs, damage stages
and blocking wrecks follow the shared rules. The legal-action list includes all
in-range target cells, avoiding information leaks through a hidden-target filter.

```json
{"type":"end_turn"}
```

Only the active player's living units can act. Actions with missing/extra fields,
insufficient points, invalid cells/directions, known occupied destinations or
unsupported ranges are rejected. For fog, a move into an unseen occupied cell
remains in the action list: attempting it is a no-op, costs no AP (as in the GUI),
and consumes a step. This prevents the mask revealing unseen occupancy and the
step limit bounds repeated blocked attempts.

The legal-action list is a dynamic action mask, not a fixed-size network output.
A trainer can score its entries or map them to a fixed action space. Avoid treating
list indices as permanent action identities across successive steps.

### Results, rewards and self-play

Every `reset`/`step` returns `observation` for the **current active player**,
`legalActions`, and `result`. `step` also returns `actor` (the player who executed
that action) and `rewards`, a two-element array indexed by player. After `end_turn`,
the returned observation normally belongs to the other player. On a terminal
attack it belongs to the attacker; check the flags before selecting another action.

`result` contains `terminated`, `truncated`, `winner` (0, 1 or null), `reason`,
`turn`, `steps` and `payoffs`. Elimination gives the winner +1 and loser -1;
intermediate actions give both zero. Draws give both zero. Reasons are `elimination`,
`mutual_elimination`, `turn_limit`, or `step_limit`; ongoing reason/winner are null.
Mutual elimination through friendly/splash damage is a draw. `payoffs` describes
the match result; querying it repeatedly does not create new reward events.

Route decisions by `currentPlayer`, and keep experience separately for both
policies. Terminal rewards apply to both players, including the one who did not
execute the final action. This is an alternating-player interface, not a drop-in
single-agent Gym environment. A later wrapper can run the opponent internally.

## Importing the client

With `examples/python` on your Python module path:

```python
from linas_client import LinasClient, launch_server

with launch_server("dist/rl/classes") as port:
    with LinasClient(port=port) as game:
        state = game.reset(seed=42, config={"maxTurns": 20})
        while not (state["result"]["terminated"] or state["result"]["truncated"]):
            player = state["observation"]["currentPlayer"]
            # Replace this with policies[player](observation, legal_actions).
            action = state["legalActions"][-1]  # end_turn, for a minimal example
            state = game.step(action)
        print(game.match_result())
```

`examples/python/linas_client.py` includes a more useful complete demonstration
that moves toward enemies and attacks. It exercises both sides without training
or requiring a neural-network framework. Network/model training, army purchasing,
interactive deployment and graphical replays are future additions; initial armies
and positions are supplied through reset configuration.

## Verification

After building, run the TCP/Python integration tests:

```powershell
python tests/test_rl_protocol.py
```

`tests/HeadlessBattleTest.java` additionally checks all 3,120 cell/direction boundary
cases against the previous GUI conditions, AP costs, collision, direct/splash
damage, damage stages, wrecks and JSON parsing. Compile it against the generated
classes and run with `-Djava.awt.headless=true`.
