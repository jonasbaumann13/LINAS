"""LINAS JSON-lines client and two-bot demo. Python 3.10+, standard library only."""
from __future__ import annotations

import argparse
from contextlib import contextmanager
import json
import os
from pathlib import Path
import queue
import re
import random
import shutil
import socket
import subprocess
import threading

ROOT = Path(__file__).resolve().parents[2]


class LinasError(RuntimeError):
    """The server rejected a request without advancing the match."""


class LinasClient:
    def __init__(self, host="127.0.0.1", port=8765, timeout=30):
        self.socket = socket.create_connection((host, port), timeout=timeout)
        self.stream = self.socket.makefile("rwb")
        self._id = 0

    def call(self, method, **params):
        self._id += 1
        request = {"version": 1, "id": self._id, "method": method, "params": params}
        self.stream.write((json.dumps(request, allow_nan=False) + "\n").encode("utf-8"))
        self.stream.flush()
        line = self.stream.readline(16 * 1024 * 1024)
        if not line or not line.endswith(b"\n"):
            raise ConnectionError("Missing or oversized LINAS response")
        response = json.loads(line)
        if response.get("version") != 1 or response.get("id") != self._id:
            raise ConnectionError("Unexpected LINAS response id/version")
        if not response["ok"]:
            error = response["error"]
            raise LinasError(f"{error['code']}: {error['message']}")
        return response["result"]

    def reset(self, seed=0, config=None):
        return self.call("reset", seed=seed, config=config or {})

    def observe(self, player):
        return self.call("observe", player=player)

    def legal_actions(self):
        return self.call("legalActions")

    def step(self, action):
        return self.call("step", action=action)

    def match_result(self):
        return self.call("matchResult")

    def catalog(self):
        return self.call("catalog")

    def close(self):
        if self.stream.closed:
            return
        try:
            self.call("close")
        except (OSError, ValueError, LinasError):
            pass
        finally:
            self.stream.close()
            self.socket.close()

    def __enter__(self):
        return self

    def __exit__(self, *_):
        self.close()


def find_java(explicit=None):
    if explicit:
        return explicit
    executable = "java.exe" if os.name == "nt" else "java"
    candidates = []
    if os.environ.get("JAVA_HOME"):
        candidates.append(Path(os.environ["JAVA_HOME"]) / "bin" / executable)
    if found := shutil.which(executable):
        candidates.append(Path(found))
    candidates.extend(sorted((Path.home() / ".jdks").glob(f"*/bin/{executable}"), reverse=True))
    for candidate in candidates:
        if not candidate.is_file():
            continue
        version = subprocess.run([str(candidate), "-version"], capture_output=True, text=True,
                                 creationflags=subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0)
        match = re.search(r'version "(\d+)', version.stderr + version.stdout)
        if version.returncode == 0 and match and int(match.group(1)) >= 17:
            return str(candidate)
    raise FileNotFoundError("Java 17+ required; set JAVA_HOME or pass --java")


@contextmanager
def launch_server(classpath, java=None):
    """Own one Java worker; an ephemeral port avoids conflicts with other workers."""
    process = subprocess.Popen(
        [find_java(java), "-Djava.awt.headless=true", "-cp", str(Path(classpath).resolve()),
         "Linas", "--rl-server", "0"],
        cwd=ROOT, stdout=subprocess.PIPE, text=True, encoding="utf-8",
        creationflags=subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0,
    )
    ready = queue.Queue()
    threading.Thread(target=lambda: ready.put(process.stdout.readline()), daemon=True).start()
    try:
        try:
            line = ready.get(timeout=30)
        except queue.Empty as error:
            raise RuntimeError("LINAS server did not start within 30 seconds") from error
        if not line:
            raise RuntimeError("LINAS server exited before readiness; check compiled classes and Java")
        info = json.loads(line)
        if info.get("event") != "ready" or info.get("version") != 1:
            raise RuntimeError(f"Unexpected server greeting: {info}")
        yield info["port"]
    finally:
        if process.poll() is None:
            process.terminate()
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
        process.stdout.close()


def choose_action(observation, actions, rng):
    """Tiny demonstration policy. Replace this function with a neural policy later."""
    enemies = [u for u in observation["units"]
               if u["player"] != observation["currentPlayer"] and u["alive"]]
    positions = {(u["x"], u["y"]) for u in enemies}
    attacks = [a for a in actions if a["type"] == "attack" and (a["x"], a["y"]) in positions]
    if attacks:
        return rng.choice(attacks)
    moves = [a for a in actions if a["type"] == "move"]
    if not moves:
        return {"type": "end_turn"}
    units = {u["id"]: u for u in observation["units"]}
    offsets = [(-22, 14), (0, 28), (22, 14), (-22, -14), (0, -28), (22, -14)]
    targets = list(positions) or [(550 if observation["currentPlayer"] == 0 else 0, 280)]

    def score(action):
        unit = units[action["unit"]]
        dx, dy = offsets[action["direction"]]
        before = min(((unit["x"] - x) / 22) ** 2 + ((unit["y"] - y) / 28) ** 2 for x, y in targets)
        after = min(((unit["x"] + dx - x) / 22) ** 2 + ((unit["y"] + dy - y) / 28) ** 2 for x, y in targets)
        return after - before

    best = min(map(score, moves))
    return rng.choice([a for a in moves if score(a) == best])


def demo(port, episodes, seed):
    with LinasClient(port=port) as game:
        print(f"Connected: {game.call('ping')}; {len(game.catalog())} unit types")
        for episode in range(episodes):
            rng = random.Random(seed + episode)
            state = game.reset(seed=seed + episode, config={"maxTurns": 80, "maxSteps": 2000})
            while not (state["result"]["terminated"] or state["result"]["truncated"]):
                # currentPlayer chooses which policy to call in a self-play trainer.
                action = choose_action(state["observation"], state["legalActions"], rng)
                state = game.step(action)
            print(f"Episode {episode + 1}: {game.match_result()}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, help="Connect to an existing local server instead of launching one")
    parser.add_argument("--classpath", default=str(ROOT / "dist/rl/classes"))
    parser.add_argument("--java", help="Path to Java 17+ executable")
    parser.add_argument("--episodes", type=int, default=2)
    parser.add_argument("--seed", type=int, default=42)
    args = parser.parse_args()
    if args.port is not None:
        demo(args.port, args.episodes, args.seed)
    else:
        with launch_server(args.classpath, args.java) as port:
            demo(port, args.episodes, args.seed)


if __name__ == "__main__":
    main()
