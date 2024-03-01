MAVEN_REPO_URL := $(shell echo ${MAVEN_REPO_URL})
MAVEN_REPO_USERNAME := $(shell echo ${MAVEN_REPO_USERNAME})
MAVEN_REPO_PASSWORD := $(shell echo ${MAVEN_REPO_PASSWORD})

.DEFAULT_GOAL := build

build:
	./gradlew build

publish:
	./gradlew publish -PmavenRepoUrl=$(MAVEN_REPO_URL) -PmavenRepoUsername=$(MAVEN_REPO_USERNAME) -PmavenRepoPassword=$(MAVEN_REPO_PASSWORD)

clean:
	./gradlew clean

.PHONY: build publish clean
