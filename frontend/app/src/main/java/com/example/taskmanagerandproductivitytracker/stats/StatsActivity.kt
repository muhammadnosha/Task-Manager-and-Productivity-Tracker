package com.example.taskmanagerandproductivitytracker.stats

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.taskmanagerandproductivitytracker.ui.theme.TaskManagerAndProductivityTrackerTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class StatsActivity : ComponentActivity() {
    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        viewModel.loadStats(userId)

        setContent {
            TaskManagerAndProductivityTrackerTheme {
                StatsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: StatsViewModel, onNavigateBack: () -> Unit) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productivity Stats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.data.isEmpty() && uiState.error == null -> {
                    Text(
                        "No productivity data available.",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
                uiState.error != null -> {
                    Text(
                        "Could not load chart data.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.error
                    )
                }
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Time Spent per Day (Last 30 Days)",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProductivityBarChart(data = uiState.data)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityBarChart(data: List<ProductivityData>) {
    val chartColor = MaterialTheme.colors.primary.toArgb()
    val textColor = MaterialTheme.colors.onSurface.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBarShadow(false)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = textColor
                xAxis.granularity = 1f
                xAxis.valueFormatter = DateAxisFormatter(data.map { it.date })

                axisLeft.textColor = textColor
                axisLeft.valueFormatter = TimeAxisFormatter()
                axisLeft.axisMinimum = 0f
                axisLeft.setDrawGridLines(true)

                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, productivityData ->
                BarEntry(index.toFloat(), productivityData.totalSeconds / 60f)
            }

            val dataSet = BarDataSet(entries, "Time Spent").apply {
                color = chartColor
                valueTextColor = textColor
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getBarLabel(barEntry: BarEntry?): String {
                        return "${barEntry?.y?.toInt()}m"
                    }
                }
            }

            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}

class TimeAxisFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val minutes = value.toLong()
        return if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = TimeUnit.MINUTES.toHours(minutes)
            val remainingMinutes = minutes - TimeUnit.HOURS.toMinutes(hours)
            if (remainingMinutes == 0L) "${hours}h" else "${hours}h ${remainingMinutes}m"
        }
    }
}

class DateAxisFormatter(private val dates: List<String>) : ValueFormatter() {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val outputFormat = SimpleDateFormat("MMM d", Locale.US)

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index >= 0 && index < dates.size) {
            try {
                val date = inputFormat.parse(dates[index])
                date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                ""
            }
        } else ""
    }
}
