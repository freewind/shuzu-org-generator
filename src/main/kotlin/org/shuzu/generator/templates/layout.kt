package org.shuzu.generator.templates

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.StringWriter

fun layout(body: TagConsumer<StringWriter>.() -> Unit): String {
    val page = StringWriter().appendHTML()
    return page.html {
        head {
            title { +"数组网 - 彪悍的Demo无需解释" }
            link("/css/site.css", "stylesheet")
            script(src = "/js/less-3.8.0.min.js") {}
            link("/css/github-markdown-2.10.0.css", "stylesheet")
            script(src = "/js/markdown-0.5.0.js") {}
        }
        body {
            div {
                id = "header"
                div("site-slogan") { +"彪悍的Demo无须解释" }
                div {
                    span("site-name") { a("/") { +"数组网" } }
                    span("site-url") { a("shuzu.org") { +"shuzu.org" } }
                }
            }
            div {
                id = "main"
                page.body()
            }
            script(src = "/js/site.js") {}
        }
    }.toString()
}