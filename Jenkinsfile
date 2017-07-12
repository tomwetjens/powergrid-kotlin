node {
    stage('Checkout') {
        checkout scm
    }

    stage('Build') {
        env.JAVA_HOME = tool 'jdk8'
        env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

        sh './gradlew clean build'
    }

    stage('Docker') {
        def tag = env.BRANCH_NAME == 'master' ? 'latest' : "${env.BRANCH_NAME}-${env.BUILD_ID}"

        sh "docker build -t powerline:$tag"
        sh "docker tag powerline:$tag registry.swarm.wetjens.com/poweline:$tag ."
        sh "docker push registry.swarm.wetjens.com/powerline:$tag"
    }

    if (env.BRANCH_NAME == 'master') {
        stage('Deploy Prod') {
            sh "docker stack deploy -c docker-compose.yml --prune"
        }
    }
}