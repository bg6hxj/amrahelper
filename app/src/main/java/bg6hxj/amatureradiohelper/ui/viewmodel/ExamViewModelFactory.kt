package bg6hxj.amatureradiohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bg6hxj.amatureradiohelper.data.UserPreferences
import bg6hxj.amatureradiohelper.data.repository.ExamRepository
import bg6hxj.amatureradiohelper.data.repository.QuestionRepository

/**
 * ExamViewModel 工厂类
 */
class ExamViewModelFactory(
    private val questionRepository: QuestionRepository,
    private val examRepository: ExamRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
            return ExamViewModel(questionRepository, examRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
