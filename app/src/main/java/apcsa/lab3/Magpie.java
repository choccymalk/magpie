package apcsa.lab3;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.net.*;
import net.dankito.readability4j.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Base64;
import java.util.Base64.Encoder;

public class Magpie {
    public static String sourceURL;
    public static boolean searchedWeb = false;
    private static final HttpClient client = HttpClient.newHttpClient();
    public static final List<String> history = new ArrayList<>();
    public static void main(String[] args) {

        /*
         * try {
         * System.out.println(searchWeb("random access memory"));
         * } catch (Exception e) {
         * System.out.println("Error: " + e.getMessage());
         * }
         */
        UI ui = new UI();
        ui.main(null);

        /*Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit"))
                break;
            history.add("{\"role\":\"user\",\"content\":\"" + message + "\"}");
            try {
                sendMessage();
            } catch (Exception e) {
                System.out.println("Error communicating with the server: " + e.getMessage());
            }
        }*/
    }

    public static void sendMessage() throws Exception {
        Gson gson = new Gson();
        List<Message> messagesList = new ArrayList<>();
        Message systemMessage = new Message();
        systemMessage.role = "system";
        systemMessage.content = "You are Magpie, a helpful assistant. You should respond to the user's question in a way that is as helpful and informative as possible. If the user asks about recent events, you must search the web. To search the web, say '!webquery! question !endwebquery!', where question is your query. The user will respond with the result from the query, summarize the response, no matter the length. Should the search fail, 'No results found' will be returned, if this happens, inform the user that there was an error and that they should try again later. You can generate images. To generate an image, say '!imagequery! description !endimagequery!', where description is a detailed description of the image. If image generation was successful, the image will be displayed and the user will respond with 'Generation Successful.' If image generation fails, the user will respond with 'Generation Failed.'" ;
        messagesList.add(systemMessage);

        for (String msg : history) {
            try {
                Message usrMsg = gson.fromJson(msg, Message.class);
                messagesList.add(usrMsg);
            } catch (JsonSyntaxException e) {
                System.out.println("Malformed JSON: " + msg);
            }
        }

        Request request = new Request();
        request.model = "qwen2.5:14b";
        request.messages = messagesList;
        request.stream = false;
        String jsonPayload = gson.toJson(request);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://cucumber:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            if (postResponse.statusCode() != 200) {
                System.out.println("Error: Received status code " + postResponse.statusCode());
                return;
            }

            // Parse the response
            System.out.println(postResponse.body());
            JsonReader reader = new JsonReader(new StringReader(postResponse.body()));
            reader.setStrictness(Strictness.LENIENT);
            Response responseObject = gson.fromJson(reader, Response.class);
            if (responseObject.message.content.contains("!webquery")) {
                String query = responseObject.message.content.substring(responseObject.message.content.indexOf("y!") + 1,
                responseObject.message.content.indexOf("!end"));
                String searchResult = searchWeb(query);
                history.add("{\"role\":\"user\",\"content\":\"" + searchResult + "\"}");
                searchedWeb = true;
                System.out.println("Magpie is searching the web.");
                UI.appendTextToLabel("Magpie is searching the web.");
                UI.appendTextToLabel("Source: " + Magpie.sourceURL);
                sendMessage();
            } else if (responseObject.message.content.contains("!imagequery")) {
                String query = responseObject.message.content.substring(responseObject.message.content.indexOf("y!") + 1,
                responseObject.message.content.indexOf("!end"));
                String imageResult = generateImage(query);
                history.add("{\"role\":\"user\",\"content\":\"" + imageResult + "\"}");
                UI.appendTextToLabel("Magpie is creating an image, result will be saved to image.png.");
                UI.appendTextToLabel("<html><img height='256' width='256' src='file:///C:\\Users\\Bentley\\Documents\\GitHub\\magpie\\image.png'/></html>");
                sendMessage();
            } else {
                if (responseObject.message != null && responseObject.message.content != null
                        && !responseObject.message.content.equals("") && responseObject.done != false) {
                    history.add("{\"role\":\"assistant\",\"content\":\"" + responseObject.message.content + "\"}");
                    searchedWeb = false;
                    UI.appendTextToLabel("Magpie: " + responseObject.message.content.replaceAll("(\r\n|\n)", "<br />"));
                    System.out.println("Magpie: " + responseObject.message.content);
                }
            }

            StringBuilder fullResponse = new StringBuilder();
            while (!responseObject.done) {
                if (responseObject.message != null && responseObject.message.content != null) {
                    fullResponse.append(responseObject.message.content);
                }
                responseObject = gson.fromJson(postResponse.body(), Response.class); // Update with the next streamed
                                                                                     // response
            }

