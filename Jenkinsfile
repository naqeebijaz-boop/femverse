pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SLACK_CHANNEL = '#femverse'
        SLACK_CREDENTIALS_ID = 'slack-bot-token'
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
                archiveArtifacts artifacts: 'Femverse_API_Report.docx', fingerprint: true
            }
        }

        stage('Send Slack Notification') {
            steps {
                script {
                    withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                        slackSend(
                            channel: env.SLACK_CHANNEL,
                            color: 'good',
                            message: "✅ Femverse build #${env.BUILD_NUMBER} completed successfully.",
                            token: SLACK_TOKEN
                        )
                    }
                }
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
<<<<<<< HEAD
                    def reportPath = "${env.WORKSPACE}/Femverse_API_Report.docx"
                    
                    if (fileExists(reportPath)) {
                        withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                            // Method 1: Try the new files.uploadV2 endpoint (if available)
                            bat """
                                echo "Attempting to upload report via Slack API..."
                                curl -X POST ^
                                     -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                     -F "file=@${reportPath}" ^
                                     -F "channels=${env.SLACK_CHANNEL}" ^
                                     -F "initial_comment=📊 Femverse Test Report - Build #${env.BUILD_NUMBER}" ^
                                     "https://slack.com/api/files.uploadV2"
                            """
                            
                            // Method 2: If the above fails, use chat.postMessage with a shareable link
                            // First, we'll just send a message since file upload is problematic
                            bat """
                                echo "Sending notification with chat.postMessage..."
                                curl -X POST ^
                                     -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                     -H "Content-type: application/json" ^
                                     -d "{\\"channel\\":\\"${env.SLACK_CHANNEL}\\",\\"text\\":\\"📊 Femverse Test Report - Build #${env.BUILD_NUMBER} has been generated. Check Jenkins artifacts for the full report.\\",\\"attachments\\":[{\\"color\\":\\"#36a64f\\",\\"title\\":\\"Download Report\\",\\"title_link\\":\\"${env.BUILD_URL}artifact/Femverse_API_Report.docx\\",\\"text\\":\\"The test report is available for download from Jenkins\\"}]}" ^
                                     "https://slack.com/api/chat.postMessage"
                            """
                        }
                        echo "✅ Slack notification sent with report information"
                    } else {
                        echo "⚠️ Report not found: ${reportPath}"
                        withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                            slackSend(
                                channel: env.SLACK_CHANNEL,
                                color: 'warning',
                                message: "⚠️ Femverse build #${env.BUILD_NUMBER} completed, but test report was not generated.",
                                token: SLACK_TOKEN
=======
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
>>>>>>> 325ad7b (Resolved merge conflicts)
                            )
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished."
        }
        
        failure {
            script {
                withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                    slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: 'danger',
                        message: "❌ Femverse build #${env.BUILD_NUMBER} failed!",
                        token: SLACK_TOKEN
                    )
                }
            }
        }
    }
}
