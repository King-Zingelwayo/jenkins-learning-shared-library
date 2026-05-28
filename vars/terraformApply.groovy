def call(Map config = [:]) {
    String tfDir   = config.get('dir', '.')
    String roleArn = config.get('roleArn')

    withAwsOidc(roleArn: roleArn) {
        dir(tfDir) {
            unstash 'tfplan'
            sh 'terraform apply -input=false tfplan'
        }
    }
}
