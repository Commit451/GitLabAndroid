package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.ProjectsFragment
import com.commit451.gitlab.fragment.UsersFragment

/**
 * The pager that controls the fragments when on the search activity
 */
class SearchPagerAdapter(context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles: Array<String> = context.resources.getStringArray(R.array.search_tabs)

    private val projectsFragment: ProjectsFragment = ProjectsFragment.newInstance(ProjectsFragment.MODE_SEARCH)
    private val usersFragment: UsersFragment = UsersFragment.newInstance()

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return projectsFragment
            1 -> return usersFragment
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    fun searchQuery(query: String) {
        projectsFragment.searchQuery(query)
        usersFragment.searchQuery(query)
    }
}
