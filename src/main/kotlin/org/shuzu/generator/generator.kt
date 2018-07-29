package org.shuzu.generator

import java.io.File
import org.apache.commons.io.FileUtils
import org.rythmengine.Rythm

private val userHome = System.getProperty("user.home")
private val LocalRoot = File(userHome, "tmp/shuzu-org-generator/repos").also {
    if (!it.exists()) it.mkdirs()
}

private val SiteRoot = File(userHome, "tmp/sites").also {
    if (!it.exists()) it.mkdirs()
}

fun main(args: Array<String>) {
    Generator.main(args)
}

object Generator {
    @JvmStatic
    fun main(args: Array<String>) {
        val orgs = fetchGithub()

        deleteNonExistLocalRepos(orgs)
        cloneOrPullRepos(orgs)

        val site = readSiteData(orgs)
        renderSite(site)
    }
}

private fun renderSite(site: Site) {
    Rythm.render(File("src/main/resources/rythm/index.rythm"), site).run {
        File(SiteRoot, "index.html").writeText(this)
    }

    site.orgs.forEach { org ->
        Rythm.render(File("src/main/resources/rythm/org.rythm"), site).run {
            File(SiteRoot, "${org.name}/index.html").writeText(this)
        }

        org.repos.forEach { repo ->
            Rythm.render(File("src/main/resources/rythm/repo.rythm"), site).run {
                File(SiteRoot, "${org.name}/${repo.name}.html").writeText(this)
            }
        }
    }
}

private fun readSiteData(orgs: List<Organization>): Site {
    return Site(orgs.map { org ->
        org.copy(repos = org.repos.map { repo ->
            repo.copy(codeFiles = readCodeFiles(org, repo))
        })
    })
}

private fun readCodeFiles(org: Organization, repo: Repository): List<CodeFile> {
    val dir = File(LocalRoot, "${org.name}/${repo.name}")
    dir.walkTopDown().filter { file ->
        file.isFile && hasExpectedExtension(file) && !inExcludedDirs(file)
    }.map { file ->
        // FIXME relative path
        CodeFile(file.name, file.path, file.readText())
    }
    return listOf()
}

fun inExcludedDirs(file: File): Boolean {
    val excludeDirs = listOf(".git", ".gradle", "gradle", ".idea")
    return excludeDirs.any { dir -> file.path.contains("/$dir/") }
}

fun hasExpectedExtension(file: File): Boolean {
    val extensions = listOf("java", "kt", "scala", "js", "ts", "css", "html", "go", "hx", "py", "rb",
            "xml", "gradle", "sql", "txt", "md")
    return extensions.contains(file.name)
}

private fun cloneOrPullRepos(orgs: List<Organization>) {
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

private fun deleteNonExistLocalRepos(orgs: List<Organization>) {
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
