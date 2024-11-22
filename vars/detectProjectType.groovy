// def call(String repoUrl) {
//     // Clone the repo temporarily
//     def repoName = repoUrl.tokenize("/").last().replace(".git", "")
//     def tempDir = "/tmp/${repoName}"
    
//     // Clean up temp directory if it exists
//     sh "rm -rf ${tempDir}"

//     // Clone the GitHub repository
//     sh "git clone ${repoUrl} ${tempDir}"

//     // Check for project type indicators
//     def projectType = 'Unknown'
    
//     // Node.js (package.json)
//     if (fileExists("${tempDir}/package.json")) {
//         projectType = 'Node.js'
//     }
//     // Java (pom.xml or build.gradle)
//     else if (fileExists("${tempDir}/pom.xml")) {
//         projectType = 'Java (Maven)'
//     } else if (fileExists("${tempDir}/build.gradle")) {
//         projectType = 'Java (Gradle)'
//     }
//     // Python (requirements.txt)
//     else if (fileExists("${tempDir}/requirements.txt")) {
//         projectType = 'Python'
//     }
//     // Ruby (Gemfile)
//     else if (fileExists("${tempDir}/Gemfile")) {
//         projectType = 'Ruby'
//     }

//     // Clean up the temporary directory
//     sh "rm -rf ${tempDir}"

//     return projectType
// }









// def call(String repoUrl) {
//     def projectType = detectProjectTypeFromGithub(repoUrl)

//     if (projectType) {
//         echo "Detected project type: ${projectType}"

//         def projectPath = "/tmp/${repoUrl.tokenize('/').last().replace('.git', '')}"
        
//         if (!dockerfileExists(projectPath)) {
//             def packageManager = detectPackageManager(projectPath)
//             writeDockerfile(projectType, projectPath, packageManager)
//         } else {
//             echo "Dockerfile already exists at ${projectPath}/Dockerfile, skipping generation."
//         }

//         return projectType
//     } else {
//         error "Unable to detect the project type for repository: ${repoUrl}."
//     }
// }

// def dockerfileExists(String projectPath) {
//     return fileExists("${projectPath}/Dockerfile")
// }

// def detectProjectTypeFromGithub(String repoUrl) {
//     // Clone the GitHub repository to a temporary location
//     def repoName = repoUrl.tokenize("/").last().replace(".git", "")
//     def tempDir = "/tmp/${repoName}"
    
//     // Clean up temp directory if it exists
//     sh "rm -rf ${tempDir}"

//     // Clone the GitHub repository
//     sh "git clone ${repoUrl} ${tempDir}"

//     // Check for project type indicators
//     def projectType = detectProjectType(tempDir)

//     // Clean up the temporary directory after detection
//     sh "rm -rf ${tempDir}"

//     return projectType
// }

// def detectProjectType(String projectPath) {
//     if (fileExists("${projectPath}/package.json")) {
//         def packageJson = readJSON file: "${projectPath}/package.json"
//         if (packageJson.dependencies?.'next') {
//             return 'nextjs'
//         } else if (packageJson.dependencies?.'react') {
//             return 'react'
//         }
//     } else if (fileExists("${projectPath}/pom.xml")) {
//         return 'springboot-maven'
//     } else if (fileExists("${projectPath}/build.gradle")) {
//         return 'springboot-gradle'
//     } else if (fileExists("${projectPath}/pubspec.yaml")) {
//         return 'flutter'
//     }

//     return null
// }

// def detectPackageManager(String projectPath) {
//     if (fileExists("${projectPath}/package-lock.json")) {
//         return 'npm'
//     } else if (fileExists("${projectPath}/yarn.lock")) {
//         return 'yarn'
//     } else if (fileExists("${projectPath}/pnpm-lock.yaml")) {
//         return 'pnpm'
//     } else if (fileExists("${projectPath}/bun.lockb")) {
//         return 'bun'
//     }
//     return 'npm'
// }

// def writeDockerfile(String projectType, String projectPath, String packageManager) {
//     try {
//         def dockerfileContent = libraryResource "dockerfileTemplates/Dockerfile-${projectType}"
//         dockerfileContent = dockerfileContent.replaceAll("\\{\\{packageManager\\}\\}", packageManager)
//         writeFile file: "${projectPath}/Dockerfile", text: dockerfileContent
//         echo "Dockerfile successfully written for ${projectType} project at ${projectPath}/Dockerfile"
//     } catch (Exception e) {
//         error "Failed to write Dockerfile for ${projectType} project: ${e.message}"
//     }
// }

// def pushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId) {
//     try {
//         withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
//             def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"

//             // Docker login
//             sh """
//             docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
//             """

//             // Tag the image for the registry
//             sh """
//             docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo}
//             """

//             // Push the image to Docker Hub
//             sh """
//             docker push ${dockerHubRepo}
//             """

//             echo "Image pushed to Docker Hub: ${dockerHubRepo}"
//         }
//     } catch (Exception e) {
//         error "Failed to push Docker image: ${e.message}"
//     }
// }


/**
 * Pipeline script for detecting project type, generating Dockerfiles, and pushing Docker images.
 */

def call(String repoUrl) {
    try {
        def projectType = detectProjectTypeFromGithub(repoUrl)

        if (projectType) {
            echo "Detected project type: ${projectType}"

            def projectPath = "/tmp/${repoUrl.tokenize('/').last().replace('.git', '')}"

            if (!dockerfileExists(projectPath)) {
                def packageManager = detectPackageManager(projectPath)
                writeDockerfile(projectType, projectPath, packageManager)
            } else {
                echo "Dockerfile already exists at ${projectPath}/Dockerfile, skipping generation."
            }

            return projectType
        } else {
            error "Unable to detect the project type for repository: ${repoUrl}."
        }
    } catch (Exception e) {
        error "Pipeline failed: ${e.message}"
    }
}

