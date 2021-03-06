package com.zen.alchan.ui.common.filter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zen.alchan.R
import com.zen.alchan.helper.Constant
import com.zen.alchan.helper.pojo.MediaFilteredData
import com.zen.alchan.helper.replaceUnderscore
import com.zen.alchan.helper.utils.Utility
import kotlinx.android.synthetic.main.bottomsheet_filter_media.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaSort
import type.MediaType

class MediaFilterBottomSheet : BottomSheetDialogFragment() {

    interface MediaFilterListener {
        fun passFilterData(filterData: MediaFilteredData?)
    }

    private val viewModel by viewModel<MediaFilterViewModel>()
    private lateinit var listener: MediaFilterListener

    private lateinit var dialogView: View

    companion object {
        const val BUNDLE_MEDIA_TYPE = "mediaType"
        const val BUNDLE_IS_FILTER_SEARCH = "isFilterSearch"
        const val BUNDLE_FILTERED_DATA = "filteredData"

        private const val LIST_GENRE = 1
        private const val LIST_TAG = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.bottomsheet_filter_media, container, false)

        if (!this::listener.isInitialized) {
            dismiss()
        }

        viewModel.mediaType = MediaType.valueOf(arguments?.getString(BUNDLE_MEDIA_TYPE)!!)
        viewModel.isHandleSearch = arguments?.getBoolean(BUNDLE_IS_FILTER_SEARCH, false)!!
        viewModel.filteredData = viewModel.gson.fromJson(arguments?.getString(BUNDLE_FILTERED_DATA), MediaFilteredData::class.java)

        initLayout()

