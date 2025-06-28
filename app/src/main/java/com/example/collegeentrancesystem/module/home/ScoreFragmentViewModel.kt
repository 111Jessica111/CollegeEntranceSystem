package com.example.collegeentrancesystem.module.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseViewModel
import com.example.collegeentrancesystem.bean.College
import com.example.collegeentrancesystem.bean.Entry
import com.example.collegeentrancesystem.bean.ScoreData
import com.example.collegeentrancesystem.constant.PageName
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry as ChartEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreFragmentViewModel: BaseViewModel() {

    private val _scoreChartData = MutableLiveData<LineData>()
    val scoreChartData: LiveData<LineData> = _scoreChartData

    private val _scoreList = MutableLiveData<List<ScoreData>>()
    val scoreList: LiveData<List<ScoreData>> = _scoreList

    fun loadScoreChartData(){
        try {
            //测试数据
            val scoreDatas = listOf(
                Entry(xScore = 200, yPeople = 20),
                Entry(xScore = 500, yPeople = 25),
                Entry(xScore = 750, yPeople = 8)
            )
            
            //转换为MPAndroidChart需要的Entry格式
            val chartEntries = scoreDatas.map { entry ->
                ChartEntry(entry.xScore.toFloat(), entry.yPeople.toFloat())
            }
            val scoreDataSet = LineDataSet(chartEntries, "分数段人数分布")
            //最简化的样式设置
            scoreDataSet.color = android.graphics.Color.parseColor("#FF5722") // 使用红色
            scoreDataSet.lineWidth = 2f
            scoreDataSet.setCircleColor(android.graphics.Color.RED)
            scoreDataSet.circleRadius = 4f
            scoreDataSet.setDrawValues(false) //关闭数值显示
            scoreDataSet.setDrawCircles(true) //显示数据点
            scoreDataSet.setDrawCircleHole(false) //显示空心圆
            
            val lineData = LineData(scoreDataSet)
            
            _scoreChartData.value = lineData
            
        } catch (e: Exception) {
            //创建一个简单的空数据作为fallback
            try {
                val emptyDataSet = LineDataSet(listOf<ChartEntry>(), "暂无数据")
                val emptyLineData = LineData(emptyDataSet)
                _scoreChartData.value = emptyLineData
            } catch (e2: Exception) {
                Log.e("ScoreFragmentViewModel", "创建空数据失败", e2)
            }
        }
    }

    fun loadScoreData(context: Context) {
        try {
            //读取JSON文件
            val inputStream = context.assets.open("score.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            //使用Gson解析JSON
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val jsonList: List<Map<String, String>> = Gson().fromJson(jsonString, type)
            
            //转换为ScoreData对象列表
            val scoreDataList = jsonList.map { jsonMap ->
                ScoreData(
                    score = jsonMap["分数"] ?: "",
                    people = jsonMap["人数"] ?: "",
                    rank = jsonMap["排名"] ?: ""
                )
            }
            
            //过滤掉人数为0的数据，只显示有人的分数段
            val filteredScoreData = scoreDataList.filter { it.people != "0" }
            
            _scoreList.value = filteredScoreData
            
        } catch (e: Exception) {
            Log.e("ScoreFragmentViewModel", "加载分数数据失败", e)
            // 如果加载失败，设置空列表
            _scoreList.value = emptyList()
        }
    }

    override fun getPageName(): PageName {
        return PageName.MAIN
    }
}