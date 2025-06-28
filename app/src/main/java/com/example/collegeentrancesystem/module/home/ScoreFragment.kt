package com.example.collegeentrancesystem.module.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.list.BaseAdapter
import com.example.collegeentrancesystem.constant.Province
import com.example.collegeentrancesystem.constant.SubjectModule
import com.example.collegeentrancesystem.constant.YearModule
import com.example.collegeentrancesystem.module.adapter.ScoreViewHolder
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry as ChartEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreFragment : Fragment() {
    private lateinit var scoreChart: LineChart
    private lateinit var viewModel: ScoreFragmentViewModel
    private lateinit var scoreRecyclerView: RecyclerView
    private lateinit var scoreAdapter: BaseAdapter

    private lateinit var provinceSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var subjectSpinner: Spinner

    private var provinceList: List<Province> = emptyList()
    private var yearsList: List<String> = emptyList()
    private var subjectList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            viewModel = ScoreFragmentViewModel()
        } catch (e: Exception) {
            Log.e("ScoreFragment", "ViewModel创建失败", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            // Inflate the layout for this fragment
            val view = inflater.inflate(R.layout.fragment_score, container, false)

            initViews(view)

            scoreChart = view.findViewById(R.id.score_line)
            scoreRecyclerView = view.findViewById(R.id.score_RecycleView)
            
            //初始化图表
            setupChart()
            //设置RecyclerView
            setupRecyclerView()
            //测试数据加载
            viewModel.testDataLoading(requireContext())
            //加载数据
            viewModel.loadScoreChartData(requireContext())
            viewModel.loadScoreData(requireContext())
            
            //观察数据变化
            viewModel.scoreChartData.observe(viewLifecycleOwner) { lineData ->
                try {
                    scoreChart.data = lineData
                    scoreChart.invalidate()
                } catch (e: Exception) {
                    Log.e("ScoreFragment", "设置图表数据失败", e)
                }
            }

            loadProvinceData()
            loadYearData()
            loadSubjectData()
            setupListeners()

            return view
        } catch (e: Exception) {
            Log.e("ScoreFragment", "创建视图失败", e)
            return null
        }
    }

    private fun setupRecyclerView() {
        scoreRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        scoreAdapter = BaseAdapter().build {
            setItems<ScoreViewHolder, com.example.collegeentrancesystem.bean.ScoreData>(
                lifecycleOwner = viewLifecycleOwner,
                layoutId = R.layout.score_show,
                list = viewModel.scoreList
            ) { holder, scoreData ->
                holder.bind(scoreData)
            }
        }
        scoreRecyclerView.adapter = scoreAdapter
    }

    private fun loadProvinceData() {
        try {
            //获取 Fragment 的上下文
            val context = context ?: return

            //读取 json 文件
            val inputStream = context.assets.open("province.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            //Gson 解析 json
            val type = object : TypeToken<List<Province>>() {}.type
            provinceList = Gson().fromJson(jsonString, type)

            //设置省份数据
            val provinceNames = provinceList.map { it.name }
            val provinceAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                provinceNames
            )
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = provinceAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadYearData() {
        try {
            //获取 Fragment 的上下文
            val context = context ?: return

            //获取年份列表
            yearsList = YearModule.getAllYears()

            val yearsAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, yearsList)
            yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = yearsAdapter

            //默认选择最新年份
            val defaultYearIndex = yearsList.indexOfFirst { it == "2025" }
            if (defaultYearIndex != -1) {
                yearSpinner.setSelection(defaultYearIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSubjectData() {
        try {
            //获取 Fragment 的上下文
            val context = context ?: return

            //获取分类列表
            subjectList = SubjectModule.getAllSubject()

            val subjectAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, subjectList)
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subjectSpinner.adapter = subjectAdapter

            //默认选择物理类
            val defaultYearIndex = yearsList.indexOfFirst { it == "物理类" }
            if (defaultYearIndex != -1) {
                yearSpinner.setSelection(defaultYearIndex)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun setupListeners() {
        //设置省份选择监听
        provinceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //省份选择监听事件
                val selectedProvince = provinceList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        //设置年份选择监听
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //年份选择监听事件
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun initViews(view: View) {
        provinceSpinner = view.findViewById(R.id.edit_user_province)
        yearSpinner = view.findViewById(R.id.edit_user_year)
        subjectSpinner = view.findViewById(R.id.edit_user_subjrct)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    private fun setupChart() {
        try {
            //最小化设置，避免NegativeArraySizeException
            scoreChart.description.isEnabled = false
            scoreChart.legend.isEnabled = true
            scoreChart.setTouchEnabled(true)
            scoreChart.setScaleEnabled(false) //暂时关闭缩放
            scoreChart.setPinchZoom(false) //暂时关闭双指缩放
            
            // 性能优化设置
            scoreChart.setMaxVisibleValueCount(50) //限制可见数据点数量
            scoreChart.setVisibleXRangeMaximum(50f) //限制X轴可见范围
            scoreChart.setAutoScaleMinMaxEnabled(true) //自动缩放
            
            //设置选择监听器
            scoreChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: ChartEntry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                    try {
                        e?.let {
                            val score = it.x.toInt()
                            val people = it.y.toInt()
                            val message = "分数: ${score}分, 人数: ${people}人"
                            context?.let { ctx ->
                                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ScoreFragment", "处理点击事件失败", e)
                    }
                }
                
                override fun onNothingSelected() {
                    //处理取消选择事件
                }
            })
            
            //设置X轴 - 最小化配置
            val xAxis = scoreChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false) //关闭X轴网格线
            xAxis.setLabelCount(5, true) // 限制X轴标签数量
            
            //设置Y轴
            val leftAxis = scoreChart.axisLeft
            leftAxis.setDrawGridLines(false) //关闭Y轴网格线
            leftAxis.setLabelCount(5, true) // 限制Y轴标签数量
            
            //隐藏右侧Y轴
            scoreChart.axisRight.isEnabled = false
            
            //关闭网格背景
            scoreChart.setDrawGridBackground(false)

            scoreChart.animateX(1000)
            
        } catch (e: Exception) {
            Log.e("ScoreFragment", "设置图表失败", e)
        }
    }
}