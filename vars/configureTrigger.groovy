def call(String branch = 'main') {
    properties([
        pipelineTriggers([githubPush()])
    ])
}
