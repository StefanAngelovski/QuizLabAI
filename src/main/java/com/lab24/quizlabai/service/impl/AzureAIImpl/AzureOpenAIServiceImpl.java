package com.lab24.quizlabai.service.impl.AzureAIImpl;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AzureOpenAIServiceImpl implements AzureOpenAIService {

    private OpenAIClient client;

    @Value("${azure.openai.api-key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String endpoint;

    private static final String DEPLOYMENT_NAME = "gpt-4.1";

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("Azure API key or endpoint is not configured properly.");
        }

        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
    }

    @Override
    public List<Question> generateQuestions(Quiz quiz) {
        StringBuilder promptBuilder = new StringBuilder();


        String language = quiz.getLanguage();
        if (language != null && language.equals("Macedonian")) {
            promptBuilder.append("Generate the quiz questions in Macedonian. ");
        } else {
            promptBuilder.append("Generate the quiz questions in English. ");
        }

        promptBuilder.append(String.format(
                "Generate %d %s-level quiz questions on the topic '%s' in the subject '%s'. ",
                quiz.getNumQuestions(),
                quiz.getDifficulty(),
                quiz.getTopic(),
                quiz.getSubject().getName()
        ));

        List<String> questionTypes = quiz.getQuestionTypes();
        if (questionTypes != null && !questionTypes.isEmpty()) {
            promptBuilder.append("The questions should be of the following types: ");
            promptBuilder.append(String.join(", ", questionTypes));
            promptBuilder.append(". ");
        }

        promptBuilder.append("Format the response as a JSON array of objects with fields: text, type, options (array or empty if not needed), correctAnswer.");

        if (quiz.getPdfFile() != null) {
            String pdfContent = extractPdfContent(quiz.getPdfFile());
            promptBuilder.append(" Also, use the content from the uploaded PDF to generate some questions: ").append(pdfContent);
        }

        String prompt = promptBuilder.toString();

        List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestSystemMessage("You are a helpful quiz generator."));
        messages.add(new ChatRequestUserMessage(prompt));

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxTokens(1500);
        options.setTemperature(0.7);
        options.setTopP(1.0);

        try {
            ChatCompletions result = client.getChatCompletions(DEPLOYMENT_NAME, options);
            String content = result.getChoices().get(0).getMessage().getContent();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode questionList = mapper.readTree(content);

            List<Question> questions = new ArrayList<>();
            int totalQuestions = questionList.size();
            int maxPoints = 100;
            int basePoints = maxPoints / totalQuestions;
            int remainder = maxPoints % totalQuestions;

            for (int i = 0; i < totalQuestions; i++) {
                JsonNode node = questionList.get(i);

                Question q = new Question();
                q.setText(node.get("text").asText());
                q.setType(node.get("type").asText());
                q.setOptions(mapper.convertValue(node.get("options"), List.class));
                q.setCorrectAnswer(node.get("correctAnswer").asText());

                int points = basePoints + (i < remainder ? 1 : 0);
                q.setPoints(points);

                questions.add(q);
            }

            return questions;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    @Override
    public String evaluateAnswersWithAI(List<Question> questions, Map<Long, List<String>> userAnswers) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Evaluate the following answers. For each, strictly use this format:\n");
        prompt.append("Question: [text]\n");
        prompt.append("User's Answer: [user's answer]\n");
        prompt.append("Correct Answer: [correct answer]\n");
        prompt.append("Feedback: [feedback about the answer]\n\n");

        for (Question question : questions) {
            List<String> userAnswerList = userAnswers.getOrDefault(question.getId(), List.of());
            String userAnswerStr = userAnswerList.isEmpty() ? "No answer" : String.join(", ", userAnswerList);

            prompt.append("Question: ").append(question.getText()).append("\n");
            prompt.append("User's Answer: ").append(userAnswerStr).append("\n");
            prompt.append("Correct Answer: ").append(question.getCorrectAnswer()).append("\n");
            prompt.append("Feedback: Provide detailed evaluation.\n\n");
        }

        List<ChatRequestMessage> messages = List.of(
                new ChatRequestSystemMessage("You are a helpful AI that strictly follows response formatting."),
                new ChatRequestUserMessage(prompt.toString())
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages)
                .setMaxTokens(2000)
                .setTemperature(0.5);

        try {
            ChatCompletions result = client.getChatCompletions(DEPLOYMENT_NAME, options);
            return result.getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String extractPdfContent(byte[] pdfFile) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfFile))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}

