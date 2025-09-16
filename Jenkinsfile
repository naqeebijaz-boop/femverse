pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SLACK_CHANNEL = '#femverse'
        REPO_URL = 'https://github.com/naqeebijaz-boop/femverse.git'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: "${REPO_URL}"
            }
        }

        stage('Run TestNG Suite') {
            steps {
                bat "mvn clean test -DsuiteXmlFile=testng.xml"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Send Slack Notification') {
            steps {
                slackSend(
                    channel: "${SLACK_CHANNEL}",
                    tokenCredentialId: 'slack-bot-token',   // Jenkins credential ID
                    color: 'good',
                    message: "✅ Femverse build finished!\nBranch: ${env.GIT_BRANCH}\nBuild: ${env.BUILD_NUMBER}"
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    def reportPath = "target\\Femverse_API_Report.docx"

                    withCredentials([string(credentialsId: 'slack-bot-token', variable: 'SLACK_TOKEN')]) {
                        bat """
                            if exist ${reportPath} (
                                curl -F "file=@${reportPath}" ^
                                     -F "initial_comment=📊 Femverse Test Report - Build #${BUILD_NUMBER}" ^
                                     -F "channels=${SLACK_CHANNEL}" ^
                                     -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                     https://slack.com/api/files.upload
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
            echo "✅ Pipeline finished. Slack notified with report."
        }
    }
}
