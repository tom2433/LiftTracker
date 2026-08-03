package github.tom2433.lifttracker.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import github.tom2433.lifttracker.R
import github.tom2433.lifttracker.data.musclegroup.MuscleGroup
import github.tom2433.lifttracker.data.liftunit.LiftUnit
import kotlinx.coroutines.delay

@Composable
fun ShowElementEntryDialog(
    dialogTitle: String,
    submitBtnText: String,
    elementNameInputLabel: String,
    elementNoteInputLabel: String,
    buttonEnabled: Boolean,
    newElementName: String,
    newElementNote: String,
    onElementNameValueChanged: (String) -> Unit,
    onElementNoteValueChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // focus requester to pop up the keyboard when the user selects to edit or add an element
    val elementNameFocusRequester = remember { FocusRequester() }
    val elementNoteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(100)
        elementNameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // name input
                    TextField(
                        value = newElementName,
                        onValueChange = onElementNameValueChanged,
                        label = {
                            Text(elementNameInputLabel)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = elementNameInputLabel
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                elementNoteFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(elementNameFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // note input
                    TextField(
                        value = newElementNote,
                        onValueChange = onElementNoteValueChanged,
                        label = {
                            Text(elementNoteInputLabel)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = elementNoteInputLabel
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (buttonEnabled) {
                                    onSubmit()
                                }
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(elementNoteFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
                }

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to submit element
                Button(
                    onClick = onSubmit,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(submitBtnText)
                }

                // button to dismiss dialog
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

/**
 * Prompt user for LiftSet name, LiftSet note, weightMetric value, weightMetric note, secondMetric
 * value, secondMetric note
 */
@Composable
fun ShowHistoricalSetEditDialog(
    dialogTitle: String,
    unitName: String,
    metricType: String,
    newSetName: String,
    newSetNote: String,
    newSetWeightValue: String,
    newSetWeightNote: String,
    newSetSecondMetricNote: String,
    submitBtnText: String,
    buttonEnabled: Boolean,
    onSetNameValueChanged: (String) -> Unit,
    onSetNoteValueChanged: (String) -> Unit,
    onSetWeightValueChanged: (String) -> Unit,
    onSetWeightNoteValueChanged: (String) -> Unit,
    onSetSecondMetricNoteValueChanged: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    newSetRepsValue: String = "",
    newSetHoursValue: String = "",
    newSetMinutesValue: String = "",
    newSetSecondsValue: String = "",
    onSetRepsValueChanged: (String) -> Unit = {},
    onSetHoursValueChanged: (String) -> Unit = {},
    onSetMinutesValueChanged: (String) -> Unit = {},
    onSetSecondsValueChanged: (String) -> Unit = {}
) {
    val liftSetNameFocusRequester = remember { FocusRequester() }
    val liftSetNoteFocusRequester = remember { FocusRequester() }
    val weightMetricValueFocusRequester = remember { FocusRequester() }
    val weightMetricNoteFocusRequester = remember { FocusRequester() }
    val secondMetricValueFocusRequester = remember { FocusRequester() }
    val secondMetricValue2FocusRequester = remember { FocusRequester() }
    val secondMetricValue3FocusRequester = remember { FocusRequester() }
    val secondMetricNoteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(100)
        liftSetNameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // card to hold dialog content
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            // column to hold card contents
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // column to hold scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // title for set name and note
                    Text(
                        text = "Name and note for '${newSetName}'",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // text field for set name
                    TextField(
                        value = newSetName,
                        onValueChange = onSetNameValueChanged,
                        label = {
                            Text(stringResource(R.string.set_name))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = stringResource(R.string.set_name_input)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                liftSetNoteFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(liftSetNameFocusRequester)
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )

                    // text field for set note
                    TextField(
                        value = newSetNote,
                        onValueChange = onSetNoteValueChanged,
                        label = {
                            Text(stringResource(R.string.set_note))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = stringResource(R.string.set_note_input)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                weightMetricValueFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(liftSetNoteFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // title for weight metric value and note
                    Text(
                        text = "Value and note for $unitName",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // text field for weight value
                    TextField(
                        value = newSetWeightValue,
                        onValueChange = onSetWeightValueChanged,
                        label = {
                            Text(unitName)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = unitName
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                weightMetricNoteFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(weightMetricValueFocusRequester)
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )

                    // text field for weight note
                    TextField(
                        value = newSetWeightNote,
                        onValueChange = onSetWeightNoteValueChanged,
                        label = {
                            Text("Note for $unitName")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "$unitName note input"
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                secondMetricValueFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(weightMetricNoteFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // title for second metric value and note
                    Text(
                        text = "Value and note for $metricType",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (metricType == "reps") {
                        // text field for reps value
                        TextField(
                            value = newSetRepsValue,
                            onValueChange = onSetRepsValueChanged,
                            label = {
                                Text(metricType)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FitnessCenter,
                                    contentDescription = metricType
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    secondMetricNoteFocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(secondMetricValueFocusRequester)
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        // textfield for hours value
                        TextField(
                            value = newSetHoursValue,
                            onValueChange = onSetHoursValueChanged,
                            label = {
                                Text(stringResource(R.string.hours))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = stringResource(R.string.hours)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    secondMetricValue2FocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(secondMetricValueFocusRequester)
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )

                        // textfield for minutes value
                        TextField(
                            value = newSetMinutesValue,
                            onValueChange = onSetMinutesValueChanged,
                            label = {
                                Text(stringResource(R.string.minutes))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = stringResource(R.string.minutes)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    secondMetricValue3FocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(secondMetricValue2FocusRequester)
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )

                        // textfield for seconds value
                        TextField(
                            value = newSetSecondsValue,
                            onValueChange = onSetSecondsValueChanged,
                            label = {
                                Text(stringResource(R.string.seconds))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = stringResource(R.string.seconds)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    secondMetricNoteFocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(secondMetricValue3FocusRequester)
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        )
                    }

                    // textfield for second metric note
                    TextField(
                        value = newSetSecondMetricNote,
                        onValueChange = onSetSecondMetricNoteValueChanged,
                        label = {
                            Text("Note for $metricType")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "$metricType note input"
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (buttonEnabled) {
                                    onSubmit()
                                }
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(secondMetricNoteFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
                }

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to submit element
                Button(
                    onClick = onSubmit,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(submitBtnText)
                }

                // button to dismiss dialog
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun ShowMetricEntryDialog(
    dialogTitle: String,
    submitBtnText: String,
    metricInputLabel: String,
    buttonEnabled: Boolean,
    newMetric: String,
    onMetricValueChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // focus requester to pop up the keyboard when the user selects to edit a metric
    val metricFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(100)
        metricFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // metric input
                    TextField(
                        value = newMetric,
                        onValueChange = onMetricValueChanged,
                        label = {
                            Text(metricInputLabel)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = metricInputLabel
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (buttonEnabled) {
                                    onSubmit()
                                }
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(metricFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // button to submit metric
                    Button(
                        onClick = onSubmit,
                        enabled = buttonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(submitBtnText)
                    }

                    // button to dismiss dialog
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeDialog(
    buttonEnabled: Boolean,
    newProfileName: String,
    newProfileNote: String,
    onProfileNameValueChanged: (String) -> Unit,
    onProfileNoteValueChanged: (String) -> Unit,
    onCreateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // welcome title
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // profile name input
                TextField(
                    value = newProfileName,
                    onValueChange = onProfileNameValueChanged,
                    label = {
                        Text(stringResource(R.string.profile_name_input_label))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = stringResource(R.string.profile_name_input_label)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                // profile note input
                TextField(
                    value = newProfileNote,
                    onValueChange = onProfileNoteValueChanged,
                    label = {
                        Text(stringResource(R.string.profile_note_input_label))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = stringResource(R.string.profile_note_input_label)
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // welcome description (tell user to create one profile)
                Text(
                    text = stringResource(R.string.welcome_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to create profile
                Button(
                    onClick = onCreateProfile,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(R.string.create_profile_btn_text))
                }
            }
        }
    }
}

@Composable
fun ShowElementDeleteDialog(
    dialogTitle: String,
    warningDescription: String,
    deleteBtnText: String,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // warning description
                    Text(
                        text = warningDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to delete
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = deleteBtnText,
                        textAlign = TextAlign.Center
                    )
                }

                // button to dismiss
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.cancel_profile_deletion_btn_text)
                    )
                }
            }
        }
    }
}


@Composable
fun ShowLiftEntryDialog(
    dialogTitle: String,
    submitBtnText: String,
    buttonEnabled: Boolean,
    newLiftName: String,
    newLiftNote: String,
    newLiftUnitName: String,
    onLiftNameValueChanged: (String) -> Unit,
    onLiftNoteValueChanged: (String) -> Unit,
    repsSelected: Boolean,
    onRepsSelected: () -> Unit,
    timeSelected: Boolean,
    onTimeSelected: () -> Unit,
    liftUnitList: List<LiftUnit>,
    onLiftUnitValueChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // focus requester to pop up the keyboard when the user selects to edit or add an element
    val elementNameFocusRequester = remember { FocusRequester() }
    val elementNoteFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(100)
        elementNameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // name input
                    TextField(
                        value = newLiftName,
                        onValueChange = onLiftNameValueChanged,
                        label = {
                            Text(stringResource(R.string.lift_name))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = stringResource(R.string.lift_name)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                elementNoteFocusRequester.requestFocus()
                            }
                        ),
                        modifier = Modifier
                            .focusRequester(elementNameFocusRequester)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // note input
                    TextField(
                        value = newLiftNote,
                        onValueChange = onLiftNoteValueChanged,
                        label = {
                            Text(stringResource(R.string.lift_note_optional))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = stringResource(R.string.lift_note_optional)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .focusRequester(elementNoteFocusRequester)
                            .padding(bottom = 32.dp)
                            .fillMaxWidth()
                    )

                    // select metric type label and info button
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // label for selecting metric type
                        Text(
                            text = stringResource(R.string.metric_type),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // separate label and info button
                        Spacer(modifier = Modifier.weight(1f))
                        // info button
                        InfoButton {
                            Text(
                                text = stringResource(R.string.select_metric_type_sublabel)
                            )
                        }
                    }
                    // radiobutton for reps option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = repsSelected,
                                onClick = onRepsSelected,
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = repsSelected,
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.reps),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // radiobutton for time option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = timeSelected,
                                onClick = onTimeSelected,
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = timeSelected,
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.time),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // select lift unit label and info button
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // label for selecting lift unit
                        Text(
                            text = stringResource(R.string.unit),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // separate label and info button
                        Spacer(modifier = Modifier.weight(1f))
                        // info button
                        InfoButton {
                            Text(
                                text = stringResource(R.string.choose_unit_sublabel)
                            )
                        }
                    }

                    // quick-add buttons for previous lift units
                    if (liftUnitList.isNotEmpty()) {
                        // quick add buttons in flow row to wrap multiple lines if needed
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (liftUnit in liftUnitList) {
                                OutlinedButton(
                                    onClick = {
                                        onLiftUnitValueChanged(liftUnit.name)
                                    }
                                ) {
                                    Text(
                                        text = liftUnit.name
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lift unit text input
                    TextField(
                        value = newLiftUnitName,
                        onValueChange = onLiftUnitValueChanged,
                        label = {
                            Text(stringResource(R.string.unit))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Scale,
                                contentDescription = stringResource(R.string.unit)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (buttonEnabled) {
                                    onSubmit()
                                }
                            }
                        ),
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .fillMaxWidth()
                    )
                }

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // button to submit element
                Button(
                    onClick = onSubmit,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(submitBtnText)
                }

                // button to dismiss dialog
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun ShowMuscleGroupSelectionDialog(
    dialogTitle: String,
    onDismissRequest: () -> Unit,
    muscleGroupList: List<MuscleGroup>,
    onMuscleGroupSelected: (MuscleGroup) -> Unit,
    selectedMuscleGroup: MuscleGroup?,
    onSubmit: () -> Unit,
    buttonEnabled: Boolean,
    submitBtnText: String,
    switchChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cascadeSwitchChecked: Boolean,
    onCascadeCheckedChange: (Boolean) -> Unit,
    cascadeSwitchEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            // column to hold card contents
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    // title
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    // divider
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                    // list of muscle group radiobuttons
                    for (muscleGroup in muscleGroupList) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (selectedMuscleGroup?.id ?: -1) == muscleGroup.id,
                                    onClick = { onMuscleGroupSelected(muscleGroup) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedMuscleGroup?.id ?: -1) == muscleGroup.id,
                                onClick = null
                            )
                            Text(
                                text = muscleGroup.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                }

                // divider
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

                // row to hold migrate old data switch
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    // row to hold info button and label
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.migrate_old_set_data))
                        InfoButton {
                            Text(
                                text = stringResource(R.string.migrate_old_set_data_dialog_text),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // migrate old data switch
                    Switch(
                        checked = switchChecked,
                        onCheckedChange = onCheckedChange,
                    )
                }

                // row to hold cascade migration switch and label
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    // row to hold info button and label
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cascade migration")
                        InfoButton {
                            Text(
                                text = stringResource(R.string.cascade_old_set_data_dialog_text_1),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.cascade_old_set_data_dialog_text_2),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = stringResource(R.string.cascade_old_set_data_dialog_text_3),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // cascade old set data switch
                    Switch(
                        checked = cascadeSwitchChecked,
                        onCheckedChange = onCascadeCheckedChange,
                        enabled = cascadeSwitchEnabled
                    )
                }

                // button to submit element
                Button(
                    onClick = onSubmit,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(submitBtnText)
                }

                // button to dismiss dialog
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@Composable
fun BasicDialog(
    onDismissRequest: () -> Unit,
    dialogContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .wrapContentSize()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // dialog content
                dialogContent()

                // confirm button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ok"
                    )
                }
            }
        }
    }
}

@Composable
fun InfoButton(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showDialog = true
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = stringResource(R.string.info),
            tint = MaterialTheme.colorScheme.outline
        )
    }

    if (showDialog) {
        BasicDialog(
            onDismissRequest = {
                @Suppress("AssignedValueIsNeverRead")
                showDialog = false
            },
            dialogContent = content
        )
    }
}

@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = stringResource(R.string.select_date_range)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}
