package org.shuzu.generator.templates

import kotlinx.html.TagConsumer
import kotlinx.html.a
import org.shuzu.generator.Organization
import org.shuzu.generator.ProjectFile
import org.shuzu.generator.Repository
import org.shuzu.generator.SitePaths

fun TagConsumer<*>.linkToOrg(org: Organization, text: String) {
    a("/" + SitePaths.orgPath(org)) { +text }
}

fun TagConsumer<*>.linkToRepo(org: Organization, repo: Repository, text: String) {
    a("/" + SitePaths.repoPath(org, repo)) { +text }
}

fun TagConsumer<*>.linkToGithubFile(repo: Repository, file: ProjectFile, text: String) {
    a(repo.fileUrl(file), target = "_blank") { +text }
}

fun TagConsumer<*>.linkToGithubRepo(repo: Repository, text: String) {
    a(repo.url) { +text }
}

fun TagConsumer<*>.linkToRepoIssues(repo: Repository, text: String) {
    a(repo.issuesUrl) { +text }
}
