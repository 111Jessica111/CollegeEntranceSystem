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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.example.collegeentrancesystem.base.list.BaseAdapter
import com.example.collegeentrancesystem.bean.College
import com.example.collegeentrancesystem.bean.DifficultyCoefficient
import com.example.collegeentrancesystem.bean.DifficultyCoefficientSet
import com.example.collegeentrancesystem.constant.Network
import com.example.collegeentrancesystem.module.adapter.CollegeViewHolder
import com.example.collegeentrancesystem.module.detail.UserInfoActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import android.util.Log
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.collegeentrancesystem.base.BaseActivity
import com.example.collegeentrancesystem.constant.PageName
import com.example.collegeentrancesystem.navigation.Router.navigation
import java.io.File

class HomeFragment : Fragment() {

    private lateinit var btnEditInfo: LinearLayout
    private lateinit var userProvince: TextView
    private lateinit var userCourseChoose: TextView
    private lateinit var userYear: TextView
    private lateinit var userScore: TextView
    private lateinit var inputUserScore: EditText
    private lateinit var inputUserDiff: EditText
    private lateinit var predictScore: TextView
    private lateinit var test4: TextView
    private lateinit var btnAdventure: LinearLayout
    private lateinit var btnSafe: LinearLayout
    private lateinit var btnGuarantee: LinearLayout


    private lateinit var collegeRecycleView: RecyclerView
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var collegeAdapter: BaseAdapter

    private val userInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val province = data?.getStringExtra("province")
            val year = data?.getStringExtra("year")
            val subjects = data?.getStringArrayListExtra("subjects")
            val needRefresh = data?.getBooleanExtra("needRefresh", false) ?: false

            userProvince.text = province?.take(2) ?: ""
            userYear.text = year
            userCourseChoose.text = subjects?.joinToString("/") { it.take(1) } ?: ""
            
            //根据年份更新test4的文本
            updateTest4Text()
            
            // 如果标记需要刷新，延迟一下确保文件写入完成后再刷新
            if (needRefresh) {
                // 延迟500ms确保文件写入完成
                view?.postDelayed({
                    updateRecommendCounts()
                }, 500)
            } else {
                // 用户信息更新后，刷新推荐数量
                updateRecommendCounts()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = HomeFragmentViewModel()
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
        test4 = view.findViewById(R.id.test_4)
        btnAdventure = view.findViewById(R.id.btn_adventure)
        btnSafe = view.findViewById(R.id.btn_safe)
        btnGuarantee = view.findViewById(R.id.btn_guarantee)

        collegeRecycleView = view.findViewById(R.id.college_recycleView)
        
        //设置RecyclerView
        setupRecyclerView()
        
        //加载大学数据
        viewModel.loadCollegeData()
        
        //初始化test4文本
        updateTest4Text()

        btnAdventure.setOnClickListener {
            (requireActivity() as? BaseActivity<*>)?.let { activity ->
                activity.navigation(PageName.ADVENTURE){intent ->

                }
            }
        }

        btnGuarantee.setOnClickListener {
            (requireActivity() as? BaseActivity<*>)?.let { activity ->
                activity.navigation(PageName.GURANTEE){intent ->

                }
            }
        }

        btnSafe.setOnClickListener {
            (requireActivity() as? BaseActivity<*>)?.let { activity ->
                activity.navigation(PageName.SAFE){intent ->

                }
            }
        }


        btnEditInfo.setOnClickListener {
            val intent = Intent(requireContext(), UserInfoActivity::class.java)
            userInfoLauncher.launch(intent)
        }

        view.findViewById<ImageButton>(R.id.btn_edit_score).setOnClickListener {
            InputUserScore()
        }
        
        // 更新推荐数量显示
        updateRecommendCounts()
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        // 每次返回首页时更新推荐数量
        updateRecommendCounts()
    }
    
