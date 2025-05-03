package com.lab24.quizlabai.service.AzureAI;

import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;

import java.util.List;
import java.util.Map;

public interface AzureOpenAIService {
    List<Question> generateQuestions(Quiz quiz);
    String evaluateAnswersWithAI(List<Question> questions, Map<Long, List<String>> userAnswers);
}
