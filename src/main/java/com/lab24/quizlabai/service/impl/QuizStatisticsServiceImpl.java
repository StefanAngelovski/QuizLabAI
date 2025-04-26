package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.QuizStatistics;
import com.lab24.quizlabai.repository.QuizRepository;
import com.lab24.quizlabai.repository.QuizResultRepository;
import com.lab24.quizlabai.service.QuizStatisticsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizStatisticsServiceImpl implements QuizStatisticsService {
    private final QuizResultRepository quizResultRepository;
    private final QuizRepository quizRepository;

    public QuizStatisticsServiceImpl(QuizResultRepository quizResultRepository, QuizRepository quizRepository) {
        this.quizResultRepository = quizResultRepository;
        this.quizRepository = quizRepository;
    }

    public QuizStatistics calculateStatistics(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));
        List<QuizResult> quizResults = quizResultRepository.findByQuizId(quizId);

        int totalAttempts = quizResults.size();
        double totalScore = 0;
        double highestScore = 0;
        double lowestScore = Double.MAX_VALUE;
        int completedAttempts = 0;
        double totalTimeSpent = 0;
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (QuizResult result : quizResults) {
            double score = result.getScore();
            totalScore += score;

            if (score > highestScore) {
                highestScore = score;
            }

            if (score < lowestScore) {
                lowestScore = score;
            }

            if (result.isCompleted()) {
                completedAttempts++;
            }

            totalTimeSpent += result.getTimeSpent();

            Map<String, Object> leaderboardEntry = new HashMap<>();
            leaderboardEntry.put("studentName", result.getUser().getFirstName() + " " + result.getUser().getLastName());
            leaderboardEntry.put("score", score);

            leaderboard.add(leaderboardEntry);
        }

        //leaderboard
        leaderboard.sort((entry1, entry2) -> Double.compare((double) entry2.get("score"), (double) entry1.get("score")));


        //completion rate and average time spent
        double completionRate = totalAttempts == 0 ? 0 : (completedAttempts / (double) totalAttempts) * 100;
        double averageTimeSpent = totalAttempts == 0 ? 0 : totalTimeSpent / totalAttempts;

        //average score
        double averageScore = totalAttempts == 0 ? 0 : totalScore / totalAttempts;

        //QuizStatistics object
        QuizStatistics stats = new QuizStatistics();
        stats.setQuiz(quiz);
        stats.setTotalAttempts(totalAttempts);
        stats.setAverageScore(averageScore);
        stats.setHighestScore(highestScore);
        stats.setLowestScore(lowestScore);
        stats.setCompletionRate(completionRate);
        stats.setAverageTimeSpent(averageTimeSpent);

        return stats;
    }
}
