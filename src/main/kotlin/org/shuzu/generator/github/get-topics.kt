package org.shuzu.generator.github

import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson

data class Topics(val names: List<String>)

fun getTopics(orgName: String, repoName: String): List<String>? {
    val response = Fuel.get("https://api.github.com/repos/$orgName/$repoName/topics")
            .header(mapOf("Accept" to "application/vnd.github.mercy-preview+json"))
            .responseString().third.get()
    println("- topics: $response")
    val topics = Gson().fromJson<Topics>(response, Topics::class.java)
    return topics.names.takeIf { it.isNotEmpty() }
}
