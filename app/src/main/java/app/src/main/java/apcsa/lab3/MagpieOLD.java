package app.src.main.java.apcsa.lab3.apcsa.lab3;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Import other necessary packages

public class MagpieOLD {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final List<String> history = new ArrayList<>();

    public static void main(String[] args) {
        // Initial setup
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) break;

            // Add user message to history
            history.add("{\"role\":\"user\",\"content\":\"" + message + "\"}");
            
            try {
                sendMessage();
            } catch (Exception e) {
                System.out.println("Error communicating with the server: " + e.getMessage());
            }
        }
    }

    public static void sendMessage() throws IOException, InterruptedException {
        Gson gson = new Gson();

        // Create a list to hold message objects
        List<Message> messagesList = new ArrayList<>();
        
        // Add system message
        Message systemMessage = new Message();
        systemMessage.role = "system";
        systemMessage.content = "You are Magpie, a helpful assistant. Nothing more, nothing less.";
        messagesList.add(systemMessage);
        
        // Add user messages from history
        for (String msg : history) {
            Message usrMsg = gson.fromJson(msg, Message.class);
            messagesList.add(usrMsg);
        }

        // Create the request object
        Request request = new Request();
        request.model = "llama3.2:1b";
        request.messages = messagesList;

        // Serialize the request to JSON
        String jsonPayload = gson.toJson(request);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            
            if (postResponse.statusCode() != 200) {
                System.out.println("Error: Received status code " + postResponse.statusCode());
                System.out.println(postResponse.body());
                return;
            }

            // Parse the response
            Response responseObject = gson.fromJson(postResponse.body(), Response.class);
            
            if (responseObject.error != null) {
                System.out.println("API Error: " + responseObject.error);
                history.add("{\"role\":\"assistant\",\"content\":\"Error occurred. Please try again later.\"}");
                return;
            }

            // Handle successful response
            String response = responseObject.choices.get(0).message.content;
            history.add("{\"role\":\"assistant\",\"content\":\"" + response + "\"}");
            System.out.println("Magpie: " + response);

        } catch (IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Define data models using Gson
    static class Request {
        public String model;
        public List<Message> messages;
    }

    static class Message {
        public String role;
        public String content;
    }

    static class Response {
        public String error;
        public List<Choice> choices;

        static class Choice {
            public Message message;
        }
    }
}