plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
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
    }
}

dependencies {
    add("kspJvm", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}