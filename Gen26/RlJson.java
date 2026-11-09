import java.util.*;

/** Dependency-free JSON codec for the bounded local RL protocol. */
public final class RlJson {
    private RlJson() {}
    public static Map<String, Object> object(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], pairs[i + 1]);
        return map;
    }
    public static Map<String, Object> asObject(Object value) {
        if (!(value instanceof Map<?, ?>)) throw new IllegalArgumentException("Expected JSON object");
        @SuppressWarnings("unchecked") Map<String, Object> result = (Map<String, Object>) value;
        return result;
    }
    public static List<?> asList(Object value) {
        if (!(value instanceof List<?> list)) throw new IllegalArgumentException("Expected JSON array");
        return list;
    }
    public static String string(Object value) {
        if (!(value instanceof String result)) throw new IllegalArgumentException("Expected string");
        return result;
    }
    public static long integer(Object value) {
        if (!(value instanceof Long || value instanceof Integer)) throw new IllegalArgumentException("Expected integer");
        return ((Number) value).longValue();
    }
    public static int bounded(Object value, int min, int max) {
        long result = integer(value);
        if (result < min || result > max) throw new IllegalArgumentException("Integer must be " + min + ".." + max);
        return (int) result;
    }
    public static Object parse(String source) {
        Parser parser = new Parser(source);
        Object value = parser.value(0);
        parser.space();
        if (parser.index != source.length()) throw parser.error();
        return value;
    }
    public static String encode(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) {
            StringBuilder out = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '"': out.append("\\\""); break;
                    case '\\': out.append("\\\\"); break;
                    case '\n': out.append("\\n"); break;
                    case '\r': out.append("\\r"); break;
                    case '\t': out.append("\\t"); break;
                    default:
                        if (c < 32) out.append(String.format("\\u%04x", (int) c));
                        else out.append(c);
                }
            }
            return out.append('"').toString();
        }
        if (value instanceof Boolean || value instanceof Number) {
            if (value instanceof Number n && !Double.isFinite(n.doubleValue()))
                throw new IllegalArgumentException("Non-finite JSON number");
            return value.toString();
        }
        StringJoiner out;
        if (value instanceof Map<?, ?> map) {
            out = new StringJoiner(",", "{", "}");
            map.forEach((k, v) -> out.add(encode(k.toString()) + ":" + encode(v)));
        } else if (value instanceof Iterable<?> list) {
            out = new StringJoiner(",", "[", "]");
            for (Object item : list) out.add(encode(item));
        } else throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass());
        return out.toString();
    }
    private static final class Parser {
        final String source;
        int index;
        Parser(String source) { this.source = source; }
        IllegalArgumentException error() { return new IllegalArgumentException("Invalid JSON at character " + index); }
        void space() { while (index < source.length() && " \r\n\t".indexOf(source.charAt(index)) >= 0) index++; }
        boolean take(char c) {
            space();
            if (index < source.length() && source.charAt(index) == c) { index++; return true; }
            return false;
        }
        boolean digit(char c) { return c >= '0' && c <= '9'; }
        Object value(int depth) {
            if (depth > 32) throw error();
            space();
            if (index >= source.length()) throw error();
            char c = source.charAt(index);
            if (c == '"') return quoted();
            if (take('{')) {
                Map<String, Object> map = new LinkedHashMap<>();
                if (take('}')) return map;
                do {
                    space();
                    if (index >= source.length() || source.charAt(index) != '"') throw error();
                    String key = quoted();
                    if (!take(':') || map.containsKey(key)) throw error();
                    map.put(key, value(depth + 1));
                    if (take('}')) return map;
                } while (take(','));
                throw error();
            }
            if (take('[')) {
                List<Object> list = new ArrayList<>();
                if (take(']')) return list;
                do {
                    list.add(value(depth + 1));
                    if (take(']')) return list;
                } while (take(','));
                throw error();
            }
            for (String token : new String[] {"true", "false", "null"}) {
                if (source.startsWith(token, index)) {
                    index += token.length();
                    return token.equals("null") ? null : Boolean.valueOf(token);
                }
            }
            int start = index;
            if (c == '-') index++;
            if (index >= source.length() || !digit(source.charAt(index))) throw error();
            if (source.charAt(index) == '0') index++;
            else while (index < source.length() && digit(source.charAt(index))) index++;
            boolean decimal = false;
            if (index < source.length() && source.charAt(index) == '.') {
                decimal = true; index++;
                int digits = index;
                while (index < source.length() && digit(source.charAt(index))) index++;
                if (digits == index) throw error();
            }
            if (index < source.length() && "eE".indexOf(source.charAt(index)) >= 0) {
                decimal = true; index++;
                if (index < source.length() && "+-".indexOf(source.charAt(index)) >= 0) index++;
                int digits = index;
                while (index < source.length() && digit(source.charAt(index))) index++;
                if (digits == index) throw error();
            }
            try {
                String number = source.substring(start, index);
                if (!decimal) return Long.valueOf(number);
                double result = Double.parseDouble(number);
                if (!Double.isFinite(result)) throw error();
                return result;
            } catch (NumberFormatException e) { throw error(); }
        }
        String quoted() {
            index++;
            StringBuilder out = new StringBuilder();
            while (index < source.length()) {
                char c = source.charAt(index++);
                if (c == '"') return out.toString();
                if (c < 32) throw error();
                if (c == '\\') {
                    if (index >= source.length()) throw error();
                    c = source.charAt(index++);
                    switch (c) {
                        case '"': case '\\': case '/': out.append(c); break;
                        case 'b': out.append('\b'); break;
                        case 'f': out.append('\f'); break;
                        case 'n': out.append('\n'); break;
                        case 'r': out.append('\r'); break;
                        case 't': out.append('\t'); break;
                        case 'u':
                            if (index + 4 > source.length()) throw error();
                            for (int i = index; i < index + 4; i++)
                                if ("0123456789abcdefABCDEF".indexOf(source.charAt(i)) < 0) throw error();
                            try { out.append((char) Integer.parseInt(source.substring(index, index + 4), 16)); }
                            catch (NumberFormatException e) { throw error(); }
                            index += 4; break;
                        default: throw error();
                    }
                } else out.append(c);
            }
            throw error();
        }
    }
}
