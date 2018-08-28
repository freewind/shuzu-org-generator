package org.shuzu.generator

data class SimpleRepo(
        val name: String,
        val urlPath: String? = null,
        val description: String? = null,
        val topics: List<String>? = null
)

data class ProjectFile(
        @Transient val repo: Repository,
        val name: String,
        val path: String,
        val content: String
) {
    val language: String = name.substringAfterLast(".")
    val githubUrl: String = "https://github.com/freewind-demos/${repo.name}/blob/master/$path"
}

data class Repository(
        val name: String,
        val githubUrl: String,
        val description: String? = null,
        val topics: List<String>? = null,
        val readmeFile: ProjectFile? = null,
        val codeFiles: List<ProjectFile> = emptyList()
) {
    val issuesUrl = "$githubUrl/issues?q="
    val cloneUrl = githubUrl
    fun fileUrl(file: ProjectFile): String = "$githubUrl/blob/master/${file.path}"
}

data class Site(
        val name: String = "数组网",
        val url: String = "http://shuzu.org",
        val githubOrgUrl: String = "https://github.com/freewind-demos",
        val repos: List<Repository>
) {
    val demoCount = repos.size
}

object SitePaths {
    //    fun orgPath(org: Organization): String = "orgs/${org.name}/index.html"
    fun repoPath(repo: Repository): String = "demos/${repo.name}.html"
}