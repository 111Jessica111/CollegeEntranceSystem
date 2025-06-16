package com.example.collegeentrancesystem.module.home

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.navigation.Router.navigation

class HomeFragment : Fragment() {

    private lateinit var btnEditInfo: LinearLayout
    private lateinit var userProvince: TextView
    private lateinit var userCourseChoose: TextView
    private lateinit var userYear: TextView
    private lateinit var userScore: TextView
    private lateinit var inputUserScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        //初始化
        btnEditInfo = view.findViewById(R.id.btn_edit_info)
        userProvince = view.findViewById(R.id.user_province)
        userCourseChoose = view.findViewById(R.id.user_course_choose)
        userYear = view.findViewById(R.id.user_year)
        userScore = view.findViewById(R.id.user_score)

        btnEditInfo.setOnClickListener {
            (requireActivity() as? BaseActivity<*>)?.let { activity ->
                activity.navigation(PageName.USER_INFO){ intent ->

                }
            }
        }

        view.findViewById<ImageButton>(R.id.btn_edit_score).setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog.setContentView(R.layout.input_score)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()

            inputUserScore = dialog.findViewById(R.id.user_score)

            //监听
            inputUserScore.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    userScore.text = inputUserScore.text.toString()
                    dialog.dismiss()
                    true //表示事件已处理
                } else {
                    false //事件未处理
                }
            }
        }

        return view
    }
}