package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.repository.ExamRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.exam.ExamInfoScreen
import com.example.ui.exam.ExamQuestionsScreen
import com.example.ui.exam.QuestionEditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ExamRepository(database.examDao()) }
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var splashVisible by remember { mutableStateOf(true) }
                    if (splashVisible) {
                        SplashScreen(onFinished = { splashVisible = false })
                    } else {
                        AppNavigation(viewModel)
                    }
                }
            }
        }
    }
    
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onCreateNewExam = { navController.navigate("exam_info/0") },
                onOpenExam = { examId -> navController.navigate("exam_questions/$examId") }
            )
        }
        composable(
            "exam_info/{examId}",
            arguments = listOf(navArgument("examId") { type = NavType.LongType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            ExamInfoScreen(
                viewModel = viewModel,
                examId = examId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuestions = {
                    navController.navigate("exam_questions/$it") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable(
            "exam_questions/{examId}",
            arguments = listOf(navArgument("examId") { type = NavType.LongType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            ExamQuestionsScreen(
                viewModel = viewModel,
                examId = examId,
                onNavigateBack = { navController.popBackStack() },
                onEditExamInfo = { navController.navigate("exam_info/$examId") },
                onAddQuestion = { navController.navigate("question_editor/$examId/0") },
                onEditQuestion = { eId, qId -> navController.navigate("question_editor/$eId/$qId") },
                onPreviewPdf = { eId -> navController.navigate("preview/$eId") }
            )
        }
        composable(
            "question_editor/{examId}/{questionId}",
            arguments = listOf(
                navArgument("examId") { type = NavType.LongType },
                navArgument("questionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
           val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
           val questionId = backStackEntry.arguments?.getLong("questionId") ?: 0L
           QuestionEditorScreen(
               viewModel = viewModel,
               examId = examId,
               questionId = questionId,
               onNavigateBack = { navController.popBackStack() }
           )
        }
        composable(
             "preview/{examId}",
             arguments = listOf(navArgument("examId") { type = NavType.LongType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getLong("examId") ?: 0L
            com.example.ui.pdf.PdfPreviewScreen(
               viewModel = viewModel,
               examId = examId,
               onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
