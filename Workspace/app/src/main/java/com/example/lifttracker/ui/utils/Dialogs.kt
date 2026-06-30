package com.example.lifttracker.ui.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lifttracker.R
import kotlinx.coroutines.delay

@Composable
fun ShowElementEntryDialog(
    @StringRes dialogTitle: Int,
    @StringRes submitBtnText: Int,
    @StringRes elementNameInputLabel: Int,
    @StringRes elementNoteInputLabel: Int,
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
                    text = stringResource(dialogTitle),
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
                        Text(stringResource(elementNameInputLabel))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = stringResource(elementNameInputLabel)
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
                        Text(stringResource(elementNoteInputLabel))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = stringResource(elementNoteInputLabel)
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
                    Text(stringResource(submitBtnText))
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
