package com.example.collegeentrancesystem.module.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.base.list.BaseAdapter
import com.example.collegeentrancesystem.bean.CollegeItem
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.databinding.ActivityCollegeSafeBinding
import com.example.collegeentrancesystem.module.adapter.CollegeRecommendationViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CollegeSafeActivity : BaseActivity<ActivityCollegeSafeBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter
    private var collegeList: List<CollegeItem> = emptyList()

    override val inflater: (LayoutInflater) -> ActivityCollegeSafeBinding
        get() = ActivityCollegeSafeBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initViews()
        loadMockData() // 这里先用模拟数据，实际应该从后端获取
        setupRecyclerView()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.safe_recycleView)
    }

    private fun setupRecyclerView() {
        adapter = BaseAdapter().build {
            setItems(
                lifecycleOwner = this@CollegeSafeActivity,
                layoutId = R.layout.predict_college_show,
                list = androidx.lifecycle.MutableLiveData(collegeList),
                action = { holder: CollegeRecommendationViewHolder, item: CollegeItem ->
                    holder.bind(item)
                }
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadMockData() {
        // 模拟后端返回的数据
        val mockJson = """
        {
            "data": {
                "safe": [
                    {
                        "major": "计算机类",
                        "minRank": 14031,
                        "probability": 96.1,
                        "university": "中国地质大学（武汉）"
                    },
                    {
                        "major": "计算机类",
                        "minRank": 15751,
                        "probability": 98,
                        "university": "武汉科技大学"
                    },
                    {
                        "major": "计算机类",
                        "minRank": 17900,
                        "probability": 99,
                        "university": "武汉工程大学"
                    },
                    {
                        "major": "计算机类",
                        "minRank": 20092,
                        "probability": 99,
                        "university": "武汉工程大学"
                    }
                ]
            }
        }
        """.trimIndent()

        try {
            val type = object : TypeToken<Map<String, Any>>(){}.type
            val jsonMap = Gson().fromJson<Map<String, Any>>(mockJson, type)
            val data = jsonMap["data"] as Map<String, Any>
            val safe = data["safe"] as List<Map<String, Any>>
            
            collegeList = safe.map { item ->
                CollegeItem(
                    major = item["major"] as String,
                    minRank = (item["minRank"] as Double).toInt(),
                    probability = item["probability"] as Double,
                    university = item["university"] as String
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getPageName(): PageName {
        return PageName.SAFE
    }
}