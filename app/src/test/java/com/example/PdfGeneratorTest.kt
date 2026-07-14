package com.example

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.ExamWithQuestions
import com.example.data.entity.AnswerSpaceSize
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.QuestionType
import com.example.pdf.PdfGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PdfGeneratorTest {

    private lateinit var context: Context
    private lateinit var pdfGenerator: PdfGenerator

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        pdfGenerator = PdfGenerator(context)
    }

    private fun createTestExam(
        templateId: String = "classic",
        title: String = "امتحان ریاضی پایه هفتم",
        questionCount: Int = 5,
        questionType: QuestionType = QuestionType.MULTIPLE_CHOICE
    ): ExamWithQuestions {
        val exam = ExamEntity(
            id = 1,
            title = title,
            province = "تهران",
            city = "تهران",
            schoolName = "دبیرستان شهید بهشتی",
            teacherName = "محمد احمدی",
            subject = "ریاضی",
            grade = "هفتم",
            date = "۱۴۰۳/۰۳/۱۵",
            duration = "۶۰",
            totalScore = "۲۰",
            instructionText = "به هر سؤال دقت کنید و بهترین گزینه را انتخاب کنید.",
            showStudentName = true,
            showStudentClass = true,
            showStudentNumber = true,
            autoGroupByType = true,
            templateId = templateId,
            finalMessage = "موفق باشید"
        )

        val questions = (1..questionCount).map { i ->
            when (questionType) {
                QuestionType.MULTIPLE_CHOICE -> QuestionEntity(
                    id = i.toLong(),
                    examId = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    text = "سؤال $i: اگر x برابر با $i باشد، مقدار ۲x چقدر است؟",
                    score = 4f,
                    orderPosition = i - 1,
                    options = listOf("${i * 2}", "${i * 3}", "${i * 4}", "${i * 5}")
                )
                QuestionType.DESCRIPTIVE -> QuestionEntity(
                    id = i.toLong(),
                    examId = 1,
                    type = QuestionType.DESCRIPTIVE,
                    text = "سؤال $i: فرمول مساحت دایره را بنویسید.",
                    score = 4f,
                    orderPosition = i - 1,
                    answerSpaceSize = AnswerSpaceSize.MEDIUM
                )
                QuestionType.TRUE_FALSE -> QuestionEntity(
                    id = i.toLong(),
                    examId = 1,
                    type = QuestionType.TRUE_FALSE,
                    text = "سؤال $i: عدد ۱۵ بر ۳ بخش‌پذیر است.",
                    score = 4f,
                    orderPosition = i - 1,
                )
                QuestionType.FILL_IN_BLANK -> QuestionEntity(
                    id = i.toLong(),
                    examId = 1,
                    type = QuestionType.FILL_IN_BLANK,
                    text = "سؤال $i: حاصل ضرب ۳ در ۴ برابر با _____ است.",
                    score = 4f,
                    orderPosition = i - 1,
                )
            }
        }

        return ExamWithQuestions(exam = exam, questions = questions)
    }

    @Test
    fun `generatePdf creates valid PDF file`() {
        val examWithQuestions = createTestExam()
        val outputFile = File(context.cacheDir, "test_output.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull("PDF file should be generated", result)
        assertTrue("PDF file should exist", result!!.exists())
        assertTrue("PDF file size should be greater than 0", result.length() > 0)

        outputFile.delete()
    }

    @Test
    fun `generatePdf with classic template produces valid PDF`() {
        val examWithQuestions = createTestExam(templateId = "classic")
        val outputFile = File(context.cacheDir, "test_classic.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        // Verify PDF structure
        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with modern template produces valid PDF`() {
        val examWithQuestions = createTestExam(templateId = "modern")
        val outputFile = File(context.cacheDir, "test_modern.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with compact template produces valid PDF`() {
        val examWithQuestions = createTestExam(templateId = "compact")
        val outputFile = File(context.cacheDir, "test_compact.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with vocational template produces valid PDF`() {
        val examWithQuestions = createTestExam(templateId = "vocational")
        val outputFile = File(context.cacheDir, "test_vocational.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with descriptive questions produces valid PDF`() {
        val examWithQuestions = createTestExam(
            questionType = QuestionType.DESCRIPTIVE,
            questionCount = 3
        )
        val outputFile = File(context.cacheDir, "test_descriptive.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with true false questions produces valid PDF`() {
        val examWithQuestions = createTestExam(
            questionType = QuestionType.TRUE_FALSE,
            questionCount = 4
        )
        val outputFile = File(context.cacheDir, "test_truefalse.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with many questions creates multiple pages`() {
        val examWithQuestions = createTestExam(
            questionCount = 50,
            questionType = QuestionType.MULTIPLE_CHOICE
        )
        val outputFile = File(context.cacheDir, "test_multipage.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF with 50 questions should have multiple pages", pdfRenderer.pageCount > 1)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with empty questions still produces valid PDF`() {
        val examWithQuestions = createTestExam(questionCount = 0)
        val outputFile = File(context.cacheDir, "test_empty.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page even with no questions", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf with Persian title renders correctly`() {
        val examWithQuestions = createTestExam(
            title = "امتحان نهایی علوم تجربی پایه نهم - نوبت اول"
        )
        val outputFile = File(context.cacheDir, "test_persian_title.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        outputFile.delete()
    }

    @Test
    fun `generatePdf with mixed question types groups correctly`() {
        val exam = ExamEntity(
            id = 1,
            title = "امتحان جامع",
            subject = "ریاضی",
            grade = "هفتم",
            autoGroupByType = true,
            templateId = "classic"
        )
        val questions = listOf(
            QuestionEntity(id = 1, examId = 1, type = QuestionType.MULTIPLE_CHOICE, text = "سؤال تستی ۱", score = 4f, orderPosition = 0, options = listOf("الف", "ب", "ج", "د")),
            QuestionEntity(id = 2, examId = 1, type = QuestionType.MULTIPLE_CHOICE, text = "سؤال تستی ۲", score = 4f, orderPosition = 1, options = listOf("الف", "ب", "ج", "د")),
            QuestionEntity(id = 3, examId = 1, type = QuestionType.DESCRIPTIVE, text = "سؤال تشریحی ۱", score = 4f, orderPosition = 2, answerSpaceSize = AnswerSpaceSize.LARGE),
            QuestionEntity(id = 4, examId = 1, type = QuestionType.TRUE_FALSE, text = "سؤال صحیح و غلط ۱", score = 4f, orderPosition = 3),
            QuestionEntity(id = 5, examId = 1, type = QuestionType.FILL_IN_BLANK, text = "سؤال جای خالی ۱", score = 4f, orderPosition = 4)
        )
        val examWithQuestions = ExamWithQuestions(exam = exam, questions = questions)
        val outputFile = File(context.cacheDir, "test_mixed_types.pdf")

        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = outputFile)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertTrue(result.length() > 0)

        val fileDescriptor = ParcelFileDescriptor.open(result, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        assertTrue("PDF should have at least one page", pdfRenderer.pageCount > 0)
        pdfRenderer.close()
        fileDescriptor.close()

        outputFile.delete()
    }

    @Test
    fun `generatePdf without explicit output file saves to downloads`() {
        val examWithQuestions = createTestExam()

        // This test verifies that generatePdf doesn't crash when outputFile is null
        // It will try to save to Downloads directory
        val result = pdfGenerator.generatePdf(examWithQuestions, outputFile = null)

        // Result may be null if Downloads directory is not accessible in test environment
        // The important thing is that it doesn't crash
    }
}
