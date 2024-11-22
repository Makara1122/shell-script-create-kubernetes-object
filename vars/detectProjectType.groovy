/**
 * Detects the project type from a GitHub repository.
 * Clones the repository, analyzes its structure, and determines the project type.
 * @param repoUrl The GitHub repository URL.
 * @param cleanUp Whether to clean up non-Docker files after detection.
 * @return The detected project type.
 */
def detectProjectTypeFromGithub(String repoUrl, boolean cleanUp = true) {
    def repoName = repoUrl.tokenize("/").last().replace(".git", "")
    def tempDir = "/tmp/${repoName}"

    try {
        sh "rm -rf ${tempDir}"
        sh "git clone ${repoUrl} ${tempDir}"

        def projectType = detectProjectType(tempDir)

        if (cleanUp) {
            sh "find ${tempDir} -mindepth 1 ! -name Dockerfile -exec rm -rf {} +"
        }

        return projectType
    } catch (Exception e) {
        sh "rm -rf ${tempDir}"
        error "Failed to detect project type: ${e.message}"
    }
}

/**
 * Detects the type of project based on its directory structure.
 * @param projectPath The local path to the project.
 * @return The detected project type or null if undetectable.
 */
def detectProjectType(String projectPath) {
    if (fileExists("${projectPath}/package.json")) {
        def packageJson = readJSON file: "${projectPath}/package.json"
        if (packageJson.dependencies?.'next') {
            return 'nextjs'
        } else if (packageJson.dependencies?.'react') {
            return 'react'
        }
    } else if (fileExists("${projectPath}/pom.xml")) {
        return 'springboot-maven'
    } else if (fileExists("${projectPath}/build.gradle")) {
        return 'springboot-gradle'
    } else if (fileExists("${projectPath}/pubspec.yaml")) {
        return 'flutter'
    }
    return null
}

/**
 * Detects the package manager for the project.
 * @param projectPath The local path to the project.
 * @return The detected package manager.
 */
def detectPackageManager(String projectPath) {
    if (fileExists("${projectPath}/package-lock.json")) {
        return 'npm'
    } else if (fileExists("${projectPath}/yarn.lock")) {
        return 'yarn'
    } else if (fileExists("${projectPath}/pnpm-lock.yaml")) {
        return 'pnpm'
    } else if (fileExists("${projectPath}/bun.lockb")) {
        return 'bun'
    }
    return 'npm'
}

/**
 * Writes a Dockerfile for the detected project type.
 * @param projectType The detected project type.
 * @param projectPath The local path to the project.
 * @param packageManager The package manager to use.
 */
def writeDockerfile(String projectType, String projectPath, String packageManager) {
    try {
        def dockerfileContent = libraryResource "dockerfileTemplates/Dockerfile-${projectType}"
        dockerfileContent = dockerfileContent.replaceAll("\\{\\{packageManager\\}\\}", packageManager)
        writeFile file: "${projectPath}/Dockerfile", text: dockerfileContent
        echo "Dockerfile successfully written for ${projectType} project at ${projectPath}/Dockerfile"
    } catch (Exception e) {
        error "Failed to write Dockerfile for ${projectType} project: ${e.message}"
    }
}

/**
 * Pushes a Docker image to Docker Hub.
 * @param dockerImageName The name of the Docker image.
 * @param dockerImageTag The tag for the Docker image.
 * @param credentialsId The Jenkins credentials ID for Docker Hub.
 */
def pushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId) {
    try {
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
            def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"

            sh """
            docker login -u ${DOCKER_USER} -p ${DOCKER_PASS} || exit 1
            docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo} || exit 1
            docker push ${dockerHubRepo} || exit 1
            """

            echo "Image pushed to Docker Hub: ${dockerHubRepo}"
        }
    } catch (Exception e) {
        echo "Error during Docker push: ${e.message}"
        currentBuild.result = 'FAILURE'
        error "Failed to push Docker image: ${e.message}"
    }
}

/**
 * Builds and pushes a Docker image to a registry.
 * @param dockerImageName The name of the Docker image.
 * @param dockerImageTag The tag for the Docker image.
 * @param credentialsId The Jenkins credentials ID for Docker Hub.
 * @param dockerfilePath The path to the Dockerfile (default: current directory).
 */
def buildAndPushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId, String dockerfilePath = '.') {
    try {
        echo "Building Docker image: ${dockerImageName}:${dockerImageTag}"
        sh """
        docker build -t ${dockerImageName}:${dockerImageTag} ${dockerfilePath} || exit 1
        """

        echo "Logging in to Docker registry"
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
            sh """
            docker login -u ${DOCKER_USER} -p ${DOCKER_PASS} || exit 1
            """
        }

        echo "Tagging Docker image"
        def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"
        sh """
        docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo} || exit 1
        """

        echo "Pushing Docker image to registry: ${dockerHubRepo}"
        sh """
        docker push ${dockerHubRepo} || exit 1
        """

        echo "Docker image ${dockerHubRepo} successfully pushed."
    } catch (Exception e) {
        echo "Error during Docker build and push: ${e.message}"
        currentBuild.result = 'FAILURE'
        error "Failed to build and push Docker image: ${e.message}"
    }
}
