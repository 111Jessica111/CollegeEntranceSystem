package com.example.collegeentrancesystem.module.home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.collegeentrancesystem.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry as ChartEntry

class ScoreFragment : Fragment() {
    private lateinit var scoreChart: LineChart
    private lateinit var viewModel: ScoreFragmentViewModel

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
            scoreChart = view.findViewById(R.id.score_line)
            //初始化图表
            setupChart()
            //加载数据
            viewModel.loadScoreChartData()
            //观察数据变化
            viewModel.scoreChartData.observe(viewLifecycleOwner) { lineData ->
                try {
                    scoreChart.data = lineData
                    scoreChart.invalidate()
                    context?.let { ctx ->
                        Toast.makeText(ctx, "图表数据加载完成", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ScoreFragment", "设置图表数据失败", e)
                }
            }
            return view
        } catch (e: Exception) {
            Log.e("ScoreFragment", "创建视图失败", e)
            return null
        }
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
            scoreChart.setScaleEnabled(false) // 暂时关闭缩放
            scoreChart.setPinchZoom(false) // 暂时关闭双指缩放
            
            // 设置选择监听器
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
            
            //设置Y轴
            val leftAxis = scoreChart.axisLeft
            leftAxis.setDrawGridLines(false) //关闭Y轴网格线
            
            //隐藏右侧Y轴
            scoreChart.axisRight.isEnabled = false
            
            //关闭网格背景
            scoreChart.setDrawGridBackground(false)
            
            //暂时关闭动画，避免计算问题
            scoreChart.animateX(1000)
            
        } catch (e: Exception) {
            Log.e("ScoreFragment", "设置图表失败", e)
        }
    }
}