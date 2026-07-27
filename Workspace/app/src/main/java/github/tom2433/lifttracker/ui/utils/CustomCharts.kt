package github.tom2433.lifttracker.ui.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup

@Composable
fun MuscleGroupDonutChart(
    muscleGroupFrequencyList: List<LiftSetCountPerMuscleGroup>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    innerHeight: Dp = 96.dp
) {
    val chartItems = muscleGroupFrequencyList.filter { it.setCount > 0 }

    if (chartItems.isEmpty()) {
        Text("No muscle group data yet")
        return
    }

    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(chartItems) {
        modelProducer.runTransaction {
            pieSeries {
                series(chartItems.map { it.setCount })
            }
        }
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer.copy(
            alpha = 0.75f
        ),
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )

    val labelTextComponent = rememberTextComponent(
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onBackground
        )
    )

    val labeledSlices = List(chartItems.size) { index ->
        PieChart.Slice(
            fill = Fill(colors[index % colors.size]),
            label = PieChart.SliceLabel.Outside(
                textComponent = labelTextComponent,
                lineColor = MaterialTheme.colorScheme.onBackground
            ),
            strokeFill = Fill(MaterialTheme.colorScheme.onBackground),
            strokeThickness = 1.dp
        )
    }

    val chart = rememberPieChart(
        innerSize = PieSize.Inner.fixed(innerHeight),
        sliceProvider = PieChart.SliceProvider.series(labeledSlices),
        valueFormatter = { _, _, sliceIndex ->
            chartItems.getOrNull(sliceIndex)?.muscleGroupName.orEmpty()
        },
        spacing = 2.dp,
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        PieChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        )

        val numSets = chartItems.sumOf { it.setCount }

        Text(
            text = if (numSets == 1) {
                "1 set"
            } else {
                "$numSets sets"
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}