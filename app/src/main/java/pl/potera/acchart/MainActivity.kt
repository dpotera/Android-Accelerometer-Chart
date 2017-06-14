package pl.potera.acchart

import android.graphics.Color
import android.hardware.Sensor
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.androidplot.Plot
import com.androidplot.util.Redrawer
import com.androidplot.xy.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.startActivity
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        SensorEventListener {
    private val alpha = 0.8f
    private val gravity = FloatArray(3)
    private val linear_acceleration = FloatArray(3)
    private val historySeries: Array<SimpleXYSeries> = arrayOf(
            SimpleXYSeries("X"), SimpleXYSeries("Y"), SimpleXYSeries("Z"))
    private var redrawer: Redrawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        setupChart()
        setupAccelerometer()
        redrawer = Redrawer(Arrays.asList(*arrayOf<Plot<*, *, *, *, *>>(history_plot)), 100f, false)
    }

    private fun setupChart() {
        setupChartRange()
        setupChartDomain()
        setupChartLabels()
        setupChartSeries()
    }

    private fun setupAccelerometer() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun setupChartLabels() {
        history_plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("#")
        history_plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = DecimalFormat("#")
    }

    private fun setupChartSeries() {
        historySeries.forEach { series -> series.useImplicitXVals() }
        history_plot.addSeries(historySeries[0], LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null))
        history_plot.addSeries(historySeries[1], LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null))
        history_plot.addSeries(historySeries[2], LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null))
    }

    private fun setupChartDomain() {
        history_plot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED)
        history_plot.setDomainLabel("Dimensions")
        history_plot.domainStepMode = StepMode.INCREMENT_BY_VAL
        history_plot.domainStepValue = (HISTORY_SIZE / 10).toDouble()
        history_plot.domainTitle.pack()
    }

    private fun setupChartRange() {
        history_plot.setRangeBoundaries(-50, 50, BoundaryMode.FIXED)
        history_plot.setRangeLabel("Acceleration (-g - ∑F / mass)")
        history_plot.setRangeStep(StepMode.INCREMENT_BY_FIT, 10.0)
        history_plot.linesPerRangeLabel = 5
        history_plot.rangeTitle.pack()
    }

    @Synchronized override fun onSensorChanged(event: SensorEvent) {
        calculateAcceleration(event)
        if (historySeries[0].size() > HISTORY_SIZE)
            historySeries.forEach { series -> series.removeFirst() }
        historySeries.forEachIndexed { index, series ->
            series.addLast(null, linear_acceleration[index]) }
    }

    private fun calculateAcceleration(event: SensorEvent) {
        calculateGravity(event)
        calculateLinearAcceleration(event)
    }

    private fun calculateGravity(event: SensorEvent) {
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
    }

    private fun calculateLinearAcceleration(event: SensorEvent) {
        linear_acceleration[0] = event.values[0] - gravity[0]
        linear_acceleration[1] = event.values[1] - gravity[1]
        linear_acceleration[2] = event.values[2] - gravity[2]
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //  not implemented
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when(item.itemId) {
            R.id.nav_chart -> {}
            R.id.nav_values -> startActivity<ValuesActivity>()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
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
