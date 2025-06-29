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
        android.util.Log.d("CollegeGuranteeActivity", "开始加载本地数据")
        Thread {
            try {
                val file = File(filesDir, "recommend_result.json")
                android.util.Log.d("CollegeGuranteeActivity", "文件路径: ${file.absolutePath}")
                android.util.Log.d("CollegeGuranteeActivity", "文件是否存在: ${file.exists()}")

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
                
                // 检查stable数组是否存在
                if (!dataObject.has("stable")) {
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "JSON数据格式错误：缺少stable字段", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                val stableArray = dataObject.getJSONArray("safe")

                if (stableArray.length() == 0) {
                    android.util.Log.w("CollegeGuranteeActivity", "stable数组为空")
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "暂无保底院校推荐", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val stableList = mutableListOf<CollegeItem>()
                for (i in 0 until stableArray.length()) {
                    val item = stableArray.getJSONObject(i)
                    android.util.Log.d("CollegeGuranteeActivity", "解析第${i+1}个院校: ${item.toString()}")
                    
                    val collegeItem = CollegeItem(
                        major = item.getString("major"),
                        minRank = item.getInt("minRank"),
                        probability = item.getDouble("probability"),
                        university = item.getString("university")
                    )
                    stableList.add(collegeItem)
                    android.util.Log.d("CollegeGuranteeActivity", "添加院校: ${collegeItem.university}")
                }

                android.util.Log.d("CollegeGuranteeActivity", "解析完成，共${stableList.size}个院校")
                ThreadUtils.runOnUiThread {
                    collegeListLiveData.value = stableList
                    android.util.Log.d("CollegeGuranteeActivity", "LiveData已更新，加载了 ${stableList.size} 个稳妥院校")
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