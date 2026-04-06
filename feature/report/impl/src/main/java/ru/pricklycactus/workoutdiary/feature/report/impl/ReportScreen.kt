package ru.pricklycactus.workoutdiary.feature.report.impl

import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.collectLatest
import ru.pricklycactus.workoutdiary.feature.common.Dimensions
import ru.pricklycactus.workoutdiary.feature.report.api.DatePickerTarget
import ru.pricklycactus.workoutdiary.feature.report.api.ReportEffect
import ru.pricklycactus.workoutdiary.feature.report.api.ReportIntent
import ru.pricklycactus.workoutdiary.feature.report.api.ReportPeriod
import ru.pricklycactus.workoutdiary.feature.report.api.ReportRow
import ru.pricklycactus.workoutdiary.feature.report.api.ReportStore
import ru.pricklycactus.workoutdiary.feature.report.api.ReportTable
import ru.pricklycactus.workoutdiary.feature.report.api.ReportViewState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    state: ReportViewState,
    store: ReportStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(store) {
        store.effect.collectLatest { effect ->
            when (effect) {
                is ReportEffect.OpenDatePicker -> {
                    val calendar = Calendar.getInstance().apply { timeInMillis = effect.initialDateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selected = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }.timeInMillis
                            store.dispatch(
                                when (effect.target) {
                                    DatePickerTarget.START -> ReportIntent.UpdateCustomStartDate(selected)
                                    DatePickerTarget.END -> ReportIntent.UpdateCustomEndDate(selected)
                                }
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                is ReportEffect.ShareReport -> {
                    val bitmap = createReportBitmap(context, effect.report)
                    val reportName = reportFileName(effect.periodLabel)
                    val cacheUri = saveBitmapToCache(context, bitmap, reportName)
                    saveBitmapToDownloads(context, bitmap, reportName)
                    shareReport(context, cacheUri)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text(text = stringResource(R.string.report_title), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.report_hide)) }
            Button(
                onClick = { store.dispatch(ReportIntent.ShareReport) },
                modifier = Modifier.weight(1f),
                enabled = state.report.hasData
            ) {
                Text(stringResource(R.string.report_share))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)
                ) {
                    ReportPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = state.selectedReportPeriod == period,
                            onClick = { store.dispatch(ReportIntent.SelectReportPeriod(period)) },
                            label = { Text(text = stringResource(period.labelResId())) }
                        )
                    }
                }
            }

            if (state.selectedReportPeriod == ReportPeriod.CUSTOM) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)) {
                        Button(onClick = { store.dispatch(ReportIntent.PickCustomStartDate) }) {
                            Text(
                                stringResource(
                                    R.string.report_start_date,
                                    state.customStartDate?.let(::formatPickerDate) ?: stringResource(R.string.report_select_date)
                                )
                            )
                        }
                        Button(onClick = { store.dispatch(ReportIntent.PickCustomEndDate) }) {
                            Text(
                                stringResource(
                                    R.string.report_end_date,
                                    state.customEndDate?.let(::formatPickerDate) ?: stringResource(R.string.report_select_date)
                                )
                            )
                        }
                    }
                }
            }

            item {
                if (state.report.hasData) {
                    ReportTableView(state.report)
                } else {
                    Text(text = stringResource(R.string.report_empty), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable private fun ReportTableView(report: ReportTable) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(Dimensions.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)) {
                TableCell(
                    text = stringResource(R.string.report_exercise_header),
                    width = exerciseWidth,
                    isHeader = true
                )
                report.columns.forEach { TableCell(text = it.label, width = dateWidth, isHeader = true) }
            }
            report.rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)) {
                    TableCell(text = row.exerciseName, width = exerciseWidth)
                    row.values.forEach { TableCell(text = it, width = dateWidth) }
                }
            }
        }
    }
}

@Composable private fun TableCell(text: String, width: Dp, isHeader: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = if (isHeader) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
    )
}

private fun ReportPeriod.labelResId(): Int = when (this) {
    ReportPeriod.WEEK -> R.string.report_period_week
    ReportPeriod.MONTH -> R.string.report_period_month
    ReportPeriod.THREE_MONTHS -> R.string.report_period_three_months
    ReportPeriod.HALF_YEAR -> R.string.report_period_half_year
    ReportPeriod.CUSTOM -> R.string.report_period_custom
}

private fun formatPickerDate(value: Long): String = SimpleDateFormat(
    "dd.MM.yyyy",
    Locale.getDefault()
).format(Date(value))

private val exerciseWidth = 160.dp
private val dateWidth = 72.dp

private fun createReportBitmap(context: android.content.Context, report: ReportTable): Bitmap {
    val density = context.resources.displayMetrics.density
    val padding = (16 * density).toInt()
    val rowHeight = (40 * density).toInt()
    val exerciseWidthPx = (160 * density).toInt()
    val dateWidthPx = (88 * density).toInt()
    val headerHeight = (48 * density).toInt()
    val width = padding * 2 + exerciseWidthPx + report.columns.size * dateWidthPx
    val height = padding * 2 + headerHeight + maxOf(report.rows.size, 1) * rowHeight
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val headerPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        color = Color.BLACK
        textSize = 14 * density
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 13 * density
    }
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 1 * density
    }
    var x = padding.toFloat()
    val headerBaseline = padding + (headerHeight * 0.65f)
    canvas.drawText(context.getString(R.string.report_exercise_header), x, headerBaseline, headerPaint)
    x += exerciseWidthPx
    report.columns.forEach {
        canvas.drawText(it.label, x + 8 * density, headerBaseline, headerPaint)
        x += dateWidthPx
    }
    var y = padding + headerHeight
    canvas.drawLine(padding.toFloat(), y.toFloat(), (width - padding).toFloat(), y.toFloat(), linePaint)
    report.rows.ifEmpty { listOf(ReportRow("-", emptyList())) }.forEach { row ->
        y += rowHeight
        var cellX = padding.toFloat()
        val baseline = y - rowHeight * 0.35f
        canvas.drawText(row.exerciseName, cellX, baseline, textPaint)
        cellX += exerciseWidthPx
        row.values.forEach { value ->
            canvas.drawText(
                value,
                cellX + 8 * density,
                baseline,
                textPaint
            )
            cellX += dateWidthPx
        }
        canvas.drawLine(padding.toFloat(), y.toFloat(), (width - padding).toFloat(), y.toFloat(), linePaint)
    }
    return bitmap
}

private fun saveBitmapToCache(context: android.content.Context, bitmap: Bitmap, fileName: String): Uri {
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun saveBitmapToDownloads(context: android.content.Context, bitmap: Bitmap, fileName: String) {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(
                    uri
                )?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            FileOutputStream(File(downloadsDir, fileName)).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        Toast.makeText(context, context.getString(R.string.report_saved_to_downloads), Toast.LENGTH_SHORT).show()
    }
}

private fun shareReport(context: android.content.Context, reportUri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, reportUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(intent, context.getString(R.string.report_share))
    val excludedComponents = context.packageManager
        .queryIntentActivities(intent, 0)
        .mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName ?: return@mapNotNull null
            if (packageName.contains(bluetoothPackageMarker, ignoreCase = true)) {
                ComponentName(packageName, activityInfo.name)
            } else {
                null
            }
        }
        .toTypedArray()

    if (excludedComponents.isNotEmpty()) {
        chooserIntent.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponents)
    }

    context.startActivity(chooserIntent)
}

private fun reportFileName(periodLabel: String): String = "workout_report_${periodLabel}_${System.currentTimeMillis()}.png"

private const val bluetoothPackageMarker = "bluetooth"
