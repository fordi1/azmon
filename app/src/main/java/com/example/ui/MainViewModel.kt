package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.utils.toEnglishDigits

class MainViewModel(private val repository: ExamRepository) : ViewModel() {
    
    val allExams: Flow<List<ExamEntity>> = repository.allExams

    fun getExamById(id: Long): Flow<ExamEntity?> = repository.getExamById(id)
    
    fun getExamWithQuestions(id: Long): Flow<com.example.data.dao.ExamWithQuestions?> = repository.getExamWithQuestions(id)
    
    fun getQuestionsForExam(id: Long): Flow<List<QuestionEntity>> = repository.getQuestionsForExam(id)

    fun insertExam(exam: ExamEntity, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insertExam(exam)
            onComplete(id)
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.updateExam(exam)
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }
    
    fun getQuestionByIdStream(examId: Long, questionId: Long): Flow<QuestionEntity?> {
        // Find question manually or add a DAO method. 
        // For simplicity we can map the list flow
        return getQuestionsForExam(examId).map { list -> list.find { it.id == questionId } }
    }

    fun saveQuestion(
        question: QuestionEntity,
        onComplete: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val examWithQuestions = repository.getExamWithQuestionsSync(question.examId)
            val totalScore = examWithQuestions?.exam?.totalScore
                ?.toEnglishDigits()
                ?.replace(',', '.')
                ?.replace('٫', '.')
                ?.toFloatOrNull()
            if (totalScore != null) {
                val otherQuestionsScore = examWithQuestions?.questions.orEmpty()
                    .filterNot { it.id == question.id }
                    .sumOf { it.score.toDouble() }
                    .toFloat()
                if (otherQuestionsScore + question.score > totalScore + 0.001f) {
                    onError("مجموع نمرات سوالات نمی‌تواند بیشتر از نمره کل برگه باشد.")
                    return@launch
                }
            }
            if (question.id == 0L) {
                repository.insertQuestion(question)
            } else {
                repository.updateQuestion(question)
            }
            onComplete()
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun reorderQuestions(questions: List<QuestionEntity>) {
         viewModelScope.launch {
            val updated = questions.mapIndexed { index, q -> q.copy(orderPosition = index) }
            repository.reorderQuestions(updated)
        }
    }

}

class MainViewModelFactory(private val repository: ExamRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
