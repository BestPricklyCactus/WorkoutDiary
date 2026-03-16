pipeline {
    agent {
        docker {
            image 'workoutdiary-jenkins-agent:latest'
            reuseNode true
        }
    }

    options {
        disableConcurrentBuilds()
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle-ci"
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Debug APK') {
            steps {
                sh 'chmod +x gradlew || true'
                sh './gradlew --no-daemon clean assembleDebug'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'app/build/outputs/**/*.apk', fingerprint: true, onlyIfSuccessful: false
        }
        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
