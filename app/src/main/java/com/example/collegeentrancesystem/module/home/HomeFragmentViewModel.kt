package com.example.collegeentrancesystem.module.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseViewModel
import com.example.collegeentrancesystem.bean.College
import com.example.collegeentrancesystem.constant.PageName

class HomeFragmentViewModel: BaseViewModel() {
    
    private val _collegeList = MutableLiveData<List<College>>()
    val collegeList: LiveData<List<College>> = _collegeList
    
    override fun getPageName(): PageName {
        return PageName.MAIN
    }
    
    fun loadCollegeData() {
        //模拟数据，实际应该从网络或数据库获取
        val colleges = listOf(
            College(
                photo = R.mipmap.college_1,
                name = "华中科技大学",
                location = "湖北武汉",
                type = "综合类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985", "211", "强基计划")
            ),
            College(
                photo = R.mipmap.college_2,
                name = "北京大学",
                location = "北京",
                type = "综合类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985", "211", "强基计划")
            ),
            College(
                photo = R.mipmap.college_3,
                name = "武汉大学",
                location = "湖北武汉",
                type = "综合类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985", "211")
            ),
            College(
                photo = R.mipmap.college_4,
                name = "复旦大学",
                location = "上海",
                type = "综合类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985", "211", "强基计划")
            ),
            College(
                photo = R.mipmap.college_5,
                name = "南京大学",
                location = "江苏南京",
                type = "综合类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985", "211","C9联盟")
            ),
            College(
                photo = R.mipmap.college_6,
                name = "陕西西安",
                location = "湖北武汉",
                type = "财经类",
                level = "本科",
                nature = "公办",
                tags = listOf("双一流", "985","211")
            )
        )
        _collegeList.value = colleges
    }
}