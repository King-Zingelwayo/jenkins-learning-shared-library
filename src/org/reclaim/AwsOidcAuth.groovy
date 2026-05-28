package org.reclaim

class AwsOidcAuth implements Serializable {

    private final def script

    AwsOidcAuth(def script) {
        this.script = script
    }

    def withRole(String roleArn, String region = 'eu-west-1', Closure body) {
        script.withCredentials([script.string(credentialsId: 'jenkins-oidc-token', variable: 'OIDC_TOKEN')]) {
            def creds = script.sh(
                script: """
                    aws sts assume-role-with-web-identity \\
                        --role-arn ${roleArn} \\
                        --role-session-name jenkins-${script.env.BUILD_NUMBER} \\
                        --web-identity-token \$OIDC_TOKEN \\
                        --duration-seconds 3600 \\
                        --query 'Credentials' \\
                        --output json
                """,
                returnStdout: true
            ).trim()

            def json = script.readJSON text: creds

            script.withEnv([
                "AWS_ACCESS_KEY_ID=${json.AccessKeyId}",
                "AWS_SECRET_ACCESS_KEY=${json.SecretAccessKey}",
                "AWS_SESSION_TOKEN=${json.SessionToken}",
                "AWS_DEFAULT_REGION=${region}"
            ]) {
                body()
            }
        }
    }
}
