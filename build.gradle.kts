plugins {
    id("java")
    id("signing")
    id("maven-publish")
}

group = "me.thealgorithm476"
version = "2.0.1"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "octanrepo"
            url = uri("https://maven.octandevelopment.com/releases")
            credentials(PasswordCredentials::class)

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}