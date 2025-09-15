pipeline {
    agent any

    tools {
        maven 'Maven'       // Use Maven configured in Jenkins
        jdk 'JDK21'         // Use JDK configured in Jenkins
    }

    triggers {
        cron('H 9 * * *')   // Run every day at 9 AM (server time)
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/your-repo.git'
            }
        }

        stage('Build & Test') {
            steps {
                bat "mvn clean test"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'  // Publish TestNG results
                }
            }
        }

        stage('Send Slack Notification') {
            steps {
                slackSend (
                    channel: '#femverse',
                    message: "✅ Automation Build Finished!\nBranch: ${env.GIT_BRANCH}\nBuild: ${env.BUILD_NUMBER}"
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    // Path of your generated docx file
                    def reportPath = "src/test-report.docx"

                    // Slack channel
                    def channel = "#femverse"

                    // Use stored secret instead of hardcoding
                    withCredentials([string(credentialsId: 'slack-bot-token', variable: 'SLACK_TOKEN')]) {
                        sh """
                            curl -F "file=@${reportPath}" \
                                 -F "initial_comment=📊 Daily Test Report from Jenkins Build ${env.BUILD_NUMBER}" \
                                 -F "channels=${channel}" \
                                 -H "Authorization: Bearer $SLACK_TOKEN" \
                                 https://slack.com/api/files.upload
                        """
                    }
                }
            }
        }
    }
}
