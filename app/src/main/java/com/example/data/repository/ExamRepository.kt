package com.example.data.repository

import com.example.data.dao.ExamDao
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.dao.ExamWithQuestions
import kotlinx.coroutines.flow.Flow

class ExamRepository(private val examDao: ExamDao) {
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()

    fun getExamById(id: Long): Flow<ExamEntity?> = examDao.getExamById(id)
    suspend fun getExamByIdSync(id: Long): ExamEntity? = examDao.getExamByIdSync(id)
    
    fun getExamWithQuestions(id: Long): Flow<ExamWithQuestions?> = examDao.getExamWithQuestions(id)
    suspend fun getExamWithQuestionsSync(id: Long): ExamWithQuestions? = examDao.getExamWithQuestionsSync(id)

    suspend fun insertExam(exam: ExamEntity): Long {
        return examDao.insertExam(exam.copy(lastEditedTime = System.currentTimeMillis()))
    }

    suspend fun updateExam(exam: ExamEntity) {
        examDao.updateExam(exam.copy(lastEditedTime = System.currentTimeMillis()))
    }

    suspend fun deleteExam(exam: ExamEntity) {
        examDao.deleteExam(exam)
    }

    fun getQuestionsForExam(examId: Long): Flow<List<QuestionEntity>> {
        return examDao.getQuestionsForExam(examId)
    }

    suspend fun insertQuestion(question: QuestionEntity): Long {
        touchExam(question.examId)
        return examDao.insertQuestion(question)
    }

    suspend fun updateQuestion(question: QuestionEntity) {
        touchExam(question.examId)
        examDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: QuestionEntity) {
        touchExam(question.examId)
        examDao.deleteQuestion(question)
    }
    
    suspend fun insertQuestions(questions: List<QuestionEntity>) {
        if(questions.isNotEmpty()) {
            touchExam(questions.first().examId)
            examDao.insertQuestions(questions)
        }
    }

    suspend fun reorderQuestions(questions: List<QuestionEntity>) {
        if(questions.isNotEmpty()) {
            touchExam(questions.first().examId)
            examDao.insertQuestions(questions) 
            // since insertQuestion uses REPLACE, inserting items with existing id updates them
        }
    }
    
    suspend fun touchExam(examId: Long) {
        val exam = getExamByIdSync(examId)
        if (exam != null) {
            updateExam(exam) // Update modifies lastEditedTime
        }
    }
    
    suspend fun getBackupData(): List<ExamWithQuestions> {
        // A minimal implementation, real one would collect from Flow or Dao directly.
        // For backup, we can define a one-shot query.
        return emptyList() // Will be implemented differently later using JSON serialization.
    }
}
