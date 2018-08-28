package org.shuzu.generator

import com.google.gson.Gson
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import java.io.File
import java.io.StringWriter
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
        clearSiteDir()
        renderDemoPages(site)
        copySiteFiles()
    }

    private fun clearSiteDir() {
        SiteRoot.walkBottomUp().onLeave { file ->
            if (file.name != ".gitkeep") {
                file.delete()
            }
        }
    }

}

private val LocalReposRoot = File("./cache/repos")
private val SiteRoot = File("./cache/site")
private val GithubReposInfoFile = File("./cache/github-repos.json")

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
    File("./website/resources/live-search.json").apply {
        this.writeText(json)
    }.also {
        println("write to file: $it")
    }
}

private fun renderDemoPages(site: Site) {
    val loader = FileLoader()
    val engine = PebbleEngine.Builder().loader(loader).strictVariables(true).build()
    val template = engine.getTemplate("./website/public/demos/_demo_.html")

    site.repos.forEach { repo ->
        val writer = StringWriter()
        val context = HashMap<String, Any>().apply {
            this["demo"] = repo
        }
        template.evaluate(writer, context)
        val output = writer.toString()
        File("./cache/site/demos/${repo.name}.html").apply {
            this.parentFile.mkdirs()
            writeText(output)
        }.also {
            println("write to demo: $it")
        }
    }
}

private fun copySiteFiles() {
    File("./website/dist/").apply {
        this.copyRecursively(SiteRoot, overwrite = true)
        println("copied site files from $this to $SiteRoot")
    }
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

private fun readCodeFiles(site: Site, repo: Repository): List<ProjectFile> {
    val dir = File(LocalReposRoot, "${site.name}/${repo.name}")
    val files = dir.walkTopDown().filter { file ->
        file.isFile && hasExpectedExtension(file) && !inExcludedDirs(file)
    }
    val projectFiles = files.map { file ->
        ProjectFile(repo, file.name, relativePath(file, dir), file.readText())
    }.toList()
    return projectFiles
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