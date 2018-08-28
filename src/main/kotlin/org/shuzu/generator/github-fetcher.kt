package org.shuzu.generator

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService
import org.shuzu.generator.github.getTopics
import java.io.File

fun main(args: Array<String>) {
    println(fetchGithub())
}

fun fetchGithub(): Site {
    return Site(fetchOrgRepos("freewind-demos"))
}

private fun fetchOrgRepos(orgName: String): List<Repository> {
    val service = run {
        val client = GitHubClient().setOAuth2Token(readGithubToken())
        RepositoryService(client)
    }
    return service.getRepositories(orgName)
            .filterNot { it.isPrivate }
            .map { repo ->
                println("repo: ${repo.owner.login} / ${repo.name}")
                Repository(repo.name, repo.htmlUrl, repo.description,
                        topics = emptyList() // getTopics(orgName, repo.name)
                )
            }
}

fun readGithubToken(): String {
    return File("./github-token.txt").readText().trim()
}
