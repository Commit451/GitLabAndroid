package com.commit451.gitlab.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.commit451.gitlab.R
import com.commit451.gitlab.fragment.MergeRequestCommitsFragment
import com.commit451.gitlab.fragment.MergeRequestDetailsFragment
import com.commit451.gitlab.fragment.MergeRequestDiscussionFragment
import com.commit451.gitlab.model.api.MergeRequest
import com.commit451.gitlab.model.api.Project

/**
 * Merge request pager adapter
 */
class MergeRequestSectionsPagerAdapter(context: Context, fm: androidx.fragment.app.FragmentManager, private val project: Project, private val mergeRequest: MergeRequest) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    private val titles: Array<String> = context.resources.getStringArray(R.array.merge_request_tabs)

    override fun getItem(position: Int): androidx.fragment.app.Fragment {

        when (position) {
            0 -> return MergeRequestDetailsFragment.newInstance(project, mergeRequest)
            1 -> return MergeRequestDiscussionFragment.newInstance(project, mergeRequest)
            2 -> return MergeRequestCommitsFragment.newInstance(project, mergeRequest)
        }

        throw IllegalStateException("Position exceeded on view pager")
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}
