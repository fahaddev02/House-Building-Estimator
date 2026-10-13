package com.example.paintcalculatorestimator.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.paintcalculatorestimator.R

/**
 * Jetpack Compose implementation of Ad Loading Dialog.
 *
 * Usage in Compose screen:
 * ```
 * var isLoadingAd by remember { mutableStateOf(false) }
 * ComposeAdLoadingDialog(isShowing = isLoadingAd)
 * ```
 */
@Composable
fun ComposeAdLoadingDialog(
    isShowing: Boolean,
    message: String = stringResource(id = R.string.please_wait_ad_is_loading)
) {
    if (isShowing) {
        Dialog(
            onDismissRequest = { /* Non-dismissable while ad is loading */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.White),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Activity/View based Ad Loading Dialog helper object for Interstitial/AppOpen ad helpers.
 */
object AdLoadingDialog {

    var dialog: Dialog? = null
    var isLoadingDialogShowing = false

    fun showLoadingDialog(context: Activity) {
        if (!isLoadingDialogShowing) {
            try {
                if (context.isFinishing || context.isDestroyed) return

                val builder = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                builder.setCancelable(false)
                builder.setCanceledOnTouchOutside(false)

                builder.window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        statusBarColor = Color.WHITE
                        navigationBarColor = Color.WHITE
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }

                val rootContainer = RelativeLayout(context).apply {
                    setBackgroundColor(Color.WHITE)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val dialogCard = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dpToPx(context, 24), dpToPx(context, 20), dpToPx(context, 24), dpToPx(context, 20))

                    val shape = GradientDrawable().apply {
                        setColor(0xFFF8F9FA.toInt())
                        cornerRadius = dpToPx(context, 12).toFloat()
                        setStroke(dpToPx(context, 1), 0xFFE0E0E0.toInt())
                    }
                    background = shape

                    val params = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.CENTER_IN_PARENT)
                    }
                    layoutParams = params
                }

                val progressBar = ProgressBar(context).apply {
                    indeterminateDrawable?.setTint(0xFF2196F3.toInt())
                    val params = LinearLayout.LayoutParams(dpToPx(context, 36), dpToPx(context, 36))
                    layoutParams = params
                }

                val textView = TextView(context).apply {
                    text = context.getString(R.string.please_wait_ad_is_loading)
                    setTextColor(0xFF212121.toInt())
                    textSize = 14f
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = dpToPx(context, 16)
                    }
                    layoutParams = params
                }

                dialogCard.addView(progressBar)
                dialogCard.addView(textView)
                rootContainer.addView(dialogCard)

                builder.setContentView(rootContainer)
                dialog = builder
                dialog?.show()
                isLoadingDialogShowing = true
            } catch (e: Exception) {
                Log.e("AdLoadingDialog", "showLoadingDialog error: $e")
            }
        } else {
            Log.e("AdLoadingDialog", "already showing")
        }
    }

    fun hideLoadingDialog() {
        try {
            isLoadingDialogShowing = false
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
            dialog = null
        } catch (e: Exception) {
            Log.e("AdLoadingDialog", "hideLoadingDialog error: $e")
        }
    }

    fun inShowing(): Boolean? {
        return dialog?.isShowing
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}