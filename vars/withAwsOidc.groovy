def call(Map config = [:], Closure body) {
    String roleArn = config.get('roleArn')
    String region  = config.get('region', 'eu-west-1')

    if (!roleArn) {
        error "withAwsOidc: 'roleArn' is required"
    }

    def auth = new org.reclaim.AwsOidcAuth(this)
    auth.withRole(roleArn, region, body)
}
