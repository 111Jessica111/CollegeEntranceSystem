package com.example.collegeentrancesystem.module.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> SchoolFragment()
            2 -> ScoreFragment()
            3 -> MineFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int {
        return 4
    }

}