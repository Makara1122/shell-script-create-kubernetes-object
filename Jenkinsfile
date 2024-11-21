@Library('my-share-library-makara') _

pipeline {
    agent any

    stages {
        stage('Deploy Service') {
            steps {
                script {
                    // Call the shared library function with the app name as parameter
                    deploy_kubernetes_service('makara-nextjs-la')
                }
            }
        }
    }
}
