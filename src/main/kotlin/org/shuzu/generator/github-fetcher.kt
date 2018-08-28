package org.shuzu.generator

import org.eclipse.egit.github.core.service.RepositoryService
import org.shuzu.generator.github.getTopics

fun main(args: Array<String>) {
    println(fetchGithub())
}

fun fetchGithub(): Site {
    return Site(
            githubOrgUrl = "https://github.com/freewind-demos",
            repos = fetchOrgRepos("freewind-demos")
    )
}

private fun fetchOrgRepos(orgName: String): List<Repository> {
    val service = RepositoryService()
    return service.getRepositories(orgName)
            .filterNot { it.isPrivate }
            .map { repo ->
                println("repo: ${repo.owner.login} / ${repo.name}")
                Repository(repo.name, repo.htmlUrl, repo.description, getTopics(orgName, repo.name))
            }
}
