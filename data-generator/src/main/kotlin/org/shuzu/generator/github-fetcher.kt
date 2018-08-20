package org.shuzu.generator

import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.OrganizationService
import org.eclipse.egit.github.core.service.RepositoryService
import java.io.File

fun main(args: Array<String>) {
    println(fetchGithub())
}

fun fetchGithub(): Organization {
    val client = GitHubClient().apply {
        setOAuth2Token(readGithubToken())
    }
    val org = fetchFreewindDemosOrg(client)
    return Organization(org.login, org.url, fetchOrgRepos(client, org))
}

private fun fetchOrgRepos(client: GitHubClient, org: User): List<Repository> {
    val service = RepositoryService(client)
    return service.getRepositories(org.login)
            .filterNot { it.isPrivate }
            .map { repo ->
                println("repo: ${repo.owner.login} / ${repo.name}")
                Repository(repo.name, repo.htmlUrl, repo.cloneUrl, repo.description, null, emptyList())
            }
}

private fun fetchFreewindDemosOrg(client: GitHubClient): User {
    val service = OrganizationService(client)
    return service.organizations.find { it.login == "freewind-demos" }!!
}

private fun readGithubToken(): String {
    return File("github-token.txt").readText().trim()
}
