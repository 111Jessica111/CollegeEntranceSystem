package com.example.collegeentrancesystem.module.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ThreadUtils
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.base.list.BaseAdapter
import com.example.collegeentrancesystem.bean.CollegeItem
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.databinding.ActivityCollegeAdventureBinding
import com.example.collegeentrancesystem.module.adapter.CollegeRecommendationViewHolder
import org.json.JSONObject
import java.io.File

class CollegeAdventureActivity : BaseActivity<ActivityCollegeAdventureBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter
    private val collegeListLiveData = MutableLiveData<List<CollegeItem>>()

    override val inflater: (LayoutInflater) -> ActivityCollegeAdventureBinding
        get() = ActivityCollegeAdventureBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        android.util.Log.d("CollegeAdventureActivity", "onCreate开始")
        initViews()
        setupRecyclerView()

        loadChallengeDataFromLocal()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun loadChallengeDataFromLocal() {
        Thread {
            try {
                val file = File(filesDir, "recommend_result.json")
                
                if (!file.exists()) {
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "未找到推荐结果文件", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                val jsonString = file.readText()
                
                val jsonObject = JSONObject(jsonString)
                
                if (jsonObject.optBoolean("error", true)) {
                    throw Exception("JSON数据错误")
                }
                
                val dataObject = jsonObject.getJSONObject("data")
                val challengeArray = dataObject.getJSONArray("challenge")

                val challengeList = mutableListOf<CollegeItem>()
                for (i in 0 until challengeArray.length()) {
                    val item = challengeArray.getJSONObject(i)
                    val collegeItem = CollegeItem(
                        major = item.getString("major"),
                        minRank = item.getInt("minRank"),
                        probability = item.getDouble("probability"),
                        university = item.getString("university")
                    )
                    challengeList.add(collegeItem)
                }

                ThreadUtils.runOnUiThread {
                    collegeListLiveData.value = challengeList
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ThreadUtils.runOnUiThread {
                    Toast.makeText(this, "加载挑战数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.adventure_recycleView)
    }

    private fun setupRecyclerView() {
        adapter = BaseAdapter().build {
            setItems(
                lifecycleOwner = this@CollegeAdventureActivity,
                layoutId = R.layout.predict_college_show,
                list = collegeListLiveData,
                action = { holder: CollegeRecommendationViewHolder, item: CollegeItem ->
                    holder.bind(item)
                }
            )
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun getPageName(): PageName {
        return PageName.ADVENTURE
    }
}