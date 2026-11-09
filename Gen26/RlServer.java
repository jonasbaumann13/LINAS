import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Persistent loopback-only JSON-lines server. A new connection owns a fresh environment. */
public final class RlServer {
    public static final int VERSION = 1;
    private RlServer() {}
    public static void run(int port) throws IOException {
        System.setProperty("java.awt.headless", "true");
        // Validate assets before reporting readiness.
        HeadlessBattle.catalogue();
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
            System.out.println(RlJson.encode(RlJson.object("event", "ready", "version", VERSION,
                "host", "127.0.0.1", "port", server.getLocalPort())));
            System.out.flush();
            while (true) {
                try (Socket socket = server.accept()) {
                    socket.setTcpNoDelay(true);
                    serve(socket);
                } catch (IOException e) { System.err.println("RL client disconnected: " + e.getMessage()); }
            }
        }
    }
    private static String readLine(Reader reader) throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\n') return line.toString();
            if (line.length() >= 65536) throw new IOException("Request exceeds 65536 characters");
            line.append((char) c);
        }
        if (line.length() != 0) throw new IOException("Incomplete JSON line");
        return null;
    }
    private static void fields(Map<String, Object> values, String... allowed) {
        if (!Set.of(allowed).containsAll(values.keySet())) throw new IllegalArgumentException("Unexpected parameter");
    }
    private static void serve(Socket socket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        HeadlessBattle battle = null;
        String line;
        while ((line = readLine(input)) != null) {
            Object id = null;
            boolean close = false;
            Map<String, Object> response;
            try {
                Map<String, Object> request = RlJson.asObject(RlJson.parse(line));
                id = request.get("id");
                if (id != null && !(id instanceof String || id instanceof Long))
                    throw new IllegalArgumentException("id must be a string, integer or null");
                fields(request, "id", "version", "method", "params");
                if (RlJson.integer(request.getOrDefault("version", VERSION)) != VERSION)
                    throw new IllegalArgumentException("Unsupported protocol version");
                String method = RlJson.string(request.get("method"));
                Map<String, Object> params = RlJson.asObject(request.getOrDefault("params", RlJson.object()));
                Object result;
                switch (method) {
                    case "ping":
                        fields(params);
                        result = RlJson.object("name", "LINAS", "version", VERSION); break;
                    case "catalog":
                        fields(params);
                        result = HeadlessBattle.catalogue(); break;
                    case "reset":
                        fields(params, "seed", "config");
                        // Construct first: a failed reset leaves the current match intact.
                        HeadlessBattle replacement = new HeadlessBattle(RlJson.integer(params.getOrDefault("seed", 0)),
                            RlJson.asObject(params.getOrDefault("config", RlJson.object())));
                        battle = replacement;
                        result = battle.view(); break;
                    case "observe":
                        fields(params, "player");
                        requireBattle(battle);
                        result = battle.observe(RlJson.bounded(params.get("player"), 0, 1)); break;
                    case "legalActions":
                        fields(params);
                        requireBattle(battle);
                        result = battle.legalActions(); break;
                    case "step":
                        fields(params, "action");
                        requireBattle(battle);
                        result = battle.step(RlJson.asObject(params.get("action"))); break;
                    case "matchResult":
                        fields(params);
                        requireBattle(battle);
                        result = battle.result(); break;
                    case "close":
                        fields(params);
                        result = RlJson.object("closed", true); close = true; break;
                    default: throw new IllegalArgumentException("Unknown method: " + method);
                }
                response = RlJson.object("id", id, "version", VERSION, "ok", true, "result", result);
            } catch (IllegalArgumentException | IllegalStateException e) {
                response = RlJson.object("id", id, "version", VERSION, "ok", false, "error",
                    RlJson.object("code", "invalid_request", "message", e.getMessage()));
            } catch (IOException e) {
                response = RlJson.object("id", id, "version", VERSION, "ok", false, "error",
                    RlJson.object("code", "io_error", "message", e.getMessage()));
            }
            output.write(RlJson.encode(response)); output.write('\n'); output.flush();
            if (close) return;
        }
    }
    private static void requireBattle(HeadlessBattle battle) {
        if (battle == null) throw new IllegalArgumentException("Call reset first");
    }
}