    /**
     * 更新推荐数量显示
     */
    private fun updateRecommendCounts() {
        try {
            val file = File(requireContext().filesDir, "recommend_result.json")
            
            if (!file.exists()) {
                // 文件不存在，显示默认值
                setRecommendCounts("--", "--", "--")
                return
            }
            
            val jsonString = file.readText()

            val jsonObject = JSONObject(jsonString)
            
            if (jsonObject.optBoolean("error", true)) {
                setRecommendCounts("--", "--", "--")
                return
            }
            
            val dataObject = jsonObject.getJSONObject("data")
            
            // 获取各类型推荐数量
            val adventureCount = if (dataObject.has("adventure")) dataObject.getJSONArray("adventure").length() else 0
            val stableCount = if (dataObject.has("stable")) dataObject.getJSONArray("stable").length() else 0
            val safeCount = if (dataObject.has("safe")) dataObject.getJSONArray("safe").length() else 0
            
            setRecommendCounts(adventureCount.toString(), stableCount.toString(), safeCount.toString())
            
        } catch (e: Exception) {
            e.printStackTrace()
            setRecommendCounts("--", "--", "--")
        }
    }
    
    /**
     * 设置推荐数量到UI
     */
    private fun setRecommendCounts(adventure: String, stable: String, safe: String) {
        view?.findViewById<TextView>(R.id.adventure_sum)?.text = adventure
        view?.findViewById<TextView>(R.id.safe_sum)?.text = stable
        view?.findViewById<TextView>(R.id.guarantee_sum)?.text = safe
    }
    
