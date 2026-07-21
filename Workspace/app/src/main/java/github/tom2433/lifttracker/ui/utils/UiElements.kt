package github.tom2433.lifttracker.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.structures.LiftSetCountPerMuscleGroup

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
    setCountPerMuscleGroupList: List<LiftSetCountPerMuscleGroup>
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
        modifier = Modifier.padding(bottom = 8.dp)
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
    modifier: Modifier = Modifier
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
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                alpha = 0.75f
            ),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // value
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
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


