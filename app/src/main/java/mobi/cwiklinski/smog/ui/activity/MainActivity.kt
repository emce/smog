package mobi.cwiklinski.smog.ui.activity

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.TypiconsIcons
import kotlinx.android.synthetic.main.activity_main.*
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.Viewport
import mobi.cwiklinski.bloodline.ui.extension.setToolbar
import mobi.cwiklinski.smog.R
import mobi.cwiklinski.smog.model.MainViewModel
import mobi.cwiklinski.smog.service.ReadingService

class MainActivity : MainViewModel.MainViewModelListener, AppCompatActivity() {

    var callbackManager = CallbackManager.Factory.create()
    var viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar()
        ReadingService.refresh(this)
        FacebookSdk.sdkInitialize(applicationContext);
        viewModel.listener = this
        mainChart.isZoomEnabled = false
        viewModel.mainAmount0Text.subscribe()
        viewModel.mainAmount1Text.subscribe({
            mainAmount1.text = it
        })
        viewModel.mainAmount2Text.subscribe({
            mainAmount2.text = it
        })
        viewModel.mainAmount3Text.subscribe({
            mainAmount3.text = it
        })
        viewModel.mainAmount4Text.subscribe({
            mainAmount4.text = it
        })
        viewModel.mainTime0Text.subscribe({
            mainTime0.text = it
        })
        viewModel.mainTime1Text.subscribe({
            mainTime1.text = it
        })
        viewModel.mainTime2Text.subscribe({
            mainTime2.text = it
        })
        viewModel.mainTime3Text.subscribe({
            mainTime3.text = it
        })
        viewModel.mainTime4Text.subscribe({
            mainTime4.text = it
        })
        viewModel.progress0Color.subscribe({
            setProgressColor(it, mainValue0)
        })
        viewModel.progress1Color.subscribe({
            setProgressColor(it, mainValue1)
        })
        viewModel.progress2Color.subscribe({
            setProgressColor(it, mainValue2)
        })
        viewModel.progress3Color.subscribe({
            setProgressColor(it, mainValue3)
        })
        viewModel.progress4Color.subscribe({
            setProgressColor(it, mainValue4)
        })
        viewModel.progress0Value.subscribe({
            mainValue0.progress = it
        })
        viewModel.progress1Value.subscribe({
            mainValue1.progress = it
        })
        viewModel.progress2Value.subscribe({
            mainValue2.progress = it
        })
        viewModel.progress3Value.subscribe({
            mainValue3.progress = it
        })
        viewModel.progress4Value.subscribe({
            mainValue4.progress = it
        })
        viewModel.progress0Max.subscribe({
            mainValue0.max = it
        })
        viewModel.progress1Max.subscribe({
            mainValue1.max = it
        })
        viewModel.progress2Max.subscribe({
            mainValue2.max = it
        })
        viewModel.progress3Max.subscribe({
            mainValue3.max = it
        })
        viewModel.progress4Max.subscribe({
            mainValue4.max = it
        })
        viewModel.queryForData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        var refresh = menu!!.add(R.id.menu_group_main, R.id.menu_refresh, 2, R.string.app_name)
        MenuItemCompat.setShowAsAction(refresh, MenuItemCompat.SHOW_AS_ACTION_ALWAYS)
        refresh.setIcon(IconDrawable(this, TypiconsIcons.typcn_arrow_sync_outline).actionBarSize().color(Color.WHITE))
        var share = menu.add(R.id.menu_group_main, R.id.menu_share, 1, R.string.app_name)
        MenuItemCompat.setShowAsAction(share, MenuItemCompat.SHOW_AS_ACTION_ALWAYS)
        share.setIcon(IconDrawable(this, TypiconsIcons.typcn_export_outline).actionBarSize().color(Color.WHITE))
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_refresh -> {
                ReadingService.refresh(this)
                return true
            }
            R.id.menu_share -> {
                mainRoot.isDrawingCacheEnabled = true
                var photo = SharePhoto.Builder().setBitmap(mainRoot.drawingCache!!).build()
                var content = SharePhotoContent.Builder().addPhoto(photo).build()
                if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                    ShareDialog(this).show(content)
                    return true
                }
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onGraphDataLoaded(chartData: LineChartData, min: Float?, max: Float?) {
        mainChart.lineChartData = chartData
        var v = Viewport(mainChart.maximumViewport);
        v.bottom = min!!.minus(5f)
        v.top = max!!.plus(5f)
        mainChart.maximumViewport = v
        mainChart.setCurrentViewportWithAnimation(v)
    }

    private fun setProgressColor(color: Int, progress: ProgressBar) {
        try {
            if (progress.progressDrawable!! is LayerDrawable) {
                var layerDrawable = progress.progressDrawable as LayerDrawable;
                var progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
                progressDrawable.setColorFilter(resources.getColor(color), PorterDuff.Mode.SRC_IN)
            }
        } catch (e : Resources.NotFoundException) {
            e.printStackTrace()
        }
    }
}