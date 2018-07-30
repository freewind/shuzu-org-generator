package org.shuzu.generator

import org.apache.commons.lang3.StringEscapeUtils

data class ProjectFile(val name: String, val path: String,
                       val content: String) {
    fun contentEscaped() = StringEscapeUtils.escapeHtml4(content)
    val language: String = name.substringAfterLast(".")
}

data class Repository(val name: String, val url: String,
                      val cloneUrl: String,
                      val description: String? = null,
                      val readmeFile: ProjectFile? = null,
                      val codeFiles: List<ProjectFile> = emptyList()) {
    val issuesUrl = "$url/issues?q="
    fun fileUrl(file: ProjectFile): String = "$url/${file.path}"
}

data class Organization(val name: String, val url: String, val repos: List<Repository>)

data class Site(
        val orgs: List<Organization>
)

object SitePaths {
    fun orgPath(org: Organization): String = "orgs/${org.name}/index.html"
    fun repoPath(org: Organization, repo: Repository): String = "orgs/${org.name}/${repo.name}.html"
}