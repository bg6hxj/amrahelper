package bg6hxj.amatureradiohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import bg6hxj.amatureradiohelper.data.repository.ContactLogRepository

class ContactLogViewModelFactory(
    private val repository: ContactLogRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactLogViewModel::class.java)) {
            return ContactLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
