package bg6hxj.amatureradiohelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import bg6hxj.amatureradiohelper.ui.screen.AboutScreen
import bg6hxj.amatureradiohelper.ui.screen.ContactLogListScreen
import bg6hxj.amatureradiohelper.ui.screen.AddContactLogScreen
import bg6hxj.amatureradiohelper.ui.screen.DiscoverScreen
import bg6hxj.amatureradiohelper.ui.screen.ExamScreen
import bg6hxj.amatureradiohelper.ui.screen.ImageViewerScreen
import bg6hxj.amatureradiohelper.ui.screen.ProfileScreen
import bg6hxj.amatureradiohelper.ui.screen.PropagationScreen
import bg6hxj.amatureradiohelper.ui.screen.ReferenceDetailScreen
import bg6hxj.amatureradiohelper.ui.screen.WavelengthCalculatorScreen
import bg6hxj.amatureradiohelper.ui.theme.AmatureRadioHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmatureRadioHelperTheme {
                AmatureRadioHelperApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AmatureRadioHelperApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.EXAM) }
    var showAboutScreen by rememberSaveable { mutableStateOf(false) }
    var showSequentialReviewScreen by rememberSaveable { mutableStateOf(false) }
    var showSequentialPracticeScreen by rememberSaveable { mutableStateOf(false) }
    var showRandomPracticeScreen by rememberSaveable { mutableStateOf(false) }
    var showMockExamScreen by rememberSaveable { mutableStateOf(false) }
    var showLearnedQuestionsScreen by rememberSaveable { mutableStateOf(false) }
    var showUnlearnedQuestionsScreen by rememberSaveable { mutableStateOf(false) }
    var showWrongQuestionsScreen by rememberSaveable { mutableStateOf(false) }
    var showFavoriteQuestionsScreen by rememberSaveable { mutableStateOf(false) }
    var showContactLogListScreen by rememberSaveable { mutableStateOf(false) }
    var showAddContactLogScreen by rememberSaveable { mutableStateOf(false) }
    var showPropagationScreen by rememberSaveable { mutableStateOf(false) }
    var showWavelengthCalculatorScreen by rememberSaveable { mutableStateOf(false) }
    var showReferenceDetailScreen by rememberSaveable { mutableStateOf(false) }
    var referenceDetailType by rememberSaveable { mutableStateOf("") }
    var showImageViewerScreen by rememberSaveable { mutableStateOf(false) }
    var imageViewUrl by rememberSaveable { mutableStateOf("") }
    var imageViewTitle by rememberSaveable { mutableStateOf("") }
    var selectedLevel by rememberSaveable { mutableStateOf("A") }

    if (showAboutScreen) {
        BackHandler { showAboutScreen = false }
        AboutScreen(onBackClick = { showAboutScreen = false })
    } else if (showAddContactLogScreen) {
        BackHandler { showAddContactLogScreen = false }
        AddContactLogScreen(onNavigateBack = { showAddContactLogScreen = false })
    } else if (showContactLogListScreen) {
        BackHandler { showContactLogListScreen = false }
        ContactLogListScreen(
            onNavigateBack = { showContactLogListScreen = false },
            onAddLogClick = { showAddContactLogScreen = true }
        )
    } else if (showPropagationScreen) {
        BackHandler { showPropagationScreen = false }
        PropagationScreen(onNavigateBack = { showPropagationScreen = false })
    } else if (showWavelengthCalculatorScreen) {
        BackHandler { showWavelengthCalculatorScreen = false }
        WavelengthCalculatorScreen(onNavigateBack = { showWavelengthCalculatorScreen = false })
    } else if (showImageViewerScreen) {
        BackHandler { showImageViewerScreen = false }
        ImageViewerScreen(
            url = imageViewUrl,
            title = imageViewTitle,
            onBack = { showImageViewerScreen = false }
        )
    } else if (showReferenceDetailScreen) {
        BackHandler { showReferenceDetailScreen = false }
        ReferenceDetailScreen(
            type = referenceDetailType,
            onNavigateBack = { showReferenceDetailScreen = false },
            onImageClick = { url, title ->
                imageViewUrl = url
                imageViewTitle = title
                showImageViewerScreen = true
            }
        )
    } else if (showSequentialReviewScreen) {
        bg6hxj.amatureradiohelper.ui.screen.SequentialReviewScreen(
            level = selectedLevel,
            onNavigateBack = { showSequentialReviewScreen = false }
        )
    } else if (showSequentialPracticeScreen) {
        bg6hxj.amatureradiohelper.ui.screen.SequentialPracticeScreen(
            level = selectedLevel,
            onNavigateBack = { showSequentialPracticeScreen = false }
        )
    } else if (showRandomPracticeScreen) {
        bg6hxj.amatureradiohelper.ui.screen.RandomPracticeScreen(
            level = selectedLevel,
            onNavigateBack = { showRandomPracticeScreen = false }
        )
    } else if (showMockExamScreen) {
        bg6hxj.amatureradiohelper.ui.screen.MockExamScreen(
            level = selectedLevel,
            onNavigateBack = { showMockExamScreen = false }
        )
    } else if (showLearnedQuestionsScreen) {
        bg6hxj.amatureradiohelper.ui.screen.LearnedQuestionsScreen(
            level = selectedLevel,
            onNavigateBack = { showLearnedQuestionsScreen = false }
        )
    } else if (showUnlearnedQuestionsScreen) {
        bg6hxj.amatureradiohelper.ui.screen.UnlearnedQuestionsScreen(
            level = selectedLevel,
            onNavigateBack = { showUnlearnedQuestionsScreen = false }
        )
    } else if (showWrongQuestionsScreen) {
        bg6hxj.amatureradiohelper.ui.screen.WrongQuestionsScreen(
            level = selectedLevel,
            onBack = { showWrongQuestionsScreen = false }
        )
    } else if (showFavoriteQuestionsScreen) {
        bg6hxj.amatureradiohelper.ui.screen.FavoriteQuestionsScreen(
            level = selectedLevel,
            onBack = { showFavoriteQuestionsScreen = false }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.contentDescription
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.EXAM -> ExamScreen(
                        onStartSequentialReview = { level ->
                            selectedLevel = level
                            showSequentialReviewScreen = true
                        },
                        onStartSequentialPractice = { level ->
                            selectedLevel = level
                            showSequentialPracticeScreen = true
                        },
                        onStartRandomPractice = { level ->
                            selectedLevel = level
                            showRandomPracticeScreen = true
                        },
                        onStartMockExam = { level ->
                            selectedLevel = level
                            showMockExamScreen = true
                        },
                        onShowLearnedQuestions = { level ->
                            selectedLevel = level
                            showLearnedQuestionsScreen = true
                        },
                        onShowUnlearnedQuestions = { level ->
                            selectedLevel = level
                            showUnlearnedQuestionsScreen = true
                        },
                        onShowWrongQuestions = { level ->
                            selectedLevel = level
                            showWrongQuestionsScreen = true
                        },
                        onShowFavoriteQuestions = { level ->
                            selectedLevel = level
                            showFavoriteQuestionsScreen = true
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                    AppDestinations.DISCOVER -> DiscoverScreen(
                        onNavigate = { route ->
                            when {
                                route == "contact_log_list" -> showContactLogListScreen = true
                                route == "propagation_prediction" -> showPropagationScreen = true
                                route == "wavelength_calculator" -> showWavelengthCalculatorScreen = true
                                route.startsWith("reference_detail/") -> {
                                    referenceDetailType = route.substringAfter("reference_detail/")
                                    showReferenceDetailScreen = true
                                }
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                    AppDestinations.PROFILE -> ProfileScreen(
                        onAboutClick = { showAboutScreen = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    EXAM("考试", Icons.Default.Edit, "考试模块"),
    DISCOVER("发现", Icons.Default.Search, "发现模块"),
    PROFILE("我的", Icons.Default.AccountCircle, "个人中心")
}