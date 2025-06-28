package com.example.collegeentrancesystem.module.detail

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.databinding.ActivityCollegeGuranteeBinding

class CollegeGuranteeActivity : BaseActivity<ActivityCollegeGuranteeBinding>() {
    override val inflater: (LayoutInflater) -> ActivityCollegeGuranteeBinding
        get() = ActivityCollegeGuranteeBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }

    override fun getPageName(): PageName {
        return PageName.GURANTEE
    }
}