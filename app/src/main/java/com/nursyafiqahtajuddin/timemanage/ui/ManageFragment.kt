package com.nursyafiqahtajuddin.timemanage.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nursyafiqahtajuddin.timemanage.R

class ManageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manage, container, false)

        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        viewPager.adapter = ManagePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Task Creation"
                1 -> "Quadrant Scheduler"
                2 -> "Timetable Creation"
                else -> null
            }
        }.attach()

        return view
    }
}