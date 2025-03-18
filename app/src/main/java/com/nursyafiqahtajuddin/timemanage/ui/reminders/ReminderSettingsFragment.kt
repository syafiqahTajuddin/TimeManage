package com.nursyafiqahtajuddin.timemanage.ui.reminders

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.nursyafiqahtajuddin.timemanage.R

class ReminderSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.reminder_preferences, rootKey)
    }
}