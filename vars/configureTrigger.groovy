def call(String branch = 'main') {
    script {
        currentBuild.rawBuild.getParent().setScm(
            new hudson.plugins.git.GitSCM(
                hudson.plugins.git.GitSCM.createRepoList(env.GIT_URL, 'origin'),
                [new hudson.plugins.git.BranchSpec("*/${branch}")],
                false, [], null, null, []
            )
        )

        properties([
            pipelineTriggers([githubPush()])
        ])
    }
}
