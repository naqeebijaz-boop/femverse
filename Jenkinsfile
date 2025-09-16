pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    triggers {
        cron('*/5 * * * *')   // Run every 5 minutes
    }

    environment {
        SLACK_TOKEN_ID = 'slack-bot-token'   // Jenkins secret for Slack
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
                // Run TestNG using testng.xml
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
                slackSend (
                    channel: "${SLACK_CHANNEL}",
                    message: "✅ Automation Build Finished!\nBranch: ${env.GIT_BRANCH}\nBuild: ${env.BUILD_NUMBER}"
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    // Path of generated report inside Jenkins workspace
                    def reportPath = "${env.WORKSPACE}\\Femverse_API_Report.docx"

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
            echo "✅ Pipeline finished. Slack notified with report."
        }
    }
}
