package com.zen.alchan.ui.browse.activity


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.zen.alchan.R
import com.zen.alchan.data.response.*
import com.zen.alchan.helper.enums.ResponseStatus
import com.zen.alchan.helper.pojo.ListActivity
import com.zen.alchan.helper.pojo.MessageActivity
import com.zen.alchan.helper.pojo.TextActivity
import com.zen.alchan.helper.utils.AndroidUtility
import com.zen.alchan.helper.utils.DialogUtility
import com.zen.alchan.ui.base.BaseFragment
import com.zen.alchan.ui.social.ActivityListener
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_activity_list.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType

/**
 * A simple [Fragment] subclass.
 */
class ActivityListFragment : BaseFragment() {

    private val viewModel by viewModel<ActivityListViewModel>()

    private lateinit var adapter: ActivityListRvAdapter
    private var isLoading = false

    private var maxWidth = 0
    private lateinit var markwon: Markwon

    private lateinit var itemFilter: MenuItem

    companion object {
        const val USER_ID = "userId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userId = arguments?.getInt(USER_ID)

        maxWidth = AndroidUtility.getScreenWidth(activity)
        markwon = AndroidUtility.initMarkwon(activity!!)

        adapter = assignAdapter()
        activityRecyclerView.adapter = adapter

        toolbarLayout.title = getString(R.string.activity)
        toolbarLayout.setNavigationOnClickListener { activity?.finish() }
        toolbarLayout.navigationIcon = ContextCompat.getDrawable(activity!!, R.drawable.ic_delete)
        toolbarLayout.inflateMenu(R.menu.menu_filter)
        itemFilter = toolbarLayout.menu.findItem(R.id.itemFilter)

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.activityListResponse.observe(viewLifecycleOwner, Observer {
            loadingLayout.visibility = View.GONE
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (isLoading) {
                        viewModel.activityList.removeAt(viewModel.activityList.lastIndex)
                        adapter.notifyItemRemoved(viewModel.activityList.size)
                        isLoading = false
                    }

                    if (!viewModel.hasNextPage) {
                        return@Observer
                    }

                    viewModel.hasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.page += 1
                    viewModel.isInit = true

                    it.data?.page?.activities?.forEach { act ->
                        val activityItem = when (act?.__typename) {
                            viewModel.textActivityText -> {
                                val item = act.fragments.onTextActivity
                                val user = User(id = item?.user?.id!!, name = item.user.name, avatar = UserAvatar(null, item.user.avatar?.medium))
                                TextActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, null, null, item.userId, item.text, user)
                            }
                            viewModel.listActivityText -> {
                                val item = act.fragments.onListActivity!!
                                val media = Media(id = item.media?.id!!, title = MediaTitle(item.media.title?.userPreferred!!), coverImage = MediaCoverImage(null, item.media.coverImage?.medium), type = item.media.type, format = item.media.format, startDate = FuzzyDate(item.media.startDate?.year, item.media.startDate?.month, item.media.startDate?.day))
                                val user = User(id = item.user?.id!!, name = item.user.name, avatar = UserAvatar(null, item.user.avatar?.medium))
                                ListActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, null, null, item.userId, item.status, item.progress, media, user)
                            }
                            viewModel.messageActivityText -> {
                                val item = act.fragments.onMessageActivity!!
                                val recipient = User(id = item.recipient?.id!!, name = item.recipient.name, avatar = UserAvatar(null, item.recipient.avatar?.medium))
                                val messenger = User(id = item.messenger?.id!!, name = item.messenger.name, avatar = UserAvatar(null, item.messenger.avatar?.medium))
                                MessageActivity(item.id, item.type, item.replyCount, item.siteUrl, item.isSubscribed, item.likeCount, item.isLiked, item.createdAt, null, null, item.recipientId, item.messengerId, item.message, item.isPrivate, recipient, messenger)
                            }
                            else -> null
                        }

                        if (activityItem != null) {
                            viewModel.activityList.add(activityItem)
                        }
                    }

                    adapter.notifyDataSetChanged()
                    emptyLayout.visibility = if (viewModel.activityList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    if (isLoading) {
                        viewModel.activityList.removeAt(viewModel.activityList.lastIndex)
                        adapter.notifyItemRemoved(viewModel.activityList.size)
                        isLoading = false
                    }

                    emptyLayout.visibility = if (viewModel.activityList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        if (!viewModel.isInit) {
            viewModel.getActivities()
            loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun initLayout() {
        activityListRefreshLayout.setOnRefreshListener {
            activityListRefreshLayout.isRefreshing = false
            loadingLayout.visibility = View.VISIBLE
            isLoading = false
            viewModel.refresh()
            // refresh
        }

        itemFilter.setOnMenuItemClickListener {
            val activityTypeStringArray = viewModel.activityTypeArray.map { getString(it) }.toTypedArray()
            MaterialAlertDialogBuilder(activity)
                .setItems(activityTypeStringArray) { _, which ->
                    viewModel.selectedActivityType = viewModel.activityTypeList[which]
                    loadingLayout.visibility = View.VISIBLE
                    isLoading = false
                    viewModel.refresh()
                }
                .show()

            true
        }

        activityRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1) && viewModel.isInit && !isLoading) {
                    loadMore()
                    isLoading = true
                }
            }
        })

        newActivityButton.setOnClickListener {
            // open editor
        }
    }

    private fun loadMore() {
        if (viewModel.hasNextPage) {
            viewModel.activityList.add(null)
            adapter.notifyItemInserted(viewModel.activityList.lastIndex)
            viewModel.getActivities()
        }
    }

    private fun assignAdapter(): ActivityListRvAdapter {
        return ActivityListRvAdapter(
            activity!!,
            viewModel.activityList,
            viewModel.currentUserId,
            maxWidth,
            markwon,
            object : ActivityListener {
                override fun openActivityPage(activityId: Int) {

                }

                override fun openUserPage(userId: Int) {

                }

                override fun toggleLike(activityId: Int) {

                }

                override fun toggleSubscribe(activityId: Int, subscribe: Boolean) {

                }

                override fun editActivity(activityId: Int) {

                }

                override fun deleteActivity(activityId: Int) {

                }

                override fun viewOnAniList(siteUrl: String?) {

                }

                override fun copyLink(siteUrl: String?) {

                }

                override fun openMediaPage(mediaId: Int, mediaType: MediaType?) {

                }
            }
        )
    }
}