package org.shuzu.generator

import org.rythmengine.Rythm

fun main(args: Array<String>) {
    val orgs = listOf(Organization("org-111", "http://sdfsdf.com", repos = listOf(
            Repository("repo1", "http://sdfsdf.com", "sdfsdfdsf", "sdf", listOf(
                    CodeFile("dsdfsdf", "wefwf", "sasdfsfasdf"),
                    CodeFile("dsdfsdf", "wefwf", "sasdfsfasdf"),
                    CodeFile("dsdfsdf", "wefwf", "sasdfsfasdf"),
                    CodeFile("dsdfsdf", "wefwf", "sasdfsfasdf")
            ))
    )))
    val content = Rythm.render("index.rythm", orgs)
    println(content)
}

data class CodeFile(val name: String, val path: String, val content: String)
data class Repository(val name: String, val url: String, val cloneUrl: String, val description: String? = null, val codeFiles: List<CodeFile> = emptyList())
data class Organization(val name: String, val url: String, val repos: List<Repository>)

data class Site(
        val orgs: List<Organization>
)