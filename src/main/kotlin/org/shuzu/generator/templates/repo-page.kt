package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Organization
import org.shuzu.generator.Repository

fun repoPage(org: Organization, repo: Repository): String {
    return layout {
        div("repo") {
            div {
                div { linkToOrg(org, org.name) }
                div { +repo.name }
                div { +(repo.description ?: "") }
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
                                +file.contentEscaped()
                            }
                        }
                    }
                }
            }
            div {
                linkToGithubRepo(repo, "在Github上打开")
                linkToRepoIssues(repo, "到Github Issues上讨论")
            }
        }
    }
}
