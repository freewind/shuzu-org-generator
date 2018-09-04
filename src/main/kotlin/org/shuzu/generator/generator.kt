package org.shuzu.generator

import com.google.gson.Gson
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import com.redfin.sitemapgenerator.WebSitemapGenerator
import com.redfin.sitemapgenerator.WebSitemapUrl
import java.io.File
import java.io.StringWriter
import java.lang.ProcessBuilder.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object DoAll {
    @JvmStatic
    fun main(args: Array<String>) {
        FetchGithubData.main(args)
        SyncLocalRepos.main(args)
        RenderLiveSearchData.main(args)
        BuildWebsiteProject.main(args)
        SiteGenerator.main(args)
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

object RenderLiveSearchData {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = calcSiteData()
        renderLiveSearchData(site)
    }
}

object BuildWebsiteProject {
    @JvmStatic
    fun main(args: Array<String>) {
        runCommand(File("./website"), "npm install")
        runCommand(File("./website"), "npm run build")
    }

    private fun runCommand(workingDir: File, command: String) {
        val parts = command.split("\\s".toRegex())
        ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(Redirect.INHERIT)
                .redirectError(Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.MINUTES)
    }
}

object SiteGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val site = calcSiteData()
        clearSiteDir()
        renderDemoPages(site)
        copyDemoImages(site)
        copySiteFiles()
        generateSiteMap(site)
    }

    private fun clearSiteDir() {
        fun keep(file: File) = file.name.startsWith(".") || file.name == "CNAME" || file.name == "README.md"
        SiteRoot.listFiles().filterNot { keep(it) }.forEach { it.deleteRecursively() }
    }

}

private val LocalReposRoot = File("./cache/repos")
private val SiteRoot = File("./cache/site")
private val GithubReposInfoFile = File("./cache/github-repos.json")

private fun saveToLocalFile(site: Site) {
    val json = Gson().toJson(site)
    GithubReposInfoFile.writeText(json)
}

private fun generateSiteMap(site: Site) {
    val domain = "http://shuzu.org"
    val wsg = WebSitemapGenerator.builder(domain, SiteRoot)
            .autoValidate(true)
            .build()
    site.repos.forEach { repo ->
        wsg.addUrl(domain + repoIndexPagePath(repo))
    }
    wsg.write()
    println("wrote sitemap: " + File(SiteRoot, "sitemap.xml"))
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
    val template = engine.getTemplate("./website/public/demos/_demo_/index.html")

    site.repos.forEach { repo ->
        val writer = StringWriter()
        val context = HashMap<String, Any>().apply {
            this["demo"] = repo
        }
        template.evaluate(writer, context)
        val output = writer.toString()
        File(SiteRoot, repoIndexPagePath(repo)).apply {
            this.parentFile.mkdirs()
            writeText(output)
        }.also {
            println("write to demo: $it")
        }
    }
}

private fun repoIndexPagePath(repo: Repository) = "/demos/${repo.name}/index.html"

private fun copyDemoImages(site: Site) {
    site.repos.forEach { repo ->
        val repoDir = File(LocalReposRoot, repo.name)
        val imageDir = File(repoDir, "images")
        if (imageDir.exists()) {
            val targetDir = File("./cache/site/demos/${repo.name}/images")
            imageDir.copyRecursively(targetDir, overwrite = true).also {
                println("copied images from $imageDir to $targetDir")
            }
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
        val files = readCodeFiles(repo)
        val readme = files.find { it.name.toLowerCase() == "readme.md" }
        val codeFiles = files.filterNot { it == readme }
        repo.copy(readmeFile = readme, codeFiles = codeFiles)
    })
}

private fun readCachedGithubData(): Site {
    val json = GithubReposInfoFile.readText()
    return Gson().fromJson(json, Site::class.java)!!
}

private fun readCodeFiles(repo: Repository): List<ProjectFile> {
    val dir = File(LocalReposRoot, repo.name)
    val files = dir.walkTopDown().filter { file ->
        file.isFile && !inExcludedDirs(file) && !isExcludeFiles(file)
    }
    val projectFiles = files.map { file ->
        ProjectFile(repo, file.name, relativePath(file, dir), file.readText())
    }.toList()
    return projectFiles
}

fun isExcludeFiles(file: File): Boolean {
    return listOf("package-lock.json", ".gitignore", "gradlew", "gradlew.bat").contains(file.name)
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
    for (repoDir in LocalReposRoot.listFiles().filter { it.isDirectory }) {
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
