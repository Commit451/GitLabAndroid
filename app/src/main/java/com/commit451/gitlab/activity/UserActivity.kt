package com.commit451.gitlab.activity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.graphics.Palette
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.addendum.parceler.getParcelerParcelableExtra
import com.commit451.addendum.parceler.putParcelerParcelableExtra
import com.commit451.addendum.themeAttrColor
import com.commit451.alakazam.Alakazam
import com.commit451.easel.Easel
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.extension.feedUrl
import com.commit451.gitlab.fragment.FeedFragment
import com.commit451.gitlab.model.api.User
import com.commit451.gitlab.transformation.PaletteTransformation
import com.commit451.gitlab.util.ImageUtil

/**
 * User activity, which shows the user!
 */
class UserActivity : BaseActivity() {

    companion object {

        private val KEY_USER = "user"

        fun newIntent(context: Context, user: User): Intent {
            val intent = Intent(context, UserActivity::class.java)
            intent.putParcelerParcelableExtra(KEY_USER, user)
            return intent
        }
    }

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.collapsing_toolbar) lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    @BindView(R.id.backdrop) lateinit var backdrop: ImageView

    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        ButterKnife.bind(this)
        user = intent.getParcelerParcelableExtra<User>(KEY_USER)!!

        // Default content and scrim colors
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE)

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = user.username
        val url = ImageUtil.getAvatarUrl(user, resources.getDimensionPixelSize(R.dimen.user_header_image_size))

        App.get().picasso
                .load(url)
                .transform(PaletteTransformation.instance())
                .into(backdrop, object : PaletteTransformation.PaletteCallback(backdrop) {
                    override fun onSuccess(palette: Palette?) {
                        bindPalette(palette!!)
                    }

                    override fun onError() {}
                })

        if (savedInstanceState == null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction
                    .add(R.id.user_feed, FeedFragment.newInstance(user.feedUrl))
                    .commit()
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    fun bindPalette(palette: Palette) {
        val animationTime = 1000
        val vibrantColor = palette.getVibrantColor(this.themeAttrColor(R.attr.colorPrimary))
        val darkerColor = this.themeAttrColor(vibrantColor)

        if (Build.VERSION.SDK_INT >= 21) {
            Alakazam.navigationBarColorAnimator(window, darkerColor)
                    .setDuration(animationTime.toLong())
                    .start()
            window.statusBarColor = darkerColor
        }

        ObjectAnimator.ofObject(collapsingToolbarLayout, "contentScrimColor", ArgbEvaluator(),
                this.themeAttrColor(R.attr.colorPrimary), vibrantColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(collapsingToolbarLayout, "statusBarScrimColor", ArgbEvaluator(),
                this.themeAttrColor(R.attr.colorPrimaryDark), darkerColor)
                .setDuration(animationTime.toLong())
                .start()

        ObjectAnimator.ofObject(toolbar, "titleTextColor", ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime.toLong())
                .start()
    }
}
