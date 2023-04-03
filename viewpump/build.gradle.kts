@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.agp.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

// Register dokka to generate javadoc
val dokkaJavadoc = tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// Move to common place if more modules exist
publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = "viewpump"
            afterEvaluate {
                from(components["release"])
            }
            artifacts {
                artifact(dokkaJavadoc)
            }
            pom {
                name.set("ViewPump")
                description.set("View inflation with pre/post-inflation interceptors")
                packaging = "aar"
                url.set("https://github.com/InflationX/ViewPump")
                scm {
                    url.set("https://github.com/InflationX/ViewPump")
                    connection.set("scm:git@github.com:InflationX/ViewPump.git")
                    developerConnection.set("scm:git@github.com:InflationX/ViewPump.git")
                }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("InflationX")
                        name.set("InflationX")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            val releaseRepoUrl = uri(
                "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            val snapshotRepoUrl = uri(
                "https://oss.sonatype.org/content/repositories/snapshots/"
            )
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl
            credentials {
                username = propOrEnv("SONATYPE_USERNAME")
                password = propOrEnv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}

tasks.withType<Sign>().configureEach {
    onlyIf("Is not SNAPSHOT") { !version.toString().endsWith("SNAPSHOT") }
}


android {
    compileSdk = 33
    namespace = "io.github.inflationx.viewpump"

    defaultConfig {
        minSdk = 14
    }

    lint {
        // we don't always want to use the latest version of the support library
        disable.add("GradleDependency")
        textReport = (System.getenv("CI") == "true")
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
//            withJavadocJar()
        }
    }
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.enable = (variant.buildType == "release")
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    compileOnly(libs.androidx.appcompat)
    dokkaPlugin(libs.dokka.android)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)

    testImplementation(libs.androidx.annotation)
    testImplementation(libs.androidx.test.runner)
}

fun propOrEnv(key: String): String {
    return System.getenv(key) ?: project.findProperty(key)?.toString() ?: ""
}
