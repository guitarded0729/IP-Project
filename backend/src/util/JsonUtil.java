package util;

import java.util.ArrayList;

// Utility class for manual JSON serialization (no external libraries like Jackson/Gson)
public class JsonUtil {

    // Sanitizes and escapes special characters for valid JSON strings
    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\b') {
                sb.append("\\b");
            } else if (c == '\f') {
                sb.append("\\f");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c < 32) {
                // Control character
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toJsonString(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + escape(s) + "\"";
    }

    public static String toJsonNumber(int n) {
        return String.valueOf(n);
    }

    public static String toJsonNumber(double d) {
        return String.valueOf(d);
    }

    public static String toJsonBoolean(boolean b) {
        return b ? "true" : "false";
    }

    // Takes a list of already serialized JSON element strings and builds a JSON array
    public static String toJsonArray(ArrayList<String> jsonElements) {
        if (jsonElements == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < jsonElements.size(); i++) {
            sb.append(jsonElements.get(i));
            if (i < jsonElements.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
