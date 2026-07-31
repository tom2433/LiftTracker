package github.tom2433.lifttracker.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.utils.DateTimeCalculator

@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // label for detail metric
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        // divider to link label to metric
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        // metric
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun RowWithSeparator(
    dividerColor: Color,
    leftHandSide: @Composable () -> Unit,
    rightHandSide: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        leftHandSide()
        HorizontalDivider(
            color = dividerColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )
        rightHandSide()
    }
}

@Composable
fun DisplaySetCountPerMuscleGroup(
    setCountPerMuscleGroupList: List<LiftSetCountPerMuscleGroup>,
    modifier: Modifier = Modifier,
    showTotalSets: Boolean = true
) {
    var totalSets = 0
    for (setCount in setCountPerMuscleGroupList) {
        totalSets += setCount.setCount
    }

    // card to hold set counts per muscle group
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.75f
            ),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = modifier
    ) {
        // column to hold header and set counts per muscle group
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // row to hold muscle group icon and # icon
            RowWithSeparator(
                dividerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                leftHandSide = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arm_flex),
                        contentDescription = stringResource(R.string.muscle_group)
                    )
                },
                rightHandSide = {
                    Icon(
                        imageVector = Icons.Filled.Numbers,
                        contentDescription = stringResource(R.string.set_count)
                    )
                }
            )

            // display all muscle group set counts
            for (setCountPerMuscleGroup in setCountPerMuscleGroupList) {
                RowWithSeparator(
                    dividerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    leftHandSide = {
                        Text(
                            text = setCountPerMuscleGroup.muscleGroupName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    rightHandSide = {
                        Text(
                            text = if (setCountPerMuscleGroup.setCount == 1) {
                                "1 set"
                            } else {
                                "${setCountPerMuscleGroup.setCount} sets"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }

            // row to display total
            if (showTotalSets) {
                RowWithSeparator(
                    dividerColor = MaterialTheme.colorScheme.primary,
                    leftHandSide = {
                        Text(
                            text = stringResource(R.string.total),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    rightHandSide = {
                        Text(
                            text = if (totalSets == 1) {
                                "1 set"
                            } else {
                                "$totalSets sets"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CustomFilterChip(
    label: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = label
            )
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done Icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        modifier = modifier
    )
}

@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp
        ),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                ) {
                    content()
                }
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SetNumberRow(
    labelText: String,
    valueText: String,
    noteColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    boldSetNumber: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
    ) {
        // label
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            color = noteColor,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // value
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = if (boldSetNumber) {
                FontWeight.Bold
            } else {
                null
            },
            softWrap = false
        )
    }
}

@Composable
fun LiftDetailFlowRow(
    muscleGroupName: String,
    metricType: String,
    unitName: String,
    modifier: Modifier = Modifier,
    tintColor: Color = MaterialTheme.colorScheme.onBackground
) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        // muscle group
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arm_flex),
                contentDescription = stringResource(R.string.muscle_group),
                tint = tintColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = muscleGroupName,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // metric type
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ruler),
                contentDescription = stringResource(R.string.metric_type),
                tint = tintColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = metricType,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // lift unit
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Scale,
                contentDescription = stringResource(R.string.unit),
                tint = tintColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = unitName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LabelHeader(
    headerText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // row to hold header and dividers
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // divider 1
        HorizontalDivider(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f),
            color = color
        )
        // header text
        Text(
            text = headerText,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        // divider 2
        HorizontalDivider(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            color = color
        )
    }
}

@Composable
fun LoadMoreLabelAndButton(
    numDisplayed: Int,
    numExisting: Int,
    elementNamePlural: String,
    onClickLoadMore: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // label
        Text(
            text = if (numDisplayed == 0) {
                "Showing 0 of $numExisting $elementNamePlural."
            } else {
                "Showing 1-$numDisplayed of $numExisting ${elementNamePlural}."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.75f
            ),
            textAlign = TextAlign.Center
        )

        // button to load more (if applicable)
        if (numExisting > numDisplayed) {
            val numToLoad = if (numExisting - numDisplayed > 10) {
                10
            } else {
                numExisting - numDisplayed
            }

            Card(
                colors = CardDefaults.cardColors().copy(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                onClick = { onClickLoadMore(numToLoad) },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Load $numToLoad more",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LayoutSwitcher(
    onListLayoutClicked: () -> Unit,
    listLayoutEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // list format icon button
    IconButton(
        onClick = onListLayoutClicked,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (listLayoutEnabled) {
                Icons.AutoMirrored.Filled.ViewList
            } else {
                Icons.AutoMirrored.Outlined.ViewList
            },
            contentDescription = "Switch to list layout"
        )
    }
}

/**
 * Displays expandable lift cards for a session in order specified by [displaySetList].
 *
 * @param displaySetList ordered list of pairs ordered by session_set_number with first element
 * the Lift id, and the second element a List of LiftSet ids maintaining order.
 * @param liftDetailMap map of lift ids pointing to their corresponding [LiftSearchDetail]
 * objects.
 * @param liftSetMap map of LiftSet ids pointing to Triples containing a LiftSet object and
 * both of its SetMetric objects.
 * @param modifier optional modifier for the [Column] that the expandable lift cards are stored in.
 */
@Composable
fun DisplayAllSetDataForSession(
    displaySetList: List<Pair<Int, List<Int>>>,
    liftDetailMap: Map<Int, LiftSearchDetail>,
    liftSetMap: Map<Int, Triple<LiftSet, SetMetric, SetMetric>>,
    noteColor: Color,
    onClickLiftCard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier.fillMaxWidth()
    ) {
        // loop thru each pair<Lift id, list of LiftSet ids>
        for ((index, liftAndSetsPair) in displaySetList.withIndex()) {
            // retrieve the LiftSearchDetail object from liftDetailMap
            val liftDetail: LiftSearchDetail = liftDetailMap[liftAndSetsPair.first] ?: continue
            val liftSetIds: List<Int> = liftAndSetsPair.second
            key(liftDetail.liftObj.id) {
                HistoricalLiftCard(
                    liftDetail = liftDetail,
                    noteColor = noteColor,
                    liftSetIds = liftSetIds,
                    liftSetMap = liftSetMap,
                    onClickLiftCard = onClickLiftCard
                )

                if (index != displaySetList.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HistoricalLiftCard(
    liftDetail: LiftSearchDetail,
    noteColor: Color,
    liftSetIds: List<Int>,
    liftSetMap: Map<Int, Triple<LiftSet, SetMetric, SetMetric>>,
    onClickLiftCard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // animate the border color
    val borderColor by animateColorAsState(
        targetValue = if (liftDetail.selected) {
            MaterialTheme.colorScheme.secondary
        } else {
            Color.Transparent
        }
    )
    // animate the corner radius
    val bottomCornerRadius by animateDpAsState(
        targetValue = if (liftDetail.selected) {
            0.dp
        } else {
            8.dp
        }
    )

    // column to hold lift card and its set data
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // lift card to hold lift name, num of sets, and muscle group
        Card(
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = bottomCornerRadius,
                bottomEnd = bottomCornerRadius
            ),
            onClick = { onClickLiftCard(liftDetail.liftObj.id) },
            modifier = Modifier
                .fillMaxWidth(),
            border = BorderStroke(
                width = 1.dp,
                color = borderColor
            )
        ) {
            // row to hold card contents (lift name, note, set count on left, muscle
            // group on right)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // column to hold lift name, note, set count
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(2f)
                ) {
                    // lift name
                    Text(
                        text = liftDetail.liftObj.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    // lift note (if applicable)
                    if (liftDetail.liftObj.note.isNotBlank()) {
                        Text(
                            text = liftDetail.liftObj.note,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 15.sp,
                            color = noteColor
                        )
                    }
                    // lift set count
                    Text(
                        text = if (liftSetIds.size == 1) {
                            "1 set"
                        } else {
                            "${liftSetIds.size} sets"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        color = noteColor
                    )
                }

                // row to hold muscle group icon and name
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = true)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arm_flex),
                        contentDescription = stringResource(R.string.muscle_group),
                        tint = noteColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = liftDetail.muscleGroupName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        color = noteColor
                    )
                }
            }
        }

        // animated visibility for set sections
        AnimatedVisibility(
            visible = liftDetail.selected,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeIn(tween(300)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        ) {
            // column to hold set sections
            Column {
                // loop thru each set and display its section
                for ((index, liftSetId) in liftSetIds.withIndex()) {
                    HistoricalSetSection(
                        liftSet = liftSetMap[liftSetId]?.first ?: continue,
                        weightMetric = liftSetMap[liftSetId]?.second ?: continue,
                        secondMetric = liftSetMap[liftSetId]?.third ?: continue,
                        muscleGroupName = liftDetail.muscleGroupName,
                        unitName = liftDetail.unitName,
                        metricType = liftDetail.metricType,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                    if (index != liftSetIds.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.4f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricalSetSection(
    liftSet: LiftSet,
    weightMetric: SetMetric,
    secondMetric: SetMetric,
    muscleGroupName: String,
    unitName: String,
    metricType: String,
    modifier: Modifier = Modifier
) {
    // row to hold lift set details on the left, set metric details on the right
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // column to hold set label, set note (if applicable), session set # and muscle group
        // session set #
        LiftSetLabels(
            setLabel = liftSet.set_label,
            setNote = liftSet.set_note,
            muscleGroupName = muscleGroupName,
            sessionSetNumber = liftSet.session_set_number,
            muscleGroupSessionSetNumber = liftSet.muscle_group_session_set_number,
            noteColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.75f
            ),
            borderColor = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.5f
            ),
            contentColor = MaterialTheme.colorScheme.onBackground,
            showSpacerToSeparateSetLabel = false,
            boldSetNumbers = false,
            modifier = Modifier
                .weight(0.75f)
                .padding(end = 4.dp)
        )

        // column to hold weight metrics and rep/time metrics
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        ) {
            // card to hold weight metric
            MetricContainer(
                value = weightMetric.value.toString(),
                label = unitName
            )
            // card to hold weight metric note if applicable
            if (weightMetric.note.isNotBlank()) {
                Text(
                    text = weightMetric.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.75f
                    ),
                    textAlign = TextAlign.Right
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // determine whether to format for reps or time
            if (metricType == "reps") {
                MetricContainer(
                    value = secondMetric.value.toString(),
                    label = metricType
                )
            } else {
                val timeTriple: Triple<Int, Int, Double>
                    = DateTimeCalculator.convertDoubleTimeToTripleTime(
                        minutes = secondMetric.value
                    )
                MetricContainer(
                    value = timeTriple.first.toString(),
                    label = "hours",
                    value2 = timeTriple.second.toString(),
                    label2 = "minutes",
                    value3 = timeTriple.third.toString(),
                    label3 = "seconds"
                )
            }
            // card to hold second metric note if applicable
            if (secondMetric.note.isNotBlank()) {
                Text(
                    text = secondMetric.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.75f
                    ),
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun MetricContainer(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    value2: String? = null,
    label2: String? = null,
    value3: String? = null,
    label3: String? = null
) {
    Card(
        colors = CardDefaults.cardColors().copy(
            contentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.5f
            )
        ),
        modifier = modifier
            .wrapContentSize()
    ) {
        // column to hold all values/labels
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // row to hold value and label 1
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (value2 != null) {
                    Modifier.padding(bottom = 8.dp)
                } else {
                    Modifier
                }
            ) {
                // value
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                // label
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        0.75f
                    )
                )
            }
            // rows to hold value and label 2 and 3 (if applicable)
            if (value2 != null && label2 != null && value3 != null && label3 != null) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // value 2
                    Text(
                        text = value2,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // label 2
                    Text(
                        text = label2,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            0.75f
                        )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // value 3
                    Text(
                        text = value3,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // label 3
                    Text(
                        text = label3,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(
                            0.75f
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LiftSetLabels(
    setLabel: String,
    setNote: String,
    muscleGroupName: String,
    sessionSetNumber: Int,
    muscleGroupSessionSetNumber: Int,
    noteColor: Color,
    borderColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    showSpacerToSeparateSetLabel: Boolean = true,
    boldSetNumbers: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // set label
        Text(
            text = setLabel,
            style = MaterialTheme.typography.titleMedium
        )
        // set note (if applicable)
        if (setNote.isNotBlank()) {
            Text(
                text = setNote,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = noteColor
            )
        }

        if (showSpacerToSeparateSetLabel) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // row to hold set # table header
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // set # table header
            Text(
                text = "Set #",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                color = noteColor
            )
        }

        // column to hold lift set #'s
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // session set #
            SetNumberRow(
                labelText = stringResource(R.string.session),
                valueText = "#${sessionSetNumber}",
                noteColor = noteColor,
                contentColor = contentColor,
                boldSetNumber = boldSetNumbers
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = borderColor
            )
            // muscle group set #
            SetNumberRow(
                labelText = muscleGroupName,
                valueText = "#${muscleGroupSessionSetNumber}",
                noteColor = noteColor,
                contentColor = contentColor,
                boldSetNumber = boldSetNumbers
            )
        }
    }
}
