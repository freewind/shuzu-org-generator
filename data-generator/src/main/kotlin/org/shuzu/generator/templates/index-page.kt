package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Site

fun indexPage(site: Site): String {
    return layout {
        div {
            id = "index-main"
            div("tip") { +"当前共有 ${site.demoCount} 个Demo" }
        }
    }
}
