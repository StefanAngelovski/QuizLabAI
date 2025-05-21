package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.*;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizResultService;
import com.lab24.quizlabai.service.QuizService;
import com.lab24.quizlabai.service.QuizStatisticsService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/attempt/result")
public class QuizResultController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizStatisticsService quizStatisticsService;

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private AzureOpenAIService azureOpenAIService;


    @GetMapping("/{quizId}")
    public String showResult(@PathVariable Long quizId, HttpSession session, Model model) {
        Integer score = (Integer) session.getAttribute("score");
        Integer totalPoints = (Integer) session.getAttribute("totalPoints");
        Quiz quiz = quizService.getQuizById(quizId);

        if (quiz == null) {
            return "redirect:/error";
        }

        double percentage = (score.doubleValue() / totalPoints) * 100;
        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");

        int answeredQuestions = 0;
        if (userAnswers != null && !userAnswers.isEmpty()) {
            for (Question question : quiz.getQuestions()) {
                List<String> answers = userAnswers.get(question.getId());
                if (answers != null && !answers.isEmpty()) {
                    boolean hasRealAnswer = answers.stream()
                            .anyMatch(ans -> ans != null && !ans.trim().equalsIgnoreCase("No Answer") && !ans.trim().isEmpty());
                    if (hasRealAnswer) {
                        answeredQuestions++;
                    }
                }
            }
        }

        String timeTaken = (String) session.getAttribute("timeTaken");
        if (timeTaken == null) {
            long startTime = (Long) session.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long timeDifference = endTime - startTime;
            long timeInSeconds = timeDifference / 1000;
            timeTaken = String.format("%d minutes %d seconds", timeInSeconds / 60, timeInSeconds % 60);
            session.setAttribute("timeTaken", timeTaken);
        }

        int quizTime = quiz.getQuizTime();
        int totalQuestions = quiz.getQuestions().size();

        model.addAttribute("answeredQuestions", answeredQuestions);
        model.addAttribute("score", score);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("percentage", percentage);
        model.addAttribute("quiz", quiz);
        model.addAttribute("quizTime", quizTime);
        model.addAttribute("timeTaken", timeTaken);
        model.addAttribute("totalQuestions", totalQuestions);

        return "quizResultPage";
    }

    @GetMapping("/{quizId}/statistics")
    public String showQuizStatistics(@PathVariable Long quizId, Model model) {
        QuizStatistics quizStatistics = quizStatisticsService.calculateStatistics(quizId);
        model.addAttribute("quizStatistics", quizStatistics);

        return "quizStatistics";
    }

    @GetMapping("/export-pdf/{quizId}")
    public void exportResultsToPdf(
            @PathVariable Long quizId,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quiz not found");
            return;
        }

        Integer score = (Integer) session.getAttribute("score");
        Integer totalPoints = (Integer) session.getAttribute("totalPoints");
        String timeTaken = (String) session.getAttribute("timeTaken");
        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");

        int totalQuestions = quiz.getQuestions().size();
        int answeredQuestions = userAnswers != null ? userAnswers.size() : 0;

        if (timeTaken == null) {
            Long startTime = (Long) session.getAttribute("startTime");
            if (startTime != null) {
                long endTime = System.currentTimeMillis();
                long diff = endTime - startTime;
                long seconds = diff / 1000;
                timeTaken = String.format("%d minutes %d seconds", seconds / 60, seconds % 60);
            } else {
                timeTaken = "Unknown";
            }
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"quiz_results_" + quizId + ".pdf\"");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

        Paragraph title = new Paragraph("QuizLabAI - Quiz Results", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Subject: " + quiz.getSubject().getName(), boldFont));
        document.add(new Paragraph("Topic: " + (quiz.getTopic() != null ? quiz.getTopic() : "N/A"), boldFont));
        document.add(new Paragraph("Score: " + (score != null ? score : 0) + " points", boldFont));
        document.add(new Paragraph("Answered Questions: " + answeredQuestions + "/" + totalQuestions, boldFont));
        document.add(new Paragraph("Time Taken: " + timeTaken, boldFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 3, 3, 2});

        Stream.of("Question", "Your Answer", "Correct Answer", "Evaluation").forEach(headerTitle -> {
            PdfPCell header = new PdfPCell(new Phrase(headerTitle, boldFont));
            header.setBackgroundColor(Color.LIGHT_GRAY);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(5);
            table.addCell(header);
        });

        for (Question question : quiz.getQuestions()) {
            String questionText = question.getText();
            List<String> userAnsList = userAnswers != null ? userAnswers.get(question.getId()) : null;

            String userAnswer = (userAnsList != null && !userAnsList.isEmpty())
                    ? String.join(", ", userAnsList)
                    : "No Answer";

            List<String> correctAnswerList = getCorrectAnswersList(question);

            boolean isCorrect = false;
            if (userAnsList != null) {
                Set<String> userSet = userAnsList.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
                Set<String> correctSet = correctAnswerList.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                isCorrect = userSet.equals(correctSet);
            }

            String evaluation = isCorrect ? "Correct" : "Incorrect";

            PdfPCell questionCell = new PdfPCell(new Phrase(questionText, normalFont));
            PdfPCell userAnswerCell = new PdfPCell(new Phrase(userAnswer, normalFont));
            PdfPCell correctAnswerCell = new PdfPCell(new Phrase(String.join(", ", correctAnswerList), normalFont));
            PdfPCell evaluationCell = new PdfPCell(new Phrase(evaluation, normalFont));

            if ("Correct".equals(evaluation)) {
                userAnswerCell.setBackgroundColor(new Color(212, 237, 218)); // light green
            } else {
                userAnswerCell.setBackgroundColor(new Color(248, 215, 218)); // light red
            }

            table.addCell(questionCell);
            table.addCell(userAnswerCell);
            table.addCell(correctAnswerCell);
            table.addCell(evaluationCell);
        }

        document.add(table);
        document.close();
    }




    private List<String> getCorrectAnswersList(Question question) {
        String correctAnswersStr = question.getCorrectAnswer();
        if (correctAnswersStr == null || correctAnswersStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(correctAnswersStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
