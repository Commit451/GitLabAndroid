package com.commit451.gitlab.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.viewHolder.IssueViewHolder
import com.commit451.gitlab.viewHolder.LoadingFooterViewHolder
import java.util.*

/**
 * Issues adapter
 */
class IssueAdapter(private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val FOOTER_COUNT = 1

        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }

    val values: ArrayList<Issue> = ArrayList()
    private var loading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                val holder = IssueViewHolder.inflate(parent)
                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    listener.onIssueClicked(getValueAt(position))
                }
                return holder
            }
            TYPE_FOOTER -> return LoadingFooterViewHolder.inflate(parent)
        }
        throw IllegalStateException("No holder for view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IssueViewHolder -> {
                val issue = getValueAt(position)
                holder.bind(issue)
                holder.itemView.setTag(R.id.list_position, position)
            }
            is LoadingFooterViewHolder -> {
                holder.bind(loading)
            }
            else -> {
                throw IllegalStateException("What is this holder?")
            }
        }
    }

    override fun getItemCount(): Int {
        return values.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == values.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }

    fun setIssues(issues: Collection<Issue>?) {
        values.clear()
        addIssues(issues)
    }

    fun addIssues(issues: Collection<Issue>?) {
        if (issues != null) {
            values.addAll(issues)
        }
        notifyDataSetChanged()
    }

    fun addIssue(issue: Issue) {
        values.add(0, issue)
        notifyItemInserted(0)
    }

    fun updateIssue(issue: Issue) {
        var indexToDelete = -1
        for (i in values.indices) {
            if (values[i].id == issue.id) {
                indexToDelete = i
                break
            }
        }
        if (indexToDelete != -1) {
            values.removeAt(indexToDelete)
            values.add(indexToDelete, issue)
        }
        notifyItemChanged(indexToDelete)
    }

    fun getValueAt(position: Int): Issue {
        return values[position]
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyItemChanged(values.size)
    }

    interface Listener {
        fun onIssueClicked(issue: Issue)
    }
}
