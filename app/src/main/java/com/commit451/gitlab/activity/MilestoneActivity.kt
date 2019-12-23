package com.commit451.gitlab.activity


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.adapter.DividerItemDecoration
import com.commit451.gitlab.adapter.MilestoneIssueAdapter
import com.commit451.gitlab.event.MilestoneChangedEvent
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Milestone
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.util.LinkHeaderParser
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

class MilestoneActivity : BaseActivity() {

    companion object {

        private const val EXTRA_PROJECT = "extra_project"
        private const val EXTRA_MILESTONE = "extra_milestone"

        fun newIntent(context: Context, project: Project, milestone: Milestone): Intent {
            val intent = Intent(context, MilestoneActivity::class.java)
            intent.putExtra(EXTRA_PROJECT, project)
            intent.putExtra(EXTRA_MILESTONE, milestone)
            return intent
        }
    }

    @BindView(R.id.root)
    lateinit var root: View
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.swipe_layout)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.list)
    lateinit var listIssues: RecyclerView
    @BindView(R.id.message_text)
    lateinit var textMessage: TextView
    @BindView(R.id.progress)
    lateinit var progress: View

    lateinit var adapterMilestoneIssues: MilestoneIssueAdapter
    lateinit var layoutManagerIssues: LinearLayoutManager
    lateinit var menuItemOpenClose: MenuItem

    lateinit var project: Project
    lateinit var milestone: Milestone
    var nextPageUrl: Uri? = null
    var loading = false

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManagerIssues.childCount
            val totalItemCount = layoutManagerIssues.itemCount
            val firstVisibleItem = layoutManagerIssues.findFirstVisibleItemPosition()
            if (firstVisibleItem + visibleItemCount >= totalItemCount && !loading && nextPageUrl != null) {
                loadMore()
            }
        }
    }

    @OnClick(R.id.add)
    fun onAddClick(fab: View) {
        Navigator.navigateToAddIssue(this@MilestoneActivity, fab, project)
    }

    @OnClick(R.id.edit)
    fun onEditClicked(fab: View) {
        Navigator.navigateToEditMilestone(this@MilestoneActivity, project, milestone)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milestone)
        ButterKnife.bind(this)
        App.bus().register(this)

        project = intent.getParcelableExtra(EXTRA_PROJECT)!!
        milestone = intent.getParcelableExtra(EXTRA_MILESTONE)!!

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.inflateMenu(R.menu.close)
        menuItemOpenClose = toolbar.menu.findItem(R.id.action_close)
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_close -> {
                    closeOrOpenIssue()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })

        adapterMilestoneIssues = MilestoneIssueAdapter(object : MilestoneIssueAdapter.Listener {
            override fun onIssueClicked(issue: Issue) {
                Navigator.navigateToIssue(this@MilestoneActivity, project, issue)
            }
        })
        bind(milestone)
        listIssues.adapter = adapterMilestoneIssues
        layoutManagerIssues = LinearLayoutManager(this)
        listIssues.layoutManager = layoutManagerIssues
        listIssues.addItemDecoration(DividerItemDecoration(this))
        listIssues.addOnScrollListener(onScrollListener)
        swipeRefreshLayout.setOnRefreshListener { loadData() }

        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.bus().unregister(this)
    }

    fun bind(milestone: Milestone) {
        toolbar.title = milestone.title
        adapterMilestoneIssues.setMilestone(milestone)
        setOpenCloseMenuStatus()
    }

    fun loadData() {
        textMessage.visibility = View.GONE
        loading = true
        swipeRefreshLayout.isRefreshing = true
        App.get().gitLab.getMilestoneIssues(project.id, milestone.id)
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Issue>>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        loading = false
                        swipeRefreshLayout.isRefreshing = false
                        textMessage.visibility = View.VISIBLE
                        textMessage.setText(R.string.connection_error_issues)
                        adapterMilestoneIssues.setIssues(null)
                    }

                    override fun responseNonNullSuccess(issues: List<Issue>) {
                        swipeRefreshLayout.isRefreshing = false
                        loading = false

                        if (!issues.isEmpty()) {
                            textMessage.visibility = View.GONE
                        } else {
                            Timber.d("No issues found")
                            textMessage.visibility = View.VISIBLE
                            textMessage.setText(R.string.no_issues)
                        }

                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterMilestoneIssues.setIssues(issues)
                    }
                })
    }

    fun loadMore() {

        if (nextPageUrl == null) {
            return
        }

        loading = true

        Timber.d("loadMore called for %s", nextPageUrl)
        App.get().gitLab.getMilestoneIssues(nextPageUrl!!.toString())
                .with(this)
                .subscribe(object : CustomResponseSingleObserver<List<Issue>>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        loading = false
                    }

                    override fun responseNonNullSuccess(issues: List<Issue>) {
                        loading = false
                        nextPageUrl = LinkHeaderParser.parse(response()).next
                        adapterMilestoneIssues.addIssues(issues)
                    }
                })
    }

    private fun closeOrOpenIssue() {
        progress.visibility = View.VISIBLE
        if (milestone.state == Milestone.STATE_ACTIVE) {
            updateMilestoneStatus(App.get().gitLab.updateMilestoneStatus(project.id, milestone.id, Milestone.STATE_EVENT_CLOSE))
        } else {
            updateMilestoneStatus(App.get().gitLab.updateMilestoneStatus(project.id, milestone.id, Milestone.STATE_EVENT_ACTIVATE))
        }
    }

    private fun updateMilestoneStatus(observable: Single<Milestone>) {
        observable.with(this)
                .subscribe({
                    progress.visibility = View.GONE
                    milestone = it
                    App.bus().post(MilestoneChangedEvent(milestone))
                    setOpenCloseMenuStatus()
                }, {
                    Timber.e(it)
                    progress.visibility = View.GONE
                    Snackbar.make(root, getString(R.string.failed_to_create_milestone), Snackbar.LENGTH_SHORT)
                            .show()
                })
    }

    private fun setOpenCloseMenuStatus() {
        menuItemOpenClose.setTitle(if (milestone.state == Milestone.STATE_CLOSED) R.string.reopen else R.string.close)
    }

    @Subscribe
    fun onEvent(event: MilestoneChangedEvent) {
        if (milestone.id == event.milestone.id) {
            milestone = event.milestone
            bind(milestone)
        }
    }
}
