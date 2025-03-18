package com.nursyafiqahtajuddin.timemanage.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.navigation.fragment.NavHostFragment
import com.nursyafiqahtajuddin.timemanage.R

class ManagePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NavHostFragment.create(R.navigation.task_navigation)
            1 -> NavHostFragment.create(R.navigation.quadrant_navigation)
            2 -> NavHostFragment.create(R.navigation.timetable_navigation)
            else -> throw IllegalStateException("Invalid position")
        }
    }
}