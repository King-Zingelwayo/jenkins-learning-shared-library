def call(Map config = [:]) {
    String credentialsId = config.get('credentialsId', 'github-token')
    String jenkinsUrl    = config.get('jenkinsUrl', env.JENKINS_URL)

    withCredentials([string(credentialsId: credentialsId, variable: 'GITHUB_TOKEN')]) {
        String gitUrl    = env.GIT_URL.replace('https://github.com/', '').replace('git@github.com:', '').replace('.git', '')
        String repoPath  = gitUrl
        String webhookUrl = "${jenkinsUrl.replaceAll('/+$', '')}/github-webhook/"

        def hooks = sh(
            script: """
                curl -s \\
                    -H "Authorization: token \$GITHUB_TOKEN" \\
                    -H "Accept: application/vnd.github+json" \\
                    https://api.github.com/repos/${repoPath}/hooks
            """,
            returnStdout: true
        ).trim()

        if (hooks.contains(webhookUrl)) {
            echo "Webhook already exists, skipping creation."
            return
        }

        sh """
            curl -s -X POST \\
                -H "Authorization: token \$GITHUB_TOKEN" \\
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
