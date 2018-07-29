package org.shuzu.generator

import org.apache.commons.lang3.StringEscapeUtils

data class ProjectFile(val name: String, val path: String, val content: String) {
    fun contentEscaped() = StringEscapeUtils.escapeHtml4(content)
}

data class Repository(val name: String, val url: String, val cloneUrl: String, val description: String? = null,
                      val readmeFile: ProjectFile? = null,
                      val codeFiles: List<ProjectFile> = emptyList())

data class Organization(val name: String, val url: String, val repos: List<Repository>)

data class Site(
        val orgs: List<Organization>
) {
    fun orgPath(org: Organization): String = "orgs/${org.name}/index.html"
    fun repoPath(org: Organization, repo: Repository): String = "orgs/${org.name}/${repo.name}.html"
}