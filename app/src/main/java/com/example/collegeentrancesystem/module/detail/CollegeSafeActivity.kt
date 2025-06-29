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
import com.example.collegeentrancesystem.databinding.ActivityCollegeSafeBinding
import com.example.collegeentrancesystem.module.adapter.CollegeRecommendationViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File

class CollegeSafeActivity : BaseActivity<ActivityCollegeSafeBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter
    private val collegeListLiveData = MutableLiveData<List<CollegeItem>>()

    override val inflater: (LayoutInflater) -> ActivityCollegeSafeBinding
        get() = ActivityCollegeSafeBinding::inflate

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
        android.util.Log.d("CollegeSafeActivity", "开始加载本地数据")
        Thread {
            try {
                val file = File(filesDir, "recommend_result.json")
                android.util.Log.d("CollegeSafeActivity", "文件路径: ${file.absolutePath}")
                android.util.Log.d("CollegeSafeActivity", "文件是否存在: ${file.exists()}")

                if (!file.exists()) {
                    android.util.Log.w("CollegeSafeActivity", "文件不存在")
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "未找到推荐结果文件", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val jsonString = file.readText()
                android.util.Log.d("CollegeSafeActivity", "读取到的JSON: $jsonString")

                val jsonObject = JSONObject(jsonString)

                if (jsonObject.optBoolean("error", true)) {
                    android.util.Log.e("CollegeSafeActivity", "JSON数据错误")
                    throw Exception("JSON数据错误")
                }

                val dataObject = jsonObject.getJSONObject("data")
                android.util.Log.d("CollegeSafeActivity", "data对象内容: ${dataObject.toString()}")
                
                // 检查safe数组是否存在
                if (!dataObject.has("safe")) {
                    android.util.Log.e("CollegeSafeActivity", "JSON中没有safe字段")
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "JSON数据格式错误：缺少safe字段", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }
                
                val safeArray = dataObject.getJSONArray("stable")
                android.util.Log.d("CollegeSafeActivity", "safe数组长度: ${safeArray.length()}")

                if (safeArray.length() == 0) {
                    android.util.Log.w("CollegeSafeActivity", "safe数组为空")
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(this, "暂无稳妥院校推荐", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val safeList = mutableListOf<CollegeItem>()
                for (i in 0 until safeArray.length()) {
                    val item = safeArray.getJSONObject(i)
                    android.util.Log.d("CollegeSafeActivity", "解析第${i+1}个院校: ${item.toString()}")
                    
                    val collegeItem = CollegeItem(
                        major = item.getString("major"),
                        minRank = item.getInt("minRank"),
                        probability = item.getDouble("probability"),
                        university = item.getString("university")
                    )
                    safeList.add(collegeItem)
                    android.util.Log.d("CollegeSafeActivity", "添加院校: ${collegeItem.university}")
                }

                android.util.Log.d("CollegeSafeActivity", "解析完成，共${safeList.size}个院校")
                ThreadUtils.runOnUiThread {
                    collegeListLiveData.value = safeList
                    android.util.Log.d("CollegeSafeActivity", "LiveData已更新，加载了 ${safeList.size} 个保底院校")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CollegeSafeActivity", "加载保底数据失败", e)
                ThreadUtils.runOnUiThread {
                    Toast.makeText(this, "加载保底数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.safe_recycleView)
    }

    private fun setupRecyclerView() {
        adapter = BaseAdapter().build {
            setItems(
                lifecycleOwner = this@CollegeSafeActivity,
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
        return PageName.SAFE
    }
}