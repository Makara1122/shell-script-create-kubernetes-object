def call(String appName) {
    // Ensure that Ansible is installed on the Jenkins agent
    sh """
        ansible-playbook deploy-playbook-with-sh-v2.yml -e "app_name=${appName}"
    """
}
