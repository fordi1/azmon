package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExamDao
import com.example.data.entity.Converters
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity

@Database(entities = [ExamEntity::class, QuestionEntity::class], version = 6, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun examDao(): ExamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exam_designer_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exams ADD COLUMN universityName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN facultyName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN departmentName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN courseCode TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN examSession TEXT NOT NULL DEFAULT ''")

                db.execSQL(
                    """
                    CREATE TABLE questions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        examId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        text TEXT NOT NULL,
                        score REAL NOT NULL,
                        orderPosition INTEGER NOT NULL,
                        options TEXT NOT NULL,
                        answerSpaceSize TEXT NOT NULL,
                        customLines INTEGER NOT NULL,
                        FOREIGN KEY(examId) REFERENCES exams(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO questions_new
                        (id, examId, type, text, score, orderPosition, options, answerSpaceSize, customLines)
                    SELECT id, examId, type, text, score, orderPosition, options, answerSpaceSize, customLines
                    FROM questions
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE questions")
                db.execSQL("ALTER TABLE questions_new RENAME TO questions")
                db.execSQL("CREATE INDEX index_questions_examId ON questions(examId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN imageUri TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exams ADD COLUMN examineeFirstName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN examineeLastName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN examineeClass TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN examineeGrade TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exams ADD COLUMN institutionName TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
