plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    jacoco
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            api(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Dipendenze aggiunte dal team per la navigazione.
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization)
            // Source: https://mvnrepository.com/artifact/androidx.compose.material/material-icons-extended
            implementation("androidx.compose.material:material-icons-extended:1.7.8")
            implementation("io.github.vinceglb:filekit-dialogs:0.17.1")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.17.1")

        }
        jvmTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation("jakarta.mail:jakarta.mail-api:2.1.3")
            implementation("org.eclipse.angus:angus-mail:2.0.3")
            implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
        }
    }
}

dependencies {
    add("kspJvm", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoTestReportTask = tasks.register<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }
    val mainClasses = fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main"))
    val mainSources = files("src/commonMain/kotlin", "src/jvmMain/kotlin")
    classDirectories.setFrom(mainClasses)
    sourceDirectories.setFrom(mainSources)
    executionData.setFrom(fileTree(layout.buildDirectory) { include("jacoco/*.exec") })
}

tasks.named("jvmTest") {
    finalizedBy(jacocoTestReportTask)
}