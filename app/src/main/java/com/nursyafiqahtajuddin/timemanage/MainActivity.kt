package com.nursyafiqahtajuddin.timemanage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.ui.auth.SignInFragment

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toolbarTitle: TextView = toolbar.findViewById(R.id.toolbarTitle)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.welcomeFragment,
                R.id.signupFragment,
                R.id.signinFragment,
                R.id.profileFragment,
                R.id.pomodoroFragment,
                R.id.manageFragment,
                R.id.reminderFragment,
                R.id.habitTrackerFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcomeFragment -> {
                    toolbar.visibility = View.GONE
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.signupFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.signup_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.signinFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.login_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.profileFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.profile_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.pomodoroFragment -> {
                    toolbar.visibility = View.GONE
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.pomodoroSettingsFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.pomodoro_setting_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.manageFragment -> {
                    toolbar.visibility = View.GONE
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.reminderFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.reminder_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.reminderSettingsFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.reminder_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.habitTrackerFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.habit_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                R.id.addHabitFragment -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = getString(R.string.add_habit_title)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    toolbar.visibility = View.VISIBLE
                    toolbarTitle.text = destination.label ?: ""
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, SignInFragment::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}