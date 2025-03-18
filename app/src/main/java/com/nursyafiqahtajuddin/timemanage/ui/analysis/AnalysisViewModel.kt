package com.nursyafiqahtajuddin.timemanage.ui.analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.repository.PomodoroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalysisViewModel(private val repository: PomodoroRepository = PomodoroRepository()) : ViewModel() {

    private val _todayFocusTime = MutableLiveData<Int>()
    val todayFocusTime: LiveData<Int> get() = _todayFocusTime

    private val _weeklyFocusTime = MutableLiveData<Int>()
    val weeklyFocusTime: LiveData<Int> get() = _weeklyFocusTime

    private val _monthlyFocusTime = MutableLiveData<Int>()
    val monthlyFocusTime: LiveData<Int> get() = _monthlyFocusTime

    fun loadFocusTimes() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val focusTimes = repository.getFocusTimes(userID)
            focusTimes?.let {
                _todayFocusTime.postValue((it["todayFocusTime"] as? Long)?.toInt() ?: 0)
                _weeklyFocusTime.postValue((it["weeklyFocusTime"] as? Long)?.toInt() ?: 0)
                _monthlyFocusTime.postValue((it["monthlyFocusTime"] as? Long)?.toInt() ?: 0)
            }
        }
    }
}
