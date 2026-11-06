plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)

    // Usa la stessa versione Kotlin configurata per il progetto.
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

            // Espone Room al modulo desktopApp che utilizza AppDatabase.
            api(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Dipendenze aggiunte dal team per la navigazione.
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    add("kspJvm", libs.androidx.room3.compiler)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}