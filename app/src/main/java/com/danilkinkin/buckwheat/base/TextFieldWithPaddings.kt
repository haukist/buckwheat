package com.danilkinkin.buckwheat.base

import android.util.Log
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.danilkinkin.buckwheat.editor.calcAdaptiveFont
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import kotlinx.coroutines.CoroutineStart
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextFieldWithPaddings(
    value: String = "",
    onChangeValue: (output: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp),
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    currency: ExtendCurrency? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    val scrollState = rememberScrollState()
    var containerHeight by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }
    var inputWidth by remember { mutableStateOf(0) }
    var currencySymbolSize by remember(value) { mutableStateOf(Size(0, 0)) }
    var valueSize by remember(value) { mutableStateOf(Size(0, 0)) }
    var requestScrollToCursor by remember { mutableStateOf(false) }
    val layoutDirection = when (LocalConfiguration.current.layoutDirection) {
        0 -> LayoutDirection.Rtl
        1 -> LayoutDirection.Ltr
        else -> LayoutDirection.Rtl
    }

    var lastCursorPosition by remember { mutableStateOf(0) }
    var lastTextValue by remember(value) { mutableStateOf(value) }
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val localContext = LocalContext.current
    val gapStart =
        with(localDensity) { contentPadding.calculateStartPadding(layoutDirection).toPx().toInt() }
    val gapEnd =
        with(localDensity) { contentPadding.calculateEndPadding(layoutDirection).toPx().toInt() }

    val currSymbol = currency?.let { currency ->
        prettyCandyCanes(
            0.toBigDecimal(),
            currency,
            maximumFractionDigits = 0,
            minimumFractionDigits = 0,
        ).filter { it !='0' }
    }

    val fontSize = calcAdaptiveFont(
        height = containerHeight.toFloat(),
        width = (containerWidth - gapStart - gapEnd).toFloat(),
        maxFontSize = 80.sp,
        minFontSize = 40.sp,
        text = (currSymbol ?: "") + textFieldValue.text,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.W700,
        )
    )

    val textStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = fontSize,
        fontWeight = FontWeight.W700,
        color = colorOnEditor,
    )

    val currencyStyle = MaterialTheme.typography.displaySmall.copy(
        fontSize = textStyle.fontSize * 0.5f,
        fontWeight = FontWeight.W700,
        color = colorOnEditor,
    )

    currencySymbolSize = calculateIntrinsics(
        currSymbol ?: "",
        currencyStyle,
    )

    valueSize = calculateIntrinsics(
        visualTransformation.filter(
            AnnotatedString(value)
        ).text.text,
        textStyle,
    )

    fun restoreScrollPosition(forceEnd: Boolean = false) {
        val transformation = visualTransformation.filter(
            AnnotatedString(value)
        )

        val position = ParagraphIntrinsics(
            text = transformation.text.text.substring(
                0,
                transformation.offsetMapping.originalToTransformed(
                    if (forceEnd) value.length else textFieldValueState.selection.start,
                ).coerceAtMost(transformation.text.text.length),
            ),
            style = textStyle,
            density = localDensity,
            fontFamilyResolver = createFontFamilyResolver(localContext)
        ).maxIntrinsicWidth

        coroutineScope.launch {
            if (requestScrollToCursor || forceEnd) {
                if (position.toInt() - scrollState.value > containerWidth) {
                    scrollState.animateScrollTo(
                        position.toInt() - containerWidth + gapStart, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                } else if (valueSize.width < containerWidth) {
                    scrollState.animateScrollTo(
                        containerWidth + gapEnd, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                } else {
                    requestScrollToCursor = false

                    if (scrollState.value < inputWidth - valueSize.width + gapStart) {
                        scrollState.animateScrollTo(
                            inputWidth - valueSize.width + gapStart, tween(
                                durationMillis = 240,
                                easing = EaseInOutQuad,
                            )
                        )
                    }
                }
            } else {
                if (valueSize.width > containerWidth) {
                    scrollState.animateScrollTo(
                        valueSize.width, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                } else {
                    scrollState.animateScrollTo(
                        containerWidth + gapStart, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                }
            }
        }
    }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onGloballyPositioned {
                    containerHeight = it.size.height
                    containerWidth = it.size.width
                }
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newTextFieldValueState ->
                    textFieldValueState = newTextFieldValueState

                    val stringChangedSinceLastInvocation =
                        lastTextValue != newTextFieldValueState.text
                    val positionChangedSinceLastInvocation =
                        lastCursorPosition != newTextFieldValueState.selection.start

                    lastTextValue = newTextFieldValueState.text
                    lastCursorPosition = newTextFieldValueState.selection.start

                    if (stringChangedSinceLastInvocation || positionChangedSinceLastInvocation) {
                        requestScrollToCursor = true

                        onChangeValue(lastTextValue)
                    }
                },
                onTextLayout = { restoreScrollPosition() },
                textStyle = textStyle.copy(textAlign = TextAlign.End),
                singleLine = true,
                cursorBrush = cursorBrush,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .disabledHorizontalPointerInputScroll(
                        scrollState.value,
                        inputWidth - valueSize.width - currencySymbolSize.width - gapStart,
                    )
                    .focusRequester(focusRequester),
                decorationBox = { input ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                            .padding(contentPadding)
                            .onGloballyPositioned {
                                inputWidth = it.size.width
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            input()
                        }

                        if (currSymbol !== null && textFieldValue.text.isNotEmpty()) {
                            Text(
                                text = currSymbol,
                                style = currencyStyle,
                                modifier = Modifier.offset(
                                    with(localDensity) { ( -valueSize.width - currencySymbolSize.width * 0.3f).toDp() },
                                    with(localDensity) { (valueSize.height - currencySymbolSize.height + valueSize.height * 0.14f - valueSize.height * 0.5f).toDp() },
                                )
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        DisposableEffect(value) {
            if (textFieldValueState.selection.start != 0) {
                return@DisposableEffect onDispose { }
            }

            restoreScrollPosition(true)

            onDispose { }
        }
    }
}

@Composable
fun calculateIntrinsics(input: String, style: TextStyle): Size {
    val intrinsics = ParagraphIntrinsics(
        text = input,
        style = style,
        density = LocalDensity.current,
        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
    )

    val paragraph = Paragraph(
        paragraphIntrinsics = intrinsics,
        constraints = Constraints(maxWidth = ceil(1000f).toInt()),
        maxLines = 1,
        ellipsis = false
    )

    return Size(intrinsics.maxIntrinsicWidth.toInt(), paragraph.height.toInt())
}

data class Size(val width: Int, val height: Int)

fun Modifier.disabledHorizontalPointerInputScroll(
    valueScroll: Int,
    breakpoint: Int,
): Modifier {
    return this.nestedScroll(object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            Log.d("onPreScroll", "valueScroll = $valueScroll available.x = ${available.x} breakpoint = $breakpoint")
            if ((valueScroll - available.x < breakpoint) && (available.x.toInt() > 0)) {
                Log.d("onPreScroll", "block = ${(valueScroll - available.x) - breakpoint}")
                return Offset(breakpoint - (valueScroll - available.x), 0f)
            }

            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return available.copy(y = 0f)
        }
    })
}