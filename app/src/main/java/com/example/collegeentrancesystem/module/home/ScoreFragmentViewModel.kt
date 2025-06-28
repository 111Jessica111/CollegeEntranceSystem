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

    fun loadScoreChartData(context: Context){
        try {
            //限制数据点数量，避免内存溢出
            val maxDataPoints = 100 //最多显示100个数据点
            
            //从score.json加载实际数据
            val inputStream = context.assets.open("score.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val jsonList: List<Map<String, String>> = Gson().fromJson(jsonString, type)
            
            //过滤掉人数为0的数据，并按分数排序
            val validData = jsonList
                .filter { it["人数"] != "0" }
                .map { 
                    Entry(
                        xScore = it["分数"]?.toIntOrNull() ?: 0,
                        yPeople = it["人数"]?.toIntOrNull() ?: 0
                    )
                }
                .filter { it.xScore > 0 && it.yPeople > 0 }
                .sortedBy { it.xScore }
            
            //数据采样：如果数据点过多，进行智能采样
            val sampledData = try {
                if (validData.size > maxDataPoints) {
                    //智能采样：保留重要数据点（峰值、谷值、边界值）
                    val step = validData.size / maxDataPoints
                    val sampled = mutableListOf<Entry>()
                    
                    //保留第一个数据点
                    sampled.add(validData.first())
                    
                    //保留最后一个数据点
                    sampled.add(validData.last())
                    
                    //寻找峰值和谷值
                    val peaks = findPeaks(validData, maxDataPoints / 4)
                    sampled.addAll(peaks)
                    
                    //均匀采样剩余数据点
                    val remainingPoints = maxDataPoints - sampled.size
                    if (remainingPoints > 0) {
                        val step2 = validData.size / remainingPoints
                        for (i in step2 until validData.size step step2) {
                            if (sampled.size < maxDataPoints && !sampled.contains(validData[i])) {
                                sampled.add(validData[i])
                            }
                        }
                    }
                    
                    //按分数排序并去重
                    sampled.distinctBy { it.xScore }.sortedBy { it.xScore }
                } else {
                    validData
                }
            } catch (e: Exception) {
                Log.e("ScoreFragmentViewModel", "智能采样失败，使用简单采样", e)
                // 备用方案：简单均匀采样
                if (validData.size > maxDataPoints) {
                    val step = validData.size / maxDataPoints
                    validData.filterIndexed { index, _ -> index % step == 0 }.take(maxDataPoints)
                } else {
                    validData
                }
            }
            
            Log.d("ScoreFragmentViewModel", "原始数据点: ${validData.size}, 采样后数据点: ${sampledData.size}")
            
            //转换为MPAndroidChart需要的Entry格式
            val chartEntries = sampledData.map { entry ->
                ChartEntry(entry.xScore.toFloat(), entry.yPeople.toFloat())
            }
            
            if (chartEntries.isEmpty()) {
                //如果没有有效数据，使用测试数据
                val testData = listOf(
                    Entry(xScore = 200, yPeople = 20),
                    Entry(xScore = 500, yPeople = 25),
                    Entry(xScore = 750, yPeople = 8)
                )
                val testEntries = testData.map { entry ->
                    ChartEntry(entry.xScore.toFloat(), entry.yPeople.toFloat())
                }
                createLineDataSet(testEntries, "测试数据")
            } else {
                createLineDataSet(chartEntries, "分数段人数分布")
            }
            
        } catch (e: Exception) {
            Log.e("ScoreFragmentViewModel", "加载图表数据失败", e)
            // 创建一个简单的空数据作为fallback
            try {
                val emptyDataSet = LineDataSet(listOf<ChartEntry>(), "暂无数据")
                val emptyLineData = LineData(emptyDataSet)
                _scoreChartData.value = emptyLineData
            } catch (e2: Exception) {
                Log.e("ScoreFragmentViewModel", "创建空数据失败", e2)
            }
        }
    }
    
    private fun createLineDataSet(chartEntries: List<ChartEntry>, label: String) {
        val scoreDataSet = LineDataSet(chartEntries, label)
        
        //优化样式设置，减少内存使用
        scoreDataSet.color = android.graphics.Color.parseColor("#FF5722")
        scoreDataSet.lineWidth = 2f
        scoreDataSet.setCircleColor(android.graphics.Color.RED)
        scoreDataSet.circleRadius = 2f //减小圆点半径
        scoreDataSet.setDrawValues(false) //关闭数值显示
        scoreDataSet.setDrawCircles(true) //显示数据点
        scoreDataSet.setDrawCircleHole(false) //显示空心圆
        
        //优化性能设置
        scoreDataSet.setDrawFilled(false) //关闭填充
        scoreDataSet.mode = LineDataSet.Mode.LINEAR //使用线性模式
        
        val lineData = LineData(scoreDataSet)
        _scoreChartData.value = lineData
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

    /**
     * 寻找数据中的峰值点
     */
    private fun findPeaks(data: List<Entry>, maxPeaks: Int): List<Entry> {
        if (data.size < 3) return emptyList()
        
        val peaks = mutableListOf<Entry>()
        
        //寻找局部最大值
        for (i in 1 until data.size - 1) {
            if (data[i].yPeople > data[i-1].yPeople && data[i].yPeople > data[i+1].yPeople) {
                peaks.add(data[i])
                if (peaks.size >= maxPeaks) break
            }
        }
        
        //如果峰值不够，添加一些高值点
        if (peaks.size < maxPeaks) {
            val sortedByValue = data.sortedByDescending { it.yPeople }
            for (entry in sortedByValue) {
                if (!peaks.contains(entry) && peaks.size < maxPeaks) {
                    peaks.add(entry)
                }
            }
        }
        
        return peaks.take(maxPeaks)
    }

    /**
     * 测试数据加载功能
     */
    fun testDataLoading(context: Context) {
        try {
            Log.d("ScoreFragmentViewModel", "开始测试数据加载...")
            
            // 测试JSON文件读取
            val inputStream = context.assets.open("score.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            Log.d("ScoreFragmentViewModel", "JSON文件大小: ${jsonString.length} 字符")
            
            // 测试JSON解析
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val jsonList: List<Map<String, String>> = Gson().fromJson(jsonString, type)
            Log.d("ScoreFragmentViewModel", "解析到 ${jsonList.size} 条原始数据")
            
            // 测试数据过滤
            val validData = jsonList
                .filter { it["人数"] != "0" }
                .map { 
                    Entry(
                        xScore = it["分数"]?.toIntOrNull() ?: 0,
                        yPeople = it["人数"]?.toIntOrNull() ?: 0
                    )
                }
                .filter { it.xScore > 0 && it.yPeople > 0 }
            
            Log.d("ScoreFragmentViewModel", "有效数据点: ${validData.size}")
            Log.d("ScoreFragmentViewModel", "数据范围: ${validData.minOfOrNull { it.xScore }} - ${validData.maxOfOrNull { it.xScore }}")
            Log.d("ScoreFragmentViewModel", "人数范围: ${validData.minOfOrNull { it.yPeople }} - ${validData.maxOfOrNull { it.yPeople }}")
            
        } catch (e: Exception) {
            Log.e("ScoreFragmentViewModel", "测试数据加载失败", e)
        }
    }

    override fun getPageName(): PageName {
        return PageName.MAIN
    }
}