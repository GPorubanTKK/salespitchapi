plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.hibernate.orm") version "6.5.3.Final"
	id("org.graalvm.buildtools.native") version "0.10.3"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.rld"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
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
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.security:spring-security-crypto:6.3.0")
	implementation("org.bouncycastle:bcprov-jdk18on:1.78")
	implementation("com.google.code.gson:gson:2.11.0")
	implementation("jakarta.mail:jakarta.mail-api:2.1.3")
	implementation("org.springframework:spring-websocket:6.1.10")
	implementation("org.springframework:spring-messaging:6.1.10")
	implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
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
