package com.example.collegeentrancesystem.base.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlin.reflect.KClass

class BaseAdapter : RecyclerView.Adapter<ViewHolder>() {

    /**
     * 构建域，用来构建Adapter的信息
     */
    inner class BuildScope {

        /**
         * 保存view的数据
         */
        val viewDataList = mutableListOf<IViewData>()

        /**
         * 设置不变的item
         */
        inline fun <reified VH : ViewHolder> setItem(
            @LayoutRes layoutId: Int,
            noinline action: (VH) -> Unit
        ) {
            @Suppress("UNCHECKED_CAST")
            viewDataList.add(ItemViewData(VH::class, layoutId, null, action as (Any) -> Unit))
        }

        /**
         * 设置列表items
         */
        @SuppressLint("NotifyDataSetChanged")
        inline fun <reified VH : ViewHolder, reified T> setItems(
            lifecycleOwner: LifecycleOwner,
            @LayoutRes layoutId: Int,
            list: LiveData<List<T>>,
            noinline action: (VH, T) -> Unit
        ) {
            @Suppress("UNCHECKED_CAST")
            viewDataList.add(
                ItemsViewData(
                    VH::class,
                    layoutId,
                    list.value ?: emptyList<T>(),
                    action as (Any, Any) -> Unit
                )
            )
            val index = viewDataList.size - 1
            list.observe(lifecycleOwner) {
                viewDataList[index] = (viewDataList[index] as ItemsViewData).copy(list = it)
                notifyDataSetChanged()
            }
        }

        /**
         * 设置可变的item
         */
        @SuppressLint("NotifyDataSetChanged")
        inline fun <reified VH : ViewHolder, reified T : Any> setMutableItem(
            lifecycleOwner: LifecycleOwner,
            @LayoutRes layoutId: Int,
            data: LiveData<T>,
            noinline action: (VH, T) -> Unit
        ) {
            @Suppress("UNCHECKED_CAST")
            viewDataList.add(
                MutableItemData(
                    VH::class,
                    layoutId,
                    data.value ?: Unit,
                    action as (Any, Any) -> Unit
                )
            )
            val index = viewDataList.size - 1
            data.observe(lifecycleOwner) {
                viewDataList[index] = (viewDataList[index] as MutableItemData).copy(data = it)
                notifyDataSetChanged()
            }
        }

        /**
         * 根据position获取当前是处于哪个type
         */
        fun getType(position: Int): Int {
            var index = 0
            viewDataList.forEachIndexed { count, it ->
                when (it) {
                    is IItemsViewData -> {
                        val list = it.list
                        if (index + list.size > position) {
                            return count
                        }
                        index += list.size
                    }

                    is IItemViewData -> {
                        if (index == position) {
                            return count
                        }
                        index++
                    }
                }
            }
            throw IllegalArgumentException("position: $this is out of range")
        }

        /**
         * 根据position获取当前处于type下的第几个index
         */
        fun getRangeIndex(position: Int): Int {
            var index = 0
            viewDataList.forEach {
                when (it) {
                    is IItemsViewData -> {
                        val list = it.list
                        if (index + list.size > position) {
                            return position - index
                        }
                        index += list.size
                    }

                    is IItemViewData -> {
                        if (index == position) {
                            return 0
                        }
                        index++
                    }
                }
            }
            throw IllegalArgumentException("position: $this is out of range")
        }

    }

    private val buildScope = BuildScope()
    private val viewDataList get() = buildScope.viewDataList

    fun build(block: BuildScope.() -> Unit): BaseAdapter {
        block.invoke(buildScope)
        return this
    }

    /**
     * 抽象的ViewData接口，保存viewHolder的类信息和layoutID
     */
    interface IViewData {
        val viewHolder: KClass<out ViewHolder>

        @get:LayoutRes
        val layoutId: Int
    }

    /**
     * 单一item的ViewData接口
     */
    interface IItemViewData : IViewData {
        val data: Any?
    }

    /**
     * 列表items的ViewData接口
     */
    interface IItemsViewData : IViewData {
        val list: List<*>
    }


    /**
     * 不可变item的ViewData实现类
     */
    data class ItemViewData(
        override val viewHolder: KClass<out ViewHolder>,
        override val layoutId: Int,
        override val data: Any?,
        val itemAction: (Any) -> Unit,
    ) : IItemViewData

    /**
     * 可变item的ViewData实现类
     */
    data class MutableItemData(
        override val viewHolder: KClass<out ViewHolder>,
        override val layoutId: Int,
        override val data: Any,
        val itemAction: (Any, Any) -> Unit
    ) : IItemViewData

    /**
     * 列表items的ViewData实现类
     */
    data class ItemsViewData(
        override val viewHolder: KClass<out ViewHolder>,
        override val layoutId: Int,
        override val list: List<*>,
        val itemAction: (Any, Any) -> Unit
    ) : IItemsViewData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(viewDataList[viewType].layoutId, parent, false)
        return viewDataList[viewType].viewHolder.constructors.first().call(view)
    }

    override fun getItemCount(): Int {
        return viewDataList.sumOf {
            when (it) {
                is IItemsViewData -> it.list.size
                is IItemViewData -> 1
                else -> throw IllegalArgumentException("type: $it is not supported")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return buildScope.getType(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val viewData = viewDataList[buildScope.getType(position)]) {
            is ItemViewData -> viewData.itemAction(holder)
            is MutableItemData -> viewData.itemAction(holder, viewData.data)
            is ItemsViewData -> {
                val list = viewData.list
                list[buildScope.getRangeIndex(position)]?.let {
                    viewData.itemAction(holder, it)
                }
            }
        }
    }
}