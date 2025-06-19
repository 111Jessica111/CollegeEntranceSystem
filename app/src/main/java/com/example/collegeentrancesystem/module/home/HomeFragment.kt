package com.example.collegeentrancesystem.module.home

import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import android.widget.Toast
import com.example.collegeentrancesystem.R
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.example.collegeentrancesystem.constant.Network
import com.example.collegeentrancesystem.module.detail.UserInfoActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import kotlin.jvm.java

class HomeFragment : Fragment() {

    private lateinit var btnEditInfo: LinearLayout
    private lateinit var userProvince: TextView
    private lateinit var userCourseChoose: TextView
    private lateinit var userYear: TextView
    private lateinit var userScore: TextView
    private lateinit var inputUserScore: EditText
    private lateinit var inputUserDiff: EditText
    private lateinit var predictScore: TextView

    private val userInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val province = data?.getStringExtra("province")
            val year = data?.getStringExtra("year")
            val subjects = data?.getStringArrayListExtra("subjects")

            userProvince.text = province?.take(2) ?: ""
            userYear.text = year
            userCourseChoose.text = subjects?.joinToString("/") { it.take(1) } ?: ""
        }
    }

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
        predictScore = view.findViewById(R.id.predict_score)


        btnEditInfo.setOnClickListener {
            val intent = Intent(requireContext(), UserInfoActivity::class.java)
            userInfoLauncher.launch(intent)
        }

        view.findViewById<ImageButton>(R.id.btn_edit_score).setOnClickListener {
            InputUserScore()
        }
        return view
    }
    //分数
    private fun InputUserScore() {
        val dialog = Dialog(requireActivity())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setContentView(R.layout.input_score)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        inputUserScore = dialog.findViewById(R.id.user_score)
        inputUserScore.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                userScore.text = inputUserScore.text.toString()
                dialog.dismiss()
                sendMessagetoPy()
                // 分数输入完成后判断年份
                if (userYear.text.toString() == "2023") {
                    InputDiff()
                }
                true //表示事件已处理
            } else {
                false //事件未处理
            }
        }
    }
    //难度
    private fun InputDiff() {

        val dialog = Dialog(requireActivity())
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setContentView(R.layout.input_difficulty)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        inputUserDiff = dialog.findViewById(R.id.user_diff)
        inputUserDiff.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.dismiss()
                true //表示事件已处理
            } else {
                false //事件未处理
            }
        }
    }

    private fun sendMessagetoPy() {
        //连接后端
        Thread{
            try {
                if (userScore.text.toString() == "---"){
                    runOnUiThread {
                        Toast.makeText(requireActivity(), "未输入分数", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val userInfoJson = JSONObject().apply {
                    put("province", userProvince.text.toString())
                    put("year", userYear.text.toString())
                    put("subject", userCourseChoose.text.toString().first())
                    put("inputScore", userScore.text.toString())
                }.toString()

                //创建HTTP
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP}/api/difficulty_coefficient")
                    .post(RequestBody.create(mediaType, userInfoJson))
                    .build()
                //执行
                val response = client.newCall(request).execute()

                val res = response.body?.string() ?: ""
                val jsonObject = JSONObject(res)

                if (userYear.text.toString() == "2023") {
                    val rank_now = jsonObject.getString("predictRank")

                }else{
                    val rank_before = jsonObject.getString("searchRank")
                    predictScore.text = rank_before.toString()
                }

            }catch (e: Exception){
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(requireActivity(), "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

}