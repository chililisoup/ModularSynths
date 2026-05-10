plugins {
    id("java-library")
    id("idea")
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

class ModData {
    val version = property("mod.version") as String
    val group = property("mod.group") as String
    val id = property("mod.id") as String
    val name = property("mod.name") as String
    val authors = property("mod.authors") as String
    val description = property("mod.description") as String
    val homepage = property("mod.homepage") as String
    val sources = property("mod.sources") as String
    val issues = property("mod.issues") as String
    val license = property("mod.license") as String
}

class ModDeps {
    val minecraft = property("deps.minecraft") as String
    val minecraftRange = property("deps.minecraft_range") as String
    val fabricLoader = property("deps.fabric_loader") as String
    val fabricApi = property("deps.fabric_api") as String
}

val mod = ModData()
val deps = ModDeps()

group = mod.group
version = "${mod.version}+${deps.minecraft}"
base.archivesName = mod.id

loom {
    accessWidenerPath = file("src/main/resources/${mod.id}.classtweaker")

    runConfigs.forEach {
        it.vmArgs("-Dmixin.debug.export=true")
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${deps.minecraft}")
    implementation("net.fabricmc:fabric-loader:${deps.fabricLoader}")
    api("net.fabricmc.fabric-api:fabric-api:${deps.fabricApi}")
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    named<ProcessResources>("processResources") {
        fun inputProps(props: Map<String, Any>): Map<String, Any> {
            inputs.properties(*props.map { entry -> entry.key to entry.value }.toTypedArray() )
            return props
        }

        val props = inputProps(mapOf(
            "mod_id" to mod.id,
            "mod_name" to mod.name,
            "mod_version" to version,
            "mod_license" to mod.license,
            "mod_description" to mod.description,
            "mod_homepage" to mod.homepage,
            "mod_sources" to mod.sources,
            "mod_issues" to mod.issues,
            "mod_author_list" to mod.authors.split(", ").joinToString("\",\""),
            "minecraft_range" to deps.minecraftRange
        ))
        filesMatching("fabric.mod.json") { expand(props) }
    }
}
