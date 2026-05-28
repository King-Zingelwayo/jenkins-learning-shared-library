def call(Map config = [:]) {
    String tfDir   = config.get('dir', '.')
    String workspace = config.get('workspace', 'default')
    String roleArn = config.get('roleArn')

    echo "Running Terraform plan in '${tfDir}' (workspace: ${workspace})"

    withAwsOidc(roleArn: roleArn) {
        dir(tfDir) {
            sh 'terraform init -input=false'
            sh "terraform workspace select ${workspace} || terraform workspace new ${workspace}"
            sh 'terraform validate'
            sh 'terraform plan -out=tfplan -input=false -no-color -compact-warnings'
            stash name: 'tfplan', includes: 'tfplan'
        }
    }
}
