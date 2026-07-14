package com.example.ui.pdf

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.pdf.PdfGenerator
import com.example.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    viewModel: MainViewModel,
    examId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val examInfo by viewModel.getExamWithQuestions(examId)
        .collectAsStateWithLifecycle(initialValue = null)
    
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val source = pdfFile
        if (uri != null && source != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    source.inputStream().use { input -> input.copyTo(output) }
                } ?: error("Unable to open selected destination")
            }.onSuccess {
                android.widget.Toast.makeText(context, "فایل PDF ذخیره شد.", android.widget.Toast.LENGTH_SHORT).show()
            }.onFailure {
                android.widget.Toast.makeText(context, "ذخیره فایل انجام نشد.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(examInfo) {
        examInfo?.let { examWithQs ->
            withContext(Dispatchers.IO) {
                val cacheFile = File(context.cacheDir, "preview_exam.pdf")
                val generator = PdfGenerator(context)
                val generated = generator.generatePdf(examWithQs, outputFile = cacheFile)
                
                pdfFile = generated
                
                generated?.let { file ->
                    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = PdfRenderer(fileDescriptor)
                    if (pdfRenderer.pageCount > 0) {
                        val page = pdfRenderer.openPage(0)
                        val bm = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        bm.eraseColor(android.graphics.Color.WHITE)
                        page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        previewBitmap = bm
                    }
                    pdfRenderer.close()
                    fileDescriptor.close()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پیش‌نمایش و خروجی") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 16.dp, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                examInfo?.let {
                                    val safeTitle = it.exam.title
                                        .replace(Regex("""[\\/:*?"<>|]"""), "_")
                                        .ifBlank { "Exam" }
                                    saveLauncher.launch("$safeTitle.pdf")
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("ذخیره PDF", maxLines = 1, softWrap = false, fontSize = 11.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                pdfFile?.let { file ->
                                    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "برنامه‌ای برای مشاهده PDF پیدا نشد.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("مشاهده PDF", maxLines = 1, softWrap = false, fontSize = 11.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                pdfFile?.let { file ->
                                    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(intent, "ارسال PDF"))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "برنامه‌ای برای ارسال فایل پیدا نشد.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("ارسال PDF", maxLines = 1, softWrap = false, fontSize = 11.sp)
                        }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (previewBitmap != null) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = androidx.compose.ui.graphics.RectangleShape
                ) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Preview",
                        modifier = Modifier.fillMaxWidth().aspectRatio(previewBitmap!!.width.toFloat() / previewBitmap!!.height.toFloat())
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "فقط صفحه اول پیش‌نمایش داده می‌شود. برای مشاهده دقیق همه صفحات، فایل را مشاهده یا ذخیره کنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
