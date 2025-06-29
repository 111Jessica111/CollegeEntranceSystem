package com.example.collegeentrancesystem.module.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.blankj.utilcode.util.ThreadUtils
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.DataModule
import com.example.collegeentrancesystem.constant.Network
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.constant.Province
import com.example.collegeentrancesystem.constant.Subject
import com.example.collegeentrancesystem.constant.YearModule
import com.example.collegeentrancesystem.databinding.ActivityUserInfoBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import com.example.collegeentrancesystem.bean.MajorClass
import com.example.collegeentrancesystem.bean.Major
import java.io.File

class UserInfoActivity : BaseActivity<ActivityUserInfoBinding>() {

    private lateinit var provinceSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var flexBox: FlexboxLayout
    private lateinit var userProvinceSpinner: Spinner
    private lateinit var userLikeMajor: Spinner
    private lateinit var userLikeMajorClass: Spinner

    private lateinit var selectedProvince: String
    private lateinit var selectedYear: String
    private lateinit var selectedSubjects: List<String>

    private var provinceList: List<Province> = emptyList()
    private val subjects = mutableListOf<Subject>()
    private val subjectViews = mutableMapOf<String, TextView>()
    private var yearsList: List<String> = emptyList()
    private var majorClassList: List<MajorClass> = emptyList()

