pipeline {
    agent {
        docker {
            image 'workoutdiary-jenkins-agent:latest'
            reuseNode true
        }
    }

    triggers {
        // Nightly build at 00:00 AM
        cron('H 0 * * *')
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
                    triggeredBy 'TimerTrigger'
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
                not { triggeredBy 'TimerTrigger' }
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

        stage('Performance Profiling') {
            when {
                triggeredBy 'TimerTrigger'
            }
            steps {
                echo 'Running Gradle Profiler...'
                sh '''
                    # Install gradle-profiler if not present
                    if [ ! -f "./gradle-profiler" ]; then
                        curl -L -o gradle-profiler.zip https://github.com/gradle/gradle-profiler/releases/latest/download/gradle-profiler.zip
                        unzip gradle-profiler.zip
                    fi

                    # Run profiler
                    ./gradle-profiler --scenario ./build-scenario.profile --output-dir ./profiler-results
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'profiler-results/**/*', allowEmptyArchive: true
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
