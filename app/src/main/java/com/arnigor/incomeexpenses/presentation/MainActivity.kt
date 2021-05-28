package com.arnigor.incomeexpenses.presentation

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arnigor.incomeexpenses.R
import com.arnigor.incomeexpenses.presentation.main.HeaderDataChangedListener
import com.google.android.material.navigation.NavigationView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


class MainActivity : AppCompatActivity(R.layout.activity_main), HasAndroidInjector,
    HeaderDataChangedListener {
    companion object {
        const val PREF_KEY_USER_NAME = "pref_key_user_name"
        const val PREF_KEY_USER_EMAIl = "pref_key_user_email"
    }

    private var navController: NavController? = null

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun headerDataChanged(name: String?, email: String?) {
        val navigationView = findViewById<View>(R.id.nav_view) as? NavigationView
        val hView: View? = navigationView?.getHeaderView(0)
        hView?.findViewById<TextView>(R.id.tvUserName)?.text = name ?: "No name"
        hView?.findViewById<TextView>(R.id.tvUserEmail)?.text = email ?: "No email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController!!)
    }

    override fun onBackPressed() {
        Log.i(
            MainActivity::class.java.canonicalName,
            "onBackPressed currentDestination:${navController?.currentDestination}"
        )
        when (navController?.currentDestination?.id) {
            R.id.nav_details -> {
                navController?.navigate(
                    R.id.nav_home,
                    null,
                    NavOptions.Builder().setLaunchSingleTop(true).build()
                )
            }
            R.id.nav_home -> {
                finish()
            }
            else -> {
                navController?.popBackStack()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}