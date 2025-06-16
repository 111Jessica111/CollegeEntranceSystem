package com.example.collegeentrancesystem.module.detail

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.collegeentrancesystem.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// 数据模型类
data class Province_1(
    val name: String,
    val city: List<City>
)

data class City(
    val name: String,
    val area: List<String>
)

class TestActivity : AppCompatActivity() {
    private lateinit var provinceSpinner: Spinner
    private lateinit var citySpinner: Spinner
    private lateinit var areaSpinner: Spinner
    private var provinceList: List<Province_1> = emptyList()
    private var currentCityList: List<City> = emptyList()
    private var currentAreaList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)

        // 初始化视图
        initViews()
        // 加载省份数据
        loadProvinceData()
        // 设置监听器
        setupListeners()
    }

    private fun initViews() {
        provinceSpinner = findViewById(R.id.province)
        citySpinner = findViewById(R.id.city)
        areaSpinner = findViewById(R.id.area)
    }

    private fun loadProvinceData() {
        try {
            // 读取 JSON 文件
            val inputStream = assets.open("province.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // 使用 Gson 解析 JSON 数据
            val type = object : TypeToken<List<Province_1>>() {}.type
            provinceList = Gson().fromJson(jsonString, type)

            // 设置省份数据
            val provinceNames = provinceList.map { it.name }
            val provinceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinceNames)
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = provinceAdapter

            // 初始化城市和地区数据
            updateCityList(0)
            updateAreaList(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        // 省份选择监听
        provinceSpinner.setOnItemSelectedListener { _, _, position, _ ->
            updateCityList(position)
        }

        // 城市选择监听
        citySpinner.setOnItemSelectedListener { _, _, position, _ ->
            updateAreaList(position)
        }
    }

    private fun updateCityList(provincePosition: Int) {
        if (provincePosition >= 0 && provincePosition < provinceList.size) {
            currentCityList = provinceList[provincePosition].city
            val cityNames = currentCityList.map { it.name }
            val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            citySpinner.adapter = cityAdapter
            citySpinner.setSelection(0)
        }
    }

    private fun updateAreaList(cityPosition: Int) {
        if (cityPosition >= 0 && cityPosition < currentCityList.size) {
            currentAreaList = currentCityList[cityPosition].area
            val areaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentAreaList)
            areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            areaSpinner.adapter = areaAdapter
            areaSpinner.setSelection(0)
        }
    }
}

// Spinner 扩展函数，简化监听器设置
fun Spinner.setOnItemSelectedListener(onItemSelected: (parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) -> Unit) {
    this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
            onItemSelected(parent, view, position, id)
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            // 不做处理
        }
    }
}