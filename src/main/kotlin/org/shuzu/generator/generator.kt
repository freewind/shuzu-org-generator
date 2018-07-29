package org.shuzu.generator

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import java.io.FileFilter

private val LocalRoot = File(System.getProperty("user.home"), "tmp/shuzu-org-generator").also {
    if (!it.exists()) it.mkdirs()
}

object Generator {
    @JvmStatic
    fun main(args: Array<String>) {
        val orgs = fetchGithub()

        deleteNonExistLocalRepos(orgs)
        cloneOrPullRepos(orgs)
    }
}

fun main(args: Array<String>) {
    val orgs = fetchGithub()

    deleteNonExistLocalRepos(orgs)
    cloneOrPullRepos(orgs)

//    val site = readSiteData(orgs)
}

fun readSiteData(orgs: List<Organization>): Site {
    return Site(orgs.map { org ->
        org.copy(repos = org.repos.map { repo ->
            repo.copy(codeFiles = readCodeFiles(org, repo))
        })
    })
}

fun readCodeFiles(org: Organization, repo: Repository): List<CodeFile> {
    val dir = File(LocalRoot, "${org.name}/${repo.name}")
    return listOf()
}

fun cloneOrPullRepos(orgs: List<Organization>) {
    for (org in orgs) {
        val localOrgDir = File(LocalRoot, org.name)
        if (!localOrgDir.exists()) {
            println("creating dir: $localOrgDir")
            FileUtils.forceMkdir(localOrgDir)
        }

        for (repo in org.repos) {
            val localRepoDir = File(localOrgDir, repo.name)
            if (localRepoDir.exists()) {
                println("git pull: $localRepoDir")
                gitPull(localRepoDir)
            } else {

                println("git clone ${repo.cloneUrl} to local: $localRepoDir")
                gitClone(repo.cloneUrl, localRepoDir)
            }
        }
    }
}

fun deleteNonExistLocalRepos(orgs: List<Organization>) {
    for (orgDir in LocalRoot.listFiles()) {
        if (!orgExistsOnGithub(orgs, orgDir)) {
            println("delete not exist org: $orgDir")
            orgDir.deleteRecursively()
            continue
        }

        for (repoDir in orgDir.listFiles()) {
            if (!repoExistsOnGithub(orgs, repoDir)) {
                println("delete not exist repo: $repoDir")
                repoDir.deleteRecursively()
                continue
            }
        }
    }
}

private fun repoExistsOnGithub(orgs: List<Organization>, repoDir: File): Boolean {
    return orgs.any { it.name == repoDir.parentFile.name && it.repos.any { it.name == repoDir.name } }
}

private fun orgExistsOnGithub(orgs: List<Organization>, orgDir: File): Boolean {
    return orgs.any { it.name == orgDir.name }
}
