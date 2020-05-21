package io.pivotal.pal.tracker.gradlebuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec

class DependenciesGraphPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.with {
            task("clean", type: Delete) {
                delete "build"
            }

            task("dependenciesGraphDot") {
                mustRunAfter "clean"
                group = "DependenciesGraph"
                description = "Generate DOT file"

                def graphBuildDir = "build/dependenciesGraph"
                def dotFile = file "$graphBuildDir/graph.dot"

                doLast {
                    delete graphBuildDir
                    mkdir graphBuildDir

                    dotFile << "digraph dependencies {\n"

                    subprojects.forEach { Project subProject ->
                        if (isProjectExcluded(subProject)) {
                            return
                        }

                        try {
                            Configuration compileConfig = subProject.configurations["compile"]

                            compileConfig
                                .dependencies
                                .grep { it.respondsTo("getDependencyProject") && !isProjectExcluded(it) }
                                .forEach { dotFile << """  "$subProject.name" -> "$it.dependencyProject.name"\n""" }
                        } catch (UnknownConfigurationException ignored) {
                        }
                    }

                    dotFile << "}\n"
                }
            }

            task("dependenciesGraph", dependsOn: "dependenciesGraphDot", type: Exec) {
                workingDir "$buildDir/dependenciesGraph"
                commandLine "dot", "-O", "-Tpng", "graph.dot"
                group = "DependenciesGraph"
                description = "Generate PNG file"
            }
        }
    }

    private static boolean isProjectExcluded(def project) {
        return project.name.contains("support")
    }
}
