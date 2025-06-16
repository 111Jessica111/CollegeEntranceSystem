package com.example.collegeentrancesystem

import com.example.collegeentrancesystem.base.BaseViewModel
import com.example.collegeentrancesystem.constant.PageName

class MainViewModel: BaseViewModel(){
    override fun getPageName(): PageName {
        return PageName.MAIN
    }
}