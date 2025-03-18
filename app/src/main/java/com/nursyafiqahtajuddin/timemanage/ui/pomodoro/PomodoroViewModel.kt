package com.nursyafiqahtajuddin.timemanage.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nursyafiqahtajuddin.timemanage.data.models.Timer
import com.nursyafiqahtajuddin.timemanage.repository.PomodoroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PomodoroViewModel(private val repository: PomodoroRepository = PomodoroRepository()) : ViewModel() {

    private var countDownTimer: CountDownTimer? = null

    private val _settings = MutableLiveData<Timer>()
    val settings: LiveData<Timer> get() = _settings

    private val _timeLeft = MutableLiveData<Int>()
    val timeLeft: LiveData<Int> get() = _timeLeft

    private val _isRunning = MutableLiveData<Boolean>()
    val isRunning: LiveData<Boolean> get() = _isRunning

    private var _isPaused = false
    val isPaused: Boolean get() = _isPaused

    private val _isBreak = MutableLiveData<Boolean>()
    val isBreak: LiveData<Boolean> get() = _isBreak

    private val _todayFocusTime = MutableLiveData<Int>()
    val todayFocusTime: LiveData<Int> get() = _todayFocusTime

    private val _weeklyFocusTime = MutableLiveData<Int>()
    val weeklyFocusTime: LiveData<Int> get() = _weeklyFocusTime

    private val _monthlyFocusTime = MutableLiveData<Int>()
    val monthlyFocusTime: LiveData<Int> get() = _monthlyFocusTime

    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    var selectedTaskID: String? = null

    init {
        fetchFocusTimes()
        loadSettings()
        _timeLeft.value = 1500 // Default 25 minutes

        if (_settings.value?.sessionNumber == null) {
            updateSessionNumber(0)
        }
    }

    fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val settingsMap = repository.getPomodoroSettings()
            if (settingsMap != null) {
                val focusDuration = (settingsMap["focusDuration"] as? Long)?.toInt() ?: 25
                val shortBreakDuration = (settingsMap["shortBreakDuration"] as? Long)?.toInt() ?: 5
                val longBreakDuration = (settingsMap["longBreakDuration"] as? Long)?.toInt() ?: 15

                val newSettings = Timer(focusDuration, shortBreakDuration, longBreakDuration)
                _settings.postValue(newSettings)
                _timeLeft.postValue(focusDuration * 60)
            }
        }
    }

    fun saveSettings(userID: String, focusDuration: Int, shortBreakDuration: Int, longBreakDuration: Int, callback: (Boolean) -> Unit) {
        val updatedFields = mutableMapOf<String, Any>()

        val currentSettings = _settings.value
        if (currentSettings != null) {
            if (currentSettings.focusDuration != focusDuration) {
                updatedFields["focusDuration"] = focusDuration
            }
            if (currentSettings.shortBreakDuration != shortBreakDuration) {
                updatedFields["shortBreakDuration"] = shortBreakDuration
            }
            if (currentSettings.longBreakDuration != longBreakDuration) {
                updatedFields["longBreakDuration"] = longBreakDuration
            }
        }

        if (updatedFields.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val success = repository.savePomodoroSettings(userID, updatedFields)
                if (success) {
                    _settings.postValue(
                        Timer(
                            focusDuration = focusDuration,
                            shortBreakDuration = shortBreakDuration,
                            longBreakDuration = longBreakDuration
                        )
                    )
                    _timeLeft.postValue(focusDuration * 60)

                    loadSettings()
                }

                withContext(Dispatchers.Main) {
                    callback(success)
                }
            }
        } else {
            println("No changes detected, skipping Firestore update.")
            callback(true)
        }
    }

    fun startTimer(initialTime: Int, isBreak: Boolean = false) {
        _timeLeft.postValue(initialTime)
        _isRunning.postValue(true)
        _isBreak.postValue(isBreak)
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(initialTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.postValue((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                _isRunning.postValue(false)
                handleTimerCompletion(isBreak)
            }
        }.start()
    }

    private fun handleTimerCompletion(isBreak: Boolean) {
        if (isBreak) {
            val sessionDuration = _settings.value?.focusDuration ?: 25
            val updatedSessionNumber = (_settings.value?.sessionNumber ?: 0) + 1
            updateSessionNumber(updatedSessionNumber)
            saveSessionCount(updatedSessionNumber)

            val focusTime = _settings.value?.focusDuration ?: 25
            startTimer(focusTime * 60, isBreak = false)
            updateFocusTime(sessionDuration)

        } else {
            val currentSession = _settings.value?.sessionNumber ?: 0
            val totalSessions = _settings.value?.totalSessions ?: 4

            if ((currentSession + 1) % totalSessions == 0) {
                val longBreakTime = _settings.value?.longBreakDuration ?: 15
                startTimer(longBreakTime * 60, isBreak = true)
            } else {
                val shortBreakTime = _settings.value?.shortBreakDuration ?: 5
                startTimer(shortBreakTime * 60, isBreak = true)
            }
        }
    }

    fun pauseTimer() {
        _isPaused = true
        _isRunning.postValue(false)
        countDownTimer?.cancel()
    }

    fun resetTimer(initialTime: Int) {
        countDownTimer?.cancel()
        _isPaused = false
        _timeLeft.postValue(initialTime)
        _isRunning.postValue(false)
    }

    fun resumeTimer() {
        _timeLeft.value?.let { remainingTime ->
            startTimer(remainingTime)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    fun updateSessionNumber(updatedSessionNumber: Int) {
        val currentSettings = _settings.value
        if (currentSettings != null) {
            val updatedSettings = currentSettings.copy(sessionNumber = updatedSessionNumber)
            _settings.postValue(updatedSettings)

            viewModelScope.launch(Dispatchers.IO) {
                val userID = currentUserID ?: return@launch
                val timerRef = FirebaseFirestore.getInstance().collection("Timer").document(userID)

                try {
                    timerRef.update("sessionNumber", updatedSessionNumber).await()
                    println("Session number updated successfully!")
                } catch (e: Exception) {
                    println("Error updating session number: ${e.message}")
                }
            }
        }
    }

    fun saveSessionCount(sessionCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.savePomodoroSessionCount(sessionCount)
            if (success) {
                println("Session count saved successfully.")
            } else {
                println("Failed to save session count.")
            }
        }
    }

    fun updateFocusTime(sessionDuration: Int) {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.updateFocusTimes(userID, sessionDuration)
            if (success) {
                fetchFocusTimes()
                println("Focus times updated successfully in Firestore.")
            } else {
                println("Error updating focus times.")
            }
        }
    }

    fun fetchFocusTimes() {
        if (currentUserID == null) return

        viewModelScope.launch(Dispatchers.IO) {
            val focusTimes = repository.getFocusTimes(currentUserID)
            if (focusTimes != null) {
                _todayFocusTime.postValue((focusTimes["todayFocusTime"] as? Long)?.toInt() ?: 0)
                _weeklyFocusTime.postValue((focusTimes["weeklyFocusTime"] as? Long)?.toInt() ?: 0)
                _monthlyFocusTime.postValue((focusTimes["monthlyFocusTime"] as? Long)?.toInt() ?: 0)
            }
        }
    }

}