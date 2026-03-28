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
                    branch 'develop'
                    branch 'main'
                    expression { env.BRANCH_NAME?.startsWith('feature/') }
                    changeRequest()
                    triggeredBy 'TimerTrigger'
                }
            }
            steps {
                parallel(
                    'Lint': {
                        echo 'Running Lint...'
                        sh './gradlew lintDebug'
                    },
                    'Detekt': {
                        echo 'Running Detekt...'
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            sh './gradlew detektAll --parallel'
                        }
                    }
                )
            }
            post {
                always {
                    echo 'Searching for XML reports...'
                    sh 'find . -path "*/build/reports/*" -name "*.xml" | sort || true'

                    archiveArtifacts artifacts: '**/build/reports/lint-results*.xml', allowEmptyArchive: true
                    archiveArtifacts artifacts: '**/build/reports/detekt/*.xml', allowEmptyArchive: true
                    archiveArtifacts artifacts: '**/build/reports/detekt/*.html', allowEmptyArchive: true

                    recordIssues(tools: [
                        androidLintParser(pattern: '**/build/reports/lint-results*.xml', id: 'android-lint', name: 'Android Lint'),
                        detekt(pattern: '**/build/reports/detekt/*.xml', id: 'detekt', name: 'Detekt')
                    ])
                }
            }
        }

        stage('Unit Tests') {
            when {
                not { triggeredBy 'TimerTrigger' }
            }
            steps {
                echo 'Running Unit Tests with coverage...'
                sh './gradlew testDebugUnitTest jacocoTestReport'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                    archiveArtifacts artifacts: '**/build/reports/jacoco/**/*.xml', allowEmptyArchive: true
                    archiveArtifacts artifacts: '**/build/reports/jacoco/**/*.html', allowEmptyArchive: true
                    script {
                        def reportPath = 'app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml'
                        if (fileExists(reportPath)) {
                            def lineData = sh(script: '''python3 - <<'PY'
from pathlib import Path

text = Path("app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml").read_text(encoding="utf-8")
marker = 'counter type="LINE" missed="'
start = text.find(marker)
if start != -1:
    start += len(marker)
    rest = text[start:]
    missed, rest = rest.split('" covered="', 1)
    covered = rest.split('"', 1)[0]
    print(missed, covered)
PY''', returnStdout: true).trim()
                            if (lineData) {
                                def parts = lineData.tokenize(' ')
                                def missed = parts[0].toInteger()
                                def covered = parts[1].toInteger()
                                def total = missed + covered
                                def coverage = total > 0 ? (covered * 100.0 / total) : 0.0
                                echo String.format('JaCoCo LINE coverage: %.2f%% (%d/%d)', coverage, covered, total)
                                currentBuild.description = String.format('Coverage: %.2f%%', coverage)
                            }
                        }
                    }
                }
            }
        }

        stage('Build Debug APK') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { env.BRANCH_NAME?.startsWith('feature/') }
                    changeRequest()
                }
            }
            steps {
                echo 'Building Debug APK...'
                sh './gradlew assembleDebug'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'app/build/outputs/**/*.apk, app/build/outputs/**/*.aab', fingerprint: true, allowEmptyArchive: true
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
