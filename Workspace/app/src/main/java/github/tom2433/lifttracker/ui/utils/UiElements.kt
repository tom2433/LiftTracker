package github.tom2433.lifttracker.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.liftset.LiftSet
import github.tom2433.lifttracker.data.setmetric.SetMetric
import github.tom2433.lifttracker.data.structures.LiftSearchDetail
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup
import github.tom2433.lifttracker.data.structures.LiftSummary
import github.tom2433.lifttracker.data.structures.SessionDataPoint
import github.tom2433.lifttracker.data.structures.SessionSummary
import github.tom2433.lifttracker.data.structures.SetCardData
import github.tom2433.lifttracker.data.utils.DateTimeCalculator
import github.tom2433.lifttracker.ui.viewModels.TimeFrameType
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

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
    liftSummary: LiftSummary?,
    displaySetList: List<Pair<Int, List<Int>>>,
    liftDetailMap: Map<Int, LiftSearchDetail>,
    liftSetMap: Map<Int, SetCardData>,
    noteColor: Color,
    onClickLiftCard: (Int) -> Unit,
    onLongClickHistoricalSetSection: (Int) -> Unit,
    onClickHistoricalSetSection: (Int) -> Unit,
    onClickEditHistoricalSet: (Int) -> Unit,
    onClickMoveHistoricalSetUp: (Int) -> Unit,
    onClickMoveHistoricalSetDown: (Int) -> Unit,
    onClickDeleteHistoricalSet: (Int) -> Unit,
    onClickExpandAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier.fillMaxWidth()
    ) {
        // row to hold expand all button (expands all historical lift sections)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Expand all",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clickable(
                        onClick = onClickExpandAll
                    )
            )
        }

        // loop thru each pair<Lift id, list of LiftSet ids>
        for ((index, liftAndSetsPair) in displaySetList.withIndex()) {
            // retrieve the LiftSearchDetail object from liftDetailMap
            val liftDetail: LiftSearchDetail = liftDetailMap[liftAndSetsPair.first] ?: continue
            val liftSetIds: List<Int> = liftAndSetsPair.second
            key(liftDetail.liftObj.id, liftSetIds.firstOrNull()) {
                HistoricalLiftCard(
                    liftDetail = liftDetail,
                    liftSummary = liftSummary,
                    noteColor = noteColor,
                    liftSetIds = liftSetIds,
                    liftSetMap = liftSetMap,
                    onClickLiftCard = onClickLiftCard,
                    onLongClickHistoricalSetSection = { onLongClickHistoricalSetSection(it) },
                    onClickHistoricalSetSection = { onClickHistoricalSetSection(it) },
                    onClickMoveHistoricalSetDown = { onClickMoveHistoricalSetDown(it) },
                    onClickMoveHistoricalSetUp = { onClickMoveHistoricalSetUp(it) },
                    onClickEditHistoricalSet = { onClickEditHistoricalSet(it) },
                    onClickDeleteHistoricalSet = { onClickDeleteHistoricalSet(it) }
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
    liftSummary: LiftSummary?,
    noteColor: Color,
    liftSetIds: List<Int>,
    liftSetMap: Map<Int, SetCardData>,
    onClickLiftCard: (Int) -> Unit,
    onLongClickHistoricalSetSection: (Int) -> Unit,
    onClickHistoricalSetSection: (Int) -> Unit,
    onClickEditHistoricalSet: (Int) -> Unit,
    onClickMoveHistoricalSetUp: (Int) -> Unit,
    onClickMoveHistoricalSetDown: (Int) -> Unit,
    onClickDeleteHistoricalSet: (Int) -> Unit,
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
                // display button to show/hide lift analysis
                LiftAnalysisWindowButton(
                    liftSummary = liftSummary
                )

                // loop thru each set and display its section
                loop@ for ((index, liftSetId) in liftSetIds.withIndex()) {
                    key(liftSetId) {
                        HistoricalSetSection(
                            liftSet = liftSetMap[liftSetId]?.liftSet ?: continue@loop,
                            weightMetric = liftSetMap[liftSetId]?.weightMetric ?: continue@loop,
                            secondMetric = liftSetMap[liftSetId]?.secondMetric ?: continue@loop,
                            muscleGroupName = liftDetail.muscleGroupName,
                            unitName = liftDetail.unitName,
                            metricType = liftDetail.metricType,
                            selected = liftSetMap[liftSetId]?.selected ?: continue@loop,
                            onLongClickHistoricalSetSection = {
                                onLongClickHistoricalSetSection(
                                    liftSetId
                                )
                            },
                            onClickHistoricalSetSection = { onClickHistoricalSetSection(liftSetId) },
                            onClickEditHistoricalSet = { onClickEditHistoricalSet(liftSetId) },
                            onClickMoveHistoricalSetUp = {
                                onClickMoveHistoricalSetUp(liftSetId)
                            },
                            onClickMoveHistoricalSetDown = {
                                onClickMoveHistoricalSetDown(liftSetId)
                            },
                            onClickDeleteHistoricalSet = {
                                onClickDeleteHistoricalSet(liftSetId)
                            },
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
}

@Composable
fun LiftAnalysisWindowButton(
    liftSummary: LiftSummary?,
    modifier: Modifier = Modifier
) {
    var showLiftSummary by remember { mutableStateOf(false) }
    val animatedPadding by animateDpAsState(
        if (showLiftSummary) {
            16.dp
        } else {
            0.dp
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedContainerColor by animateColorAsState(
        targetValue =
            if (showLiftSummary) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.background
            },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedContentColor by animateColorAsState(
        targetValue =
            if (showLiftSummary) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val animatedCardElevation by animateDpAsState(
        targetValue =
            if (showLiftSummary) {
                12.dp
            } else {
                0.dp
            },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )

    // Card, start with invisible borders and zero padding,
    // increase padding and show borders when button is clicked
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            contentColor = animatedContentColor,
            containerColor = animatedContainerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedCardElevation,
            pressedElevation = animatedCardElevation,
            focusedElevation = animatedCardElevation,
            hoveredElevation = animatedCardElevation,
            draggedElevation = animatedCardElevation,
            disabledElevation = animatedCardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(animatedPadding),
        ) {
            // outined button to press and view lift summary
            OutlinedButton(
                onClick = {
                    showLiftSummary = !showLiftSummary
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text =
                        if (showLiftSummary) {
                            "Hide Lift Analysis"
                        } else {
                            "Lift Analysis"
                        }
                )
            }

            // animated visibility to hold lift summary
            AnimatedVisibility(
                visible = showLiftSummary,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut(tween(300))
            ) {
                DisplayLiftSummary(
                    liftSummary = liftSummary,
                    modifier = Modifier.padding(top =
                        if (animatedPadding > 4.dp) {
                            animatedPadding - 4.dp
                        } else  {
                            0.dp
                        }
                    )
                )
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
    selected: Boolean,
    onLongClickHistoricalSetSection: () -> Unit,
    onClickHistoricalSetSection: () -> Unit,
    onClickEditHistoricalSet: () -> Unit,
    onClickMoveHistoricalSetDown: () -> Unit,
    onClickMoveHistoricalSetUp: () -> Unit,
    onClickDeleteHistoricalSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    // column to hold all lift set details and set metric details on top, expandable edit button on
    // the bottom
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // row to hold lift set details on the left, set metric details on the right
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = onLongClickHistoricalSetSection,
                    onClick = onClickHistoricalSetSection
                )
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
                    label = unitName,
                    isWeight = true
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
                    val timeTriple: Triple<Int, Int, Double> =
                        DateTimeCalculator.convertDoubleTimeToTripleTime(
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

        // animated visibility for the move buttons, edit button, and delete button
        AnimatedVisibility(
            visible = selected,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeIn(tween(300)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        ) {
            // column to hold all buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    )
            ) {
                // row to hold move buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // up button (down in number)
                    Card(
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        onClick = onClickMoveHistoricalSetDown,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        // box to hold up icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Move set up"
                            )
                        }
                    }

                    // down button (up in number)
                    Card(
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        onClick = onClickMoveHistoricalSetUp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    ) {
                        // box to hold down icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Move set down"
                            )
                        }
                    }
                }
                // edit button (card)
                Card(
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(0.75f),
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(4.dp),
                    onClick = onClickEditHistoricalSet,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // box to hold edit icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_historical_set)
                        )
                    }
                }

                // delete button (card)
                Card(
                    colors = CardDefaults.cardColors().copy(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(4.dp),
                    onClick = onClickDeleteHistoricalSet,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Box to hold delete icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete historical set"
                        )
                    }
                }
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
    label3: String? = null,
    isWeight: Boolean = false
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
                    fontSize =
                        if (isWeight) {
                            24.sp
                        } else {
                            20.sp
                        },
                    color =
                        if (isWeight) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
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
                        color = MaterialTheme.colorScheme.tertiaryContainer
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
                        color = MaterialTheme.colorScheme.tertiaryContainer
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

@Composable
fun SlideAndExpandSection(
    borderColor: Color,
    cardContentColor: Color,
    cardContainerColor: Color,
    stage1Visible: Boolean,
    stage2Visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // column to hold all contents
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End,
    ) {
        // animated visibility for top divider
        AnimatedVisibility(
            visible = stage1Visible,
            enter = expandHorizontally(
                expandFrom = Alignment.End,
                animationSpec = tween(150)
            ),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End,
                animationSpec = tween(150)
            )
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = borderColor
            )
        }

        // row to hold vertical dividers and interior content
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // animated visibility for the left vertical divider
            AnimatedVisibility(
                visible = stage2Visible,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                )
            ) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = borderColor
                )
            }

            // animated visibility for the interior content
            AnimatedVisibility(
                visible = stage2Visible,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeOut(tween(300)),
                modifier = Modifier.weight(1f)
            ) {
                // card to hold interior content
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors().copy(
                        contentColor = cardContentColor,
                        containerColor = cardContainerColor
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    // column to hold card content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 16.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 0.dp
                            ),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        content()
                    }
                }
            }

            // animatedVisibility for the right vertical divider
            AnimatedVisibility(
                visible = stage2Visible,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                )
            ) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = borderColor
                )
            }
        }

        // animated visibility for the bottom divider
        AnimatedVisibility(
            visible = stage2Visible,
            enter = expandHorizontally(
                expandFrom = Alignment.End,
                animationSpec = tween(300)
            ),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End,
                animationSpec = tween(300)
            )
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = borderColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelAndDropdownRow(
    labelText: String,
    borderColor: Color,
    selectedItem: Pair<String, Int>,
    itemList: List<Pair<String, Int>>,
    showLoadMoreItem: Boolean,
    dropdownExpanded: Boolean,
    defaultElementLabel: String,
    anyCount: Int,
    filterApplied: (Pair<String, Int>) -> Unit,
    filterRemoved: () -> Unit,
    loadMore: () -> Unit,
    onClickDropdown: () -> Unit,
    dismissDropdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    // row to hold session name and its dropdown options
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // dropdown label
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 16.dp)
        )
        // dropdown menu
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = {},
            modifier = Modifier.weight(1f)
        ) {
            // row to hold selector
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        onClick = onClickDropdown
                    )
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                // label
                Text(
                    text = "${selectedItem.first} (${selectedItem.second})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 12.dp,
                            end = 4.dp
                        )
                        .weight(1f)
                )
                // dropdown icon
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select 'Has session name:'",
                    tint = borderColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // dropdown menu
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = dismissDropdown
            ) {
                // show default option
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$defaultElementLabel (${anyCount})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = { filterRemoved() }
                )

                itemList.forEach { itemNameAndNumber ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${itemNameAndNumber.first} (${itemNameAndNumber.second})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = { filterApplied(itemNameAndNumber) }
                    )
                }

                if (showLoadMoreItem) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Load More",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { loadMore() }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DisplaySessionAnalytics(
    trimmedSessionName: String,
    sessionSummary: SessionSummary,
    statDisplayFilterMap: Map<TimeFrameType, Boolean>,
    filterChipClicked: (TimeFrameType) -> Unit,
    modifier: Modifier = Modifier
) {
    var analyticsVisible by remember { mutableStateOf(false) }
    val borderColor = animateColorAsState(
        targetValue = if (analyticsVisible) {
            MaterialTheme.colorScheme.onBackground.copy(0.3f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )
    val colPadding = animateDpAsState(
        targetValue = if (analyticsVisible) {
            12.dp
        } else {
            0.dp
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow
        )
    )

    // card to hold full analytics section
    Card(
        colors = CardDefaults.cardColors().copy(
            contentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor.value
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // column to hold card contents
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(colPadding.value)
        ) {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    analyticsVisible = !analyticsVisible
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (analyticsVisible) {
                        "Hide Analysis"
                    } else {
                        "Analysis"
                    }
                )
            }

            // animate the visible state of the entire analytics section (controlled by button above)
            AnimatedVisibility(
                visible = analyticsVisible,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut(tween(300))
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // label before filter chips
                    Text(
                        text = "Compare this session to other $trimmedSessionName workouts from:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.75f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // filter chips for timeframe
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        for ((sessionDataTimeFrameOption, selected) in statDisplayFilterMap) {
                            CustomFilterChip(
                                label = sessionDataTimeFrameOption.label,
                                onClick = { filterChipClicked(sessionDataTimeFrameOption) },
                                selected = selected,
                                modifier = Modifier
                                    .padding(
                                        end = 8.dp
                                    )
                            )
                        }
                    }

                    // row to hold summary title and info button
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // summary title
                        Text(
                            text = "Session Summary",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        // info button
                        InfoButton {
                            // explanation title
                            Text(
                                text = "How are session summaries generated?",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "All values in the session summary are calculated as average " +
                                    "deviations from the mean; this session's data is compared " +
                                    "to the average of the previous sessions with the same name " +
                                    "within the selected timeframe.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "Deviations are first calculated for each valid lift trained " +
                                    "in this session, given that it has been trained before for " +
                                    "this session name. These deviations are averaged across all " +
                                    "valid lifts to result in the values you see in the session " +
                                    "summary.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "Because some lifts may not be accounted for (meaning you " +
                                    "haven't trained them before for this session name), the " +
                                    "summary may not always be accurate.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // explanation
                            Text(
                                text = sessionSummary.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "How do I get an accurate session summary?",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "Session summaries compare this session to the sessions " +
                                    "before it, only counting sessions that have the same name. " +
                                    "These sessions are considered \"routines.\"",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "The types of lifts trained should be mostly consistent " +
                                    "across routines, but they don't all have to be in order to " +
                                    "generate a summary.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "Values like volume and intensity will be most accurate when " +
                                    "training mostly the same lifts with the same range of " +
                                    "motion, form, conditions, etc.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "What is volume per set?",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "Volume per set measures weight * reps, and it is a better " +
                                    "indicator of how much work you're doing during a given set. " +
                                    "It can provide a better measurement of progressive overload " +
                                    "given that you train your lifts with a consistent rep range " +
                                    "in the same conditions.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text =
                                    "For timed lifts, volume per set is replaced with weight per " +
                                    "minute, which provides a better measurement for the " +
                                    "intensity of your cardio sessions.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // first paragraph of summary
                    if (sessionSummary.paragraph1.isNotBlank()) {
                        Text(
                            text = sessionSummary.paragraph1,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )
                    }
                    // second paragraph of summary
                    if (sessionSummary.paragraph2.isNotBlank()) {
                        Text(
                            text = sessionSummary.paragraph2,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )
                    }

                    // only display the line graphs if they have data to begin with
                    if (sessionSummary.timedDataPoints.isNotEmpty() ||
                        sessionSummary.untimedDataPoints.isNotEmpty()) {
                        SessionSummaryGraphsContainer(
                            timedDataPoints = sessionSummary.timedDataPoints,
                            untimedDataPoints = sessionSummary.untimedDataPoints,
                            timedUnits = sessionSummary.timedUnits,
                            untimedUnits = sessionSummary.untimedUnits
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionSummaryGraphsContainer(
    timedDataPoints: List<SessionDataPoint>,
    untimedDataPoints: List<SessionDataPoint>,
    timedUnits: String,
    untimedUnits: String,
    modifier: Modifier = Modifier
) {
    // column to hold filter chips and line graphs
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        val weightLabel: String =
            if (timedDataPoints.isNotEmpty() && untimedDataPoints.isNotEmpty()) {
                "Avg. ${untimedUnits.capitalizeFirstChar()} Deviation/Avg. ${timedUnits.capitalizeFirstChar()} Deviation"
            } else if (timedDataPoints.isNotEmpty()) {
                "Avg. ${timedUnits.capitalizeFirstChar()} Deviation"
            } else if (untimedDataPoints.isNotEmpty()) {
                "Avg. ${untimedUnits.capitalizeFirstChar()} Deviation"
            } else {
                return
            }
        val repsOrTimeLabel: String =
            if (timedDataPoints.isNotEmpty() && untimedDataPoints.isNotEmpty()) {
                "Avg. Reps/Minutes Deviation"
            } else if (timedDataPoints.isNotEmpty()) {
                "Avg. Minutes Deviation"
            } else if (untimedDataPoints.isNotEmpty()) {
                "Avg. Reps Deviation"
            } else {
                return
            }
        val intensityLabel: String =
            if (timedDataPoints.isNotEmpty() && untimedDataPoints.isNotEmpty()) {
                "Avg. Volume Per Set Deviation/Avg. ${timedUnits.capitalizeFirstChar()} Per Minute Deviation"
            } else if (timedDataPoints.isNotEmpty()) {
                "Avg. ${timedUnits.capitalizeFirstChar()} Per Minute Deviation"
            } else {
                "Avg. Volume Per Set Deviation"
            }
        var weightSelected by remember { mutableStateOf(true) }
        var repsOrTimeSelected by remember { mutableStateOf(false) }
        var intensitySelected by remember { mutableStateOf(false) }

        // scrollable row for filter chips
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

        // call SessionSummaryLineGraph for timedDataPoints with arguments varying depending on the
        // user selected filter chip
        if (timedDataPoints.isNotEmpty()) {
            SessionSummaryLineGraph(
                title =
                    if (weightSelected) {
                        weightLabel
                    } else if (repsOrTimeSelected) {
                        repsOrTimeLabel
                    } else if (intensitySelected) {
                        intensityLabel
                    } else {
                        "null"
                    },
                dataPoints = timedDataPoints,
                selectedMetric =
                    if (weightSelected) {
                        SummaryChartMetric.WEIGHT
                    } else if (repsOrTimeSelected) {
                        SummaryChartMetric.REPS_OR_TIME
                    } else if (intensitySelected) {
                        SummaryChartMetric.INTENSITY
                    } else {
                        SummaryChartMetric.WEIGHT
                    },
                timed = true
            )
        }

        // same for untimedDataPoints
        if (untimedDataPoints.isNotEmpty()) {
            SessionSummaryLineGraph(
                title =
                    if (weightSelected) {
                        weightLabel
                    } else if (repsOrTimeSelected) {
                        repsOrTimeLabel
                    } else if (intensitySelected) {
                        intensityLabel
                    } else {
                        "null"
                    },
                dataPoints = untimedDataPoints,
                selectedMetric =
                    if (weightSelected) {
                        SummaryChartMetric.WEIGHT
                    } else if (repsOrTimeSelected) {
                        SummaryChartMetric.REPS_OR_TIME
                    } else if (intensitySelected) {
                        SummaryChartMetric.INTENSITY
                    } else {
                        SummaryChartMetric.WEIGHT
                    },
                timed = false
            )
        }
    }
}

@Composable
fun DisplayLiftSummary(
    liftSummary: LiftSummary?,
    modifier: Modifier = Modifier
) {
    // column to hold full lift summary
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        // title
        Text(
            text = if (liftSummary == null) {
                "Lift Summary"
            } else {
                "${liftSummary.liftName} Summary"
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // animated visibility for when the lift summary is null (tell user to select lift)
        AnimatedVisibility(
            visible = liftSummary == null,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            // column to hold lift summary placeholder
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // body
                Text(
                    text = "Select a lift individually to view its summary.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )
            }
        }

        // animated visibility for when the lift summary is not null (display the actual summary)
        AnimatedVisibility(
            visible = liftSummary != null,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            var placeHolderString = ""
            for (i in (1..200)) {
                placeHolderString += "_"
            }
            // column to hold lift summary
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // paragraph
                Text(
                    text = liftSummary?.paragraph ?: placeHolderString,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // show bar graphs
                LiftSummaryBarGraphs(
                    liftSummary = liftSummary,
                    valueColor = MaterialTheme.colorScheme.primaryContainer,
                    valueContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    historicalValueColor = MaterialTheme.colorScheme.tertiaryContainer,
                    historicalValueContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
