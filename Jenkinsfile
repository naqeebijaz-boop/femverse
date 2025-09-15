pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    triggers {
        cron('*/5 * * * *')
    }

    environment {
        GIT_CREDENTIALS_ID = 'github-credentials'  // Jenkins credentials for GitHub
        SLACK_TOKEN_ID = 'slack-bot-token'        // Jenkins credentials for Slack
        SLACK_CHANNEL = '#femverse'
        REPO_URL = 'https://github.com/naqeebijaz-boop/femverse.git'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: "${REPO_URL}", credentialsId: "${GIT_CREDENTIALS_ID}"
            }
        }

        stage('Build & Test') {
            steps {
                bat "mvn clean test"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Rename Report with Timestamp') {
            steps {
                script {
                    def timestamp = new Date().format("yyyyMMdd_HHmm")
                    env.REPORT_FILE = "Femverse_API_Report_${timestamp}.docx"
                    bat "rename Femverse_API_Report.docx ${env.REPORT_FILE}"
                }
            }
        }

        stage('Commit & Push Report to GitHub') {
            steps {
                script {
                    bat """
                        git config user.email "naqeeb.ijaz@imaginationai.net"
                        git config user.name "Naqeeb Ijaz"
                        git add ${env.REPORT_FILE}
                        git commit -m "📄 Update Femverse report: Build #${BUILD_NUMBER}" || echo No changes to commit
                        git push origin main
                    """
                }
            }
        }

        stage('Send Slack Notification') {
            steps {
                slackSend (
                    channel: "${SLACK_CHANNEL}",
                    message: "✅ Automation Build Finished!\nBranch: ${env.GIT_BRANCH}\nBuild: ${env.BUILD_NUMBER}"
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    def reportPath = "${env.WORKSPACE}\\${env.REPORT_FILE}"
                    withCredentials([string(credentialsId: "${SLACK_TOKEN_ID}", variable: 'SLACK_TOKEN')]) {
                        bat """
                            curl -F "file=@${reportPath}" ^
                                 -F "initial_comment=📊 Femverse Test Report - Build #${BUILD_NUMBER}" ^
                                 -F "channels=${SLACK_CHANNEL}" ^
                                 -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                 https://slack.com/api/files.upload
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished. Check GitHub & Slack for the latest report."
        }
    }
}
