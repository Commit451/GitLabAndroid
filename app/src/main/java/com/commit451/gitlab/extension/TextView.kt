package com.commit451.gitlab.extension

import android.widget.TextView
import com.commit451.gitlab.App
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.ImageGetterFactory
import com.vdurmont.emoji.EmojiParser
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

private val parser: Parser = Parser.builder().build()

fun TextView.setMarkdownText(text: String, project: Project? = null) {
    val document = parser.parse(text)
    val html = renderer.render(document)

    val emojiParsedHtml = EmojiParser.parseToUnicode(html)

    // I don't like this too much for its global-ness
    val baseUrl = App.get().currentAccount.serverUrl!!
    val getter = ImageGetterFactory.create(this, baseUrl, project)
    this.text = emojiParsedHtml.formatAsHtml(getter)
}
