package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

data class ExamWithQuestions(
    @androidx.room.Embedded val exam: ExamEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "examId"
    )
    val questions: List<QuestionEntity>
)

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY lastEditedTime DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id")
    fun getExamById(id: Long): Flow<ExamEntity?>
    
    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamByIdSync(id: Long): ExamEntity?

    @Transaction
    @Query("SELECT * FROM exams WHERE id = :id")
    fun getExamWithQuestions(id: Long): Flow<ExamWithQuestions?>
    
    @Transaction
    @Query("SELECT * FROM exams WHERE id = :id")
    suspend fun getExamWithQuestionsSync(id: Long): ExamWithQuestions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
    
    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)
    
    @Query("DELETE FROM questions WHERE examId = :examId")
    suspend fun deleteQuestionsForExam(examId: Long)

    @Query("SELECT * FROM questions WHERE examId = :examId ORDER BY orderPosition ASC")
    fun getQuestionsForExam(examId: Long): Flow<List<QuestionEntity>>
}
