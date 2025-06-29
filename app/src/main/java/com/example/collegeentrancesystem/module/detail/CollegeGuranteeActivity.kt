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
import com.example.collegeentrancesystem.constant.Network
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.databinding.ActivityCollegeGuranteeBinding
import com.example.collegeentrancesystem.module.adapter.CollegeRecommendationViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

class CollegeGuranteeActivity : BaseActivity<ActivityCollegeGuranteeBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter
    private val collegeListLiveData = MutableLiveData<List<CollegeItem>>()

    override val inflater: (LayoutInflater) -> ActivityCollegeGuranteeBinding
        get() = ActivityCollegeGuranteeBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                android.util.Log.d("CollegeGuranteeActivity", "开始加载保底数据，文件路径: ${file.absolutePath}")
                android.util.Log.d("CollegeGuranteeActivity", "文件是否存在: ${file.exists()}")

                if (!file.exists()) {
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "未找到推荐结果文件", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val jsonString = file.readText()
                android.util.Log.d("CollegeGuranteeActivity", "读取到的JSON长度: ${jsonString.length}")
                android.util.Log.d("CollegeGuranteeActivity", "JSON内容前100字符: ${jsonString.take(100)}")

                val jsonObject = JSONObject(jsonString)

                if (jsonObject.optBoolean("error", true)) {
                    throw Exception("JSON数据错误")
                }

                val dataObject = jsonObject.getJSONObject("data")
                
                // 检查stable数组是否存在
                if (!dataObject.has("safe")) {
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "JSON数据格式错误：缺少safe字段", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                val safeArray = dataObject.getJSONArray("safe")
                android.util.Log.d("CollegeGuranteeActivity", "保底数组长度: ${safeArray.length()}")

                if (safeArray.length() == 0) {
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "暂无保底院校推荐", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val stableList = mutableListOf<CollegeItem>()
                for (i in 0 until safeArray.length()) {
                    val item = safeArray.getJSONObject(i)

                    val collegeItem = CollegeItem(
                        major = item.getString("major"),
                        minRank = item.getInt("minRank"),
                        probability = item.getDouble("probability"),
                        university = item.getString("university")
                    )
                    stableList.add(collegeItem)
                }

                android.util.Log.d("CollegeGuranteeActivity", "解析完成，共${stableList.size}个保底院校")
                ThreadUtils.runOnUiThread {
                    collegeListLiveData.value = stableList
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CollegeGuranteeActivity", "加载稳妥数据失败", e)
                ThreadUtils.runOnUiThread {
                    Toast.makeText(this, "加载稳妥数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.gurantee_recycleView)
    }

    private fun setupRecyclerView() {
        adapter = BaseAdapter().build {
            setItems(
                lifecycleOwner = this@CollegeGuranteeActivity,
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
        return PageName.GURANTEE
    }
}