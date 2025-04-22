package com.lab24.quizlabai.service.AzureAI;

import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;

import java.util.List;

public interface AzureOpenAIService {
    List<Question> generateQuestions(Quiz quiz);
}
