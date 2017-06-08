package pl.potera.lightsword

import android.graphics.Color
import android.hardware.Sensor
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.androidplot.Plot
import com.androidplot.util.PlotStatistics
import com.androidplot.util.Redrawer
import com.androidplot.xy.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener {
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val linear_acceleration = FloatArray(3)

    private val xHistorySeries: SimpleXYSeries = SimpleXYSeries("X")
    private val yHistorySeries: SimpleXYSeries = SimpleXYSeries("Y")
    private val zHistorySeries: SimpleXYSeries = SimpleXYSeries("Z")

    private var sensorManager: SensorManager? = null
    private var redrawer: Redrawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        initializeChart()
    }

    private fun initializeChart() {
        xHistorySeries.useImplicitXVals()
        yHistorySeries.useImplicitXVals()
        zHistorySeries.useImplicitXVals()

        history_plot.setRangeBoundaries(-50, 50, BoundaryMode.FIXED)
        history_plot.linesPerRangeLabel = 5
        history_plot.setRangeStep(StepMode.INCREMENT_BY_FIT, 10.0)
        history_plot.setRangeLabel("Acceleration (-g - ∑F / mass)")
        history_plot.rangeTitle.pack()

        history_plot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED)
        history_plot.setDomainLabel("Dimensions")
        history_plot.domainTitle.pack()

        history_plot.addSeries(xHistorySeries, LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null))
        history_plot.addSeries(yHistorySeries, LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null))
        history_plot.addSeries(zHistorySeries, LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null))
        history_plot.domainStepMode = StepMode.INCREMENT_BY_VAL
        history_plot.domainStepValue = (HISTORY_SIZE / 10).toDouble()

        history_plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("#")
        history_plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("#")

        val histStats = PlotStatistics(1000, false)
        history_plot.addListener(histStats)

        val mAccelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI)

        redrawer = Redrawer(Arrays.asList(*arrayOf<Plot<*, *, *, *, *>>(history_plot)), 100f, false)
    }

    @Synchronized override fun onSensorChanged(event: SensorEvent) {
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        linear_acceleration[0] = event.values[0] - gravity[0]
        linear_acceleration[1] = event.values[1] - gravity[1]
        linear_acceleration[2] = event.values[2] - gravity[2]

        if (xHistorySeries.size() > HISTORY_SIZE) {
            xHistorySeries.removeFirst()
            yHistorySeries.removeFirst()
            zHistorySeries.removeFirst()
        }

        xHistorySeries.addLast(null, linear_acceleration[0])
        yHistorySeries.addLast(null, linear_acceleration[1])
        zHistorySeries.addLast(null, linear_acceleration[2])
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //  not implemented
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) return true
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    public override fun onResume() {
        super.onResume()
        redrawer?.start()
    }

    public override fun onPause() {
        redrawer?.pause()
        super.onPause()
    }

    public override fun onDestroy() {
        redrawer?.finish()
        super.onDestroy()
    }

    companion object {
        private val HISTORY_SIZE = 30
    }
}
