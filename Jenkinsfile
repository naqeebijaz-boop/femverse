pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/naqeebijaz-boop/femverse.git'
            }
        }

        stage('Run TestNG Suite') {
            steps {
                bat "mvn clean test -DsuiteXmlFile=testng.xml -Dsurefire.suiteXmlFiles=testng.xml"
            }
        }

        stage('Archive Report') {
            steps {
                // ✅ Save the docx in Jenkins build artifacts
                archiveArtifacts artifacts: 'Femverse_API_Report.docx', fingerprint: true
            }
        }

        stage('Send Slack Notification') {
            steps {
                slackSend(
                    channel: '#femverse',
                    color: 'good',
                    message: "✅ Femverse build #${env.BUILD_NUMBER} completed successfully.",
                    tokenCredentialId: 'slack-bot-token'
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    def reportPath = "${env.WORKSPACE}/Femverse_API_Report.docx"  // ✅ root workspace

                    withCredentials([string(credentialsId: 'slack-bot-token', variable: 'SLACK_TOKEN')]) {
                        bat """
                            if exist "${reportPath}" (
                                curl -F "file=@${reportPath}" ^
                                     -F "channel=#femverse" ^
                                     -F "initial_comment=📊 Femverse Test Report - Build #${env.BUILD_NUMBER}" ^
                                     -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                     https://slack.com/api/files.uploadV2
                            ) else (
                                echo Report not found: ${reportPath}
                            )
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished. Slack notified with report (if available)."
        }
    }
}
