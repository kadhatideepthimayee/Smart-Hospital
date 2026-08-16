package com.example.medplus.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medplus.ui.theme.*

/**
 * Shared UI constants and premium-styled components for the MedPlus authentication
 * and onboarding flows. These map to the project's base theme but provide the
 * specific naming convention and styling used by the presentational screens.
 */

// ---- Constants ----
val MedPlusCardRadius = 16.dp

// ---- Colors (Aliased to existing theme) ----
val MedPlusPrimary = Primary
val MedPlusAccent = PrimaryLight
val MedPlusScreenBackground = Background
val MedPlusBackground = Background
val MedPlusTextPrimary = PrimaryText
val MedPlusTextSecondary = SecondaryText
val MedPlusSuccess = Success
val MedPlusBorder = Outline

// ---- Components ----

@Composable
fun MedPlusIllustrationBadge(
    icon: ImageVector,
    contentDescription: String,
    size: Int = 120
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MedPlusPrimary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size((size * 0.5).dp),
            tint = MedPlusPrimary
        )
    }
}

@Composable
fun MedPlusPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(MedPlusCardRadius),
        colors = ButtonDefaults.buttonColors(containerColor = MedPlusPrimary),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MedPlusOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(MedPlusCardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, MedPlusBorder),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MedPlusPrimary
        )
    }
}

@Composable
fun MedPlusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = if (isError) Error else MedPlusPrimary) },
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(MedPlusCardRadius),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MedPlusPrimary,
                unfocusedBorderColor = MedPlusBorder,
                errorBorderColor = Error,
                focusedLabelColor = MedPlusPrimary,
                unfocusedLabelColor = MedPlusTextSecondary
            )
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
