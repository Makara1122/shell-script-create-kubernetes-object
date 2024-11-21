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

@Library('my-share-library-makara') _

pipeline {
    agent any

    parameters {
        string(name: 'APP_NAME', defaultValue: 'makara-nextjs-la', description: 'Name of the Kubernetes service to deploy')
    }

    stages {
        stage('Deploy Service') {
            steps {
                script {
                    // Call the shared library function with the app name passed as a parameter
                    deploy_kubernetes_service(params.APP_NAME)
                }
            }
        }
    }
}

