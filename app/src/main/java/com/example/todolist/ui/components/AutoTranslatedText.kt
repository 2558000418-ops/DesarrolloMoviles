package com.example.todolist.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.concurrent.ConcurrentHashMap

val LocalSelectedLanguage = compositionLocalOf { "es" }

@Composable
fun ProvideSelectedLanguage(languageCode: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSelectedLanguage provides languageCode, content = content)
}

private object UiTextTranslator {
    private val translators = ConcurrentHashMap<String, Translator>()
    private val cache = ConcurrentHashMap<Pair<String, String>, String>()

    fun translate(text: String, languageCode: String, onResult: (String) -> Unit) {
        val targetTag = when (languageCode.lowercase()) {
            "zh-cn", "zh" -> "zh"
            "zh-tw" -> "zh"
            else -> languageCode.substringBefore('-').lowercase()
        }
        val target = TranslateLanguage.fromLanguageTag(targetTag) ?: return
        val key = text to target
        cache[key]?.let(onResult)?.also { return }
        val translator = translators.getOrPut(target) {
            Translation.getClient(
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.SPANISH)
                    .setTargetLanguage(target)
                    .build()
            )
        }
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .addOnSuccessListener {
                translator.translate(text).addOnSuccessListener { translated ->
                    cache[key] = translated
                    onResult(translated)
                }
            }
    }
}

@Composable
fun AutoTranslatedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val languageCode = LocalSelectedLanguage.current
    val translated = produceState(text, text, languageCode) {
        if (languageCode.equals("es", ignoreCase = true) || text.isBlank() || text.contains("\n")) {
            value = text
        } else {
            UiTextTranslator.translate(text, languageCode) { value = it }
        }
    }
    MaterialText(
        text = translated.value,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun AutoTranslatedText(text: AnnotatedString, modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current) {
    MaterialText(text = text, modifier = modifier, style = style)
}
