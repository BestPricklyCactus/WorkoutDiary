plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.detekt) apply false
}

val detektFormatting = libs.detekt.formatting

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = true
        ignoreFailures = true // Позволяет сгенерировать отчеты перед тем как уронить билд
    }

    tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
        reports {
            xml.required.set(true)
            xml.outputLocation.set(file("$buildDir/reports/detekt/detekt.xml"))
            html.required.set(true)
            html.outputLocation.set(file("$buildDir/reports/detekt/detekt.html"))
        }
    }

    dependencies {
        "detektPlugins"(detektFormatting)
    }
}
