package com.example.collegeentrancesystem.module.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.base.list.BaseAdapter
import com.example.collegeentrancesystem.module.adapter.SchoolViewHolder

class SchoolFragment : Fragment() {

    private lateinit var schoolRecycleView: RecyclerView
    private val viewModel: SchoolFragmentViewModel by viewModels()
    private lateinit var adapter: BaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_school, container, false)
        
        schoolRecycleView = view.findViewById(R.id.school_recycleView)
        
        setupRecyclerView()
        setupObservers()
        
        // 初始加载数据
        viewModel.loadUniversities(requireContext())
        
        return view
    }
    
    private fun setupRecyclerView() {
        adapter = BaseAdapter().build {
            setItems(
                lifecycleOwner = viewLifecycleOwner,
                layoutId = R.layout.school_show,
                list = viewModel.universities
            ) { holder: SchoolViewHolder, university ->
                holder.bind(university)
            }
        }
        
        schoolRecycleView.layoutManager = LinearLayoutManager(context)
        schoolRecycleView.adapter = adapter
        
        // 添加滚动监听器来实现分页加载
        schoolRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                    firstVisibleItemPosition >= 0 &&
                    viewModel.canLoadMore()) {
                    viewModel.loadMoreUniversities(requireContext())
                }
            }
        })
    }
    
    private fun setupObservers() {
        // 数据变化时会自动更新RecyclerView
        // BaseAdapter已经处理了LiveData的观察
    }
}