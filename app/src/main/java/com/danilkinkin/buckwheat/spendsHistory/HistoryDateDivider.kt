package com.danilkinkin.buckwheat.spendsHistory

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.prettyDate
import java.util.*

@Composable
fun HistoryDateDivider(date: Date) {
    Text(
        text = prettyDate(date, forceShowDate = true, showTime = false),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 16.dp)
    )
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        HistoryDateDivider(Date())
    }
}