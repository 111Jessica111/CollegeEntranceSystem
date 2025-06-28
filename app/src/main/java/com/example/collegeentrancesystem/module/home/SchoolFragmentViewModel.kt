package com.example.collegeentrancesystem.module.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.collegeentrancesystem.base.BaseViewModel
import com.example.collegeentrancesystem.bean.University
import com.example.collegeentrancesystem.constant.PageName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SchoolFragmentViewModel: BaseViewModel() {
    
    private val _universities = MutableLiveData<List<University>>()
    val universities: LiveData<List<University>> = _universities
    
    private var allUniversities = listOf<University>()
    private var currentPage = 0
    private val pageSize = 40
    private var isLoading = false
    
    override fun getPageName(): PageName {
        return PageName.MAIN
    }
    
    fun loadUniversities(context: Context) {
        if (isLoading) return
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                isLoading = true
                
                //如果还没有加载过所有数据，先加载
                if (allUniversities.isEmpty()) {
                    val jsonString = context.assets.open("universities.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<University>>() {}.type
                    allUniversities = Gson().fromJson(jsonString, type)
                }
                
                //计算当前页的数据
                val startIndex = currentPage * pageSize
                val endIndex = minOf(startIndex + pageSize, allUniversities.size)
                
                if (startIndex < allUniversities.size) {
                    val currentPageData = allUniversities.subList(startIndex, endIndex)
                    
                    withContext(Dispatchers.Main) {
                        if (currentPage == 0) {
                            _universities.value = currentPageData
                        } else {
                            val currentList = _universities.value?.toMutableList() ?: mutableListOf()
                            currentList.addAll(currentPageData)
                            _universities.value = currentList
                        }
                        currentPage++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadMoreUniversities(context: Context) {
        loadUniversities(context)
    }
    
    fun canLoadMore(): Boolean {
        return !isLoading && (currentPage * pageSize) < allUniversities.size
    }
}