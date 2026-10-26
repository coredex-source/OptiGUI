import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    alias(libs.plugins.moddev)
}

base {
    archivesName = "optigui"
}

version = libs.versions.optigui.get()
group = "opekope2.optigui"

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "Kotlin for Forge" } }
        filter { includeGroup("thedarkcolour") }
    }
}

neoForge {
    version = libs.versions.neoforge.get()

    runs {
        register("client") {
            client()
            gameDirectory = file("run")
        }
    }

    mods {
        register("optigui") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation(libs.kotlinforforge)

    implementation(libs.apache.commons.text)
    jarJar(libs.apache.commons.text)
    implementation(libs.ini4j)
    jarJar(libs.ini4j)

    testImplementation(kotlin("test"))

    if (project.hasProperty("javaSyntax")) {
        dokkaPlugin(libs.dokka.plugin.java.syntax)
    }
}

tasks {
    val javaVersion = libs.versions.java.get()

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.release = javaVersion.toInt()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion))
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    jar {
        from(rootDir.resolve("LICENSE"))
    }

    processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                mutableMapOf(
                    "version" to version as String,
                    "minecraft" to libs.versions.minecraft.get(),
                    "neoforge" to libs.versions.neoforge.get(),
                    "kotlinforforge" to libs.versions.kotlinforforge.get()
                )
            )
        }
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
        withSourcesJar()
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    /*dokkaHtml {
        moduleName = "OptiGUI"
        moduleVersion = version as String
        outputDirectory = layout.buildDirectory.dir(
            if (project.hasProperty("javaSyntax")) "dokka/javaHtml"
            else "dokka/kotlinHtml"
        )

        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            footerMessage =
                "© 2022-${Year.now().value} opekope2. OptiGUI is not an official Minecraft product. Not associated with or endorsed by Mojang Studios."
            customAssets = listOf(projectDir.resolve("logo-icon.svg"))
            separateInheritedMembers = true
        }

        dokkaSourceSets.configureEach {
            documentedVisibilities = setOf(
                DokkaConfiguration.Visibility.PUBLIC,
                DokkaConfiguration.Visibility.PROTECTED
            )

            sourceLink {
                localDirectory.set(projectDir.resolve("src/main/kotlin"))
                remoteUrl = uri("https://github.com/opekope2/OptiGUI/tree/$version/OptiGUI/src/main/kotlin").toURL()
                remoteLineSuffix = "#L"
            }

            externalDocumentationLink {
                val fabricVersion = fabric.bom.get().version
                url = uri("https://maven.fabricmc.net/docs/fabric-api-$fabricVersion/").toURL()
                packageListUrl = uri("https://maven.fabricmc.net/docs/fabric-api-$fabricVersion/element-list").toURL()
            }
            externalDocumentationLink {
                url = uri("https://ini4j.sourceforge.net/apidocs/").toURL()
                packageListUrl = uri("https://ini4j.sourceforge.net/apidocs/package-list").toURL()
            }

            perPackageOption {
                matchingRegex = """opekope2\.optigui\.internal(\..*)?"""
                suppress = true
                documentedVisibilities = setOf()
            }
            perPackageOption {
                matchingRegex = """opekope2\.optigui\.mixin"""
                suppress = true
                documentedVisibilities = setOf()
            }

            // Apply these last, otherwise the other options get ignored
            // You don't want to know how many hours I spent on this...
            jdkVersion = libs.versions.java.get().toInt()
            languageVersion.set(libs.versions.kotlin)
        }
    }*/
}
