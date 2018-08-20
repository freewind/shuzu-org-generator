package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Organization

fun orgPage(org: Organization): String {
    return layout {
        div {
            id = "org-main"
            div("navigation") {
                span { linkToIndex("数组网") }
            }
            div("org") {
                div("panel") {
                    div("header") {
                        span("org-name") { linkToOrg(org, org.name) }
                    }
                    div("body markdown-body") {
                        ul {
                            for (repo in org.repos) {
                                li("repo") {
                                    span("repo-name") { linkToRepo(org, repo, repo.name) }
                                    span("repo-description") {
                                        +(repo.description ?: "")
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}