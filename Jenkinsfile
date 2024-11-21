// @Library('my-share-library-makara') _

// pipeline {
//     agent any

//     stages {
//         stage('Deploy Service') {
//             steps {
//                 script {
//                     // Call the shared library function with the app name as parameter
//                     deploy_kubernetes_service('makara-nextjs-la')
//                 }
//             }
//         }
//     }
// }

// =====================================================================================

// latest version of pipeline 

// @Library('my-share-library-makara') _

// pipeline {
//     agent any

//     parameters {
//         string(name: 'APP_NAME', defaultValue: 'env-nextjs', description: 'Name of the Kubernetes service to deploy')
//     }

//     stages {
//         stage('Deploy Service') {
//             steps {
//                 script {
//                     // Call the shared library function with the app name passed as a parameter
//                     deploy_kubernetes_service(params.APP_NAME)
//                 }
//             }
//         }
//     }
// }


@Library('my-share-library-makara') _

pipeline {
    agent any

    parameters {
        string(name: 'APP_NAME', defaultValue: 'env-nextjs', description: 'Name of the Kubernetes service to deploy')
        string(name: 'PROJECT_PATH', defaultValue: '.', description: 'Path to the project source')
        string(name: 'DOCKER_IMAGE_NAME', defaultValue: 'my-nextjs-app', description: 'Docker image name')
        string(name: 'DOCKER_IMAGE_TAG', defaultValue: 'latest', description: 'Docker image tag')
        string(name: 'DOCKER_REGISTRY_CREDENTIALS', defaultValue: 'github-credentials-id', description: 'GitHub Docker registry credentials ID')
    }

    environment {
        DOCKER_REGISTRY = 'ghcr.io' // GitHub Container Registry
    }

    stages {
        stage('Detect Project Type and Generate Dockerfile') {
            steps {
                script {
                    def projectType = detectProjectType(params.PROJECT_PATH)

                    if (projectType) {
                        echo "Detected project type: ${projectType}"

                        if (!dockerfileExists(params.PROJECT_PATH)) {
                            def packageManager = detectPackageManager(params.PROJECT_PATH)
                            writeDockerfile(projectType, params.PROJECT_PATH, packageManager)
                        } else {
                            echo "Dockerfile already exists at ${params.PROJECT_PATH}/Dockerfile, skipping generation."
                        }
                    } else {
                        error "Unable to detect the project type for ${params.PROJECT_PATH}."
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                    docker build -t ${params.DOCKER_IMAGE_NAME}:${params.DOCKER_IMAGE_TAG} ${params.PROJECT_PATH}
                    """
                    echo "Docker image built successfully: ${params.DOCKER_IMAGE_NAME}:${params.DOCKER_IMAGE_TAG}"
                }
            }
        }

        stage('Push Docker Image to GitHub Container Registry') {
            steps {
                script {
                    pushDockerImage(params.DOCKER_IMAGE_NAME, params.DOCKER_IMAGE_TAG, params.DOCKER_REGISTRY_CREDENTIALS)
                }
            }
        }

        stage('Deploy Service to Kubernetes') {
            steps {
                script {
                    deploy_kubernetes_service(params.APP_NAME)
                }
            }
        }
    }
}


