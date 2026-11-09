"""Integration tests through real TCP and the shipped Python client; no third-party packages."""
import argparse
import json
from pathlib import Path
import random
import sys
import unittest

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "examples/python"))
from linas_client import LinasClient, LinasError, choose_action, launch_server

CLASSPATH = str(ROOT / "dist/rl/classes")
JAVA = None


class ProtocolTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = launch_server(CLASSPATH, JAVA)
        cls.port = cls.server.__enter__()

    @classmethod
    def tearDownClass(cls):
        cls.server.__exit__(None, None, None)

    def connect(self):
        return LinasClient(port=self.port)

    def test_seeded_complete_selfplay(self):
        with self.connect() as game:
            def episode():
                rng = random.Random(42)
                state = game.reset(seed=42)
                trace = [state]
                while not (state["result"]["terminated"] or state["result"]["truncated"]):
                    action = choose_action(state["observation"], state["legalActions"], rng)
                    state = game.step(action)
                    trace.append(state)
                return trace
            first, second = episode(), episode()
            self.assertEqual(first, second)
            result = game.match_result()
            self.assertTrue(result["terminated"])
            self.assertIn(result["winner"], [0, 1])
            self.assertEqual(sorted(first[-1]["rewards"]), [-1, 1])
            self.assertEqual(game.legal_actions(), [])
            with self.assertRaises(LinasError):
                game.step({"type": "end_turn"})

    def test_validation_and_limits(self):
        with self.connect() as game:
            with self.assertRaises(LinasError):
                game.observe(0)
            initial = game.reset(seed=123)
            before = game.observe(0)
            for action in [
                {"type": "move", "unit": 3, "direction": 0},
                {"type": "move", "unit": 0, "direction": 6},
                {"type": "attack", "unit": 0, "x": 1, "y": 1},
                {"type": "end_turn", "ignored": True},
                {"type": "move", "unit": 0, "direction": 1.0},
            ]:
                with self.assertRaises(LinasError):
                    game.step(action)
                self.assertEqual(game.observe(0), before)
            with self.assertRaises(LinasError):
                game.reset(config={"armies": [["../../bad.txt"], ["Tank/Tank.txt"]]})
            self.assertEqual(game.observe(0), before, "Failed reset is atomic")
            self.assertEqual(game.legal_actions(), initial["legalActions"])
            state = game.reset(config={"maxTurns": 1})
            state = game.step({"type": "end_turn"})
            self.assertTrue(state["result"]["truncated"])
            self.assertEqual(state["result"]["reason"], "turn_limit")
            self.assertEqual(state["rewards"], [0, 0])
            state = game.reset(config={"maxSteps": 1})
            state = game.step(state["legalActions"][0])
            self.assertEqual(state["result"]["reason"], "step_limit")

    def test_turns_observations_and_fog(self):
        with self.connect() as game:
            config = {"fog": True, "armies": [["Tank/Tank.txt"], ["Tank/Tank.txt"]],
                      "positions": [[[0, 14]], [[550, 532]]]}
            state = game.reset(seed=7, config=config)
            self.assertEqual([u["id"] for u in game.observe(0)["units"]], [0])
            self.assertEqual([u["id"] for u in game.observe(1)["units"]], [1])
            move = next(a for a in state["legalActions"] if a["type"] == "move")
            moved = game.step(move)
            self.assertEqual(moved["observation"]["units"][0]["actionPoints"], 8)
            switched = game.step({"type": "end_turn"})
            self.assertEqual(switched["actor"], 0)
            self.assertEqual(switched["observation"]["currentPlayer"], 1)
            self.assertEqual(game.observe(0)["units"][0]["actionPoints"], 10)
            self.assertTrue(all(a.get("unit", 1) == 1 for a in switched["legalActions"]))
            config["fog"] = False
            state = game.reset(config=config)
            self.assertEqual(len(state["observation"]["units"]), 2)

    def test_framing_errors_and_reconnect(self):
        with self.connect() as game:
            self.assertEqual(len(game.catalog()), 6)
            game.stream.write(b"{broken}\n")
            game.stream.flush()
            self.assertFalse(json.loads(game.stream.readline())["ok"])
            self.assertEqual(game.call("ping")["version"], 1)
            with self.assertRaises(LinasError):
                game.call("nonexistent")
            game.reset()
        with self.connect() as fresh:
            with self.assertRaises(LinasError):
                fresh.match_result()
            fresh.stream.write(b'{"id":100,"version":99,"method":"ping"}\n')
            fresh.stream.flush()
            error = json.loads(fresh.stream.readline())
            self.assertEqual(error["id"], 100)
            self.assertFalse(error["ok"])
            self.assertEqual(fresh.call("ping")["version"], 1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--classpath", default=str(ROOT / "dist/rl/classes"))
    parser.add_argument("--java")
    args, remaining = parser.parse_known_args()
    CLASSPATH, JAVA = args.classpath, args.java
    unittest.main(argv=[sys.argv[0], *remaining], verbosity=2)
