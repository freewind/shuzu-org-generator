package org.shuzu.generator.templates

import kotlinx.html.*
import org.shuzu.generator.Organization
import org.shuzu.generator.SitePaths

fun orgPage(org: Organization): String {
    return layout {
        div {
            id="org-main"
            h3 {
                a("/" + SitePaths.orgPath(org)) { +org.name }
                ul {
                    for (repo in org.repos) {
                        li {
                            a("/" + SitePaths.repoPath(org, repo)) { +repo.name }
                        }
                    }
                }
            }
        }
    }
}
