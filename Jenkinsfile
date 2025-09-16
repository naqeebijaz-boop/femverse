pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SLACK_CHANNEL = '#femverse'
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
                slackSend(
                    channel: env.SLACK_CHANNEL,
                    color: 'good',
                    message: "✅ Femverse build #${env.BUILD_NUMBER} completed successfully."
                )
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    def reportPath = "${env.WORKSPACE}/Femverse_API_Report.docx"
                    
                    if (fileExists(reportPath)) {
                        // Use the configured global Slack settings
                        slackUploadFile(
                            channel: env.SLACK_CHANNEL,
                            filePath: reportPath,
                            initialComment: "📊 Femverse Test Report - Build #${env.BUILD_NUMBER}"
                        )
                    } else {
                        echo "⚠️ Report not found: ${reportPath}"
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
            slackSend(
                channel: env.SLACK_CHANNEL,
                color: 'danger',
                message: "❌ Femverse build #${env.BUILD_NUMBER} failed!"
            )
        }
    }
}
