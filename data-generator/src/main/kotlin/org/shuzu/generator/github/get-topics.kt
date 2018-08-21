package org.shuzu.generator.github

import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun getTopics(orgName: String, repoName: String): List<String>? {
    val response = Fuel.get("https://api.github.com/repos/$orgName/$repoName/topics")
            .header(mapOf("Accept" to "application/vnd.github.mercy-preview+json"))
            .responseString().third.get()
    val listType = (object : TypeToken<List<String>>() {}).type
    val topics = Gson().fromJson<List<String>?>(response, listType)
    return topics?.takeIf { it.isNotEmpty() }
}