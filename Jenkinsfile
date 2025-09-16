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
                    def reportPath = "${env.WORKSPACE}/Femverse_API_Report.docx"
                    
                    if (fileExists(reportPath)) {
                        slackUploadFile(
                            channel: '#femverse',
                            filePath: reportPath,
                            initialComment: "📊 Femverse Test Report - Build #${env.BUILD_NUMBER}",
                            tokenCredentialId: 'slack-bot-token'
                        )
                        echo "✅ Report successfully uploaded to Slack"
                    } else {
                        echo "⚠️ Report not found: ${reportPath}"
                        // Send a notification that the report wasn't found
                        slackSend(
                            channel: '#femverse',
                            color: 'warning',
                            message: "⚠️ Femverse build #${env.BUILD_NUMBER} completed, but test report was not generated.",
                            tokenCredentialId: 'slack-bot-token'
                        )
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished. Slack notified with report (if available)."
            
            // Clean up workspace if needed
            cleanWs()
        }
        
        failure {
            slackSend(
                channel: '#femverse',
                color: 'danger',
                message: "❌ Femverse build #${env.BUILD_NUMBER} failed!",
                tokenCredentialId: 'slack-bot-token'
            )
        }
    }
}
