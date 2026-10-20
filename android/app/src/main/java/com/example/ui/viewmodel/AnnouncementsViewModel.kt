package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AnnouncementRepository
import com.example.domain.model.Announcement
import com.example.domain.validation.ApplicationValidator
import com.example.ui.viewmodel.form.FieldState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnnouncementsUiState(
    val announcements: List<Announcement> = emptyList()
)

/** Formulario de postulación a una convocatoria. */
data class ApplicationFormState(
    val announcement: Announcement? = null,
    val motivation: FieldState = FieldState(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false
)

class AnnouncementsViewModel(
    private val announcementRepository: AnnouncementRepository
) : ViewModel() {

    val uiState: StateFlow<AnnouncementsUiState> = announcementRepository.announcements
        .map { AnnouncementsUiState(announcements = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnnouncementsUiState()
        )

    var applicationForm by mutableStateOf(ApplicationFormState())
        private set

    /** Abre el formulario de postulación para una convocatoria. */
    fun startApplication(announcement: Announcement) {
        applicationForm = ApplicationFormState(announcement = announcement)
    }

    fun cancelApplication() {
        applicationForm = ApplicationFormState()
    }

    fun onMotivationChange(value: String) {
        applicationForm = applicationForm.copy(
            motivation = applicationForm.motivation.update(value, ApplicationValidator::validateMotivation),
            generalError = null
        )
    }

    /** Valida el motivo y registra la postulación en Room. */
    fun submitApplication(onSuccess: (Announcement) -> Unit) {
        val form = applicationForm
        val announcement = form.announcement ?: return
        if (form.isSubmitting) return

        val validated = form.copy(motivation = form.motivation.validate(ApplicationValidator::validateMotivation))
        applicationForm = validated
        if (!validated.motivation.isValid) return

        applicationForm = validated.copy(isSubmitting = true)
        viewModelScope.launch {
            val ok = announcementRepository.applyTo(announcement.id, validated.motivation.value)
            if (ok) {
                applicationForm = ApplicationFormState()
                onSuccess(announcement)
            } else {
                applicationForm = applicationForm.copy(
                    isSubmitting = false,
                    generalError = "No fue posible postular: ya postulaste o no quedan cupos."
                )
            }
        }
    }
}