    private fun setupRecyclerView() {
        collegeRecycleView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        
        collegeAdapter = BaseAdapter().build {
            setItems<CollegeViewHolder, College>(
                lifecycleOwner = viewLifecycleOwner,
                layoutId = R.layout.college_show,
                list = viewModel.collegeList
            ) { holder, college ->
                holder.bind(college)

                holder.parent.setOnClickListener {
                    //点击item时的监听
                }
            }
        }
        collegeRecycleView.adapter = collegeAdapter
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
                
                //更新test4文本
                updateTest4Text()
                
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

        send2023MessageToPy(dialog)

        inputUserDiff = dialog.findViewById(R.id.user_diff)
        inputUserDiff.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val userInput = inputUserDiff.text.toString()

                Thread {
                    try {
                        val userDiffJson = JSONObject().apply {
                            put("diff", userInput)
                        }.toString()
                        
                        //创建HTTP
                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .build()
                        val mediaType = "application/json".toMediaType()
                        val request = Request.Builder()
                            .url("${Network.IP_1}/api/user_difficulty")
                            .post(RequestBody.create(mediaType, userDiffJson))
                            .addHeader("Content-Type", "application/json")
                            .build()

                        //执行
                        val response = client.newCall(request).execute()

                        val res = response.body?.string() ?: ""

                        if (res.isEmpty()) {
                            throw Exception("服务器返回空响应")
                        }

                        val jsonObject = JSONObject(res)
                        
                        //检查响应状态
                        if (jsonObject.has("status") && jsonObject.getString("status") != "success") {
                            val errorMsg = jsonObject.optString("message", "未知错误")
                            throw Exception("服务器错误: $errorMsg")
                        }

                        val rank_before = jsonObject.getString("searchRank")
                        
                        //在主线程更新UI
                        runOnUiThread {
                            predictScore.text = rank_before.toString()

                            dialog.dismiss()
                        }

                    } catch (e: Exception) {
                        Log.e("HomeFragment", "发送难度系数失败", e)
                        runOnUiThread {

                            dialog.dismiss()
                        }
                    }
                }.start()
                true //表示事件已处理
            } else {
                false //事件未处理
            }
        }
    }

    private fun send2023MessageToPy(dialog: Dialog) {

        val diff_2022 = dialog.findViewById<TextView>(R.id.diff_2022)
        val diff_2021 = dialog.findViewById<TextView>(R.id.diff_2021)
        val diff_2020 = dialog.findViewById<TextView>(R.id.diff_2020)
        val diff_2019 = dialog.findViewById<TextView>(R.id.diff_2019)
        val diff_2018 = dialog.findViewById<TextView>(R.id.diff_2018)
        val diff_2017 = dialog.findViewById<TextView>(R.id.diff_2017)

        // 显示加载状态
        runOnUiThread {
            diff_2022.text = "加载中..."
            diff_2021.text = "加载中..."
            diff_2020.text = "加载中..."
            diff_2019.text = "加载中..."
            diff_2018.text = "加载中..."
            diff_2017.text = "加载中..."
        }
        //连接后端
        Thread{
            try {
                if (userScore.text.toString() == "---"){
                    runOnUiThread {
                        //重置为默认状态
                        resetDifficultyDisplay(dialog)
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
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP_1}/api/difficulty_coefficient")
                    .post(RequestBody.create(mediaType, userInfoJson))
                    .build()
                //执行
                val response = client.newCall(request).execute()

                val res = response.body?.string() ?: ""
                
                if (res.isEmpty()) {
                    throw Exception("服务器返回空响应")
                }

                val jsonObject = JSONObject(res)

                //检查响应状态
                if (jsonObject.has("status") && jsonObject.getString("status") != "success") {
                    val errorMsg = jsonObject.optString("message", "未知错误")
                    throw Exception("服务器错误: $errorMsg")
                }

                val diff_before = jsonObject.getString("result_data")
                
                //解析难度系数数据
                parseAndDisplayDifficultyCoefficients(diff_before, dialog)

                val rank_before = jsonObject.getString("searchRank")
                predictScore.text = rank_before.toString()
                sendMessagetoPy_1(rank_before.toString())

                //更新test4文本
                runOnUiThread {
                    updateTest4Text()
                }

            }catch (e: Exception){
                runOnUiThread {
                    // 重置为默认状态
                    resetDifficultyDisplay(dialog)
                }
            }
        }.start()
    }

    private fun sendMessagetoPy_1(string: String) {
        Thread {
            try {
                val userDiffJson = JSONObject().apply {
                    put("rank", string)
                }.toString()

                //创建HTTP
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP}/api/rank")
                    .post(RequestBody.create(mediaType, userDiffJson))
                    .addHeader("Content-Type", "application/json")
                    .build()

                //执行
                val response = client.newCall(request).execute()

                val res = response.body?.string() ?: ""

                if (res.isEmpty()) {
                    throw Exception("服务器返回空响应")
                }

                val jsonObject = JSONObject(res)

                //检查响应状态
                if (jsonObject.has("status") && jsonObject.getString("status") != "success") {
                    val errorMsg = jsonObject.optString("message", "未知错误")
                    throw Exception("服务器错误: $errorMsg")
                }

            } catch (e: Exception) {
                runOnUiThread {

                }
            }
        }.start()
    }

    /**
     * 重置难度系数显示为默认状态
     */
    private fun resetDifficultyDisplay(dialog: Dialog) {
        val diff_2022 = dialog.findViewById<TextView>(R.id.diff_2022)
        val diff_2021 = dialog.findViewById<TextView>(R.id.diff_2021)
        val diff_2020 = dialog.findViewById<TextView>(R.id.diff_2020)
        val diff_2019 = dialog.findViewById<TextView>(R.id.diff_2019)
        val diff_2018 = dialog.findViewById<TextView>(R.id.diff_2018)
        val diff_2017 = dialog.findViewById<TextView>(R.id.diff_2017)
        
        diff_2022.text = "---"
        diff_2021.text = "---"
        diff_2020.text = "---"
        diff_2019.text = "---"
        diff_2018.text = "---"
        diff_2017.text = "---"
    }
    
    /**
     * 解析并显示难度系数数据
     * 数据格式: [{2017: 0.3735}, {2018: 0.3259}, {2019: 0.3353}, {2020: 0.3029}, {2021: 0.3438}, {2022: 0.3502}]
     */
    private fun parseAndDisplayDifficultyCoefficients(diffData: String, dialog: Dialog) {
        try {
            val difficultySet = parseDifficultyData(diffData)
            updateDifficultyUI(difficultySet, dialog)
        } catch (e: Exception) {
            runOnUiThread {
            }
        }
    }
    
    /**
     * 解析难度系数数据为DifficultyCoefficientSet对象
     */
    private fun parseDifficultyData(diffData: String): DifficultyCoefficientSet {
        val coefficients = mutableListOf<DifficultyCoefficient>()
        
        //用JSON解析
        try {
            val jsonArray = org.json.JSONArray(diffData)
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val year = keys.next()
                    val coefficient = jsonObject.getDouble(year)
                    coefficients.add(DifficultyCoefficient(year, coefficient))
                }
            }
            
            return DifficultyCoefficientSet(coefficients)
            
        } catch (e: Exception) {
            Log.d("HomeFragment", "JSON解析失败，尝试字符串解析: ${e.message}")
        }
        
        //字符串解析
        val cleanData = diffData.trim().removeSurrounding("[", "]")
        val items = cleanData.split("}, {")
        
        for (item in items) {
            try {
                val cleanItem = item.trim().removeSurrounding("{", "}")
                val parts = cleanItem.split(": ")
                if (parts.size == 2) {
                    val year = parts[0].trim()
                    val coefficient = parts[1].trim().toDouble()
                    coefficients.add(DifficultyCoefficient(year, coefficient))
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "解析单个项目失败: $item", e)
            }
        }
        
        return DifficultyCoefficientSet(coefficients)
    }
    
    /**
     * 更新难度系数UI显示
     */
    private fun updateDifficultyUI(difficultySet: DifficultyCoefficientSet, dialog: Dialog) {
        runOnUiThread {
            try {
                val diff_2022 = dialog.findViewById<TextView>(R.id.diff_2022)
                val diff_2021 = dialog.findViewById<TextView>(R.id.diff_2021)
                val diff_2020 = dialog.findViewById<TextView>(R.id.diff_2020)
                val diff_2019 = dialog.findViewById<TextView>(R.id.diff_2019)
                val diff_2018 = dialog.findViewById<TextView>(R.id.diff_2018)
                val diff_2017 = dialog.findViewById<TextView>(R.id.diff_2017)
                
                //设置各年份的难度系数
                diff_2022.text = difficultySet.getCoefficientByYear("2022")?.getFormattedCoefficient() ?: "---"
                diff_2021.text = difficultySet.getCoefficientByYear("2021")?.getFormattedCoefficient() ?: "---"
                diff_2020.text = difficultySet.getCoefficientByYear("2020")?.getFormattedCoefficient() ?: "---"
                diff_2019.text = difficultySet.getCoefficientByYear("2019")?.getFormattedCoefficient() ?: "---"
                diff_2018.text = difficultySet.getCoefficientByYear("2018")?.getFormattedCoefficient() ?: "---"
                diff_2017.text = difficultySet.getCoefficientByYear("2017")?.getFormattedCoefficient() ?: "---"
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "更新UI失败", e)
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
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaType()
                val request = Request.Builder()
                    .url("${Network.IP_1}/api/difficulty_coefficient")
                    .post(RequestBody.create(mediaType, userInfoJson))
                    .build()
                //执行
                val response = client.newCall(request).execute()

                val res = response.body?.string() ?: ""
                val jsonObject = JSONObject(res)

                val rank_before = jsonObject.getString("searchRank")
                predictScore.text = rank_before.toString()

                sendMessagetoPy_1(inputUserScore.text.toString())

                //更新test4文本
                runOnUiThread {
                    updateTest4Text()
                }

            }catch (e: Exception){
                e.printStackTrace()
                runOnUiThread {
                }
            }
        }.start()
    }

    //根据年份更新test4的文本
    private fun updateTest4Text() {
        if (userYear.text.toString() == "2023") {
            test4.text = "预估排名："
        } else {
            test4.text = "实际排名："
        }
    }
}