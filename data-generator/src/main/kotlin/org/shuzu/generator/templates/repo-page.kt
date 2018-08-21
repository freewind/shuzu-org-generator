package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Site
import org.shuzu.generator.Repository

fun repoPage(org: Site, repo: Repository): String {
    return layout {
        div {
            id = "repo-main"

            div("navigation") {
                span { linkToIndex("数组网") }
                span { +" > " }
                span("repo-name") { +repo.name }
                span("repo-description") { +(repo.description ?: "") }
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
                        div("name") {
                            linkToGithubFile(repo, file, file.name)
                        }
                        div("path") {
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
            div("github markdown-body") {
                span("github-icon") { img(src = "/images/github.jpg") }
                span("repo-url") {
                    linkToGithubRepo(repo, "打开Github项目地址")
                }
                span("repo-issues") {
                    linkToRepoIssues(repo, "有问题上Github Issues上讨论")
                }
            }
        }
    }
}
