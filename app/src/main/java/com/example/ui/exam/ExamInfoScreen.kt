package com.example.ui.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.ExamEntity
import com.example.ui.MainViewModel
import com.example.utils.toEnglishDigits
import com.example.utils.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamInfoScreen(
    viewModel: MainViewModel,
    examId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToQuestions: (Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var examineeClass by remember { mutableStateOf("") }
    var examineeGrade by remember { mutableStateOf("") }
    var institutionName by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var instruction by remember { mutableStateOf("") }
    var finalMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val durationNumber = duration.toEnglishDigits().toIntOrNull()
    val requiredFieldsAreValid =
        duration.isBlank() || durationNumber != null && durationNumber > 0
    
    // Load existing exam if not new
    LaunchedEffect(examId) {
        if (examId != 0L) {
            val exam = viewModel.getExamById(examId)
            exam.collect { existing ->
                if (existing != null) {
                    title = existing.title
                    examineeClass = existing.examineeClass
                    examineeGrade = existing.examineeGrade.ifBlank { existing.grade }
                    institutionName = existing.institutionName
                    schoolName = existing.schoolName.ifBlank { existing.universityName }
                    teacherName = existing.teacherName
                    subject = existing.subject
                    date = existing.date
                    duration = existing.duration
                    instruction = existing.instructionText
                    finalMessage = existing.finalMessage
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (examId == 0L) "اطلاعات آزمون جدید" else "ویرایش اطلاعات آزمون") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!requiredFieldsAreValid) {
                        Text(
                            "مدت آزمون باید به‌صورت عدد معتبر وارد شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                        val newExam = ExamEntity(
                            id = examId,
                            title = title,
                            examineeClass = examineeClass,
                            examineeGrade = examineeGrade,
                            institutionName = institutionName,
                            schoolName = schoolName,
                            teacherName = teacherName,
                            subject = subject,
                            grade = examineeGrade,
                            date = date,
                            duration = duration,
                            instructionText = instruction,
                            autoGroupByType = true,
                            universityName = schoolName,
                            finalMessage = finalMessage
                        )
                        if (examId == 0L) {
                            viewModel.insertExam(newExam) { id ->
                                onNavigateToQuestions(id)
                            }
                        } else {
                            viewModel.updateExam(newExam)
                            onNavigateToQuestions(examId)
                        }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = requiredFieldsAreValid
                    ) {
                        Text(if (examId == 0L) "مرحله بعد: افزودن سؤالات" else "ذخیره و مدیریت سؤالات")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showDatePicker) {
                com.example.ui.components.PersianDatePicker(
                    initialDate = date,
                    onDateSelected = { 
                        date = it
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }
            
            Text("مشخصات آزمون‌دهنده", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderTextField(
                    value = examineeClass,
                    onValueChange = { examineeClass = it },
                    label = "آزمون رشته",
                    placeholder = "مثلاً تجربی، ریاضی و ...",
                    modifier = Modifier.weight(1f)
                )
                HeaderTextField(
                    value = examineeGrade,
                    onValueChange = { examineeGrade = it },
                    label = "پایه",
                    placeholder = "مثلاً پایه دهم",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()
            Text("مشخصات مرکز و آزمون", style = MaterialTheme.typography.titleMedium)
            HeaderTextField(
                value = institutionName,
                onValueChange = { institutionName = it },
                label = "نهاد",
                placeholder = "مثلاً آموزش و پرورش، وزارت علوم، سازمان فنی و حرفه‌ای و ...",
                modifier = Modifier.fillMaxWidth()
            )
            HeaderTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = "نام آموزشگاه / مدرسه",
                placeholder = "مثلاً دبیرستان، هنرستان، آموزشگاه آزاد و ...",
                modifier = Modifier.fillMaxWidth()
            )
            HeaderTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "عنوان درس",
                placeholder = "مثلاً ریاضی، فیزیک، فارسی و ...",
                modifier = Modifier.fillMaxWidth()
            )
            HeaderTextField(
                value = title,
                onValueChange = { title = it },
                label = "نوع آزمون",
                placeholder = "مثلاً نوبت دوم، مستمر، شهریور، میان‌ترم و ...",
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text("مشخصات برگزاری", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = date.toPersianDigits(),
                        onValueChange = { },
                        label = { Text("تاریخ") },
                        placeholder = { Text("انتخاب تاریخ") },
                        readOnly = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }
                OutlinedTextField(
                    value = duration.toPersianDigits(),
                    onValueChange = { value ->
                        duration = value
                            .toEnglishDigits()
                            .filter { it.isDigit() }
                            .take(3)
                            .toPersianDigits()
                    },
                    label = { Text("وقت / مدت آزمون") },
                    placeholder = { Text("برای مثال: ۹۰ دقیقه") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
                    modifier = Modifier.weight(1f),
                    isError = duration.isNotBlank() && (durationNumber == null || durationNumber <= 0),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            HeaderTextField(
                value = teacherName,
                onValueChange = { teacherName = it },
                label = "نام دبیر",
                placeholder = "مثلاً علی رضایی",
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            OutlinedTextField(
                value = instruction,
                onValueChange = { instruction = it },
                label = { Text("متن راهنما") },
                placeholder = { Text("راهنمای لازم برای پاسخ‌گویی به آزمون") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right)
            )
            OutlinedTextField(
                value = finalMessage,
                onValueChange = { finalMessage = it },
                label = { Text("متن پایان آزمون") },
                placeholder = { Text("متنی که بعد از آخرین سؤال نمایش داده می‌شود") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeaderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(placeholder, textAlign = TextAlign.Right)
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Right),
        modifier = modifier,
        singleLine = true
    )
}
