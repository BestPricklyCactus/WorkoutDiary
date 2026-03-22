pipeline {
    agent {
        docker {
            image 'workoutdiary-jenkins-agent:latest'
            reuseNode true
        }
    }

    triggers {
        // Nightly build at 02:00 AM
        cron('H 2 * * *')
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 2, unit: 'HOURS')
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
                    trigger 'TimerTrigger'
                }
            }
            steps {
                echo 'Running Lint...'
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
            when {
                // Skip unit tests in nightly run to save time,
                // as they are usually covered by regular builds
                not { trigger 'TimerTrigger' }
            }
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
                    branch 'main'
                    branch 'origin/main'
                    branch 'origin/develop'
                    branch 'origin/feature/*'
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
    }

    post {
        failure {
            echo 'Pipeline failed!'
        }
        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}
