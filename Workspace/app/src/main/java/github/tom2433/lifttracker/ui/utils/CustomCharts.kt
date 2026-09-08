package github.tom2433.lifttracker.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.LiftSummary
import github.tom2433.lifttracker.data.structures.SessionDataPoint
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import java.util.Locale
import kotlin.math.roundToInt
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import github.tom2433.lifttracker.data.structures.SetDistributionPoint
import kotlin.math.abs

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

@Composable
fun LiftSummaryBarGraphs(
    liftSummary: LiftSummary?,
    valueColor: Color,
    valueContentColor: Color,
    historicalValueColor: Color,
    historicalValueContentColor: Color,
    modifier: Modifier = Modifier
) {
    var weightSelected by remember { mutableStateOf(true) }
    var repsOrTimeSelected by remember { mutableStateOf(false) }
    var intensitySelected by remember { mutableStateOf(false) }
    val weightLabel: String = liftSummary?.unitName?.capitalizeFirstChar() ?: "Weight"
    val repsOrTimeLabel: String = if (liftSummary == null) {
        "Reps Per Set"
    } else if (liftSummary.timed) {
        "Time Per Set"
    } else {
        "Reps Per Set"
    }
    val intensityLabel: String = if (liftSummary == null) {
        "Volume Per Set"
    } else if (liftSummary.timed) {
        "${liftSummary.unitName.capitalizeFirstChar()} Per Minute"
    } else {
        "Volume Per Set"
    }
    val weightValueLabel: String = if (liftSummary == null) {
        "0 units"
    } else if (liftSummary.avgWeight == null) {
        "not calculated"
    } else {
        "${"%.2f".format(liftSummary.avgWeight)} ${liftSummary.unitName}"
    }
    var repsOrTimeValueLabel = ""
    var historicalRepsOrTimeValueLabel = ""
    if (liftSummary != null) {
        if (liftSummary.avgRepsOrTime != null) {
            if (liftSummary.timed) {
                val timeTriple: Triple<Int, Int, Double> =
                    DateTimeCalculator.convertDoubleTimeToTripleTime(
                        minutes = liftSummary.avgRepsOrTime
                    )
                repsOrTimeValueLabel =
                    "${"%02d".format(timeTriple.first)}:" +
                            "${"%02d".format(timeTriple.second)}:" +
                            "%05.2f".format(timeTriple.third)
            } else {
                repsOrTimeValueLabel =
                    "${"%.2f".format(liftSummary.avgRepsOrTime)} reps"
            }
        } else {
            repsOrTimeValueLabel = "not calculated"
        }
        if (liftSummary.historicalAvgRepsOrTime != null) {
            if (liftSummary.timed) {
                val timeTriple = DateTimeCalculator.convertDoubleTimeToTripleTime(
                    minutes = liftSummary.historicalAvgRepsOrTime
                )
                historicalRepsOrTimeValueLabel =
                    "${"%02d".format(timeTriple.first)}:" +
                            "${"%02d".format(timeTriple.second)}:" +
                            "%05.2f".format(timeTriple.third)
            } else {
                historicalRepsOrTimeValueLabel =
                    "${"%.2f".format(liftSummary.historicalAvgRepsOrTime)} reps"
            }
        } else {
            historicalRepsOrTimeValueLabel = "not calculated"
        }
    } else {
        repsOrTimeValueLabel = "0 reps"
        historicalRepsOrTimeValueLabel = "0 reps"
    }
    val intensityValueLabel: String =
        if (liftSummary == null) {
            "0 units per set"
        } else {
            if (liftSummary.avgIntensity == null) {
                "not calculated"
            } else {
                "${"%.2f".format(liftSummary.avgIntensity)} " +
                        "${liftSummary.unitName} " + if (liftSummary.timed) {
                    "per minute"
                } else {
                    "per set"
                }
            }
        }
    val historicalWeightValueLabel: String =
        if (liftSummary == null) {
            "0 units"
        } else if (liftSummary.historicalAvgWeight == null) {
            "not calculated"
        } else {
            "${"%.2f".format(liftSummary.historicalAvgWeight)} ${liftSummary.unitName}"
        }
    val historicalIntensityValueLabel: String =
        if (liftSummary == null) {
            "0 units per set"
        } else {
            if (liftSummary.historicalAvgIntensity == null) {
                "not calculated"
            } else {
                "${"%.2f".format(liftSummary.historicalAvgIntensity)} " +
                        "${liftSummary.unitName} " + if (liftSummary.timed) {
                    "per minute"
                } else {
                    "per set"
                }
            }
        }


    // column to hold bar graphs
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        // scrollable row to hold filter chips
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            CustomFilterChip(
                label = weightLabel,
                onClick = {
                    weightSelected = true
                    repsOrTimeSelected = false
                    intensitySelected = false
                },
                selected = weightSelected,
                modifier = Modifier.padding(end = 8.dp)
            )
            CustomFilterChip(
                label = repsOrTimeLabel,
                onClick = {
                    weightSelected = false
                    repsOrTimeSelected = true
                    intensitySelected = false
                },
                selected = repsOrTimeSelected,
                modifier = Modifier.padding(end = 8.dp)
            )
            CustomFilterChip(
                label = intensityLabel,
                onClick = {
                    weightSelected = false
                    repsOrTimeSelected = false
                    intensitySelected = true
                },
                selected = intensitySelected,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // title for weight bar section
        Text(
            text =
                if (weightSelected) {
                    "Avg. $weightLabel"
                } else if (repsOrTimeSelected) {
                    "Avg. $repsOrTimeLabel"
                } else {
                    "Avg. $intensityLabel"
                },
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // ComparisonBars to hold any graph
        ComparisonBars(
            value =
                if (weightSelected) {
                    liftSummary?.avgWeight
                } else if (repsOrTimeSelected) {
                    liftSummary?.avgRepsOrTime
                } else {
                    liftSummary?.avgIntensity
                },
            historicalValue =
                if (weightSelected) {
                    liftSummary?.historicalAvgWeight
                } else if (repsOrTimeSelected) {
                    liftSummary?.historicalAvgRepsOrTime
                } else {
                    liftSummary?.historicalAvgIntensity
                },
            valueColor = valueColor,
            valueContentColor = valueContentColor,
            historicalValueColor = historicalValueColor,
            historicalValueContentColor = historicalValueContentColor,
            valueLabel =
                if (weightSelected) {
                    weightValueLabel
                } else if (repsOrTimeSelected) {
                    repsOrTimeValueLabel
                } else {
                    intensityValueLabel
                },
            historicalValueLabel =
                if (weightSelected) {
                    historicalWeightValueLabel
                } else if (repsOrTimeSelected) {
                    historicalRepsOrTimeValueLabel
                } else {
                    historicalIntensityValueLabel
                },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // line chart for set distribution
        if (liftSummary != null && liftSummary.setDistributionPoints.size > 1) {
            LiftSummaryDistributionLineGraph(
                title =
                    if (weightSelected) {
                        "${weightLabel.capitalizeFirstChar()} distribution"
                    } else if (repsOrTimeSelected) {
                        "${repsOrTimeLabel.capitalizeFirstChar()} distribution"
                    } else {
                        "${intensityLabel.capitalizeFirstChar()} distribution"
                    },
                dataPoints = liftSummary.setDistributionPoints,
                selectedMetric =
                    if (weightSelected) {
                        SummaryChartMetric.WEIGHT
                    } else if (repsOrTimeSelected) {
                        SummaryChartMetric.REPS_OR_TIME
                    } else {
                        SummaryChartMetric.INTENSITY
                    },
                timed = liftSummary.timed
            )
        }
    }
}

@Composable
fun ComparisonBars(
    value: Double?,
    historicalValue: Double?,
    valueColor: Color,
    valueContentColor: Color,
    historicalValueColor: Color,
    historicalValueContentColor: Color,
    valueLabel: String,
    historicalValueLabel: String,
    modifier: Modifier = Modifier
) {
    val maxVal: Double? =
        if (value == null && historicalValue == null) {
            null
        } else if (value == null) {
            historicalValue
        } else if (historicalValue == null) {
            value
        } else if (value > historicalValue) {
            value
        } else {
            historicalValue
        }
    val valueWidth by animateFloatAsState(
        targetValue = if (maxVal == null) {
            0.001f
        } else if (value == null) {
            0.001f
        } else {
            if (maxVal == 0.0) {
                0.001f
            } else {
                (value / maxVal).toFloat()
            }
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val historicalValueWidth by animateFloatAsState(
        targetValue = if (maxVal == null) {
            0.001f
        } else if (historicalValue == null) {
            0.001f
        } else {
            if (maxVal == 0.0) {
                0.001f
            } else {
                (historicalValue / maxVal).toFloat()
            }
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )

    // row to hold axis labels on left, horizontal bars on right
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
    ) {
        // column to hold axis labels
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f)
        ) {
            // value axis label
            Text(
                text = "this session",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
            // avg value axis label
            Text(
                text = "historical avg.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }

        // column to hold horizontal bars
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            // row to hold value bar
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                // surface with valueWidth
                Surface(
                    modifier = Modifier
                        .weight(valueWidth)
                        .fillMaxHeight(),
                    color = valueColor,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 4.dp,
                        bottomEnd = 4.dp
                    )
                ) {
                    // box to hold label
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // show label if this surface is bigger
                        if (valueWidth >= 0.5f) {
                            Text(
                                text = valueLabel,
                                color = valueContentColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                // surface to fill the remaining area
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(
                            if (1f - valueWidth == 0f) {
                                0.001f
                            } else {
                                1f - valueWidth
                            }
                        ),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // show label if this surface is bigger
                        if (valueWidth < 0.5f) {
                            Text(
                                text = valueLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            // row to hold historicalValue bar
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                // surface with historicalValue weight
                Surface(
                    modifier = Modifier
                        .weight(historicalValueWidth)
                        .fillMaxHeight(),
                    color = historicalValueColor,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 4.dp,
                        bottomEnd = 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // show label if this surface is bigger
                        if (historicalValueWidth >= 0.5f) {
                            Text(
                                text = historicalValueLabel,
                                color = historicalValueContentColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                // surface with historicalValueSpacer weight
                Surface(
                    modifier = Modifier
                        .weight(
                            if (1f - historicalValueWidth == 0f) {
                                0.001f
                            } else {
                                1f - historicalValueWidth
                            }
                        )
                        .fillMaxHeight(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (historicalValueWidth < 0.5f) {
                            Text(
                                text = historicalValueLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiftSummaryDistributionLineGraph(
    title: String,
    dataPoints: List<SetDistributionPoint>,
    selectedMetric: SummaryChartMetric,
    timed: Boolean,
    modifier: Modifier = Modifier
) {
    // define chart items
    val chartItems = remember(dataPoints) {
        dataPoints.sortedWith(
            compareBy<SetDistributionPoint> { it.setNumber }
        )
    }

    // column to hold chart
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        // chart title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // placeholder text if applicable
        if (chartItems.isEmpty()) {
            Text(
                text = "Nothing to see here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
            return
        }

        // retrieve model producer
        val modelProducer = remember { CartesianChartModelProducer() }

        // extract the x and y values and update line chart
        LaunchedEffect(chartItems, selectedMetric) {
            val xValues = chartItems.indices.map { it.toDouble() }
            val yValues = chartItems.map { selectedMetric.getSetDistributionPointValue(it) }

            val historicalPoints = chartItems.mapIndexedNotNull { index, dataPoint ->
                selectedMetric.getSetDistributionHistoricalPointValue(dataPoint)
                    ?.let { historicalValue -> index.toDouble() to historicalValue }
            }

            modelProducer.runTransaction {
                lineModel {
                    series(
                        x = xValues,
                        y = yValues,
                        key = "current"
                    )

                    if (historicalPoints.isNotEmpty()) {
                        series(
                            x = historicalPoints.map { it.first },
                            y = historicalPoints.map { it.second },
                            key = "historical"
                        )
                    }
                }
            }
        }

        // calculate spacing for x values
        val xAxisSpacing = remember(chartItems.size) {
            (chartItems.size / 4).coerceAtLeast(1)
        }

        // create x axis labels
        val bottomAxisValueFormatter = remember(chartItems) {
            CartesianValueFormatter { _, value, _ ->
                val index = value.roundToInt()
                chartItems.getOrNull(index)
                    ?.setNumber
                    ?.toSetLabel()
                    ?: index.toString()
            }
        }

        // create y axis labels
        val yAxisValueFormatter = remember {
            CartesianValueFormatter.decimal(decimalCount = 2)
        }

        // create line with primary container color
        val currentLine = LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(
                Fill(MaterialTheme.colorScheme.primaryContainer)
            )
        )

        // create historical line with tertiary container color
        val historicalLine = LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(
                Fill(MaterialTheme.colorScheme.tertiaryContainer)
            ),
            stroke = LineCartesianLayer.LineStroke.Dashed(
                thickness = 2.dp,
                dashLength = 2.dp,
                gapLength = 4.dp
            )
        )

        // create background shape for marker labels
        val markerLabelBackground = rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MarkerCornerBasedShape(
                base = RoundedCornerShape(6.dp)
            )
        )

        // create marker line
        val markerGuideline = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.primaryContainer.copy(0.75f)),
            thickness = 1.dp
        )

        // create marker label as the x's y value and historical value
        val markerValueFormatter = remember(chartItems, selectedMetric) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                val target = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
                val point = target?.points?.firstOrNull()
                val index = point?.entry?.x?.roundToInt()
                val dataPoint = index?.let { chartItems.getOrNull(it) }

                val value = point?.entry?.y
                val historicalVal: Double? = dataPoint?.let { selectedMetric.getSetDistributionHistoricalPointValue(dataPoint) }

                buildString {
                    if (dataPoint != null) {
                        append(
                            "${dataPoint.setNumber.toSetLabel()}\n"
                        )
                    }
                    append("This session: ")

                    if (timed) {
                        append(value?.let { DateTimeCalculator.convertDoubleTimeToString(abs(it)) } ?: "")
                    } else {
                        append(value?.let { "%.2f".format(abs(it)) } ?: "")
                    }
                    if (historicalVal != null) {
                        append("\n")
                        append("Historical: ")
                        if (timed) {
                            append(DateTimeCalculator.convertDoubleTimeToString(abs(historicalVal)))
                        } else {
                            append("%.2f".format(abs(historicalVal)))
                        }
                    }
                }
            }
        }

        // create marker label
        val markerLabel = rememberTextComponent(
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            lineCount = 3,
            overflow = TextOverflow.Ellipsis,
            padding = Insets(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            background = markerLabelBackground
        )

        // stroke color for circle indicator which appears on the corresponding point while the
        // marker is shown
        val indicatorStrokeColor = MaterialTheme.colorScheme.background

        val chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    currentLine,
                    historicalLine
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = yAxisValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisValueFormatter,
                labelRotationDegrees = -45f,
                itemPlacer = remember(xAxisSpacing) {
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = { xAxisSpacing }
                    )
                }
            ),
            marker = rememberDefaultCartesianMarker(
                label = markerLabel,
                valueFormatter = markerValueFormatter,
                labelPosition = DefaultCartesianMarker.LabelPosition.Top,
                indicator = { color ->
                    ShapeComponent(
                        fill = Fill(color),
                        shape = CircleShape,
                        strokeFill = Fill(indicatorStrokeColor),
                        strokeThickness = 2.dp
                    )
                },
                indicatorSize = 10.dp,
                guideline = markerGuideline
            ),
            markerController = CartesianMarkerController.rememberShowOnPress(
                consumeMoveEvents = true
            )
        )

        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            placeholder = {
                Text(
                    text = "Loading chart...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            zoomState = rememberVicoZoomState(
                initialZoom = Zoom.Content
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionSummaryLineGraph(
    title: String,
    dataPoints: List<SessionDataPoint>,
    selectedMetric: SummaryChartMetric,
    timed: Boolean,
    modifier: Modifier = Modifier
) {
    // define chart items
    val chartItems = remember(dataPoints) {
        dataPoints.sortedWith(
            compareBy<SessionDataPoint> { it.sessionDateIso }
                .thenBy { it.sessionId }
        )
    }

    // column to hold chart
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        // chart title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Left,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // placeholder text if applicable
        if (chartItems.isEmpty()) {
            Text(
                text = "Nothing to see here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
            return
        }

        // retrieve model producer
        val modelProducer = remember { CartesianChartModelProducer() }

        // extract the x and y values and update line chart
        LaunchedEffect(chartItems, selectedMetric) {
            val xValues = chartItems.indices.map { it.toDouble() }
            val yValues = chartItems.map { selectedMetric.getSessionDataPointValue(it) }

            modelProducer.runTransaction {
                lineModel {
                    series(
                        x = xValues,
                        y = yValues
                    )
                }
            }
        }

        // calculate spacing for x values
        val xAxisSpacing = remember(chartItems.size) {
            (chartItems.size / 4).coerceAtLeast(1)
        }

        // create x axis labels
        val bottomAxisValueFormatter = remember(chartItems) {
            CartesianValueFormatter { _, value, _ ->
                val index = value.roundToInt()
                chartItems.getOrNull(index)
                    ?.sessionDateIso
                    ?.toShortAxisDate()
                    ?: index.toString()
            }
        }

        // create y axis labels
        val yAxisValueFormatter = remember {
            CartesianValueFormatter.decimal(decimalCount = 2)
        }

        // create line with primary container color
        val line = LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primaryContainer))
        )

        // create dotted line for average
        val averageLine = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.tertiaryContainer),
            thickness = 2.dp,
            shape = DashedShape(
                shape = RoundedCornerShape(percent = 50),
                dashLength = 2.dp,
                gapLength = 4.dp
            )
        )

        // create label for average line
        val averageLabel = rememberTextComponent(
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        )

        // create line decoration for average line
        val averageLineDecoration = remember(averageLine, averageLabel) {
            HorizontalLine(
                y = { 0.0 },
                line = averageLine,
                labelComponent = averageLabel,
                label = { "Average" },
                horizontalLabelPosition = Position.Horizontal.End,
                verticalLabelPosition = Position.Vertical.Top
            )
        }

        // create background shape for the marker labels
        val markerLabelBackground = rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MarkerCornerBasedShape(
                base = RoundedCornerShape(6.dp)
            )
        )

        // create marker line
        val markerGuideline = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.primaryContainer.copy(0.75f)),
            thickness = 1.dp
        )

        // create marker label as the x's y value with the corresponding session's note
        val markerValueFormatter = remember(chartItems, selectedMetric) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                val target = targets.firstOrNull() as? LineCartesianLayerMarkerTarget
                val point = target?.points?.firstOrNull()
                val index = point?.entry?.x?.roundToInt()
                val dataPoint = index?.let { chartItems.getOrNull(it) }
                val date: String? = index?.let { chartItems.getOrNull(it)?.sessionDateIso ?: "" }

                val value = point?.entry?.y
                val note = dataPoint?.sessionNote?.trim().orEmpty()

                buildString {
                    if (date != null) {
                        append(
                            "${DateTimeCalculator.convertIsoDateToReadableFormat(date)}  |  "
                        )
                    }
                    append(
                        if (value != null && value > 0.0) {
                            "+"
                        } else {
                            "-"
                        }
                    )

                    if (timed && selectedMetric == SummaryChartMetric.REPS_OR_TIME) {
                        append(value?.let { DateTimeCalculator.convertDoubleTimeToString(abs(it))} ?: "")
                    } else {
                        append(value?.let { "%.2f".format(abs(it)) } ?: "")
                    }

                    if (note.isNotBlank()) {
                        append("\n")
                        append(note)
                    }
                }
            }
        }

        // create marker label
        val markerLabel = rememberTextComponent(
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            lineCount = 2,
            overflow = TextOverflow.Ellipsis,
            padding = Insets(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            background = markerLabelBackground
        )

        val indicatorStrokeColor = MaterialTheme.colorScheme.background

        val chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(line)
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = yAxisValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisValueFormatter,
                labelRotationDegrees = -45f,
                itemPlacer = remember(xAxisSpacing) {
                    HorizontalAxis.ItemPlacer.aligned(
                        spacing = { xAxisSpacing }
                    )
                }
            ),
            decorations = listOf(averageLineDecoration),
            marker = rememberDefaultCartesianMarker(
                label = markerLabel,
                valueFormatter = markerValueFormatter,
                labelPosition = DefaultCartesianMarker.LabelPosition.Top,
                indicator = { color ->
                    ShapeComponent(
                        fill = Fill(color),
                        shape = CircleShape,
                        strokeFill = Fill(indicatorStrokeColor),
                        strokeThickness = 2.dp
                    )
                },
                indicatorSize = 10.dp,
                guideline = markerGuideline
            ),
            markerController = CartesianMarkerController.rememberShowOnPress(
                consumeMoveEvents = true
            )
        )

        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            placeholder = {
                Text(
                    text = "Loading chart...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            },
            zoomState = rememberVicoZoomState(
                initialZoom = Zoom.Content
            )
        )
    }
}

private fun String.toShortAxisDate(): String {
    val month = substringOrNull(5, 7)?.toIntOrNull()
    val day = substringOrNull(8, 10)?.toIntOrNull()

    return if (month != null && day != null) {
        "$month/$day"
    } else {
        this
    }
}

private fun Int.toSetLabel(): String {
    return "Set $this"
}

private fun String.substringOrNull(
    startIndex: Int,
    endIndex: Int
): String? {
    return if (length >= endIndex) {
        substring(startIndex, endIndex)
    } else {
        null
    }
}

fun String.capitalizeFirstChar(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }
}

enum class SummaryChartMetric {
    WEIGHT,
    REPS_OR_TIME,
    INTENSITY;

    fun getSessionDataPointValue(dataPoint: SessionDataPoint): Double {
        return when (this) {
            WEIGHT -> dataPoint.weightDeviation
            REPS_OR_TIME -> dataPoint.repsOrTimeDeviation
            INTENSITY -> dataPoint.intensityDeviation
        }
    }

    fun getSetDistributionPointValue(dataPoint: SetDistributionPoint): Double {
        return when(this) {
            WEIGHT -> dataPoint.weightValue
            REPS_OR_TIME -> dataPoint.repsOrTime
            INTENSITY -> dataPoint.intensity
        }
    }

    fun getSetDistributionHistoricalPointValue(dataPoint: SetDistributionPoint): Double? {
        return when(this) {
            WEIGHT -> dataPoint.avgWeightValue
            REPS_OR_TIME -> dataPoint.avgRepsOrTime
            INTENSITY -> dataPoint.avgIntensity
        }
    }
}