package xuanmo.arcartxsuite.api.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简 JSON 解析器，仅支持 Object / Array / String / Number / boolean / null。
 * <p>
 * 不支持注释、不支持尾随逗号。用于内部跨服同步等轻量场景，
 * 避免引入完整 JSON 库。
 *
 * @since 1.6.0
 */
public final class SimpleJsonParser {

    private SimpleJsonParser() {}

    /**
     * 解析 JSON 字符串为 {@code Map<String, Object>} 或 {@code List<Object>} 或标量。
     *
     * @param json JSON 字符串
     * @return 解析结果（Map / List / String / Double / Boolean / null）
     * @throws IllegalArgumentException 如果 JSON 格式非法
     */
    public static Object parseAny(String json) {
        Parser p = new Parser(json);
        p.skipWhitespace();
        Object result = p.parseValue();
        p.skipWhitespace();
        if (p.pos < p.len) {
            throw new IllegalArgumentException("JSON 尾部有多余字符: position=" + p.pos);
        }
        return result;
    }

    /**
     * 解析 JSON 对象字符串为 {@code Map<String, Object>}。
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String json) {
        Object result = parseAny(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new IllegalArgumentException("JSON 顶层不是对象");
    }

    // ── 内部解析器 ──────────────────────────────────────────

    private static final class Parser {
        final String s;
        final int len;
        int pos;

        Parser(String s) {
            this.s = s;
            this.len = s.length();
            this.pos = 0;
        }

        void skipWhitespace() {
            while (pos < len && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= len) throw new IllegalArgumentException("JSON 意外结束");
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip '{'
            skipWhitespace();
            if (pos < len && s.charAt(pos) == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                if (pos >= len || s.charAt(pos) != '"') throw new IllegalArgumentException("JSON 对象 key 必须是字符串: position=" + pos);
                String key = parseString();
                skipWhitespace();
                if (pos >= len || s.charAt(pos) != ':') throw new IllegalArgumentException("JSON 对象缺少冒号: position=" + pos);
                pos++; // skip ':'
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (pos >= len) throw new IllegalArgumentException("JSON 对象意外结束");
                char next = s.charAt(pos);
                if (next == ',') { pos++; continue; }
                if (next == '}') { pos++; break; }
                throw new IllegalArgumentException("JSON 对象缺少逗号或大括号: position=" + pos);
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip '['
            skipWhitespace();
            if (pos < len && s.charAt(pos) == ']') { pos++; return list; }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (pos >= len) throw new IllegalArgumentException("JSON 数组意外结束");
                char next = s.charAt(pos);
                if (next == ',') { pos++; continue; }
                if (next == ']') { pos++; break; }
                throw new IllegalArgumentException("JSON 数组缺少逗号或方括号: position=" + pos);
            }
            return list;
        }

        String parseString() {
            pos++; // skip opening '"'
            StringBuilder sb = new StringBuilder();
            while (pos < len) {
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= len) throw new IllegalArgumentException("JSON 字符串转义意外结束");
                    char esc = s.charAt(pos++);
                    sb.append(switch (esc) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case '/' -> '/';
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'r' -> '\r';
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'u' -> {
                            if (pos + 4 > len) throw new IllegalArgumentException("JSON \\u 转义不完整");
                            String hex = s.substring(pos, pos + 4);
                            pos += 4;
                            yield (char) Integer.parseInt(hex, 16);
                        }
                        default -> esc;
                    });
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("JSON 字符串未闭合");
        }

        Object parseNumber() {
            int start = pos;
            if (pos < len && s.charAt(pos) == '-') pos++;
            while (pos < len && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.' || s.charAt(pos) == 'e' || s.charAt(pos) == 'E' || s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                pos++;
            }
            String numStr = s.substring(start, pos);
            try {
                if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                    return Double.parseDouble(numStr);
                }
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("JSON 数字格式非法: " + numStr);
            }
        }

        Boolean parseBoolean() {
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            throw new IllegalArgumentException("JSON 布尔值格式非法: position=" + pos);
        }

        Object parseNull() {
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            throw new IllegalArgumentException("JSON null 格式非法: position=" + pos);
        }
    }
}