    override val inflater: (LayoutInflater) -> ActivityUserInfoBinding
        get() = ActivityUserInfoBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initViews()
        initSubjects()
        loadProvinceData()
        loadYearData()
        loadMajorClassData()
        setupListeners()
        setupSubjectClickListeners()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<RelativeLayout>(R.id.edit_done).setOnClickListener {
            sendUserInfoToHome()
            finish()
        }

    }

    private fun sendMessagetoPy() {
        //连接后端
        Thread{
            try {
                //获取用户选择的数据
                val userProvince = userProvinceSpinner.selectedItem.toString()
                val userMajorClass = userLikeMajorClass.selectedItem.toString()
                
                val userInfoJson = JSONObject().apply {
                    put("userProvince", selectedProvince)
                    put("subject", selectedSubjects)
                    put("preferredProvince", userProvince)
                    put("preferredMajor", userMajorClass)
                    put("rank", 9000)
                }.toString()

                android.util.Log.d("UserInfoActivity", "发送数据: $userInfoJson")
                android.util.Log.d("UserInfoActivity", "服务器地址: ${Network.IP}/api/recommend")

                //创建HTTP
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP}/api/recommend")
                    .post(RequestBody.create(mediaType, userInfoJson))
                    .addHeader("Content-Type", "application/json")
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
                
                if (response.isSuccessful) {
                    // 保存JSON到本地文件
                    saveJsonToLocal(res)
                    
                    ThreadUtils.runOnUiThread {
                        Toast.makeText(
                            this,
                            "数据发送成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    throw Exception("服务器响应错误: ${response.code}")
                }

            }catch (e: Exception){
                e.printStackTrace()
                android.util.Log.e("UserInfoActivity", "网络请求失败", e)
                ThreadUtils.runOnUiThread {
                    Toast.makeText(
                        this,
                        "网络连接失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun saveJsonToLocal(json: String) {
        try {
            val file = File(filesDir, "recommend_result.json")
            file.writeText(json)
            android.util.Log.d("UserInfoActivity", "JSON已保存到: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("UserInfoActivity", "保存JSON失败", e)
        }
    }

    private fun sendUserInfoToHome() {
        //获取输入信息
        selectedProvince = provinceSpinner.selectedItem.toString()
        selectedYear = yearSpinner.selectedItem.toString()
        selectedSubjects = subjects.filter{it.isSelected}.map { it.name }

        //发送数据到后端
        sendMessagetoPy()

        //传递
        val returnIntent = Intent()
        returnIntent.putExtra("province", selectedProvince)
        returnIntent.putExtra("year", selectedYear)
        returnIntent.putExtra("subjects", ArrayList(selectedSubjects))

        //返回
        setResult(Activity.RESULT_OK, returnIntent)
    }

    private fun loadYearData() {
        try {
            //获取年份列表
            yearsList = YearModule.getAllYears()

            val yearsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsList)
            yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = yearsAdapter
            
            //默认选择最新年份
            val defaultYearIndex = yearsList.indexOfFirst { it == "2025" }
            if (defaultYearIndex != -1) {
                yearSpinner.setSelection(defaultYearIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        //省份选择监听
        provinceSpinner.setOnItemSelectedListener { _, _, position, _ ->
            //监听事件
        }

        //倾向省份选择监听
        userProvinceSpinner.setOnItemSelectedListener { _, _, position, _ ->
            //监听事件

        }

        //年份选择监听
        yearSpinner.setOnItemSelectedListener { _, _, position, _ ->
            //监听事件
        }

    }

    private fun loadProvinceData() {
        try {
            //读取json文件
            val inputStream = assets.open("province.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            //Gson解析json
            val type = object : TypeToken<List<Province>>(){}.type
            provinceList = Gson().fromJson(jsonString, type)

            //设置省份数据
            val provinceNames = provinceList.map { it.name }
            val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinceNames)
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = provinceAdapter
            userProvinceSpinner.adapter = provinceAdapter
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun loadMajorClassData() {
        try {
            //读取json文件
            val inputStream = assets.open("all_majors.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            //Gson解析json
            val type = object : TypeToken<List<MajorClass>>(){}.type
            majorClassList = Gson().fromJson(jsonString, type)

            //设置专业大类数据
            val classNames = majorClassList.map { it.className }
            val classAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, classNames)
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userLikeMajorClass.adapter = classAdapter

            //设置专业大类选择监听器
            setupMajorClassListener()
        }catch (e: Exception){
            e.printStackTrace()
            android.util.Log.e("UserInfoActivity", "加载专业数据失败", e)
        }
    }

    private fun initViews() {
        provinceSpinner = findViewById(R.id.edit_user_province)
        yearSpinner = findViewById(R.id.user_year)
        flexBox = findViewById(R.id.flexbox)
        userProvinceSpinner = findViewById(R.id.user_like_province)
        userLikeMajor = findViewById(R.id.user_like_major)
        userLikeMajorClass = findViewById(R.id.user_like_major_class)
    }

    private fun initSubjects() {
        //初始化科目列表
        subjects.clear()
        subjects.addAll(DataModule.getAllSubjects())
    }

    private fun setupSubjectClickListeners() {
        //为每个科目标签设置点击监听器
        val subjectIds = listOf(
            R.id.btn_physics, R.id.btn_history, R.id.btn_politics,
            R.id.btn_chemical, R.id.btn_geography, R.id.btn_biology
        )

        subjectIds.forEach { id ->
            findViewById<TextView>(id)?.let { textView ->
                val subjectName = textView.text.toString()
                subjectViews[subjectName] = textView

                textView.setOnClickListener {
                    handleSubjectClick(subjectName)
                }
            }
        }
    }

    private fun handleSubjectClick(subjectName: String) {
        val subject = subjects.find { it.name == subjectName } ?: return
        val textView = subjectViews[subjectName] ?: return
        //切换选中状态
        subject.isSelected = !subject.isSelected
        //更新UI
        updateSubjectUI(textView, subject.isSelected)
        //更新已选科目显示
        updateSelectedSubjectsDisplay()
    }

    private fun updateSubjectUI(textView: TextView, isSelected: Boolean) {
        //更新标签样式
        textView.setBackgroundResource(
            if (isSelected) R.drawable.select_tag
            else R.drawable.tag
        )
        textView.setTextColor(
            resources.getColor(
                if (isSelected) R.color.white
                else R.color.black,
                theme
            )
        )
    }

    private fun updateSelectedSubjectsDisplay() {
        val selectedSubjects = subjects.filter { it.isSelected }
        val displayText = when {
            selectedSubjects.isEmpty() -> "--/--/--"
            else -> selectedSubjects.joinToString("/") { it.name.first().toString() }
        }
        findViewById<TextView>(R.id.user_course)?.text = displayText
    }

    private fun setupMajorClassListener() {
        userLikeMajorClass.setOnItemSelectedListener { _, _, position, _ ->
            //当选择专业大类时，更新具体专业列表
            val selectedMajorClass = majorClassList.getOrNull(position)
            if (selectedMajorClass != null) {
                val majors = selectedMajorClass.`class`.map { it.name }
                val majorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, majors)
                majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                userLikeMajor.adapter = majorAdapter
            }
        }

        userLikeMajor.setOnItemSelectedListener { _, _, position, _ ->
            val selectedMajor = userLikeMajor.selectedItem?.toString()
        }
    }

    override fun getPageName(): PageName {
        return PageName.USER_INFO
    }

    //Spinner 扩展函数，简化监听器设置
    fun Spinner.setOnItemSelectedListener(onItemSelected: (parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onItemSelected(parent, view, position, id)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                //不做处理
            }
        }
    }
}