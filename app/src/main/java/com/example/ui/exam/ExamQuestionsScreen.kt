package com.example.ui.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.entity.QuestionEntity
import com.example.data.entity.QuestionType
import com.example.ui.MainViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamQuestionsScreen(
    viewModel: MainViewModel,
    examId: Long,
    onNavigateBack: () -> Unit,
    onEditExamInfo: () -> Unit,
    onAddQuestion: (Long) -> Unit,
    onEditQuestion: (Long, Long) -> Unit,
    onPreviewPdf: (Long) -> Unit
) {
    val questions by viewModel.getQuestionsForExam(examId).collectAsStateWithLifecycle(initialValue = emptyList())
    val groupedQuestions = remember(questions) {
        questions.sortedWith(compareBy({ it.type.ordinal }, { it.orderPosition }))
    }
    var pendingDelete by remember { mutableStateOf<QuestionEntity?>(null) }
    var scoreError by remember { mutableStateOf<String?>(null) }

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

    pendingDelete?.let { question ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف سؤال") },
            text = { Text("این سؤال از برگه آزمون حذف شود؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQuestion(question)
                        pendingDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت سؤالات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onEditExamInfo) {
                        Icon(Icons.Default.Settings, contentDescription = "تنظیمات آزمون")
                    }
                    IconButton(onClick = { onPreviewPdf(examId) }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "پیش‌نمایش PDF")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddQuestion(examId) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن سؤال جدید") }
            )
        }
    ) { padding ->
        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "هیچ سؤالی اضافه نشده است.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groupedQuestions, key = { it.id }) { question ->
                    QuestionCard(
                        question = question,
                        onEdit = { onEditQuestion(examId, question.id) },
                        onDelete = { pendingDelete = question },
                        onDuplicate = {
                            val act = question.copy(id = 0, orderPosition = questions.size)
                            viewModel.saveQuestion(
                                question = act,
                                onComplete = {},
                                onError = { scoreError = it }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: QuestionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    val accentColor = when(question.type) {
        QuestionType.MULTIPLE_CHOICE -> androidx.compose.ui.graphics.Color(0xFF2196F3)
        QuestionType.DESCRIPTIVE -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        QuestionType.FILL_IN_BLANK -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        QuestionType.TRUE_FALSE -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when(question.type){
                            QuestionType.MULTIPLE_CHOICE -> "تستی"
                            QuestionType.DESCRIPTIVE -> "تشریحی"
                            QuestionType.FILL_IN_BLANK -> "جای خالی"
                            QuestionType.TRUE_FALSE -> "صحیح/غلط"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (question.score > 0f) {
                    Text(text = "نمره: ${question.score}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${question.orderPosition + 1}. ${question.text}", style = MaterialTheme.typography.bodyLarge)
            if (question.imageUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = question.imageUri,
                    contentDescription = "تصویر سؤال",
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDuplicate) { Text("کپی") }
                TextButton(onClick = onDelete) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
