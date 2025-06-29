package com.example.collegeentrancesystem

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.databinding.ActivityMainBinding
import com.example.collegeentrancesystem.module.home.MyPagerAdapter
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnFirst: ImageButton
    private lateinit var btnSecond: ImageButton
    private lateinit var btnThird: ImageButton
    private lateinit var btnForth: ImageButton
    private lateinit var pagerAdapter: MyPagerAdapter

    override val inflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 删除之前的推荐结果文件
        deletePreviousRecommendFile()
        
        setContentView(R.layout.activity_main)

        initViews()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        btnFirst.setOnClickListener { viewPager.setCurrentItem(0, false) }
        btnSecond.setOnClickListener { viewPager.setCurrentItem(1, false) }
        btnThird.setOnClickListener { viewPager.setCurrentItem(2, false) }
        btnForth.setOnClickListener { viewPager.setCurrentItem(3, false) }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        btnFirst = findViewById(R.id.btn_first)
        btnSecond = findViewById(R.id.btn_second)
        btnThird = findViewById(R.id.btn_third)
        btnForth = findViewById(R.id.btn_forth)
    }

    private fun setupViewPager() {
        pagerAdapter = MyPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        viewPager.isUserInputEnabled = false

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateBottomNavigation(position)
            }
        })
    }

    private fun updateBottomNavigation(i: Int) {

    }

    override fun getPageName(): PageName {
        return PageName.MAIN
    }

    /**
     * 删除之前的推荐结果文件
     */
    private fun deletePreviousRecommendFile() {
        try {
            val file = File(filesDir, "recommend_result.json")
            if (file.exists()) {
                val deleted = file.delete()
                android.util.Log.d("MainActivity", "删除之前的推荐文件: ${if (deleted) "成功" else "失败"}")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "删除推荐文件失败", e)
        }
    }
}