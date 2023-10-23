#!/usr/bin/env groovy

/* `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
buildPlugin(configurations: [
    [ platform: 'linux', jdk: '11' ],
    [ platform: 'windows', jdk: '11' ],
    // Compilation fails on Java 17
    // [ platform: 'linux', jdk: '17' ],
])
