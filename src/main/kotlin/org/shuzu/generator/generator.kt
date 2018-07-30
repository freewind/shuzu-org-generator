package org.shuzu.generator

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import org.rythmengine.Rythm
import org.shuzu.generator.templates.indexPage
import org.shuzu.generator.templates.repoPage
import org.shuzu.generator.templates.orgPage
import java.io.File
import java.nio.file.Paths

object DoAll {
    @JvmStatic
    fun main(args: Array<String>) {
        FetchGithubData.main(args)
        SyncLocalRepos.main(args)
        SiteGenerator.main(args)
    }
}

object FetchGithubData {
    @JvmStatic
    fun main(args: Array<String>) {
        val orgs = fetchGithub()
        saveToLocalFile(orgs)
    }
}

object SyncLocalRepos {
    @JvmStatic
    fun main(args: Array<String>) {
        val orgs = readCachedGithubData()
        deleteNonExistLocalRepos(orgs)
        cloneOrPullRepos(orgs)
    }
}

object SiteGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = readSiteData()
        renderSite(site)
        copySiteFiles()
    }
}

private val userHome = System.getProperty("user.home")
private val LocalRoot = File(userHome, "tmp/shuzu-org-generator").also { FileUtils.forceMkdir(it) }
private val LocalReposRoot = File(LocalRoot, "repos").also { if (!it.exists()) it.mkdirs() }
private val SiteRoot = File(LocalRoot, "site").also { if (!it.exists()) it.mkdirs() }
private val GithubReposInfoFile = File(LocalRoot, "github-repos.json")

private fun saveToLocalFile(orgs: List<Organization>) {
    val json = Klaxon().toJsonString(orgs)
    GithubReposInfoFile.writeText(json)
}

private fun renderSite(site: Site) {
    indexPage(site.orgs).run {
        File(SiteRoot, "index.html").writeText(this)
    }

    site.orgs.forEach { org ->
        orgPage(org).let { content ->
            with(File(SiteRoot, SitePaths.orgPath(org))) {
                FileUtils.forceMkdirParent(this)
                writeText(content)
            }
        }

        org.repos.forEach { repo ->
            repoPage(org, repo).let { content ->
                with(File(SiteRoot, SitePaths.repoPath(org, repo))) {
                    FileUtils.forceMkdirParent(this)
                    writeText(content)
                }
            }
        }
    }
}

private fun copySiteFiles() {
//    val targetDir = File(SiteRoot, "site-files")
    File("src/main/resources/site-files").copyRecursively(SiteRoot, overwrite = true)
}

private fun readSiteData(): Site {
    val orgs = readCachedGithubData()
    return Site(orgs.map { org ->
        org.copy(repos = org.repos.map { repo ->
            val files = readCodeFiles(org, repo)
            val readme = files.find { it.name.toLowerCase() == "readme.md" }
            val codeFiles = files.filterNot { it == readme }
            repo.copy(readmeFile = readme, codeFiles = codeFiles)
        })
    })
}

private fun readCachedGithubData(): List<Organization> {
    val json = GithubReposInfoFile.readText()
    val orgs = Klaxon().parseArray<Organization>(json)!!
    return orgs
}

private fun readCodeFiles(org: Organization, repo: Repository): List<ProjectFile> {
    val dir = File(LocalReposRoot, "${org.name}/${repo.name}")
    val files = dir.walkTopDown().filter { file ->
        file.isFile && hasExpectedExtension(file) && !inExcludedDirs(file)
    }.map { file ->
        ProjectFile(file.name, relativePath(file, dir), file.readText())
    }.toList()
    return files
}

private fun relativePath(file: File, base: File): String {
    val pathAbsolute = Paths.get(file.absolutePath)
    val pathBase = Paths.get(base.absolutePath)
    return pathBase.relativize(pathAbsolute).toString()
}

fun inExcludedDirs(file: File): Boolean {
    val excludeDirs = listOf(".git", ".gradle", "gradle", ".idea")
    return excludeDirs.any { dir -> file.path.contains("/$dir/") }
}

fun hasExpectedExtension(file: File): Boolean {
    val extensions = listOf("java", "kt", "scala", "js", "ts", "css", "html", "go", "hx", "py", "rb",
            "xml", "gradle", "sql", "txt", "md")
    return extensions.contains(file.extension.toLowerCase())
}

private fun cloneOrPullRepos(orgs: List<Organization>) {
    for (org in orgs) {
        val localOrgDir = File(LocalReposRoot, org.name)
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
    for (orgDir in LocalReposRoot.listFiles()) {
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
