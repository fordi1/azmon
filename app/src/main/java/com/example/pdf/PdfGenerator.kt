package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.example.data.dao.ExamWithQuestions
import com.example.data.entity.AnswerSpaceSize
import com.example.data.entity.QuestionEntity
import com.example.data.entity.QuestionType
import com.example.utils.toPersianDigits
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

class PdfGenerator(private val context: Context) {

    private val fontRegular: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "fonts/Vazirmatn-Regular.ttf")
    }

    private val fontBold: Typeface by lazy {
        Typeface.createFromAsset(context.assets, "fonts/Vazirmatn-Bold.ttf")
    }

    fun generatePdf(examInfo: ExamWithQuestions, outputFile: File? = null): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val pageWidth = pageInfo.pageWidth.toFloat()
        val pageHeight = pageInfo.pageHeight.toFloat()
        val margin = 30f
        val footerTop = pageHeight - 27f

        val bodyPaint = textPaint(10.2f)
        val smallPaint = textPaint(8.8f)
        val boldPaint = textPaint(10.5f, bold = true)
        val titlePaint = textPaint(14.5f, bold = true)
        val sectionPaint = textPaint(10.2f, bold = true)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(75, 82, 105)
            strokeWidth = 0.75f
            style = Paint.Style.STROKE
        }
        val answerLinePaint = Paint(linePaint).apply {
            color = Color.rgb(190, 194, 206)
            strokeWidth = 0.55f
        }
        val sectionBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 242, 250)
            style = Paint.Style.FILL
        }
        val imageBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(170, 175, 190)
            strokeWidth = 0.7f
            style = Paint.Style.STROKE
        }
        val imageSize = 92f
        val imageGap = 6f
        val imageCache = mutableMapOf<String, Bitmap?>()

        fun questionBitmap(question: QuestionEntity): Bitmap? {
            if (question.imageUri.isBlank()) return null
            return imageCache.getOrPut(question.imageUri) {
                runCatching {
                    val uri = Uri.parse(question.imageUri)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, bounds)
                    }
                    var sampleSize = 1
                    while (
                        bounds.outWidth / sampleSize > 1024 ||
                        bounds.outHeight / sampleSize > 1024
                    ) {
                        sampleSize *= 2
                    }
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                    }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }.getOrNull()
            }
        }

        var pageNumber = 1
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin

        fun layout(
            text: String,
            width: Int,
            paint: TextPaint,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            maxLines: Int = Int.MAX_VALUE
        ): StaticLayout {
            val value = text.toPersianDigits()
            return StaticLayout.Builder.obtain(value, 0, value.length, paint, width.coerceAtLeast(1))
                .setAlignment(alignment)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setLineSpacing(0f, 1.12f)
                .setIncludePad(false)
                .setBreakStrategy(android.text.Layout.BREAK_STRATEGY_SIMPLE)
                .setMaxLines(maxLines)
                .build()
        }

        fun textHeight(
            text: String,
            width: Int,
            paint: TextPaint,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
        ): Float = layout(text, width, paint, alignment).height.toFloat()

        fun drawText(
            text: String,
            x: Float,
            top: Float,
            width: Int,
            paint: TextPaint,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            maxLines: Int = Int.MAX_VALUE
        ): Float {
            val textLayout = layout(text, width, paint, alignment, maxLines)
            canvas.save()
            canvas.translate(x, top)
            textLayout.draw(canvas)
            canvas.restore()
            return textLayout.height.toFloat()
        }

        fun drawFooter() {
            drawText(
                "صفحه ${pageNumber.toString().toPersianDigits()}",
                margin,
                pageHeight - 19f,
                (pageWidth - 2 * margin).toInt(),
                smallPaint,
                Layout.Alignment.ALIGN_CENTER
            )
        }

        fun finishAndStartPage(showTitle: Boolean = true) {
            drawFooter()
            document.finishPage(page)
            pageNumber++
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin
            if (showTitle) {
                drawText(
                    "${examInfo.exam.title} - ${examInfo.exam.subject}",
                    margin,
                    y,
                    (pageWidth - 2 * margin).toInt(),
                    boldPaint,
                    Layout.Alignment.ALIGN_CENTER,
                    maxLines = 1
                )
                y += 19f
            }
        }

        fun drawHeader() {
            val contentWidth = pageWidth - 2 * margin
            drawText(
                "بسمه تعالی",
                margin,
                y,
                contentWidth.toInt(),
                titlePaint,
                Layout.Alignment.ALIGN_CENTER,
                maxLines = 1
            )
            y += textHeight("بسمه تعالی", contentWidth.toInt(), titlePaint, Layout.Alignment.ALIGN_CENTER) + 7f

            val rightWidth = (contentWidth / 3f).toInt()
            val centerWidth = (contentWidth / 3f).toInt()
            val leftWidth = contentWidth.toInt() - rightWidth - centerWidth
            val leftX = margin
            val centerX = leftX + leftWidth
            val rightX = centerX + centerWidth
            val innerPadding = 6f

            fun valueOrDots(value: String): String =
                value.trim().ifBlank { "........................" }

            val rightLines = listOf(
                "نام: ........................",
                "نام خانوادگی: ........................",
                "آزمون رشته: ${valueOrDots(examInfo.exam.examineeClass)}",
                "پایه: ${valueOrDots(examInfo.exam.examineeGrade)}"
            )
            val schoolName = examInfo.exam.schoolName.ifBlank { examInfo.exam.universityName }
            val centerLines = listOf(
                examInfo.exam.institutionName,
                schoolName,
                examInfo.exam.subject,
                examInfo.exam.title
            ).map(String::trim).filter(String::isNotBlank)
            val durationValue = examInfo.exam.duration.trim()
                .takeIf(String::isNotBlank)
                ?.let { "$it دقیقه" }
                .orEmpty()
            val leftLines = listOf(
                "تاریخ: ${valueOrDots(examInfo.exam.date)}",
                "وقت آزمون: ${valueOrDots(durationValue)}",
                "نام دبیر: ${valueOrDots(examInfo.exam.teacherName)}",
                "محل مهر آموزشگاه:"
            )

            fun columnHeight(lines: List<String>, width: Int): Float =
                lines.sumOf { (textHeight(it, width, smallPaint) + 3f).toDouble() }.toFloat()

            val rightInnerWidth = rightWidth - (innerPadding * 2).toInt()
            val centerInnerWidth = centerWidth - (innerPadding * 2).toInt()
            val leftInnerWidth = leftWidth - (innerPadding * 2).toInt()
            val headerHeight = max(
                72f,
                max(
                    columnHeight(rightLines, rightInnerWidth),
                    max(
                        columnHeight(centerLines, centerInnerWidth),
                        columnHeight(leftLines, leftInnerWidth)
                    )
                ) + innerPadding * 2
            )

            canvas.drawRect(margin, y, pageWidth - margin, y + headerHeight, linePaint)
            canvas.drawLine(centerX, y, centerX, y + headerHeight, linePaint)
            canvas.drawLine(rightX, y, rightX, y + headerHeight, linePaint)

            fun drawColumn(
                lines: List<String>,
                x: Float,
                width: Int,
                alignment: Layout.Alignment,
                boldAll: Boolean = false
            ) {
                canvas.save()
                canvas.clipRect(
                    x + 1f,
                    y + 1f,
                    x + width - 1f,
                    y + headerHeight - 1f
                )
                var lineY = y + innerPadding
                lines.forEach { value ->
                    val paint = if (boldAll) boldPaint else smallPaint
                    lineY += drawText(
                        value,
                        x + innerPadding,
                        lineY,
                        width - (innerPadding * 2).toInt(),
                        paint,
                        alignment
                    ) + 3f
                }
                canvas.restore()
            }

            drawColumn(rightLines, rightX, rightWidth, Layout.Alignment.ALIGN_NORMAL)
            drawColumn(centerLines, centerX, centerWidth, Layout.Alignment.ALIGN_CENTER, boldAll = true)
            drawColumn(leftLines, leftX, leftWidth, Layout.Alignment.ALIGN_NORMAL)
            y += headerHeight + 7f

            if (examInfo.exam.instructionText.isNotBlank()) {
                val instructionHeight = textHeight(
                    examInfo.exam.instructionText,
                    contentWidth.toInt() - 12,
                    smallPaint
                ) + 10f
                canvas.drawRect(margin, y, pageWidth - margin, y + instructionHeight, linePaint)
                drawText(
                    examInfo.exam.instructionText,
                    margin + 6f,
                    y + 5f,
                    contentWidth.toInt() - 12,
                    smallPaint
                )
                y += instructionHeight + 6f
            }
        }

        val scoreWidth = 34f
        val numberWidth = 34f
        val tableLeft = margin
        val tableRight = pageWidth - margin
        val contentLeft = tableLeft + scoreWidth
        val contentRight = tableRight - numberWidth
        val questionTextX = contentLeft + 6f
        val questionTextWidth = (contentRight - contentLeft - 12f).toInt()

        fun drawTableHeader() {
            val height = 22f
            canvas.drawRect(tableLeft, y, tableRight, y + height, sectionBackgroundPaint)
            canvas.drawRect(tableLeft, y, tableRight, y + height, linePaint)
            canvas.drawLine(contentLeft, y, contentLeft, y + height, linePaint)
            canvas.drawLine(contentRight, y, contentRight, y + height, linePaint)
            drawText("بارم", tableLeft, y + 5f, scoreWidth.toInt(), boldPaint, Layout.Alignment.ALIGN_CENTER)
            drawText(
                "سؤالات",
                contentLeft,
                y + 5f,
                (contentRight - contentLeft).toInt(),
                boldPaint,
                Layout.Alignment.ALIGN_CENTER
            )
            drawText("ردیف", contentRight, y + 5f, numberWidth.toInt(), boldPaint, Layout.Alignment.ALIGN_CENTER)
            y += height
        }

        fun sectionHeight(type: QuestionType): Float =
            textHeight("سؤالات ${questionTypeTitle(type)}", questionTextWidth, sectionPaint) + 10f

        fun drawSection(type: QuestionType) {
            val height = sectionHeight(type)
            canvas.drawRect(tableLeft, y, tableRight, y + height, sectionBackgroundPaint)
            canvas.drawRect(tableLeft, y, tableRight, y + height, linePaint)
            drawText(
                "سؤالات ${questionTypeTitle(type)}",
                questionTextX,
                y + 5f,
                questionTextWidth,
                sectionPaint
            )
            y += height
        }

        fun optionRows(question: QuestionEntity): List<List<String>> =
            question.options.filter { it.isNotBlank() }.chunked(2)

        fun questionContentX(hasImage: Boolean): Float =
            questionTextX + if (hasImage) imageSize + imageGap else 0f

        fun questionContentWidth(hasImage: Boolean): Int =
            (questionTextWidth - if (hasImage) imageSize + imageGap else 0f)
                .toInt()
                .coerceAtLeast(80)

        fun optionRowHeight(options: List<String>, contentWidth: Int): Float {
            val halfWidth = (contentWidth - 8) / 2
            return options.maxOfOrNull {
                textHeight(it, halfWidth - 18, smallPaint)
            }?.plus(4f) ?: 0f
        }

        fun descriptiveSpace(question: QuestionEntity): Float = when (question.answerSpaceSize) {
            AnswerSpaceSize.SMALL -> 30f
            AnswerSpaceSize.MEDIUM -> 55f
            AnswerSpaceSize.LARGE -> 90f
            AnswerSpaceSize.CUSTOM -> question.customLines.coerceIn(1, 20) * 15f
        }

        fun questionHeight(question: QuestionEntity): Float {
            val hasImage = questionBitmap(question) != null
            val contentWidth = questionContentWidth(hasImage)
            var contentHeight = textHeight(question.text, contentWidth, bodyPaint) + 10f
            contentHeight += when (question.type) {
                QuestionType.MULTIPLE_CHOICE ->
                    optionRows(question)
                        .sumOf { optionRowHeight(it, contentWidth).toDouble() }
                        .toFloat() + 3f
                QuestionType.TRUE_FALSE ->
                    textHeight("صحیح (     )     غلط (     )", contentWidth, smallPaint) + 4f
                QuestionType.FILL_IN_BLANK -> 2f
                QuestionType.DESCRIPTIVE -> descriptiveSpace(question) + 3f
            }
            val imageHeight = if (hasImage) imageSize + 10f else 0f
            return max(contentHeight, imageHeight).coerceAtLeast(28f)
        }

        fun drawQuestion(question: QuestionEntity, number: Int) {
            val height = questionHeight(question)
            canvas.drawRect(tableLeft, y, tableRight, y + height, linePaint)
            canvas.drawLine(contentLeft, y, contentLeft, y + height, linePaint)
            canvas.drawLine(contentRight, y, contentRight, y + height, linePaint)

            val centerOffset = max(4f, (height - bodyPaint.textSize) / 2f - 2f)
            drawText(
                scoreText(question.score),
                tableLeft,
                y + centerOffset,
                scoreWidth.toInt(),
                bodyPaint,
                Layout.Alignment.ALIGN_CENTER,
                maxLines = 1
            )
            drawText(
                number.toString(),
                contentRight,
                y + centerOffset,
                numberWidth.toInt(),
                bodyPaint,
                Layout.Alignment.ALIGN_CENTER,
                maxLines = 1
            )

            canvas.save()
            canvas.clipRect(contentLeft + 2f, y + 1f, contentRight - 2f, y + height - 1f)
            val bitmap = questionBitmap(question)
            val hasImage = bitmap != null
            val contentX = questionContentX(hasImage)
            val contentWidth = questionContentWidth(hasImage)
            var contentY = y + 5f
            contentY += drawText(
                question.text,
                contentX,
                contentY,
                contentWidth,
                bodyPaint
            ) + 4f

            bitmap?.let {
                val sourceSize = minOf(bitmap.width, bitmap.height)
                val sourceLeft = (bitmap.width - sourceSize) / 2
                val sourceTop = (bitmap.height - sourceSize) / 2
                val destination = android.graphics.RectF(
                    questionTextX,
                    y + 5f,
                    questionTextX + imageSize,
                    y + 5f + imageSize
                )
                canvas.drawBitmap(
                    bitmap,
                    android.graphics.Rect(
                        sourceLeft,
                        sourceTop,
                        sourceLeft + sourceSize,
                        sourceTop + sourceSize
                    ),
                    destination,
                    null
                )
                canvas.drawRect(destination, imageBorderPaint)
            }

            when (question.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    val labels = listOf("الف", "ب", "ج", "د", "هـ", "و")
                    val halfWidth = (contentWidth - 8) / 2
                    var optionIndex = 0
                    optionRows(question).forEach { row ->
                        val rowHeight = optionRowHeight(row, contentWidth)
                        row.forEachIndexed { index, option ->
                            val isRightCell = index == 0
                            val cellX = if (isRightCell) {
                                contentX + halfWidth + 8
                            } else {
                                contentX
                            }
                            val label = labels.getOrElse(optionIndex) { (optionIndex + 1).toString() }
                            drawText(
                                "$label) $option",
                                cellX,
                                contentY,
                                halfWidth - 4,
                                smallPaint
                            )
                            optionIndex++
                        }
                        contentY += rowHeight
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    drawText(
                        "صحیح (     )          غلط (     )",
                        contentX,
                        contentY,
                        contentWidth,
                        smallPaint
                    )
                }
                QuestionType.FILL_IN_BLANK -> Unit
                QuestionType.DESCRIPTIVE -> {
                    val bottom = y + height - 5f
                    var lineY = contentY + 12f
                    while (lineY < bottom) {
                        canvas.drawLine(contentX, lineY, contentX + contentWidth, lineY, answerLinePaint)
                        lineY += 15f
                    }
                }
            }
            canvas.restore()
            y += height
        }

        drawHeader()
        drawTableHeader()

        val orderedQuestions = examInfo.questions.sortedWith(
            compareBy<QuestionEntity>({ questionTypeOrder(it.type) }, { it.orderPosition })
        )
        var currentType: QuestionType? = null
        orderedQuestions.forEachIndexed { index, question ->
            val needsSection = currentType != question.type
            val requiredHeight = questionHeight(question) +
                if (needsSection) sectionHeight(question.type) else 0f

            if (y + requiredHeight > footerTop) {
                finishAndStartPage()
                drawTableHeader()
            }

            if (needsSection) {
                currentType = question.type
                drawSection(question.type)
            }
            drawQuestion(question, index + 1)
        }

        val scoredQuestions = orderedQuestions.filter { it.score > 0f }
        if (scoredQuestions.isNotEmpty()) {
            val totalRowHeight = 24f
            if (y + totalRowHeight > footerTop) {
                finishAndStartPage()
                drawTableHeader()
            }
            val totalScore = scoredQuestions.sumOf { it.score.toDouble() }.toFloat()
            canvas.drawRect(tableLeft, y, tableRight, y + totalRowHeight, sectionBackgroundPaint)
            canvas.drawRect(tableLeft, y, tableRight, y + totalRowHeight, linePaint)
            canvas.drawLine(contentLeft, y, contentLeft, y + totalRowHeight, linePaint)
            canvas.drawLine(contentRight, y, contentRight, y + totalRowHeight, linePaint)
            drawText(
                scoreText(totalScore),
                tableLeft,
                y + 6f,
                scoreWidth.toInt(),
                boldPaint,
                Layout.Alignment.ALIGN_CENTER,
                maxLines = 1
            )
            drawText(
                "جمع نمرات",
                contentLeft,
                y + 6f,
                (contentRight - contentLeft).toInt(),
                boldPaint,
                Layout.Alignment.ALIGN_CENTER,
                maxLines = 1
            )
            y += totalRowHeight
        }

        if (examInfo.exam.finalMessage.isNotBlank()) {
            val finalHeight = textHeight(
                examInfo.exam.finalMessage,
                (pageWidth - 2 * margin).toInt(),
                boldPaint,
                Layout.Alignment.ALIGN_CENTER
            ) + 14f
            if (y + finalHeight > footerTop) {
                finishAndStartPage()
            }
            drawText(
                examInfo.exam.finalMessage,
                margin,
                y + 8f,
                (pageWidth - 2 * margin).toInt(),
                boldPaint,
                Layout.Alignment.ALIGN_CENTER
            )
        }

        drawFooter()
        document.finishPage(page)

        return try {
            val file = outputFile ?: File(
                context.cacheDir,
                "Exam_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
            )
            FileOutputStream(file).use(document::writeTo)
            document.close()
            imageCache.values.filterNotNull().forEach(Bitmap::recycle)
            file
        } catch (error: Exception) {
            error.printStackTrace()
            document.close()
            imageCache.values.filterNotNull().forEach(Bitmap::recycle)
            null
        }
    }

    private fun questionTypeOrder(type: QuestionType): Int = when (type) {
        QuestionType.MULTIPLE_CHOICE -> 0
        QuestionType.TRUE_FALSE -> 1
        QuestionType.FILL_IN_BLANK -> 2
        QuestionType.DESCRIPTIVE -> 3
    }

    private fun questionTypeTitle(type: QuestionType): String = when (type) {
        QuestionType.MULTIPLE_CHOICE -> "تستی"
        QuestionType.TRUE_FALSE -> "صحیح/غلط"
        QuestionType.FILL_IN_BLANK -> "جای خالی"
        QuestionType.DESCRIPTIVE -> "تشریحی"
    }

    private fun textPaint(size: Float, bold: Boolean = false): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(28, 31, 52)
            textSize = size
            typeface = if (bold) fontBold else fontRegular
            textAlign = Paint.Align.LEFT
        }

    private fun scoreText(score: Float): String =
        when {
            score <= 0f -> ""
            score % 1f == 0f -> score.toInt().toString()
            else -> score.toString()
        }
}
