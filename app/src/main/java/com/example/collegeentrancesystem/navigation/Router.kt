package com.example.collegeentrancesystem.navigation

import android.content.Intent
import com.example.collegeentrancesystem.MainActivity
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.module.detail.UserInfoActivity

object Router {
    fun BaseActivity<*>.navigation(pageName: PageName, intentAction:(Intent)->Unit){
        return when(pageName){
            PageName.MAIN ->{
                val intent = Intent(this, MainActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
            PageName.USER_INFO -> {
                val intent = Intent(this, UserInfoActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
        }
    }
}