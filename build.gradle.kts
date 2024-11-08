import com.github.gradle.node.npm.task.NpmTask

val base = "${project.projectDir}/src/main/website/app/"
val baseRes = "$projectDir/src/main/resources/static"

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.hibernate.orm") version "6.5.3.Final"
	id("org.graalvm.buildtools.native") version "0.10.3"
	id("com.github.node-gradle.node") version "3.5.0"
}

group = "com.rld"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

node {
	download = false
	workDir = file("${base}nodejs")
	npmWorkDir = file("${base}npm")
	nodeProjectDir = file(base)
}

tasks {
	named<NpmTask>("npmInstall") {
		description = "read package.json and install deps"
		workingDir = file(base)
		args = listOf("install")
	}

	register<NpmTask>("buildVite") {
		description = "Build vite project"
		workingDir = file(base)
		args = listOf("run", "build")
		dependsOn("npmInstall")
	}

	register<Copy>("copyToResources") {
		from("$base/dist/")
		destinationDir = file(baseRes)
		dependsOn("buildVite")
	}

	named("bootRun") {
		dependsOn("copyToResources")
	}
}


configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("software.amazon.awssdk:ses:2.28.16")
	implementation("org.apache.commons:commons-lang3:3.17.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-web-services")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.session:spring-session-core")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.security:spring-security-crypto:6.3.0")
	implementation("org.bouncycastle:bcprov-jdk18on:1.78")
	implementation("com.google.code.gson:gson:2.11.0")
	implementation("jakarta.mail:jakarta.mail-api:2.1.3")
	implementation("org.springframework:spring-websocket:6.1.10")
	implementation("org.springframework:spring-messaging:6.1.10")
	implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
	"developmentOnly"("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmToolchain(21)
	}
}

hibernate {
	enhancement {
		enableAssociationManagement = true
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}