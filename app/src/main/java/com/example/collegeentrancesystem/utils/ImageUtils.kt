package com.example.collegeentrancesystem.utils


import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.example.collegeentrancesystem.R

object ImageUtils {
    fun loadImage(
        context: android.content.Context,
        url: String,
        imageView: ImageView,
        @DrawableRes placeholderResId: Int = R.drawable.placeholder
    ) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholderResId)
            .into(imageView)
    }

    fun loadImage(
        imageView: ImageView, url: String,
        target: Target<Drawable>, listener: RequestListener<Drawable>? = null,
    ) {
        Glide.with(imageView.context)
            .load(url) // 图片 URL
            .listener(listener)
            .placeholder(R.drawable.placeholder)
            .into(target) // 将图片加载到 ImageView 上
    }

    fun loadImage(imageView: ImageView, url: String, listener: RequestListener<Drawable>? = null) {
        Glide.with(imageView.context)
            .load(url) // 图片 URL
            .listener(listener)
            .placeholder(R.drawable.placeholder)
            .into(imageView) // 将图片加载到 ImageView 上
    }

    fun loadImage(
        imageView: ImageView, @DrawableRes res: Int,
        target: Target<Drawable>,
        listener: RequestListener<Drawable>? = null,
    ) {
        Glide.with(imageView.context)
            .load(res) // 图片 URL
            .listener(listener)
            .placeholder(R.drawable.placeholder)
            .into(target) // 将图片加载到 ImageView 上
    }

    fun loadImage(
        imageView: ImageView,
        @DrawableRes res: Int,
        listener: RequestListener<Drawable>? = null,
    ) {
        Glide.with(imageView.context)
            .load(res) // 图片 URL
            .listener(listener)
            .placeholder(R.drawable.placeholder)
            .into(imageView) // 将图片加载到 ImageView 上
    }


    fun adjustAspectRatio(width: Int, height: Int): Float {
        // 原始宽高比
        val aspectRatio = width.toFloat() / height.toFloat()

        // 定义宽高比的上下限
        val minRatio = 9f / 14f // 9:14
        val maxRatio = 12f / 9f // 12:9

        return when {
            // 如果宽高比小于最小值，则调整为 9:14
            aspectRatio < minRatio -> 9f / 14

            // 如果宽高比大于最大值，则调整为 12:9
            aspectRatio > maxRatio -> 12f / 9

            // 如果宽高比在范围内，则保持原有比例
            else -> {
                // 简化宽高比为整数形式
                val gcd = gcd(width, height) // 计算最大公约数
                (width.toFloat() / gcd) / (height.toFloat() / gcd)
            }
        }
    }

    // 辅助函数：计算最大公约数（GCD）
    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }
}

class SimpleViewTarget(private val imageView: ImageView) :
    CustomViewTarget<ImageView, Drawable>(imageView) {
    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadCleared(errorDrawable)
        imageView.setImageDrawable(errorDrawable)
    }

    override fun onResourceCleared(placeholder: Drawable?) {
        super.onResourceLoading(placeholder)
        imageView.setImageDrawable(placeholder)
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        imageView.setImageDrawable(resource)
    }

    override fun onResourceLoading(placeholder: Drawable?) {
        super.onResourceLoading(placeholder)
        imageView.setImageDrawable(placeholder)
    }
}