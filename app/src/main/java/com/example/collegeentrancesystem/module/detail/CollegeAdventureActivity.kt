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
import com.example.collegeentrancesystem.databinding.ActivityCollegeAdventureBinding
import com.example.collegeentrancesystem.module.adapter.CollegeRecommendationViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class CollegeAdventureActivity : BaseActivity<ActivityCollegeAdventureBinding>() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BaseAdapter
    private val collegeListLiveData = MutableLiveData<List<CollegeItem>>()

    override val inflater: (LayoutInflater) -> ActivityCollegeAdventureBinding
        get() = ActivityCollegeAdventureBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initViews()
        setupRecyclerView()
        loadDataFromBackend()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
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

    private fun loadDataFromBackend() {
        //连接后端
        Thread{
            try {
                //创建HTTP
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP}/api/recommend")
                    .build()
                //执行
                val response = client.newCall(request).execute()

                val res = response.body?.string() ?: ""
                val jsonObject = JSONObject(res)

                if (jsonObject.optBoolean("error", true)) {
                    val errorMessage = jsonObject.optString("message", "未知错误")
                    throw Exception(errorMessage)
                }

                val data = jsonObject.getJSONObject("data")
                val adventure = data.getJSONArray("challenge")

                val collegeList = mutableListOf<CollegeItem>()
                for (i in 0 until adventure.length()) {
                    val item = adventure.getJSONObject(i)
                    collegeList.add(
                        CollegeItem(
                            major = item.getString("major"),
                            minRank = item.getInt("minRank"),
                            probability = item.getDouble("probability"),
                            university = item.getString("university")
                        )
                    )
                }
            } catch (e: Exception){
                e.printStackTrace()
                ThreadUtils.runOnUiThread {
                    Toast.makeText(
                        this,
                        "网络连接失败，请检查网络设置",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    override fun getPageName(): PageName {
        return PageName.ADVENTURE
    }
}