package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Organization
import org.shuzu.generator.Repository

fun repoPage(org: Organization, repo: Repository): String {
    return layout {
        div {
            id = "repo-main"

            div("navigation") {
                div("org-link") { linkToOrg(org, org.name) }
                div("repo-name") { +repo.name }
                div("repo-description") { +(repo.description ?: "") }
            }
            div {
                id = "readme"
                div("markdown markdown-body") {
                    +(repo.readmeFile?.content ?: "")
                }
            }
            repo.codeFiles.forEach { file ->
                div("code-file panel markdown-body") {
                    div("header") {
                        span("name") {
                            linkToGithubFile(repo, file, file.name)
                        }
                        span("path") {
                            +file.path
                        }
                    }
                    div("body") {
                        pre("content") {
                            code(file.language) {
                                +file.content
                            }
                        }
                    }
                }
            }
            div("github") {
                div("repo-url") {
                    linkToGithubRepo(repo, "在Github上打开")
                }
                div("repo-issues") {
                    linkToRepoIssues(repo, "到Github Issues上讨论")
                }
            }
        }
    }
}
