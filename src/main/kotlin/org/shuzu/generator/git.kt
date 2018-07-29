package org.shuzu.generator

import org.eclipse.jgit.api.Git
import java.io.File


fun gitClone(uri: String, localDir: File) {
    Git.cloneRepository().setURI(uri)
            .setDirectory(localDir)
            .setCloneAllBranches(true)
            .call()
}

fun gitPull(localDir: File) {
    Git.open(localDir).pull().call()
}