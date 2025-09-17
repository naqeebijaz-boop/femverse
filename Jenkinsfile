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
                    def reportPath = "${env.WORKSPACE}/Femverse_API_Report.docx"
                    
                    if (fileExists(reportPath)) {
                        withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                            // Use the correct Slack API approach
                            bat """
                                echo Uploading report to Slack...
                                curl -X POST ^
                                     -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                     -H "Content-Type: multipart/form-data" ^
                                     -F "file=@${reportPath}" ^
                                     -F "channels=${env.SLACK_CHANNEL}" ^
                                     -F "initial_comment=📊 Femverse Test Report - Build #${env.BUILD_NUMBER}" ^
                                     "https://slack.com/api/files.upload"
                            """
                        }
                        echo "✅ Report uploaded to Slack"
                    } else {
                        echo "⚠️ Report not found: ${reportPath}"
                        withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                            slackSend(
                                channel: env.SLACK_CHANNEL,
                                color: 'warning',
                                message: "⚠️ Femverse build #${env.BUILD_NUMBER} completed, but test report was not generated.",
                                token: SLACK_TOKEN
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
