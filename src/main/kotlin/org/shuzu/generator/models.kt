@file:Suppress("unused")

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
    val parentPath: String = if (path.contains("/")) path.substringBeforeLast("/") + "/" else ""
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
}

// Keep this `Site` class just to make using Gson easier in Kotlin
data class Site(
        val repos: List<Repository>
)
