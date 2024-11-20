def call(String appName) {
    // Debug: List current directory
    sh """
        echo 'Listing files in current directory';
        pwd;
        ls -al;
    """
    
    // Debug: List files in the correct workspace path
    sh """
        echo 'Listing files in the Jenkins workspace';
        ls -al /home/jenkins/.jenkins/jobs/jenkins-ansible-v1/workspace;
    """

    // Run the Ansible playbook with corrected paths
    sh """
        ansible-playbook -i /home/jenkins/.jenkins/jobs/jenkins-ansible-v1/workspace/inventory.ini /home/jenkins/.jenkins/jobs/jenkins-ansible-v1/workspace/vars/deploy-playbook-with-sh-v2.yml -e "app_name=${appName}"
    """
}
