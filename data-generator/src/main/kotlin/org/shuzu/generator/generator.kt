package org.shuzu.generator

import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.shuzu.generator.templates.indexPage
import org.shuzu.generator.templates.repoPage
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
        val site = fetchGithub()
        saveToLocalFile(site)
    }
}

object SyncLocalRepos {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = readCachedGithubData()
        deleteNonExistLocalRepos(site)
        cloneOrPullRepos(site)
    }
}

object DataGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = calcSiteData()
        renderLiveSearchData(site)
        renderSite(site)
//        copySiteFiles()
    }

}

private val userHome = System.getProperty("user.home")
private val LocalRoot = File(userHome, "tmp/shuzu-org-generator").also { FileUtils.forceMkdir(it) }
private val LocalReposRoot = File(LocalRoot, "repos").also { if (!it.exists()) it.mkdirs() }
private val SiteRoot = File(LocalRoot, "site").also { if (!it.exists()) it.mkdirs() }
private val GithubReposInfoFile = File(LocalRoot, "github-repos.json")

private fun saveToLocalFile(site: Site) {
    val json = Gson().toJson(site)
    GithubReposInfoFile.writeText(json)
}

private fun renderLiveSearchData(site: Site) {
    val repos = site.repos.map { repo ->
        SimpleRepo(
                repo.name,
                urlPath = run {
                    val path = repo.githubUrl.removePrefix("https://github.com/freewind-demos/")
                    path.takeIf { it != repo.name }
                },
                description = repo.description
        )
    }
    val json = Gson().toJson(repos)
    File("../website/resources/live-search.json").apply {
        this.parentFile.mkdirs()
        this.writeText(json)
    }
}

private fun renderSite(site: Site) {
    indexPage(site)
    site.repos.forEach { repo ->
        repoPage(site, repo).let { content ->
            with(File(SiteRoot, SitePaths.repoPath(repo))) {
                FileUtils.forceMkdirParent(this)
                writeText(content)
            }
        }
    }
}

private fun copySiteFiles() {
//    val targetDir = File(SiteRoot, "site-files")
    File("src/main/resources/site-files").copyRecursively(SiteRoot, overwrite = true)
}

private fun calcSiteData(): Site {
    val site = readCachedGithubData()
    return Site(repos = site.repos.map { repo ->
        val files = readCodeFiles(site, repo)
        val readme = files.find { it.name.toLowerCase() == "readme.md" }
        val codeFiles = files.filterNot { it == readme }
        repo.copy(readmeFile = readme, codeFiles = codeFiles)
    })
}

private fun readCachedGithubData(): Site {
    val json = GithubReposInfoFile.readText()
    return Gson().fromJson(json, Site::class.java)!!
}

private fun readCodeFiles(org: Site, repo: Repository): List<ProjectFile> {
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

private fun cloneOrPullRepos(site: Site) {
    val failedRepos = mutableListOf<Repository>()

    for (repo in site.repos) {
        val localRepoDir = File(LocalReposRoot, repo.name)
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

private fun deleteNonExistLocalRepos(site: Site) {
    for (repoDir in LocalReposRoot.listFiles()) {
        if (!repoExistsOnGithub(site, repoDir)) {
            println("delete not exist repo: $repoDir")
            repoDir.deleteRecursively()
            continue
        }
    }
}

private fun repoExistsOnGithub(site: Site, repoDir: File): Boolean {
    return site.repos.map { it.name }.contains(repoDir.name)
}
