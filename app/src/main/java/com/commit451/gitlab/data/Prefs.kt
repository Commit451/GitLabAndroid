package com.commit451.gitlab.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.IntDef
import com.commit451.gitlab.api.MoshiProvider
import com.commit451.gitlab.model.Account
import java.io.IOException
import com.squareup.moshi.Types
import timber.log.Timber


/**
 * Shared prefs things
 */
object Prefs {

    private const val KEY_ACCOUNTS = "accounts"
    private const val KEY_STARTING_VIEW = "starting_view"
    private const val KEY_REQUIRE_DEVICE_AUTH = "require_device_auth"

    const val STARTING_VIEW_PROJECTS = 0
    const val STARTING_VIEW_GROUPS = 1
    const val STARTING_VIEW_ACTIVITY = 2
    const val STARTING_VIEW_TODOS = 3

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STARTING_VIEW_PROJECTS, STARTING_VIEW_GROUPS, STARTING_VIEW_ACTIVITY, STARTING_VIEW_TODOS)
    annotation class StartingView

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (context !is Application) {
            throw IllegalArgumentException("This should be the application context. Not the activity context")
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAccounts(): MutableList<Account> {
        val accountsJson = prefs.getString(KEY_ACCOUNTS, null)
        if (!accountsJson.isNullOrEmpty()) {
            try {
                val type = Types.newParameterizedType(List::class.java, Account::class.java)
                val adapter = MoshiProvider.moshi.adapter<List<Account>>(type)
                val accounts = adapter.fromJson(accountsJson)!!.toMutableList()
                accounts.sort()
                accounts.reverse()
                return accounts
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return mutableListOf()
    }

    private fun setAccounts(accounts: List<Account>) {
        try {
            val type = Types.newParameterizedType(List::class.java, Account::class.java)
            val adapter = MoshiProvider.moshi.adapter<List<Account>>(type)
            val json = adapter.toJson(accounts)
            prefs.edit()
                    .putString(KEY_ACCOUNTS, json)
                    .apply()
        } catch (e: IOException) {
            prefs.edit()
                    .remove(KEY_ACCOUNTS)
                    .apply()
        }
    }

    fun addAccount(account: Account) {
        val accounts = getAccounts()
        accounts.add(account)
        setAccounts(accounts)
    }

    fun removeAccount(account: Account) {
        val accounts = getAccounts()
        accounts.remove(account)
        setAccounts(accounts)
    }

    fun updateAccount(account: Account) {
        val accounts = getAccounts()
        accounts.remove(account)
        accounts.add(account)
        setAccounts(accounts)
    }

    @StartingView
    var startingView: Int
        get() = prefs.getInt(KEY_STARTING_VIEW, STARTING_VIEW_PROJECTS)
        set(value) = prefs.edit()
                .putInt(KEY_STARTING_VIEW, value)
                .apply()

    var isRequiredDeviceAuth: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_DEVICE_AUTH, false)
        set(value) = prefs.edit()
                .putBoolean(KEY_REQUIRE_DEVICE_AUTH, value)
                .apply()
}
