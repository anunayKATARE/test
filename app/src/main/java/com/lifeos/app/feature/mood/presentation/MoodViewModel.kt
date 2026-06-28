package com.lifeos.app.feature.mood.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.mood.domain.AddMoodUseCase
import com.lifeos.app.feature.mood.domain.DeleteMoodUseCase
import com.lifeos.app.feature.mood.domain.DetectMoodPatternsUseCase
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.mood.domain.MoodEntry
import com.lifeos.app.feature.mood.domain.MoodPattern
import com.lifeos.app.feature.mood.domain.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MoodViewModel @Inject constructor(
    repository: MoodRepository,
    private val addMoodUseCase: AddMoodUseCase,
    private val deleteMoodUseCase: DeleteMoodUseCase,
    private val detectMoodPatternsUseCase: DetectMoodPatternsUseCase,
) : ViewModel() {

    val moodEntries: StateFlow<List<MoodEntry>> = repository.observeRecent(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _patterns = MutableStateFlow<List<MoodPattern>>(emptyList())
    val patterns: StateFlow<List<MoodPattern>> = _patterns.asStateFlow()

    init {
        viewModelScope.launch { _patterns.value = detectMoodPatternsUseCase() }
    }

    fun addMood(
        emotion: Emotion,
        intensity: Int,
        trigger: String,
        situation: String,
        automaticThoughts: String,
        physicalSensations: String,
        actionsTaken: String,
        lessonsLearned: String,
    ) {
        viewModelScope.launch {
            addMoodUseCase(
                emotion = emotion,
                intensity = intensity,
                trigger = trigger,
                situation = situation,
                automaticThoughts = automaticThoughts,
                physicalSensations = physicalSensations,
                actionsTaken = actionsTaken,
                lessonsLearned = lessonsLearned,
            )
            _patterns.value = detectMoodPatternsUseCase()
        }
    }

    fun deleteMood(id: String) {
        viewModelScope.launch { deleteMoodUseCase(id) }
    }
}
