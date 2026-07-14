package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import org.json.JSONArray

enum class QuestionType {
    MULTIPLE_CHOICE, 
    DESCRIPTIVE, 
    FILL_IN_BLANK, 
    TRUE_FALSE
}

enum class AnswerSpaceSize {
    SMALL, MEDIUM, LARGE, CUSTOM
}

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val province: String = "",
    val city: String = "",
    val schoolName: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val grade: String = "",
    val date: String = "",
    val duration: String = "",
    val totalScore: String = "",
    val instructionText: String = "",
    val showStudentName: Boolean = true,
    val showStudentClass: Boolean = true,
    val showStudentNumber: Boolean = true,
    val autoGroupByType: Boolean = true,
    val vocationalSchoolName: String = "",
    val major: String = "",
    val term: String = "",
    val academicYear: String = "",
    val universityName: String = "",
    val facultyName: String = "",
    val departmentName: String = "",
    val courseCode: String = "",
    val examSession: String = "",
    val examineeFirstName: String = "",
    val examineeLastName: String = "",
    val examineeClass: String = "",
    val examineeGrade: String = "",
    val institutionName: String = "",
    val templateId: String = "classic",
    val finalMessage: String = "",
    val lastEditedTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("examId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val type: QuestionType,
    val text: String,
    val score: Float,
    val orderPosition: Int,
    val options: List<String> = emptyList(),
    val answerSpaceSize: AnswerSpaceSize = AnswerSpaceSize.MEDIUM,
    val customLines: Int = 3,
    val imageUri: String = ""
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        for (item in value) array.put(item)
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(value)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (e: Exception) {}
        return list
    }
}
