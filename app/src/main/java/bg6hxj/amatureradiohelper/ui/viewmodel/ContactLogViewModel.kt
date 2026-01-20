package bg6hxj.amatureradiohelper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import bg6hxj.amatureradiohelper.data.repository.ContactLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactLogViewModel(
    private val repository: ContactLogRepository
) : ViewModel() {

    // List State
    val contactLogs: StateFlow<List<ContactLog>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val logCount: StateFlow<Int> = repository.getLogCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Form State
    private val _addLogUiState = MutableStateFlow(AddLogUiState())
    val addLogUiState: StateFlow<AddLogUiState> = _addLogUiState.asStateFlow()

    fun updateForm(event: AddLogEvent) {
        when (event) {
            is AddLogEvent.ContactTimeChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(contactTime = event.time)
            }
            is AddLogEvent.ModeChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(mode = event.mode)
            }
            is AddLogEvent.FrequencyChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(frequency = event.freq)
            }
            is AddLogEvent.CqZoneChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(cqZone = event.zone)
            }
            is AddLogEvent.MyCallsignChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(myCallsign = event.callsign)
            }
            is AddLogEvent.TheirCallsignChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(theirCallsign = event.callsign)
            }
            is AddLogEvent.RstSentChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(rstSent = event.rst)
            }
            is AddLogEvent.RstReceivedChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(rstReceived = event.rst)
            }
            is AddLogEvent.MyPowerChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(myPower = event.power)
            }
            is AddLogEvent.TheirPowerChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(theirPower = event.power)
            }
            is AddLogEvent.TheirQthChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(theirQth = event.qth)
            }
            is AddLogEvent.EquipmentChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(equipment = event.eq)
            }
            is AddLogEvent.AntennaChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(antenna = event.ant)
            }
            is AddLogEvent.NotesChanged -> {
                _addLogUiState.value = _addLogUiState.value.copy(notes = event.notes)
            }
            AddLogEvent.Submit -> submitLog()
            AddLogEvent.Reset -> resetForm()
        }
    }

    private fun submitLog() {
        val state = _addLogUiState.value
        if (validateForm(state)) {
            viewModelScope.launch {
                repository.insertLog(
                    ContactLog(
                        contactTime = state.contactTime,
                        mode = state.mode,
                        frequency = state.frequency,
                        cqZone = state.cqZone.ifBlank { null },
                        myCallsign = state.myCallsign,
                        theirCallsign = state.theirCallsign,
                        rstSent = state.rstSent,
                        rstReceived = state.rstReceived,
                        myPower = state.myPower.ifBlank { null },
                        theirPower = state.theirPower.ifBlank { null },
                        theirQth = state.theirQth.ifBlank { null },
                        equipment = state.equipment.ifBlank { null },
                        antenna = state.antenna.ifBlank { null },
                        notes = state.notes.ifBlank { null }
                    )
                )
                _addLogUiState.value = _addLogUiState.value.copy(isSaved = true)
            }
        }
    }
    
    private fun validateForm(state: AddLogUiState): Boolean {
        val errors = mutableMapOf<String, String>()
        
        if (state.frequency.isBlank()) errors["frequency"] = "Required"
        if (state.myCallsign.isBlank()) errors["myCallsign"] = "Required"
        if (state.theirCallsign.isBlank()) errors["theirCallsign"] = "Required"
        if (state.rstSent.isBlank()) errors["rstSent"] = "Required"
        if (state.rstReceived.isBlank()) errors["rstReceived"] = "Required"
        
        _addLogUiState.value = state.copy(errors = errors)
        return errors.isEmpty()
    }

    private fun resetForm() {
        _addLogUiState.value = AddLogUiState(contactTime = System.currentTimeMillis())
    }
    
    fun deleteLog(log: ContactLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    /**
     * 批量导入日志
     */
    fun importLogs(logs: List<ContactLog>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            repository.insertAll(logs)
            onComplete(logs.size)
        }
    }
}

data class AddLogUiState(
    val contactTime: Long = System.currentTimeMillis(),
    val mode: String = "SSB",
    val frequency: String = "",
    val cqZone: String = "",
    val myCallsign: String = "",
    val theirCallsign: String = "",
    val rstSent: String = "59",
    val rstReceived: String = "59",
    val myPower: String = "",
    val theirPower: String = "",
    val theirQth: String = "",
    val equipment: String = "",
    val antenna: String = "",
    val notes: String = "",
    val errors: Map<String, String> = emptyMap(),
    val isSaved: Boolean = false
)

sealed class AddLogEvent {
    data class ContactTimeChanged(val time: Long) : AddLogEvent()
    data class ModeChanged(val mode: String) : AddLogEvent()
    data class FrequencyChanged(val freq: String) : AddLogEvent()
    data class CqZoneChanged(val zone: String) : AddLogEvent()
    data class MyCallsignChanged(val callsign: String) : AddLogEvent()
    data class TheirCallsignChanged(val callsign: String) : AddLogEvent()
    data class RstSentChanged(val rst: String) : AddLogEvent()
    data class RstReceivedChanged(val rst: String) : AddLogEvent()
    data class MyPowerChanged(val power: String) : AddLogEvent()
    data class TheirPowerChanged(val power: String) : AddLogEvent()
    data class TheirQthChanged(val qth: String) : AddLogEvent()
    data class EquipmentChanged(val eq: String) : AddLogEvent()
    data class AntennaChanged(val ant: String) : AddLogEvent()
    data class NotesChanged(val notes: String) : AddLogEvent()
    object Submit : AddLogEvent()
    object Reset : AddLogEvent()
}
