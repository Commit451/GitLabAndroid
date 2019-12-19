package com.commit451.gitlab.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.PopupMenu
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.viewHolder.AccountFooterViewHolder
import com.commit451.gitlab.viewHolder.AccountViewHolder

/**
 * Adapter to show all the accounts
 */
class AccountAdapter(context: Context, private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ACCOUNT = 0
        private const val TYPE_FOOTER = 1

        private const val FOOTER_COUNT = 1
    }

    private val accounts = mutableListOf<Account>()
    private val colorControlHighlight: Int = context.themeAttrColor(R.attr.colorControlHighlight)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ACCOUNT -> {
                val holder = AccountViewHolder.inflate(parent)
                holder.itemView.setOnClickListener {
                    val position = holder.adapterPosition
                    listener.onAccountClicked(getItemAtPosition(position))
                }
                return holder
            }
            TYPE_FOOTER -> {
                val footerViewHolder = AccountFooterViewHolder.inflate(parent)
                footerViewHolder.itemView.setOnClickListener { listener.onAddAccountClicked() }
                return footerViewHolder
            }
        }
        throw IllegalStateException("No known view holder for that type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AccountViewHolder -> {
                val account = getItemAtPosition(position)
                holder.bind(account, account == App.get().getAccount(), colorControlHighlight)
                holder.itemView.setTag(R.id.list_position, position)
                holder.popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_sign_out -> {
                            val itemPosition = accounts.indexOf(account)
                            accounts.remove(account)
                            notifyItemRemoved(itemPosition)
                            listener.onAccountLogoutClicked(account)
                            return@OnMenuItemClickListener true
                        }
                    }
                    false
                })
            }
            is AccountFooterViewHolder -> {
                //Nah
            }
            else -> {
                throw IllegalStateException("No known bind for this viewHolder")
            }
        }
    }

    override fun getItemCount(): Int {
        return accounts.size + FOOTER_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == accounts.size) TYPE_FOOTER else TYPE_ACCOUNT
    }

    fun setAccounts(accounts: Collection<Account>?) {
        this.accounts.clear()
        if (accounts != null) {
            this.accounts.addAll(accounts)
        }
        notifyDataSetChanged()
    }

    fun addAccount(account: Account) {
        if (!accounts.contains(account)) {
            accounts.add(0, account)
            notifyItemInserted(0)
        }
    }

    private fun getItemAtPosition(position: Int): Account {
        return accounts[position]
    }

    interface Listener {
        fun onAccountClicked(account: Account)
        fun onAddAccountClicked()
        fun onAccountLogoutClicked(account: Account)
    }
}
