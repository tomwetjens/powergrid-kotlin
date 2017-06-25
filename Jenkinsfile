node {
    stage('Checkout') {
        checkout scm
    }

    stage('Build and Test') {
        env.JAVA_HOME = tool 'jdk8'
        env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

        sh './gradlew clean build'
    }

    if (env.BRANCH_NAME == 'master') {
        stage('Deploy Prod') {
            sh 'sudo service powerline stop'

            sh 'cp -fv server/build/libs/server-1.0.0.jar /opt/powerline/server.jar'

            sh 'sudo service powerline start'
        }
    }
}