package com.example.collegeentrancesystem.module.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.collegeentrancesystem.R
import com.example.collegeentrancesystem.bean.College
import com.example.collegeentrancesystem.bean.ScoreData
import com.example.collegeentrancesystem.utils.ImageUtils
import com.example.collegeentrancesystem.utils.SimpleViewTarget
import com.google.android.flexbox.FlexboxLayout

class CollegeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val collegeName: TextView = itemView.findViewById(R.id.college_name)
    private val collegeLocation: TextView = itemView.findViewById(R.id.college_location)
    private val collegeType: TextView = itemView.findViewById(R.id.college_type)
    private val collegeLevel: TextView = itemView.findViewById(R.id.college_level)
    private val collegeNature: TextView = itemView.findViewById(R.id.college_nature)
    private val tagsContainer: FlexboxLayout = itemView.findViewById(R.id.tags_container)
    private val collegeImage: ImageView = itemView.findViewById(R.id.college_image)
    val parent = itemView

    fun bind(college: College) {

        collegeName.text = college.name
        collegeLocation.text = college.location
        collegeType.text = college.type
        collegeLevel.text = college.level
        collegeNature.text = college.nature

        // 直接设置本地图片资源
        val imageResId = college.photo// 假设 College 类中有一个 imageResId 属性，存储图片资源的 ID
        if (imageResId != 0) {
            collegeImage.setImageResource(imageResId)
        } else {
            // 如果没有图片资源 ID，设置默认图片
            collegeImage.setImageResource(R.drawable.placeholder)
        }

        //清空之前的标签
        tagsContainer.removeAllViews()
        
        //添加标签
        college.tags.forEachIndexed { index, tag ->
            val tagView = TextView(itemView.context, null, 0, R.style.TagStyle).apply {
                text = tag
                setTextColor(itemView.context.getColor(R.color.black))
            }
            
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (index > 0) {
                    marginStart = 10
                }
            }
            
            tagsContainer.addView(tagView, layoutParams)
        }
    }
}

class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    
    private val scoreText: TextView = itemView.findViewById(R.id.score)
    private val peopleText: TextView = itemView.findViewById(R.id.num)
    private val rankText: TextView = itemView.findViewById(R.id.rank)

    fun bind(scoreData: ScoreData) {
        scoreText.text = scoreData.score
        peopleText.text = scoreData.people
        rankText.text = scoreData.rank
    }
}