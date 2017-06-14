package pl.potera.acchart

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_values.*
import kotlinx.android.synthetic.main.app_bar_values.*
import kotlinx.android.synthetic.main.content_values.*
import org.jetbrains.anko.startActivity

class ValuesActivity :AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_values)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        setupAccelerometer()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @Synchronized override fun onSensorChanged(event: SensorEvent) {
        x_value.text = event.values[0].toString()
        y_value.text = event.values[1].toString()
        z_value.text = event.values[2].toString()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_chart -> startActivity<MainActivity>()
            R.id.nav_values -> {}
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupAccelerometer() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }
}
