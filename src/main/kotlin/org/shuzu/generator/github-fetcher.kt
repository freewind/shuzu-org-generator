package org.shuzu.generator

import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.OrganizationService
import org.eclipse.egit.github.core.service.RepositoryService
import java.io.File

fun main(args: Array<String>) {
    println(fetchGithub())
}

fun fetchGithub(): List<Organization> {
    val client = GitHubClient()
    client.setOAuth2Token(readGithubToken())
    val orgs = fetchMyOrgs(client).map { org ->
        println("org: ${org.login}")
        Organization(org.login, org.url, fetchOrgRepos(client, org))
    }
    return orgs
}

private fun fetchOrgRepos(client: GitHubClient, org: User): List<Repository> {
    val service = RepositoryService(client)
    return service.getRepositories(org.login)
            .filterNot { it.isPrivate }
            .map { repo ->
                println("repo: ${repo.owner.login} / ${repo.name}")
                Repository(repo.name, repo.url, repo.cloneUrl, repo.description, emptyList())
            }
}

private fun fetchMyOrgs(client: GitHubClient): List<User> {
    val service = OrganizationService(client)
    return service.organizations.filter { it.login.endsWith("-demos") }
}

private fun readGithubToken(): String {
    return File("github-token.txt").readText().trim()
}
