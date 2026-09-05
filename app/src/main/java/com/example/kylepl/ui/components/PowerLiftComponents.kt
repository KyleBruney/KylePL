package com.example.kylepl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kylepl.ui.theme.PodiumBronze
import com.example.kylepl.ui.theme.PodiumGold
import com.example.kylepl.ui.theme.PodiumSilver

/** Bold section title used consistently at the top of a list group across every screen. */
@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/**
 * Big gradient hero card for a screen's headline metric (competition total,
 * best lift, etc). [content] can add extra rows below the value.
 */
@Composable
fun HeroCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    caption: String? = null,
    centered: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
                    ),
                )
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            if (eyebrow != null) {
                Text(
                    eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            )
            Text(
                value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            content()
        }
    }
}

/** Small labeled metric tile, used for rows of related stats (e.g. Squat / Bench / Deadlift). */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Circular place-in-competition marker: gold/silver/bronze for the podium, neutral otherwise. */
@Composable
fun PlaceBadge(place: String, modifier: Modifier = Modifier) {
    val trimmed = place.trim()
    val background = when (trimmed) {
        "1" -> PodiumGold
        "2" -> PodiumSilver
        "3" -> PodiumBronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val onBackground = when (trimmed) {
        "1", "2", "3" -> Color.Black
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            trimmed.ifBlank { "–" },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = onBackground,
        )
    }
}