def dockerfileExists(String projectPath) {
    return fileExists("${projectPath}/Dockerfile")
}

/**
 * Detects the project type by analyzing repository content.
 */
// def detectProjectTypeFromGithub(String repoUrl) {
//     def repoName = repoUrl.tokenize("/").last().replace(".git", "")
//     def tempDir = "/tmp/${repoName}"

//     try {
//         sh "rm -rf ${tempDir}"
//         sh "git clone ${repoUrl} ${tempDir}"

//         def projectType = detectProjectType(tempDir)
//         sh "rm -rf ${tempDir}"

//         return projectType
//     } catch (Exception e) {
//         sh "rm -rf ${tempDir}"
//         error "Failed to detect project type: ${e.message}"
//     }
// }

// def detectProjectTypeFromGithub(String repoUrl, boolean cleanUp = true) {
//     def repoName = repoUrl.tokenize("/").last().replace(".git", "")
//     def tempDir = "/tmp/${repoName}"

//     try {
//         sh "rm -rf ${tempDir}"
//         sh "git clone ${repoUrl} ${tempDir}"

//         def projectType = detectProjectType(tempDir)

//         if (cleanUp) {
//             sh "rm -rf ${tempDir}"
//         }

//         return projectType
//     } catch (Exception e) {
//         sh "rm -rf ${tempDir}"
//         error "Failed to detect project type: ${e.message}"
//     }
// }


// def detectProjectType(String projectPath) {
//     if (fileExists("${projectPath}/package.json")) {
//         def packageJson = readJSON file: "${projectPath}/package.json"
//         if (packageJson.dependencies?.'next') {
//             return 'nextjs'
//         } else if (packageJson.dependencies?.'react') {
//             return 'react'
//         }
//     } else if (fileExists("${projectPath}/pom.xml")) {
//         return 'springboot-maven'
//     } else if (fileExists("${projectPath}/build.gradle")) {
//         return 'springboot-gradle'
//     } else if (fileExists("${projectPath}/pubspec.yaml")) {
//         return 'flutter'
//     }

//     return null
// }

// def detectPackageManager(String projectPath) {
//     if (fileExists("${projectPath}/package-lock.json")) {
//         return 'npm'
//     } else if (fileExists("${projectPath}/yarn.lock")) {
//         return 'yarn'
//     } else if (fileExists("${projectPath}/pnpm-lock.yaml")) {
//         return 'pnpm'
//     } else if (fileExists("${projectPath}/bun.lockb")) {
//         return 'bun'
//     }
//     return 'npm'
// }

// /**
//  * Writes a Dockerfile based on the detected project type and package manager.
//  */
// def writeDockerfile(String projectType, String projectPath, String packageManager) {
//     try {
//         def dockerfileContent = libraryResource "dockerfileTemplates/Dockerfile-${projectType}"
//         dockerfileContent = dockerfileContent.replaceAll("\\{\\{packageManager\\}\\}", packageManager)
//         writeFile file: "${projectPath}/Dockerfile", text: dockerfileContent
//         echo "Dockerfile successfully written for ${projectType} project at ${projectPath}/Dockerfile"
//     } catch (Exception e) {
//         error "Failed to write Dockerfile for ${projectType} project: ${e.message}"
//     }
// }

// /**
//  * Pushes a Docker image to Docker Hub.
//  */
// def pushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId) {
//     try {
//         withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
//             def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"

//             sh """
//             docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
//             docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo}
//             docker push ${dockerHubRepo}
//             """

//             echo "Image pushed to Docker Hub: ${dockerHubRepo}"
//         }
//     } catch (Exception e) {
//         error "Failed to push Docker image: ${e.message}"
//     }
// }

// def buildAndPushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId, String dockerfilePath = '.') {
//     try {
//         // Stage 1: Build the Docker image
//         echo "Building Docker image: ${dockerImageName}:${dockerImageTag}"
//         sh """
//         docker build -t ${dockerImageName}:${dockerImageTag} ${dockerfilePath}
//         """

//         // Stage 2: Log in to Docker registry
//         echo "Logging in to Docker registry"
//         withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
//             sh """
//             docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
//             """
//         }

//         // Stage 3: Tag the Docker image
//         echo "Tagging Docker image"
//         def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"
//         sh """
//         docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo}
//         """

//         // Stage 4: Push the Docker image to the registry
//         echo "Pushing Docker image to registry: ${dockerHubRepo}"
//         sh """
//         docker push ${dockerHubRepo}
//         """

//         echo "Docker image ${dockerHubRepo} successfully pushed."
//     } catch (Exception e) {
//         error "Failed to build and push Docker image: ${e.message}"
//     }
// }


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

def buildAndPushDockerImage(String dockerImageName, String dockerImageTag, String credentialsId, String dockerfilePath = '.') {
    try {
        // Stage 1: Build the Docker image
        echo "Building Docker image: ${dockerImageName}:${dockerImageTag}"
        sh """
        docker build -t ${dockerImageName}:${dockerImageTag} ${dockerfilePath} || exit 1
        """

        // Stage 2: Log in to Docker registry
        echo "Logging in to Docker registry"
        withCredentials([usernamePassword(credentialsId: credentialsId, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
            sh """
            docker login -u ${DOCKER_USER} -p ${DOCKER_PASS} || exit 1
            """
        }

        // Stage 3: Tag the Docker image
        echo "Tagging Docker image"
        def dockerHubRepo = "${DOCKER_USER}/${dockerImageName}:${dockerImageTag}"
        sh """
        docker tag ${dockerImageName}:${dockerImageTag} ${dockerHubRepo} || exit 1
        """

        // Stage 4: Push the Docker image to the registry
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