        return dialogView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return dialog
    }

    private fun initLayout() {
        // sort will never be null after being initialised for the first time
        // that's why I'm checking current data through selectedSort
        if (viewModel.currentData.selectedListSort == null) {
            viewModel.currentData.selectedListSort = viewModel.defaultSort
            viewModel.currentData.selectedSort = MediaSort.POPULARITY_DESC
            if (viewModel.filteredData != null) {
                viewModel.currentData = viewModel.filteredData!!
            }
        }

        if (viewModel.isHandleSearch) {
            val sortListIndex = viewModel.mediaSortList.indexOf(viewModel.currentData.selectedSort)
            dialogView.sortText.text = if (sortListIndex != -1) viewModel.mediaSortArray[sortListIndex] else "-"
        } else {
            dialogView.sortText.text = viewModel.currentData.selectedListSort?.name?.replaceUnderscore() ?: "-"
        }
        dialogView.filterFormatText.text = viewModel.currentData.selectedFormat?.name?.replaceUnderscore() ?: "-"
        dialogView.filterSeasonText.text = viewModel.currentData.selectedSeason?.name?.replaceUnderscore() ?: "-"
        dialogView.filterCountryText.text = viewModel.currentData.selectedCountry?.value?.replaceUnderscore() ?: "-"
        dialogView.filterStatusText.text = viewModel.currentData.selectedStatus?.name?.replaceUnderscore() ?: "-"
        dialogView.filterSourceText.text = viewModel.currentData.selectedSource?.name?.replaceUnderscore() ?: "-"
        dialogView.filterYearText.text = viewModel.currentData.selectedYear?.toString() ?: "-"
        dialogView.filterYearSeekBar.progress = if (viewModel.currentData.selectedYear == null) 0 else viewModel.currentData.selectedYear!! - Constant.FILTER_EARLIEST_YEAR
        dialogView.filterGenreRecyclerView.adapter = assignAdapter(viewModel.currentData.selectedGenreList, LIST_GENRE)
        dialogView.filterTagRecyclerView.adapter = assignAdapter(viewModel.currentData.selectedTagList, LIST_TAG)
        dialogView.filterHideMediaCheckBox.isChecked = viewModel.currentData.selectedOnList == false
        dialogView.filterOnlyShowMediaCheckBox.isChecked = viewModel.currentData.selectedOnList == true

        if (viewModel.filteredData != null) {
            dialogView.filterResetButton.visibility = View.VISIBLE
        } else {
            dialogView.filterResetButton.visibility = View.GONE
        }

        if (viewModel.isHandleSearch) {
            // TODO: uncomment later when filter by tag is supported
//            dialogView.filterTagLayout.visibility = View.VISIBLE
            dialogView.filterExtraOptionsLayout.visibility = View.VISIBLE
        } else {
            dialogView.filterTagLayout.visibility = View.GONE
            dialogView.filterExtraOptionsLayout.visibility = View.GONE
        }

        if (viewModel.mediaType == MediaType.ANIME) {
            dialogView.filterSeasonLayout.visibility = View.VISIBLE
            dialogView.filterOnlyShowMediaText.text = getString(R.string.only_show_anime_on_my_list)
            dialogView.filterHideMediaText.text = getString(R.string.hide_anime_on_my_list)
        } else {
            dialogView.filterSeasonLayout.visibility = View.GONE
            dialogView.filterOnlyShowMediaText.text = getString(R.string.only_show_manga_on_my_list)
            dialogView.filterHideMediaText.text = getString(R.string.hide_manga_on_my_list)
        }

        dialogView.sortLayout.setOnClickListener {
            if (viewModel.isHandleSearch) {
                MaterialAlertDialogBuilder(activity)
                    .setItems(viewModel.mediaSortArray) { _, which ->
                        viewModel.currentData.selectedSort = viewModel.mediaSortList[which]
                        dialogView.sortText.text = viewModel.mediaSortArray[which].replaceUnderscore()
                    }
                    .show()
            } else {
                MaterialAlertDialogBuilder(activity)
                    .setItems(viewModel.getMediaListSortStringArray()) { _, which ->
                        viewModel.currentData.selectedListSort = viewModel.mediaListSortList[which]
                        dialogView.sortText.text = viewModel.mediaListSortList[which].name.replaceUnderscore()
                    }
                    .show()
            }
        }

        dialogView.filterFormatLayout.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getMediaFormatStringArray()) { _, which ->
                    viewModel.currentData.selectedFormat = viewModel.mediaFormatList[which]
                    dialogView.filterFormatText.text = viewModel.mediaFormatList[which]?.name?.replaceUnderscore() ?: "-"
                }
                .show()
        }

        dialogView.filterSeasonLayout.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getMediaSeasonStringArray()) { _, which ->
                    viewModel.currentData.selectedSeason = viewModel.mediaSeasonList[which]
                    dialogView.filterSeasonText.text = viewModel.mediaSeasonList[which]?.name?.replaceUnderscore() ?: "-"
                }
                .show()
        }

        dialogView.filterCountryLayout.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getMediaCountryStringArray()) { _, which ->
                    viewModel.currentData.selectedCountry = viewModel.mediaCountryList[which]
                    dialogView.filterCountryText.text = viewModel.mediaCountryList[which]?.value?.replaceUnderscore() ?: "-"
                }
                .show()
        }

        dialogView.filterStatusLayout.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getMediaStatusStringArray()) { _, which ->
                    viewModel.currentData.selectedStatus = viewModel.mediaStatusList[which]
                    dialogView.filterStatusText.text = viewModel.mediaStatusList[which]?.name?.replaceUnderscore() ?: "-"
                }
                .show()
        }

        dialogView.filterSourceLayout.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getMediaSourceStringArray()) { _, which ->
                    viewModel.currentData.selectedSource = viewModel.mediaSourceList[which]
                    dialogView.filterSourceText.text = viewModel.mediaSourceList[which]?.name?.replaceUnderscore() ?: "-"
                }
                .show()
        }

        dialogView.filterYearSeekBar.max = Utility.getCurrentYear() + 1 - Constant.FILTER_EARLIEST_YEAR
        dialogView.filterYearSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    dialogView.filterYearText.text = "-"
                    viewModel.currentData.selectedYear = null
                } else {
                    dialogView.filterYearText.text = (progress + Constant.FILTER_EARLIEST_YEAR).toString()
                    viewModel.currentData.selectedYear = progress + Constant.FILTER_EARLIEST_YEAR
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialogView.filterAddMoreGenreText.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getGenreListStringArray()) { _, which ->
                    if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
                        viewModel.currentData.selectedGenreList = arrayListOf(viewModel.genreList[which])
                        dialogView.filterGenreRecyclerView.adapter?.notifyDataSetChanged()
                        dialogView.filterGenreRecyclerView.visibility = View.VISIBLE
                        dialogView.filterGenreNoItemText.visibility = View.GONE
                    } else if (!viewModel.currentData.selectedGenreList!!.contains(viewModel.genreList[which])) {
                        viewModel.currentData.selectedGenreList!!.add(viewModel.genreList[which])
                        dialogView.filterGenreRecyclerView.adapter?.notifyDataSetChanged()
                        dialogView.filterGenreRecyclerView.visibility = View.VISIBLE
                        dialogView.filterGenreNoItemText.visibility = View.GONE
                    }
                }
                .show()
        }

        dialogView.filterAddMoreGenreText.setOnClickListener {
            MaterialAlertDialogBuilder(activity)
                .setItems(viewModel.getGenreListStringArray()) { _, which ->
                    if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
                        viewModel.currentData.selectedGenreList = arrayListOf(viewModel.genreList[which])
                        dialogView.filterGenreRecyclerView.adapter = assignAdapter(viewModel.currentData.selectedGenreList,
                            LIST_GENRE
                        )
                        dialogView.filterTagRecyclerView.visibility = View.VISIBLE
                        dialogView.filterTagNoItemText.visibility = View.GONE
                    } else if (!viewModel.currentData.selectedGenreList!!.contains(viewModel.genreList[which])) {
                        viewModel.currentData.selectedGenreList!!.add(viewModel.genreList[which])
                        dialogView.filterGenreRecyclerView.adapter = assignAdapter(viewModel.currentData.selectedGenreList,
                            LIST_GENRE
                        )
                        dialogView.filterTagRecyclerView.visibility = View.VISIBLE
                        dialogView.filterTagNoItemText.visibility = View.GONE
                    }
                }
                .show()
        }

        dialogView.filterOnlyShowMediaCheckBox.setOnClickListener {
            if (dialogView.filterOnlyShowMediaCheckBox.isChecked) {
                dialogView.filterHideMediaCheckBox.isChecked = false
                viewModel.currentData.selectedOnList = true
            } else {
                viewModel.currentData.selectedOnList = null
            }
        }

        dialogView.filterOnlyShowMediaText.setOnClickListener {
            dialogView.filterOnlyShowMediaCheckBox.performClick()
        }

        dialogView.filterHideMediaCheckBox.setOnClickListener {
            if (dialogView.filterHideMediaCheckBox.isChecked) {
                dialogView.filterOnlyShowMediaCheckBox.isChecked = false
                viewModel.currentData.selectedOnList = false
            } else {
                viewModel.currentData.selectedOnList = null
            }
        }

        dialogView.filterHideMediaText.setOnClickListener {
            dialogView.filterHideMediaCheckBox.performClick()
        }

        dialogView.filterApplyButton.setOnClickListener {
            listener.passFilterData(viewModel.currentData)
            dismiss()
        }

        dialogView.filterResetButton.setOnClickListener {
            listener.passFilterData(null)
            dismiss()
        }
    }

    private fun assignAdapter(list: List<String?>?, code: Int): MediaFilterRvAdapter {
        if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
            dialogView.filterGenreRecyclerView.visibility = View.GONE
            dialogView.filterGenreNoItemText.visibility = View.VISIBLE
        } else {
            dialogView.filterGenreRecyclerView.visibility = View.VISIBLE
            dialogView.filterGenreNoItemText.visibility = View.GONE
        }

        if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
            dialogView.filterTagRecyclerView.visibility = View.GONE
            dialogView.filterTagNoItemText.visibility = View.VISIBLE
        } else {
            dialogView.filterTagRecyclerView.visibility = View.VISIBLE
            dialogView.filterTagNoItemText.visibility = View.GONE
        }

        return MediaFilterRvAdapter(
            list ?: ArrayList(),
            code,
            object :
                MediaFilterRvAdapter.MediaFilterListListener {
                override fun deleteItem(position: Int, code: Int) {
                    if (code == LIST_GENRE) {
                        viewModel.currentData.selectedGenreList?.removeAt(position)
                        dialogView.filterGenreRecyclerView.adapter?.notifyDataSetChanged()
                        if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
                            dialogView.filterGenreRecyclerView.visibility = View.GONE
                            dialogView.filterGenreNoItemText.visibility = View.VISIBLE
                        }
                    } else if (code == LIST_TAG) {
                        viewModel.currentData.selectedTagList?.removeAt(position)
                        dialogView.filterTagRecyclerView.adapter?.notifyDataSetChanged()
                        if (viewModel.currentData.selectedGenreList.isNullOrEmpty()) {
                            dialogView.filterTagRecyclerView.visibility = View.GONE
                            dialogView.filterTagNoItemText.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    fun setListener(mediaFilterListener: MediaFilterListener) {
        listener = mediaFilterListener
    }
}