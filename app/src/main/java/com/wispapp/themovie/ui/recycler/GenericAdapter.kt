package com.wispapp.themovie.ui.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class GenericAdapter<T>(private val listener: OnItemClickListener<T>? = null) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var itemList = mutableListOf<T>()
    private var diffUtil: GenericItemDiff<T>? = null

    protected abstract fun getLayoutId(position: Int, obj: T): Int

    /**
     * Override method [getViewHolder] if you want using more than one View Holder in adapter
     */
    protected open fun getViewHolder(view: View, viewType: Int): BaseViewHolder<*> =
        ViewHolderFactory.create(view, viewType)

    fun update(items: List<T>) =
        if (diffUtil != null) updateWithDiffUtils(items)
        else notifyUpdate(items)

    fun setDiffUtil(diffUtilImpl: GenericItemDiff<T>) {
        diffUtil = diffUtilImpl
    }

    fun getItem(position: Int) = itemList[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> =
        getViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
            , viewType
        )


    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) =
        @Suppress("UNCHECKED_CAST")
        (holder as Binder<T>).bind(itemList[position], listener)

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int =
        getLayoutId(position, itemList[position])

    private fun updateWithDiffUtils(items: List<T>) {
        diffUtil?.let {
            val result = DiffUtil.calculateDiff(GenericDiffUtil(itemList, items, it))

            itemList.clear()
            itemList.addAll(items)
            result.dispatchUpdatesTo(this)
        }
    }

    private fun notifyUpdate(items: List<T>) {
        itemList.clear()
        itemList.addAll(items)
        notifyDataSetChanged()
    }

    internal interface Binder<T> {

        fun bind(data: T, listener: OnItemClickListener<T>?)
    }

    interface OnItemClickListener<T> {

        fun onClickItem(data: T)
    }

    interface GenericItemDiff<T> {

        fun isSame(
            oldItems: List<T>,
            newItems: List<T>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean

        fun isSameContent(
            oldItems: List<T>,
            newItems: List<T>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean
    }
}
