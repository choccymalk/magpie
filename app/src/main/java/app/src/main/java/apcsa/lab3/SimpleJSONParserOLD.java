package app.src.main.java.apcsa.lab3;
import java.util.HashMap;
import java.util.Map;
public class SimpleJSONParserOLD {

    public static Map<String, Object> parseJson(String jsonString) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonString = jsonString.trim();

        if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
            jsonString = jsonString.substring(1, jsonString.length() - 1);
            String[] keyValuePairs = jsonString.split(",");

            for (String keyValuePair : keyValuePairs) {
                String[] parts = keyValuePair.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim().replaceAll("\"", "");
                    String value = parts[1].trim();

                    if (value.startsWith("{") || value.startsWith("[")) {
                      jsonMap.put(key, parseJson(value));
                    } else if (value.startsWith("\"") && value.endsWith("\"")) {
                        jsonMap.put(key, value.substring(1, value.length() - 1));
                    }
                     else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        jsonMap.put(key, Boolean.parseBoolean(value));
                    }
                    else {
                      try {
                        jsonMap.put(key, Integer.parseInt(value));
                      } catch (NumberFormatException e) {
                         try {
                            jsonMap.put(key, Double.parseDouble(value));
                         } catch (NumberFormatException ex) {
                           jsonMap.put(key, value);
                         }
                      }
                    }
                }
            }
        }
        return jsonMap;
    }
}