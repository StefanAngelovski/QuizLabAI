package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.*;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizResultService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequestMapping("/attempt/ai")
public class QuizAiController {

    private final AzureOpenAIService azureOpenAIService;

    private final QuizResultService quizResultService;

    public QuizAiController(AzureOpenAIService azureOpenAIService, QuizResultService quizResultService) {
        this.azureOpenAIService = azureOpenAIService;
        this.quizResultService = quizResultService;
    }

    @PostMapping("/submit")
    public String submitAndCheckWithAI(@RequestParam(value = "selectedOptions", required = false) List<String> selectedOptions,
                                       @RequestParam(value = "shortAnswer", required = false) String shortAnswer,
                                       @AuthenticationPrincipal User user,
                                       HttpSession session, Model model) {

        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");
        Integer score = (Integer) session.getAttribute("score");
        List<Question> questions = (List<Question>) session.getAttribute("quizQuestions");
        Quiz quiz = (Quiz) session.getAttribute("quiz");

        if (userAnswers == null || questions == null || quiz == null) {
            model.addAttribute("errorMessage", "Session data is missing.");
            return "common/error";
        }

        String timeTaken = (String) session.getAttribute("timeTaken");
        Long duration = 0L;
        if (timeTaken == null) {
            Long startTime = (Long) session.getAttribute("startTime");
            if (startTime != null) {
                duration = (System.currentTimeMillis() - startTime) / 1000;
                timeTaken = String.format("%d minutes %d seconds", duration / 60, duration % 60);
                session.setAttribute("timeTaken", timeTaken);
            } else {
                model.addAttribute("errorMessage", "Quiz start time is missing.");
                return "common/error";
            }
        }

        int quizTime = quiz.getQuizTime();
        int totalQuestions = questions.size();
        int answeredQuestions = (int) userAnswers.values().stream()
                .filter(answers -> answers != null && answers.stream()
                        .anyMatch(ans -> ans != null && !ans.trim().isEmpty() && !ans.equalsIgnoreCase("No Answer")))
                .count();

        String aiEvaluationResults = azureOpenAIService.evaluateAnswersWithAI(questions, userAnswers);
        List<EvaluationResult> evaluationResults = parseEvaluationResults(aiEvaluationResults);

        quizResultService.saveResult(quiz, user, (double) score, (double) duration);

        model.addAttribute("evaluationResult", evaluationResults);
        model.addAttribute("score", score);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("quizId", quiz.getId());
        model.addAttribute("quizTime", quizTime);
        model.addAttribute("timeTaken", timeTaken);
        model.addAttribute("answeredQuestions", answeredQuestions);
        model.addAttribute("totalQuestions", totalQuestions);

        session.setAttribute("score", score);
        session.setAttribute("totalQuestions", totalQuestions);
        session.setAttribute("answeredQuestions", answeredQuestions);
        session.setAttribute("evaluationResult", evaluationResults);
        session.setAttribute("quiz", quiz);
        session.setAttribute("timeTaken", timeTaken);

        return "quizResultAiPage";
    }

    private List<EvaluationResult> parseEvaluationResults(String aiEvaluationResults) {
        List<EvaluationResult> results = new ArrayList<>();
        if (aiEvaluationResults == null || aiEvaluationResults.isEmpty()) {
            return results;
        }

        String[] questionBlocks = aiEvaluationResults.split("\\n\\n+");

        for (String block : questionBlocks) {
            EvaluationResult result = new EvaluationResult();
            String[] lines = block.split("\\n");

            for (String line : lines) {
                if (line.startsWith("Question:")) {
                    result.setQuestionText(line.substring("Question:".length()).trim());
                } else if (line.startsWith("User's Answer:")) {
                    result.setUserAnswer(line.substring("User's Answer:".length()).trim());
                } else if (line.startsWith("Correct Answer:")) {
                    result.setCorrectAnswer(line.substring("Correct Answer:".length()).trim());
                } else if (line.startsWith("Feedback:")) {
                    String feedback = line.substring("Feedback:".length()).trim();
                    result.setExplanation(feedback);
                    result.setCorrect(feedback.toLowerCase().contains("correct") && !feedback.toLowerCase().contains("incorrect"));
                }
            }

            if (result.getQuestionText() != null) {
                results.add(result);
            }
        }
        return results;
    }
    @GetMapping("/export-pdf")
    public void exportResultsToPdf(HttpSession session, HttpServletResponse response) throws IOException {

        Quiz quiz = (Quiz) session.getAttribute("quiz");
        List<EvaluationResult> evaluationResults = (List<EvaluationResult>) session.getAttribute("evaluationResult");
        Integer score = (Integer) session.getAttribute("score");
        String timeTaken = (String) session.getAttribute("timeTaken");
        int totalQuestions = (int) session.getAttribute("totalQuestions");
        int answeredQuestions = (int) session.getAttribute("answeredQuestions");

        if (quiz == null || evaluationResults == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No quiz data available");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"quiz_results.pdf\"");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Наслов
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("QuizLabAI - Quiz Results", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));


        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        document.add(new Paragraph("Subject: " + quiz.getSubject().getName(), boldFont));
        document.add(new Paragraph("Topic: " + (quiz.getTopic() != null ? quiz.getTopic() : "N/A"), boldFont));
        document.add(new Paragraph("Score: " + score + " points", boldFont));
        document.add(new Paragraph("Answered Questions: " + answeredQuestions + "/" + totalQuestions, boldFont));
        document.add(new Paragraph("Time Taken: " + timeTaken, boldFont));
        document.add(new Paragraph(" "));


        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{5, 3, 3, 5});

        Stream.of("Question", "Your Answer", "Correct Answer", "Evaluation").forEach(headerTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(Color.LIGHT_GRAY);
            header.setPhrase(new Phrase(headerTitle, boldFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            table.addCell(header);
        });

        for (EvaluationResult eval : evaluationResults) {
            PdfPCell questionCell = new PdfPCell(new Phrase(eval.getQuestionText()));
            PdfPCell userAnswerCell = new PdfPCell(new Phrase(eval.getUserAnswer() != null ? eval.getUserAnswer() : "No Answer"));
            PdfPCell correctAnswerCell = new PdfPCell(new Phrase(eval.getCorrectAnswer()));
            PdfPCell evaluationCell = new PdfPCell(new Phrase(eval.getExplanation()));

            if (eval.isCorrect()) {
                userAnswerCell.setBackgroundColor(new Color(212, 237, 218));
            } else {
                userAnswerCell.setBackgroundColor(new Color(248, 215, 218));
            }

            table.addCell(questionCell);
            table.addCell(userAnswerCell);
            table.addCell(correctAnswerCell);
            table.addCell(evaluationCell);
        }

        document.add(table);
        document.close();
    }
}
