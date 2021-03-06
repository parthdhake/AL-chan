package com.zen.alchan.ui.settings.app


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.zen.alchan.R
import com.zen.alchan.helper.Constant
import com.zen.alchan.helper.enums.AppColorTheme
import com.zen.alchan.helper.replaceUnderscore
import com.zen.alchan.helper.utils.AndroidUtility
import com.zen.alchan.helper.utils.DialogUtility
import com.zen.alchan.helper.utils.Utility
import kotlinx.android.synthetic.main.fragment_app_settings.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.StaffLanguage

/**
 * A simple [Fragment] subclass.
 */
class AppSettingsFragment : Fragment() {

    private val viewModel by viewModel<AppSettingsViewModel>()

    private lateinit var itemSave: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.apply {
            title = getString(R.string.app_settings)
            navigationIcon = ContextCompat.getDrawable(activity!!, R.drawable.ic_left)
            setNavigationOnClickListener { activity?.onBackPressed() }

            inflateMenu(R.menu.menu_save)
            itemSave = menu.findItem(R.id.itemSave)
        }

        initLayout()
    }

    private fun initLayout() {
        if (!viewModel.isInit) {
            viewModel.selectedAppTheme = viewModel.appSettings.appTheme
            viewModel.selectedLanguage = viewModel.appSettings.voiceActorLanguage
            circularAvatarCheckBox.isChecked = viewModel.appSettings.circularAvatar == true
            whiteBackgroundAvatarCheckBox.isChecked = viewModel.appSettings.whiteBackgroundAvatar == true
            showRecentReviewsCheckBox.isChecked = viewModel.appSettings.showRecentReviews == true
            enableSocialCheckBox.isChecked = viewModel.appSettings.showSocialTabAutomatically != false
            showBioCheckBox.isChecked = viewModel.appSettings.showBioAutomatically != false
            showStatsCheckBox.isChecked = viewModel.appSettings.showStatsAutomatically != false
            viewModel.isInit = true
        }

        itemSave.setOnMenuItemClickListener {
            DialogUtility.showOptionDialog(
                activity,
                R.string.save_settings,
                R.string.are_you_sure_you_want_to_save_this_configuration,
                R.string.save,
                {
                    viewModel.setAppSettings(
                        circularAvatarCheckBox.isChecked,
                        whiteBackgroundAvatarCheckBox.isChecked,
                        showRecentReviewsCheckBox.isChecked,
                        enableSocialCheckBox.isChecked,
                        showBioCheckBox.isChecked,
                        showStatsCheckBox.isChecked
                    )

                    activity?.recreate()
                    DialogUtility.showToast(activity, R.string.settings_saved)
                },
                R.string.cancel,
                { }
            )
            true
        }

        selectedThemeText.text = viewModel.selectedAppTheme?.name.replaceUnderscore()
        selectedThemeText.setOnClickListener { showAppThemeDialog() }

        defaultVoiceActorLanguageText.text = viewModel.selectedLanguage?.name
        defaultVoiceActorLanguageText.setOnClickListener { showLanguageDialog() }

        resetDefaultButton.setOnClickListener {
            val isLowOnMemory = AndroidUtility.isLowOnMemory(activity)

            DialogUtility.showOptionDialog(
                activity,
                R.string.reset_to_default,
                R.string.this_will_reset_your_app_settings_to_default_configuration,
                R.string.reset,
                {
                    viewModel.setAppSettings(
                        showSocialTab = !isLowOnMemory,
                        showBio = !isLowOnMemory,
                        showStats = !isLowOnMemory
                    )
                    viewModel.isInit = false
                    initLayout()

                    activity?.recreate()
                    DialogUtility.showToast(activity, R.string.settings_saved)
                },
                R.string.cancel,
                { }
            )
        }
    }

    private fun showAppThemeDialog() {
        val dialog = AppThemeDialog()
        dialog.setListener(object : AppThemeDialogListener {
            override fun passSelectedTheme(theme: AppColorTheme) {
                viewModel.selectedAppTheme = theme
                selectedThemeText.text = theme.name.replaceUnderscore()

                val palette = viewModel.selectedAppTheme?.value ?: Constant.DEFAULT_THEME.value
                primaryColorItem.setCardBackgroundColor(ContextCompat.getColor(activity!!, palette.primaryColor))
                secondaryColorItem.setCardBackgroundColor(ContextCompat.getColor(activity!!, palette.secondaryColor))
                negativeColorItem.setCardBackgroundColor(ContextCompat.getColor(activity!!, palette.negativeColor))
            }
        })
        dialog.show(childFragmentManager, null)
    }

    private fun showLanguageDialog() {
        MaterialAlertDialogBuilder(activity)
            .setItems(viewModel.staffLanguageArray) { _, which ->
                viewModel.selectedLanguage = StaffLanguage.valueOf(viewModel.staffLanguageArray[which])
                defaultVoiceActorLanguageText.text = viewModel.staffLanguageArray[which]
            }
            .show()
    }
}
