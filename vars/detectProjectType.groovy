def call(String repoUrl) {
    // Clone the repo temporarily
    def repoName = repoUrl.tokenize("/").last().replace(".git", "")
    def tempDir = "/tmp/${repoName}"
    
    // Clean up temp directory if it exists
    sh "rm -rf ${tempDir}"

    // Clone the GitHub repository
    sh "git clone ${repoUrl} ${tempDir}"

    // Check for project type indicators
    def projectType = 'Unknown'
    
    // Node.js (package.json)
    if (fileExists("${tempDir}/package.json")) {
        projectType = 'Node.js'
    }
    // Java (pom.xml or build.gradle)
    else if (fileExists("${tempDir}/pom.xml")) {
        projectType = 'Java (Maven)'
    } else if (fileExists("${tempDir}/build.gradle")) {
        projectType = 'Java (Gradle)'
    }
    // Python (requirements.txt)
    else if (fileExists("${tempDir}/requirements.txt")) {
        projectType = 'Python'
    }
    // Ruby (Gemfile)
    else if (fileExists("${tempDir}/Gemfile")) {
        projectType = 'Ruby'
    }

    // Clean up the temporary directory
    sh "rm -rf ${tempDir}"

    return projectType
}
