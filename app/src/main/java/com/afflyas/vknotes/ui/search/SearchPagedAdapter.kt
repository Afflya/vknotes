package com.afflyas.vknotes.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afflyas.vknotes.R
import com.afflyas.vknotes.databinding.ItemNoteBinding
import com.afflyas.vknotes.databinding.ItemSearchFooterBinding
import com.afflyas.vknotes.repository.NetworkState
import com.afflyas.vknotes.ui.common.ItemNoteCallback
import com.afflyas.vknotes.ui.common.RetryCallback
import com.afflyas.vknotes.vo.VKNote

class SearchPagedAdapter(private val retryCallback: RetryCallback,
                         private val itemCallback: ItemNoteCallback) : PagedListAdapter<VKNote, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    private var networkState: NetworkState? = null

    fun setNetworkState(newNetworkState: NetworkState?) {
        this.networkState = newNetworkState
        if(itemCount == 2){
            notifyDataSetChanged()
        }else{
            notifyItemChanged(itemCount - 1)
        }
    }

    override fun onCurrentListChanged(currentList: PagedList<VKNote>?) {
        super.onCurrentListChanged(currentList)
        notifyDataSetChanged()
    }

    /**
     * +1 for header item
     * +1 for loading state footer item
     */
    override fun getItemCount(): Int {
        return super.getItemCount() + 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_note -> {
                val binding: ItemNoteBinding = DataBindingUtil
                        .inflate(LayoutInflater.from(parent.context), R.layout.item_note, parent, false)
                binding.callback = itemCallback
                ItemNoteViewHolder(binding)
            }
            R.layout.item_search_footer -> {
                val binding: ItemSearchFooterBinding = DataBindingUtil
                        .inflate(LayoutInflater.from(parent.context), R.layout.item_search_footer, parent, false)
                binding.callback = retryCallback
                ItemFooterViewHolder(binding)
            }
            R.layout.item_empty_header -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_empty_header, parent, false)
                ItemHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (getItemViewType(position)) {
            R.layout.item_note -> {
                val note = getItem(position - 1) //-1 for header position
                val itemViewHolder = holder as ItemNoteViewHolder
                itemViewHolder.binding.note = note
                itemViewHolder.binding.executePendingBindings()
            }
            R.layout.item_search_footer -> {
                val itemViewHolder = holder as ItemFooterViewHolder
                itemViewHolder.binding.networkState = networkState
                itemViewHolder.binding.listIsEmpty = itemCount == 2
                itemViewHolder.binding.executePendingBindings()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> R.layout.item_empty_header
            itemCount - 1 -> R.layout.item_search_footer
            else -> R.layout.item_note
        }

    }

    companion object {
        /**
         * Item to add top space
         */
        class ItemHeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView)
        /**
         * ViewHolder that contains binding with `item_note.xml` layout
         */
        class ItemNoteViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)
        /**
         * ViewHolder that contains binding with `item_search_footer.xml` layout
         */
        class ItemFooterViewHolder(val binding: ItemSearchFooterBinding) : RecyclerView.ViewHolder(binding.root)


        val POST_COMPARATOR = object : DiffUtil.ItemCallback<VKNote>(){

            override fun areItemsTheSame(oldItem: VKNote, newItem: VKNote): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: VKNote, newItem: VKNote): Boolean {
                return oldItem.id == newItem.id && oldItem.text == oldItem.text && oldItem.date == oldItem.date
            }

        }

    }
}