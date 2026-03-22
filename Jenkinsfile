pipeline {
    agent {
        docker {
            image 'workoutdiary-jenkins-agent:latest'
            reuseNode true
        }
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle-ci"
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        GRADLE_OPTS = "-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=4"
    }

    stages {
        stage('Prepare') {
            steps {
                sh 'chmod +x gradlew || true'
            }
        }

        stage('Lint & Static Analysis') {
            when {
                anyOf {
                    branch 'feature/*'
                    changeRequest()
                }
            }
            steps {
                echo 'Running Lint for Feature branch...'
                sh './gradlew lintDebug'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/build/reports/lint-results*.xml', allowEmptyArchive: true
                    archiveArtifacts artifacts: '**/build/reports/lint-results*.html', allowEmptyArchive: true
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running Unit Tests...'
                sh './gradlew testDebugUnitTest'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }

        stage('Build Debug APK') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'feature/*'
                    changeRequest()
                }
            }
            steps {
                echo 'Building Debug APK...'
                sh './gradlew assembleDebug'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
                }
            }
        }

        stage('Build Release Bundle (AAB)') {
            when {
                branch 'main'
            }
            steps {
                echo 'Building Release Bundle...'
                sh './gradlew bundleRelease'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/bundle/release/*.aab', fingerprint: true
                }
            }
        }
    }

    post {
        failure {
            echo 'Build failed. Check the logs and artifacts.'
        }
        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
