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
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzureOpenAIServiceImpl implements AzureOpenAIService {

    private final OpenAIClient client;
    private static final String DEPLOYMENT_NAME = "gpt-4.1";

    public AzureOpenAIServiceImpl() {
        String apiKey = "B0gyA5UcqWw0GW3I0KFIeV7dAmDWFCwRwCXZvDVQnSe9PJhR7JHJJQQJ99BDACfhMk5XJ3w3AAAAACOGEUM0";
        String endpoint = "https://andre-m9n0yn8i-swedencentral.cognitiveservices.azure.com/";

        this.client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
    }

    @Override
    public List<Question> generateQuestions(Quiz quiz) {
        StringBuilder promptBuilder = new StringBuilder();

        // Основен дел од барањето
        promptBuilder.append(String.format(
                "Generate %d %s-level quiz questions on the topic '%s' in the subject '%s'. ",
                quiz.getNumQuestions(),
                quiz.getDifficulty(),
                quiz.getTopic(),
                quiz.getSubject()
        ));

        // Преведи типови на прашања ако се присутни
        List<String> questionTypes = quiz.getQuestionTypes();
        if (questionTypes != null && !questionTypes.isEmpty()) {
            promptBuilder.append("The questions should be of the following types: ");
            promptBuilder.append(String.join(", ", questionTypes));
            promptBuilder.append(". ");
        }

        // Побарај формат за враќање во JSON
        promptBuilder.append("Format the response as a JSON array of objects with fields: text, type, options (array or empty if not needed), correctAnswer.");

        // Додавање на PDF содржина ако има
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


    private String extractPdfContent(byte[] pdfFile) {
        try {

            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfFile));

            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);

            document.close();

            return text;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
