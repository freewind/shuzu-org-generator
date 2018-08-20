package org.shuzu.generator

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Paths

object DoAll {
    @JvmStatic
    fun main(args: Array<String>) {
        FetchGithubData.main(args)
        SyncLocalRepos.main(args)
        DataGenerator.main(args)
    }
}

object FetchGithubData {
    @JvmStatic
    fun main(args: Array<String>) {
        val org = fetchGithub()
        saveToLocalFile(org)
    }
}

object SyncLocalRepos {
    @JvmStatic
    fun main(args: Array<String>) {
        val org = readCachedGithubData()
        deleteNonExistLocalRepos(org)
        cloneOrPullRepos(org)
    }
}

object DataGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = calcSiteData()
        renderSummaryJson(site)
        renderRepoJson(site.org.repos)
//        renderSite(site)
//        copySiteFiles()
    }

}

private val userHome = System.getProperty("user.home")
private val LocalRoot = File(userHome, "tmp/shuzu-org-generator").also { FileUtils.forceMkdir(it) }
private val LocalReposRoot = File(LocalRoot, "repos").also { if (!it.exists()) it.mkdirs() }
//private val SiteRoot = File(LocalRoot, "site").also { if (!it.exists()) it.mkdirs() }
private val GithubReposInfoFile = File(LocalRoot, "github-repos.json")
private val GeneratedSummarySiteJson = File(LocalRoot, "generated/site-summary.json").also {
    val dir = it.parentFile
    if (!dir.exists()) dir.mkdirs()
}

private fun generatedRepoJson(repo: Repository) = File(LocalRoot, "generated/repos/${repo.name}.json").also {
    val dir = it.parentFile
    if (!dir.exists()) dir.mkdirs()
}

private fun saveToLocalFile(org: Organization) {
    val json = Klaxon().toJsonString(org)
    GithubReposInfoFile.writeText(json)
}

private fun renderSummaryJson(site: Site) {
    val fileContentRemoved = with(site) {
        copy(org = with(org) {
            copy(repos = repos.map { repo ->
                with(repo) {
                    copy(readmeFile = null, codeFiles = emptyList())
                }
            })
        })
    }
    val json = Klaxon().toJsonString(fileContentRemoved)
    GeneratedSummarySiteJson.writeText(json)
}

private fun renderRepoJson(repos: List<Repository>) {
    repos.forEach { repo ->
        val file = generatedRepoJson(repo)
        val json = Klaxon().toJsonString(repo)
        file.writeText(json)
    }
}

//private fun renderSite(site: Site) {
//    indexPage(site.orgs).run {
//        File(SiteRoot, "index.html").writeText(this)
//    }
//
//    site.orgs.forEach { org ->
//        orgPage(org).let { content ->
//            with(File(SiteRoot, SitePaths.orgPath(org))) {
//                FileUtils.forceMkdirParent(this)
//                writeText(content)
//            }
//        }
//
//        org.repos.forEach { repo ->
//            repoPage(org, repo).let { content ->
//                with(File(SiteRoot, SitePaths.repoPath(org, repo))) {
//                    FileUtils.forceMkdirParent(this)
//                    writeText(content)
//                }
//            }
//        }
//    }
//}

//private fun copySiteFiles() {
////    val targetDir = File(SiteRoot, "site-files")
//    File("src/main/resources/site-files").copyRecursively(SiteRoot, overwrite = true)
//}

private fun calcSiteData(): Site {
    val org = readCachedGithubData()
    return Site(org.copy(repos = org.repos.map { repo ->
        val files = readCodeFiles(org, repo)
        val readme = files.find { it.name.toLowerCase() == "readme.md" }
        val codeFiles = files.filterNot { it == readme }
        repo.copy(readmeFile = readme, codeFiles = codeFiles)
    }))
}

private fun readCachedGithubData(): Organization {
    val json = GithubReposInfoFile.readText()
    return Klaxon().parse<Organization>(json)!!
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

private fun cloneOrPullRepos(org: Organization) {
    val failedRepos = mutableListOf<Repository>()

    val localOrgDir = File(LocalReposRoot, org.name).also { dir ->
        if (!dir.exists()) {
            println("creating dir: $dir")
            FileUtils.forceMkdir(dir)
        }
    }

    for (repo in org.repos) {
        val localRepoDir = File(localOrgDir, repo.name)
        try {
            if (localRepoDir.exists()) {
                println("git pull: $localRepoDir")
                gitPull(localRepoDir)
            } else {
                println("git clone ${repo.cloneUrl} to local: $localRepoDir")
                gitClone(repo.cloneUrl, localRepoDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            failedRepos.add(repo)
        }
    }
    if (failedRepos.isNotEmpty()) {
        println("----------------- failedRepos -------------------")
        failedRepos.forEach { repo ->
            println(repo.name)
        }
        throw Exception("some repos failed to sync")
    }
}

private fun deleteNonExistLocalRepos(org: Organization) {
    for (orgDir in LocalReposRoot.listFiles()) {
        if (org.name != orgDir.name) {
            println("delete not exist org: $orgDir")
            orgDir.deleteRecursively()
            continue
        }

        for (repoDir in orgDir.listFiles()) {
            if (!repoExistsOnGithub(org, repoDir)) {
                println("delete not exist repo: $repoDir")
                repoDir.deleteRecursively()
                continue
            }
        }
    }
}

private fun repoExistsOnGithub(org: Organization, repoDir: File): Boolean {
    return org.name == repoDir.parentFile.name && org.repos.any { it.name == repoDir.name }
}
