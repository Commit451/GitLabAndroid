package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.easycallback.EasyCallback;
import com.commit451.easycallback.HttpException;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.MergeRequestSectionsPagerAdapter;
import com.commit451.gitlab.event.MergeRequestChangedEvent;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Shows the details of a merge request
 */
public class MergeRequestActivity extends BaseActivity {

    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_MERGE_REQUEST = "key_merge_request";

    public static Intent newIntent(Context context, Project project, MergeRequest mergeRequest) {
        Intent intent = new Intent(context, MergeRequestActivity.class);
        intent.putExtra(KEY_PROJECT, Parcels.wrap(project));
        intent.putExtra(KEY_MERGE_REQUEST, Parcels.wrap(mergeRequest));
        return intent;
    }

    @BindView(R.id.root)
    ViewGroup mRoot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindView(R.id.progress)
    View mProgress;

    Project mProject;
    MergeRequest mMergeRequest;

    private final EasyCallback<MergeRequest> mMergeRequestCallback = new EasyCallback<MergeRequest>() {
        @Override
        public void success(@NonNull MergeRequest response) {
            mProgress.setVisibility(View.GONE);
            Snackbar.make(mRoot, R.string.merge_request_accepted, Snackbar.LENGTH_LONG)
                    .show();
            App.bus().post(new MergeRequestChangedEvent(response));
        }

        @Override
        public void failure(Throwable t) {
            Timber.e(t, null);
            mProgress.setVisibility(View.GONE);
            String message = getString(R.string.unable_to_merge);
            if (t instanceof HttpException) {
                int code = ((HttpException) t).response().code();
                if (code == 406) {
                    message = getString(R.string.merge_request_already_merged_or_closed);
                }
            }
            Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    private final Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_merge:
                    mProgress.setVisibility(View.VISIBLE);
                    App.instance().getGitLab().acceptMergeRequest(mProject.getId(), mMergeRequest.getId()).enqueue(mMergeRequestCallback);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_request);
        ButterKnife.bind(this);

        mProject = Parcels.unwrap(getIntent().getParcelableExtra(KEY_PROJECT));
        mMergeRequest = Parcels.unwrap(getIntent().getParcelableExtra(KEY_MERGE_REQUEST));

        mToolbar.setTitle(getString(R.string.merge_request_number) + mMergeRequest.getIid());
        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setSubtitle(mProject.getNameWithNamespace());
        mToolbar.inflateMenu(R.menu.menu_merge_request);
        mToolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        setupTabs();
    }

    private void setupTabs() {
        MergeRequestSectionsPagerAdapter sectionsPagerAdapter = new MergeRequestSectionsPagerAdapter(
                this,
                getSupportFragmentManager(),
                mProject,
                mMergeRequest);

        mViewPager.setAdapter(sectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
