package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LegalViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = LegalRepository(db.appDao())

    // --- State Flows ---
    val allClients = repository.allClients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCases = repository.allCases.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val upcomingCases = repository.upcomingCases.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTasks = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allDrafts = repository.allDrafts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFees = repository.allFees.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allContacts = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _aiResponse = MutableStateFlow(\"\")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // PIN Authentication security states
    private val _isPinEnabled = MutableStateFlow(false)
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private val _savedPin = MutableStateFlow(\"\")
    val savedPin: StateFlow<String> = _savedPin.asStateFlow()

    fun getTasksForDate(date: String): Flow<List<DailyTask>> = repository.getTasksForDate(date)
    fun searchLaws(query: String): Flow<List<LawBook>> = repository.searchLaws(query)
    fun searchCaseLaws(query: String): Flow<List<CaseLaw>> = repository.searchCaseLaws(query)
    fun getContactsByRole(role: String): Flow<List<LegalContact>> = repository.getContactsByRole(role)

    // --- Core Database Insert & Delete Operations ---
    fun insertClient(name: String, phone: String, totalFee: Double, paidFee: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertClient(Client(name = name, phone = phone, totalFee = totalFee, paidFee = paidFee))
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteClient(client)
        }
    }

    fun insertCase(title: String, number: String, court: String, judge: String, nextDate: String, clientPhone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCase(
                LawCase(
                    caseTitle = title,
                    caseNumber = number,
                    courtName = court,
                    judgeName = judge,
                    nextDate = nextDate,
                    clientPhone = clientPhone,
                    personalNotes = \"\"
                )
            )
        }
    }

    fun updateCase(lawCase: LawCase) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCase(lawCase)
        }
    }

    fun deleteCase(lawCase: LawCase) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCase(lawCase)
        }
    }

    fun insertTask(taskText: String, isVoice: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(
                DailyTask(
                    taskText = taskText,
                    date = getCurrentDateString(),
                    isCompleted = false,
                    isVoiceGenerated = isVoice
                )
            )
        }
    }

    fun toggleTaskStatus(id: Long, completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTaskStatus(id, completed)
        }
    }

    fun deleteTask(task: DailyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllTasks()
        }
    }

    fun insertDraft(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDraft(LegalDraft(title = title, draftContent = content, lastUpdated = getCurrentDateString()))
        }
    }

    fun deleteDraft(draft: LegalDraft) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDraft(draft)
        }
    }

    fun insertFeeRecord(clientName: String, amount: Double, purpose: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFeeRecord(FeeRecord(clientName = clientName, amount = amount, purpose = purpose, date = getCurrentDateString()))
        }
    }

    fun deleteFeeRecord(fee: FeeRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFeeRecord(fee)
        }
    }

    // --- AI Assistant Integration using Gemini AI ---
    fun askAiAssistant(prompt: String) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val systemPrompt = \"You are an advanced digital legal chamber assistant tailored for advocates in Bangladesh. Provide strategic legal analysis based on CPC, CrPC, Penal Code, or Evidence Act, in clear formal Bengali. Structure your advice using bullet points or numbered lists.\"
            val result = GeminiClient.generateContent(prompt, systemPrompt)
            _aiResponse.value = result
            _isAiLoading.value = false
        }
    }

    fun clearAiResponse() {
        _aiResponse.value = \"\"
    }

    fun draftCourtPetition(prompt: String, onCompleted: (String) -> Unit) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val systemPrompt = \"You are a senior advocate in Bangladesh drafting judicial court petitions. Provide the draft in a clean, formal text format, using formal court vocabulary.\"
            val result = GeminiClient.generateContent(prompt, systemPrompt)
            onCompleted(result)
            _isAiLoading.value = false
        }
    }

    /**
     * Integrates the senior's transcribed voice, formats into checklist items, and saves directly into To-Do list database.
     */
    fun processVoiceInstructions(voiceText: String, onFinished: () -> Unit) {
        _isAiLoading.value = true
        viewModelScope.launch {
            val tasks = GeminiClient.parseSeniorVoiceInstructions(voiceText)
            for (taskText in tasks) {
                insertTask(taskText, isVoice = true)
            }
            _isAiLoading.value = false
            onFinished()
        }
    }

    // --- PIN Secure Vault Operations ---
    fun setAppUnlockState(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    fun configurePin(pin: String, enabled: Boolean) {
        _savedPin.value = pin
        _isPinEnabled.value = enabled
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    // --- Helper Functions ---
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(\"yyyy-MM-dd\", Locale.getDefault())
        return sdf.format(Date())
    }

    private val Client.feeDue: Double
        get() = (totalFee - paidFee).coerceAtLeast(0.0)
                                   }
                                   
