package mobi.cwiklinski.bloodline.ui.extension

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import mobi.cwiklinski.smog.R

fun AppCompatActivity.showSnackBar(text: String, view: View?) {
    if (view != null) {
        Snackbar
                .make(view, text, Snackbar.LENGTH_SHORT)
                .show()
    }
}

fun AppCompatActivity.setContentFragment(containerViewId: Int, fragment: Fragment?, tag: String?): Fragment? {
    val f: Fragment? = supportFragmentManager?.findFragmentById(containerViewId)
    f?.let { return@setContentFragment f }
    supportFragmentManager?.beginTransaction()?.add(containerViewId, fragment, tag)?.commit()
    return fragment
}

fun AppCompatActivity.getContentFragment(@IdRes resourceId: Int): Fragment? {
    return supportFragmentManager.findFragmentById(resourceId)
}

fun AppCompatActivity.getContentFragment(tag: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(tag)
}

fun AppCompatActivity.addFragment(tag: String, fragment: Fragment) {
    fragment.let {
        supportFragmentManager?.beginTransaction()?.add(it, tag)?.commit()
    }
}

fun AppCompatActivity.hideKeyboard() {
    if (currentFocus != null) {
        getInputMethodManager().hideSoftInputFromWindow(currentFocus.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}

fun AppCompatActivity.setToolbar() {
    if (findViewById(R.id.toolbar) != null) {
        var toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }
}

public inline fun <reified T : AppCompatActivity> AppCompatActivity.navigate(id: String,
                     sharedView: View? = null, transitionName: String? = null) {
    val intent = Intent(this, T::class.java)
    intent.putExtra("id", id)

    var options: ActivityOptionsCompat? = null

    if (sharedView != null && transitionName != null) {
        options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedView, transitionName)
    }

    ActivityCompat.startActivity(this, intent, options?.toBundle())
}

public inline fun <reified T : Any> AppCompatActivity.startActivityForResult(requestCode: Int, options: Bundle? = null) {
    startActivityForResult(IntentFor<T>(this), requestCode, options)
}

public inline fun <reified T : Any> IntentFor(context: Context): Intent = Intent(context, T::class.java)

fun AppCompatActivity.startLoader(loaderId: Int, callback: LoaderManager.LoaderCallbacks<Cursor>) {
    if (supportLoaderManager.getLoader<CursorLoader>(loaderId) != null
            && supportLoaderManager.getLoader<CursorLoader>(loaderId).isStarted) {
        supportLoaderManager.restartLoader(loaderId, Bundle(), callback);
    } else {
        supportLoaderManager.initLoader(loaderId, Bundle(), callback);
    }
}