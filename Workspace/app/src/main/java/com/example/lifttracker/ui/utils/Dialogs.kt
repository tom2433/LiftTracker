package com.example.lifttracker.ui.utils

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lifttracker.R
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
    @StringRes warningDescription: Int,
    @StringRes deleteBtnText: Int,
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
                    text = stringResource(warningDescription),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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
                        text = stringResource(deleteBtnText),
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
    unitList: List<com.example.lifttracker.data.Unit>,
    onUnitValueChanged: (String) -> Unit,
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
                    .padding(16.dp)
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
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .focusRequester(elementNoteFocusRequester)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth()
                )

                // select metric type label and info button
                Row (
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
                    InfoButton(
                        infoString = R.string.select_metric_type_sublabel
                    )
                }

//                // sub-label for selecting metric type
//                Text(
//                    text = stringResource(R.string.select_metric_type_sublabel),
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.outline,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )

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

                // select unit label and info button
                Row (
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // label for selecting unit
                    Text(
                        text = stringResource(R.string.unit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // separate label and info button
                    Spacer(modifier = Modifier.weight(1f))
                    // info button
                    InfoButton(
                        infoString = R.string.choose_unit_sublabel
                    )
                }

                // quick-add buttons for previous units
                if (unitList.isNotEmpty()) {
                    // quick add buttons in flow row to wrap multiple lines if needed
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (unit in unitList) {
                            OutlinedButton(
                                onClick = {
                                    onUnitValueChanged(unit.name)
                                }
                            ) {
                                Text(
                                    text = unit.name
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unit text input
                TextField(
                    value = newLiftUnitName,
                    onValueChange = onUnitValueChanged,
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
fun BasicDialog(
    dialogText: String,
    onDismissRequest: () -> Unit,
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
                // dialog text
                Text(
                    text = dialogText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
    @StringRes infoString: Int,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            showDialog = true
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = stringResource(R.string.info)
        )
    }

    if (showDialog) {
        BasicDialog(
            dialogText = stringResource(infoString),
            onDismissRequest = {
                showDialog = false
            }
        )
    }
}
