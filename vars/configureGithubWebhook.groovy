def call(Map config = [:]) {
    String credentialsId = config.get('credentialsId', 'github-token')
    String jenkinsUrl    = config.get('jenkinsUrl', env.JENKINS_URL)

    withCredentials([string(credentialsId: credentialsId, variable: 'GITHUB_TOKEN')]) {
        script {
            // Parse owner/repo from GIT_URL
            // supports both https://github.com/owner/repo.git and git@github.com:owner/repo.git
            String gitUrl   = env.GIT_URL
            String repoPath = gitUrl.replaceAll(/.*github\.com[:/]/, '').replaceAll(/\.git$/, '')

            String webhookUrl = "${jenkinsUrl.replaceAll('/+$', '')}/github-webhook/"

            // Check if webhook already exists
            def response = sh(
                script: """
                    curl -s -o /dev/null -w "%{http_code}" \\
                        -H "Authorization: token ${GITHUB_TOKEN}" \\
                        -H "Accept: application/vnd.github+json" \\
                        https://api.github.com/repos/${repoPath}/hooks
                """,
                returnStdout: true
            ).trim()

            if (response == '200') {
                def hooks = sh(
                    script: """
                        curl -s \\
                            -H "Authorization: token ${GITHUB_TOKEN}" \\
                            -H "Accept: application/vnd.github+json" \\
                            https://api.github.com/repos/${repoPath}/hooks
                    """,
                    returnStdout: true
                ).trim()

                if (hooks.contains(webhookUrl)) {
                    echo "Webhook already exists, skipping creation."
                    return
                }
            }

            // Create webhook
            sh """
                curl -s -X POST \\
                    -H "Authorization: token ${GITHUB_TOKEN}" \\
                    -H "Accept: application/vnd.github+json" \\
                    https://api.github.com/repos/${repoPath}/hooks \\
                    -d '{
                        "name": "web",
                        "active": true,
                        "events": ["push"],
                        "config": {
                            "url": "${webhookUrl}",
                            "content_type": "json",
                            "insecure_ssl": "0"
                        }
                    }'
            """
            echo "Webhook created for ${repoPath} → ${webhookUrl}"
        }
    }
}
