def call(String appName) {
    // Debug: List current directories
    sh "echo 'Listing files in current directory'; pwd; ls -al"

    // Debug: List files in ansible workspace
    sh "echo 'Listing files in ansible workspace'; ls -al /home/jenkins/ansible_workspace"

    // Run the Ansible playbook
    sh """
        ansible-playbook -i /home/jenkins/ansible_workspace/inventory.ini /home/jenkins/ansible_workspace/deploy-playbook-with-sh-v2.yml -e "app_name=${appName}"
    """
}
