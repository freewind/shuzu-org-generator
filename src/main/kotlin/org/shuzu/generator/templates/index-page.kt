package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Organization

fun indexPage(orgs: List<Organization>): String {
    return layout {
        div {
            id = "index-main"
            for (org in orgs) {
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
}