            // Add the final response to history
            // history.add("{\"role\":\"assistant\",\"content\":\"" +
            // fullResponse.toString() + "\"}");

        } catch (IOException | InterruptedException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static class Request {
        public String model;
        public List<Message> messages;
        public boolean stream;
    }

    static class Message {
        public String role;
        public String content;
    }

    static class Response {
        public String model;
        public String created_at;
        public boolean done;
        public Message message;
        public String done_reason;
        public float total_duration;
        public float load_duration;
        public float prompt_eval_count;
        public float primt_eval_duration;
        public float eval_count;
        public float eval_duration;

    }

    static class ImageRequest {
        public String prompt;
        public String steps;
        public String sampler_name;
        public String scheduler; 
    }

    static class image {
        public String image;
    }

    public static void decodeBase64ToFile(String base64String, String filePath) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(decodedBytes);
            fos.close();
            System.out.println("File saved successfully to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static String generateImage(String description) throws IOException {
        // Prepare JSON request body
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("prompt", description);
        jsonMap.put("negative_prompt", "lowres, text, error, cropped, worst quality, low quality, jpeg artifacts, ugly, duplicate, morbid, mutilated, out of frame, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, mutation, deformed, blurry, dehydrated, bad anatomy, bad proportions, extra limbs, cloned face, disfigured, gross proportions, malformed limbs, missing arms, missing legs, extra arms, extra legs, fused fingers, too many fingers, long neck, username, watermark, signature");
        jsonMap.put("steps", "20");
        jsonMap.put("sampler_name", "Euler A");
        jsonMap.put("scheduler", "Automatic");
        jsonMap.put("sd_model_checkpoint", "cyberrealisticRevamp_v32.safetensors [fe9b443031]");
        Gson gson = new Gson();
        String jsonBody = gson.toJson(jsonMap);
        
        // Build POST request
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://zucchini:7860/sdapi/v1/txt2img"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        
        try {
            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            if (postResponse.statusCode() != 200) {
                System.out.println("Error: Received status code " + postResponse.statusCode());
                return "Generation Failed.";
            }

            String responseBody = postResponse.body();

            // Parse the JSON response to determine its type
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("images")) {
                // Successful response: parse into ImageResponse
                ImageResponse imageResponse = gson.fromJson(responseBody, ImageResponse.class);
                if (imageResponse.images != null && !imageResponse.images.isEmpty()) {
                    String base64image = imageResponse.images.get(0);
                    writeToFile("base64image.txt", base64image);
                    decodeBase64ToFile(base64image, "image.png");
                    return "Generation Successful."; // Return first image string
                } else {
                    System.out.println("Error: No images found in response.");
                    return "Generation Failed.";
                }
            } else if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("detail")) {
                // Validation error response: parse into ErrorResponse
                ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
                System.out.println("Validation error: " + errorResponse.detail);
                return "Generation Failed.";
            } else {
                System.out.println("Unknown response format.");
                return "Generation Failed.";
            }
        } catch (IOException | InterruptedException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return "Generation Failed.";
        }
    }
    
    // Helper classes to represent the response JSON
    static class ImageResponse {
        List<String> images;
        Map<String, Object> parameters;
        String info;
    }
    
    static class ErrorResponse {
        List<ErrorDetail> detail;
    }
    
    static class ErrorDetail {
        List<Object> loc;
        String msg;
        String type;
    }

    public static String searchWeb(String query) throws Exception {
        String bingSearchHTML = getHTML("https://www.bing.com/search?q=" + query.replaceAll("\\s+", "+"));
        writeToFile("bing_search_html.html", bingSearchHTML);
        Document doc = Jsoup.parse(bingSearchHTML);
        Element cite = doc.getElementsByClass("tilk").first();
        Element citeURL = cite.getElementsByTag("a").first();
        String citeURL2 = cite.attr("href");
        System.out.println(citeURL2);
        if (citeURL2.startsWith("http")) {
            System.out.println("Searching: " + citeURL2);
            sourceURL = citeURL2;
            String searchedWikipediaHTML = getHTML(citeURL2); // not always wikipedia, i just didn't want to change the
                                                                 // variable name
            writeToFile("searched_wikipedia_html.html", searchedWikipediaHTML);
            String rawHTML = readability4j(searchedWikipediaHTML, citeURL2);
            String parsedHTML = Jsoup.parse(rawHTML).text();
            return stripQuotes(parsedHTML);
        } else {
            return "No results found";
        }
    }
    public static String stripQuotes(String str) {
        return str.replace("\"", "");
    }

    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null;) {
                result.append(line);
            }
        }
        return result.toString();
    }

    public static void writeToFile(String filename, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }
   public static String readability4j(String html, String url) {
        Readability4J readability4J = new Readability4J(url, html);
        Article article = readability4J.parse();
        return article.getContent();
    }
}
/*
 * writeToFile("bing_search_html.txt", bingSearchHTML);
 * return doc.select("title").text() + bingSearchHTML;
 * //title.text() + "\n" + link.attr("href");
 */