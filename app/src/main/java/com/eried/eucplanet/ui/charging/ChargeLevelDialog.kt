package com.eried.eucplanet.ui.charging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.eried.eucplanet.R
import com.eried.eucplanet.service.ChargeAlertPolicy
import com.eried.eucplanet.ui.settings.NumberUpDown
import com.eried.eucplanet.ui.theme.appColors

/**
 * The rider's own charge alert: the level, and whether it is on.
 *
 * Save turns it on at the number shown, Off turns it off and keeps the
 * number for next time. Not dismissed by a tap outside, so a stray touch
 * does not drop a number half typed.
 */
@Composable
fun ChargeLevelDialog(
    initial: Int,
    on: Boolean,
    onSave: (Int) -> Unit,
    onOff: () -> Unit,
    onDismiss: () -> Unit,
) {
    var level by remember { mutableIntStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        shape = RoundedCornerShape(12.dp),
        title = { Text(stringResource(R.string.charging_notify_at)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberUpDown(
                    value = level,
                    onValueChange = { level = it },
                    range = ChargeAlertPolicy.CUSTOM_RANGE,
                    suffix = "%",
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(R.string.charging_notify_custom_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(level) }, shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row {
                if (on) {
                    TextButton(onClick = onOff, shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.state_off))
                    }
                }
                TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        },
    )
}
