package org.shuzu.generator

data class ProjectFile(val name: String, val path: String,
                       val content: String) {
    val language: String = name.substringAfterLast(".")
}

data class Repository(val name: String, val url: String,
                      val cloneUrl: String,
                      val description: String? = null,
                      val readmeFile: ProjectFile? = null,
                      val codeFiles: List<ProjectFile> = emptyList()) {
    val issuesUrl = "$url/issues?q="
    fun fileUrl(file: ProjectFile): String = "$url/blob/master/${file.path}"
}

data class Organization(val name: String, val url: String, val repos: List<Repository>)

data class Site(
        val org: Organization
)

object SitePaths {
    fun orgPath(org: Organization): String = "orgs/${org.name}/index.html"
    fun repoPath(org: Organization, repo: Repository): String = "orgs/${org.name}/${repo.name}.html"
}