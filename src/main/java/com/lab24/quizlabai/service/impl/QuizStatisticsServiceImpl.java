package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.QuizStatistics;
import com.lab24.quizlabai.repository.QuizRepository;
import com.lab24.quizlabai.repository.QuizResultRepository;
import com.lab24.quizlabai.service.QuizStatisticsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        List<QuizStatistics.LeaderboardEntry> leaderboard = new ArrayList<>();

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

            // Time validation - cap at quiz time limit
            double timeSpent = Math.min(result.getTimeSpent(), quiz.getQuizTime());
            totalTimeSpent += timeSpent;

            // Add entry to leaderboard with proper LeaderboardEntry object
            String studentName = result.getUser().getFirstName() + " " + result.getUser().getLastName();
            leaderboard.add(new QuizStatistics.LeaderboardEntry(studentName, score));
        }

        // Handle empty results case
        if (totalAttempts == 0) {
            lowestScore = 0;
        }

        // Sort leaderboard by score in descending order
        leaderboard.sort((entry1, entry2) -> Double.compare(entry2.getScore(), entry1.getScore()));

        // Limit leaderboard to top 10 entries if needed
        if (leaderboard.size() > 10) {
            leaderboard = leaderboard.subList(0, 10);
        }

        // Completion rate and average time spent
        double completionRate = totalAttempts == 0 ? 0 : (completedAttempts / (double) totalAttempts) * 100;
        double averageTimeSpent = totalAttempts == 0 ? 0 : totalTimeSpent / totalAttempts;

        // Round to one decimal place for consistency
        averageTimeSpent = Math.round(averageTimeSpent * 10.0) / 10.0;

        // Average score
        double averageScore = totalAttempts == 0 ? 0 : totalScore / totalAttempts;

        // Create and populate QuizStatistics object
        QuizStatistics stats = new QuizStatistics();
        stats.setQuiz(quiz);
        stats.setTotalAttempts(totalAttempts);
        stats.setAverageScore(averageScore);
        stats.setHighestScore(highestScore);
        stats.setLowestScore(lowestScore);
        stats.setCompletionRate(completionRate);
        stats.setAverageTimeSpent(averageTimeSpent);
        stats.setLeaderboard(leaderboard);

        return stats;
    }
}