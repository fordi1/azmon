package com.example.ui.exam

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.entity.AnswerSpaceSize
import com.example.data.entity.QuestionEntity
import com.example.data.entity.QuestionType
import com.example.ui.MainViewModel
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.utils.toEnglishDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuestionEditorScreen(
    viewModel: MainViewModel,
    examId: Long,
    questionId: Long,
    onNavigateBack: () -> Unit
) {
    var type by remember { mutableStateOf(QuestionType.DESCRIPTIVE) }
    var text by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(List(4) { "" }) }
    var answerSpaceSize by remember { mutableStateOf(AnswerSpaceSize.MEDIUM) }
    var customLines by remember { mutableStateOf(3) }
    var imageUri by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    
    var existingPosition by remember { mutableStateOf(0) }
    var scoreError by remember { mutableStateOf<String?>(null) }
    val score = scoreText.toEnglishDigits()
        .replace(',', '.')
        .replace('٫', '.')
        .toFloatOrNull()
    val scoreIsValid = scoreText.isBlank() || score != null && score > 0f
    val hasValidOptions = type != QuestionType.MULTIPLE_CHOICE ||
        (options.size >= 2 && options.all { it.isNotBlank() })
    val canSave = text.isNotBlank() && scoreIsValid && hasValidOptions
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val questionRequester = remember { BringIntoViewRequester() }
    val scoreRequester = remember { BringIntoViewRequester() }
    val customLinesRequester = remember { BringIntoViewRequester() }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            imageUri = it.toString()
        }
    }
    val bringIntoViewOnFocus: (BringIntoViewRequester) -> Modifier = { requester ->
        Modifier
            .bringIntoViewRequester(requester)
            .onFocusEvent { state ->
                if (state.isFocused) {
                    coroutineScope.launch {
                        delay(180)
                        requester.bringIntoView()
                    }
                }
            }
    }

    val saveQuestion = {
        val finalQ = QuestionEntity(
            id = questionId,
            examId = examId,
            type = type,
            text = text,
            score = score ?: 0f,
            orderPosition = existingPosition,
            options = if (type == QuestionType.MULTIPLE_CHOICE) {
                options.map { it.trim() }
            } else {
                emptyList()
            },
            answerSpaceSize = answerSpaceSize,
            customLines = customLines,
            imageUri = imageUri
        )
        viewModel.saveQuestion(
            question = finalQ,
            onComplete = onNavigateBack,
            onError = { scoreError = it }
        )
    }

    LaunchedEffect(questionId) {
        if (questionId != 0L) {
            isEditing = true
            val q = viewModel.getQuestionByIdStream(examId, questionId).firstOrNull()
            if (q != null) {
                type = q.type
                text = q.text
                scoreText = if (q.score > 0f) q.score.toString() else ""
                if (q.options.isNotEmpty()) options = q.options
                answerSpaceSize = q.answerSpaceSize
                customLines = q.customLines
                imageUri = q.imageUri
                existingPosition = q.orderPosition
            }
        } else {
            // Find next position for new question
            val existing = viewModel.getQuestionsForExam(examId).firstOrNull() ?: emptyList()
            existingPosition = existing.size
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "ویرایش سؤال" else "سؤال جدید") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (imageUri.isBlank()) "افزودن عکس" else "تغییر عکس")
                    }
                    Button(
                        onClick = saveQuestion,
                        modifier = Modifier.weight(1f),
                        enabled = canSave
                    ) {
                        Text(if (isEditing) "ذخیره ویرایش" else "ثبت سؤال")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (isKeyboardVisible) 8.dp else 12.dp)
        ) {
            // Type Selection
            if (!isEditing && !isKeyboardVisible) {
                Text("نوع سؤال", style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestionTypeCard(
                            type = QuestionType.MULTIPLE_CHOICE,
                            selectedType = type,
                            onClick = { type = QuestionType.MULTIPLE_CHOICE },
                            modifier = Modifier.weight(1f)
                        )
                        QuestionTypeCard(
                            type = QuestionType.DESCRIPTIVE,
                            selectedType = type,
                            onClick = { type = QuestionType.DESCRIPTIVE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestionTypeCard(
                            type = QuestionType.FILL_IN_BLANK,
                            selectedType = type,
                            onClick = { type = QuestionType.FILL_IN_BLANK },
                            modifier = Modifier.weight(1f)
                        )
                        QuestionTypeCard(
                            type = QuestionType.TRUE_FALSE,
                            selectedType = type,
                            onClick = { type = QuestionType.TRUE_FALSE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("متن سؤال") },
                    placeholder = { Text("صورت سؤال را بنویسید") },
                    modifier = Modifier
                        .weight(1f)
                        .then(bringIntoViewOnFocus(questionRequester)),
                    minLines = if (isKeyboardVisible) 1 else 2,
                    maxLines = if (isKeyboardVisible) 2 else 4
                )

                OutlinedTextField(
                    value = scoreText,
                    onValueChange = {
                        scoreText = it
                        scoreError = null
                    },
                    label = { Text("نمره سوال") },
                    placeholder = { Text("مثلاً ۲") },
                    modifier = Modifier
                        .width(96.dp)
                        .then(bringIntoViewOnFocus(scoreRequester)),
                    isError = !scoreIsValid || scoreError != null,
                    supportingText = {
                        when {
                            scoreError != null -> Text(scoreError!!)
                            !scoreIsValid -> Text("نمره باید عددی بزرگ‌تر از صفر باشد.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            if (!isKeyboardVisible) {
                Text("تصویر سؤال", style = MaterialTheme.typography.titleMedium)
                if (imageUri.isNotBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "تصویر سؤال",
                        modifier = Modifier
                            .size(144.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("تغییر تصویر")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { imageUri = "" }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("حذف تصویر")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("انتخاب عکس مربعی از گالری")
                    }
                }
            }

            // Dynamic fields based on type
            when (type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    if (!isKeyboardVisible) {
                        Text("گزینه‌ها", style = MaterialTheme.typography.titleMedium)
                    }
                    options.indices.chunked(2).forEach { rowIndexes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowIndexes.forEach { index ->
                                val optionRequester = remember(index) { BringIntoViewRequester() }
                                OutlinedTextField(
                                    value = options[index],
                                    onValueChange = { newVal ->
                                        val list = options.toMutableList()
                                        list[index] = newVal
                                        options = list
                                    },
                                    label = { Text("گزینه ${index + 1}") },
                                    placeholder = { Text("متن گزینه") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(bringIntoViewOnFocus(optionRequester)),
                                    singleLine = true
                                )
                            }
                            if (rowIndexes.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (!isKeyboardVisible) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { options = options + "" }) {
                                Text("افزودن گزینه")
                            }
                            if (options.size > 2) {
                                TextButton(onClick = { options = options.dropLast(1) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Text("حذف گزینه آخر")
                                }
                            }
                        }
                    }
                }
                QuestionType.DESCRIPTIVE -> {
                    Text("فضای پاسخ‌دهی", style = MaterialTheme.typography.titleMedium)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnswerSpaceSize.values().forEach { size ->
                            Surface(
                                onClick = { answerSpaceSize = size },
                                shape = MaterialTheme.shapes.small,
                                color = if (answerSpaceSize == size) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (answerSpaceSize == size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = when(size) {
                                        AnswerSpaceSize.SMALL -> "کوچک"
                                        AnswerSpaceSize.MEDIUM -> "متوسط"
                                        AnswerSpaceSize.LARGE -> "بزرگ"
                                        AnswerSpaceSize.CUSTOM -> "دلخواه"
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = if (answerSpaceSize == size) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (answerSpaceSize == AnswerSpaceSize.CUSTOM) {
                        OutlinedTextField(
                            value = customLines.toString(),
                            onValueChange = { value ->
                                customLines = value.toIntOrNull()?.coerceIn(1, 30) ?: 1
                            },
                            label = { Text("تعداد خطوط") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(bringIntoViewOnFocus(customLinesRequester)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
                QuestionType.FILL_IN_BLANK -> {
                    Text("راهنما: خط جای خالی در PDF به عنوان خط تیره (_) نمایش داده می‌شود.")
                }
                QuestionType.TRUE_FALSE -> {
                    // No additional fields needed normally
                }
            }
        }
    }

    scoreError?.let { message ->
        AlertDialog(
            onDismissRequest = { scoreError = null },
            title = { Text("خطای نمره") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { scoreError = null }) {
                    Text("متوجه شدم")
                }
            }
        )
    }
}

@Composable
fun QuestionTypeCard(
    type: com.example.data.entity.QuestionType,
    selectedType: com.example.data.entity.QuestionType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = type == selectedType
    val accentColor = when(type) {
        com.example.data.entity.QuestionType.MULTIPLE_CHOICE -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        com.example.data.entity.QuestionType.DESCRIPTIVE -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        com.example.data.entity.QuestionType.FILL_IN_BLANK -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        com.example.data.entity.QuestionType.TRUE_FALSE -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    }
    
    val title = when (type) {
        com.example.data.entity.QuestionType.MULTIPLE_CHOICE -> "تستی"
        com.example.data.entity.QuestionType.DESCRIPTIVE -> "تشریحی"
        com.example.data.entity.QuestionType.FILL_IN_BLANK -> "جای خالی"
        com.example.data.entity.QuestionType.TRUE_FALSE -> "صحیح/غلط"
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
