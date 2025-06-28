package com.example.collegeentrancesystem.navigation

import android.content.Intent
import com.example.collegeentrancesystem.MainActivity
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.module.detail.CollegeAdventureActivity
import com.example.collegeentrancesystem.module.detail.CollegeGuranteeActivity
import com.example.collegeentrancesystem.module.detail.CollegeSafeActivity
import com.example.collegeentrancesystem.module.detail.UserInfoActivity

object Router {
    fun BaseActivity<*>.navigation(
        pageName: PageName,
        requestCode: Int? = null, // 新增：可选 requestCode
        intentAction: (Intent) -> Unit = {}
    ) {
        when (pageName) {
            PageName.MAIN -> {
                val intent = Intent(this, MainActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
            PageName.USER_INFO -> {
                val intent = Intent(this, UserInfoActivity::class.java)
                intentAction(intent)
                if (requestCode != null) {
                    startActivityForResult(intent, requestCode)
                } else {
                    startActivity(intent)
                }
            }
            PageName.ADVENTURE -> {
                val intent = Intent(this, CollegeAdventureActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
            PageName.SAFE -> {
                val intent = Intent(this, CollegeSafeActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
            PageName.GURANTEE -> {
                val intent = Intent(this, CollegeGuranteeActivity::class.java)
                intentAction(intent)
                startActivity(intent)
            }
        }
    }
}
