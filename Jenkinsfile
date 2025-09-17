pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SLACK_CHANNEL = '#femverse'
        SLACK_CREDENTIALS_ID = 'slack-bot-token'
        REPORT_NAME = 'Femverse_API_Report.docx'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/naqeebijaz-boop/femverse.git'
            }
        }

        stage('Clean Workspace') {
            steps {
                bat """
                    echo "Cleaning up any existing report..."
                    if exist "${env.REPORT_NAME}" del "${env.REPORT_NAME}"
                    if exist "target/${env.REPORT_NAME}" del "target/${env.REPORT_NAME}"
                    if exist "test-output/${env.REPORT_NAME}" del "test-output/${env.REPORT_NAME}"
                """
            }
        }

        stage('Run TestNG Suite') {
            steps {
                bat "mvn clean test -DsuiteXmlFile=testng.xml -Dsurefire.suiteXmlFiles=testng.xml"
            }
        }

        stage('Locate and Verify Report') {
            steps {
                script {
                    echo "🔍 Searching for generated report..."
                    
                    // Check all possible locations
                    def locations = [
                        "${env.WORKSPACE}/${env.REPORT_NAME}",
                        "${env.WORKSPACE}/target/${env.REPORT_NAME}", 
                        "${env.WORKSPACE}/test-output/${env.REPORT_NAME}",
                        "./${env.REPORT_NAME}",
                        "target/${env.REPORT_NAME}",
                        "test-output/${env.REPORT_NAME}"
                    ]
                    
                    def reportFound = false
                    def actualReportPath = ""
                    
                    locations.each { location ->
                        if (fileExists(location)) {
                            actualReportPath = location
                            reportFound = true
                            echo "✅ Found report at: ${location}"
                        }
                    }
                    
                    if (reportFound) {
                        // Copy to workspace root for easy access
                        bat """
                            copy "${actualReportPath}" "${env.REPORT_NAME}"
                            echo "Report copied to workspace root"
                            echo "File size:"
                            for %%i in ("${env.REPORT_NAME}") do echo %%~zi bytes
                        """
                    } else {
                        echo "❌ Report not found in any location!"
                        // Create a debug report
                        bat """
                            echo "Test Execution Summary" > "${env.REPORT_NAME}"
                            echo "Build: ${env.BUILD_NUMBER}" >> "${env.REPORT_NAME}"
                            echo "Date: %DATE% %TIME%" >> "${env.REPORT_NAME}"
                            echo "Status: Tests ran but report generation failed" >> "${env.REPORT_NAME}"
                            echo "Check HtmlToDocxReport listener implementation" >> "${env.REPORT_NAME}"
                        """
                    }
                }
            }
        }

        stage('Debug: Check File System') {
            steps {
                bat """
                    echo "=== DEBUG: File System Analysis ==="
                    echo "Workspace: ${env.WORKSPACE}"
                    echo "Current dir:"
                    cd
                    echo "Files in workspace:"
                    dir /b
                    echo "Files in target:"
                    dir target /b
                    echo "Files in test-output:"
                    if exist test-output (dir test-output /b) else (echo test-output directory not found)
                    echo "Report file info:"
                    if exist "${env.REPORT_NAME}" (
                        echo "Report exists at root"
                        for %%i in ("${env.REPORT_NAME}") do echo Size: %%~zi bytes
                    ) else (
                        echo "Report not found at root"
                    )
                """
            }
        }

        stage('Archive Report') {
            steps {
                script {
                    if (fileExists(env.REPORT_NAME)) {
                        archiveArtifacts artifacts: "${env.REPORT_NAME}", fingerprint: true
                        echo "✅ Report archived successfully"
                    } else {
                        error "❌ No report found to archive after all attempts"
                    }
                }
            }
        }

        stage('Send Slack Notification') {
            steps {
                script {
                    withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                        slackSend(
                            channel: env.SLACK_CHANNEL,
                            color: 'good',
                            message: "✅ Femverse build #${env.BUILD_NUMBER} completed. Report available for download.",
                            token: SLACK_TOKEN
                        )
                    }
                }
            }
        }

        stage('Upload Report Info to Slack') {
            steps {
                script {
                    withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                        bat """
                            curl -X POST ^
                                 -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                 -H "Content-type: application/json" ^
                                 -d "{
                                    \\"channel\\":\\"${env.SLACK_CHANNEL}\\",
                                    \\"text\\":\\"📊 Femverse Test Report - Build #${env.BUILD_NUMBER}\\",
                                    \\"blocks\\": [
                                        {
                                            \\"type\\": \\"section\\",
                                            \\"text\\": {
                                                \\"type\\": \\"mrkdwn\\",
                                                \\"text\\": \\"*📊 Femverse Test Report - Build #${env.BUILD_NUMBER}*\\n✅ Test execution completed\\"
                                            }
                                        },
                                        {
                                            \\"type\\": \\"section\\",
                                            \\"text\\": {
                                                \\"type\\": \\"mrkdwn\\",
                                                \\"text\\": \\"*📋 Report Available at:*\\n${env.BUILD_URL}artifact/${env.REPORT_NAME}\\"
                                            }
                                        },
                                        {
                                            \\"type\\": \\"actions\\",
                                            \\"elements\\": [
                                                {
                                                    \\"type\\": \\"button\\",
                                                    \\"text\\": {
                                                        \\"type\\": \\"plain_text\\",
                                                        \\"text\\": \\"📥 Download Report\\"
                                                    },
                                                    \\"url\\": \\"${env.BUILD_URL}artifact/${env.REPORT_NAME}\\"
                                                },
                                                {
                                                    \\"type\\": \\"button\\",
                                                    \\"text\\": {
                                                        \\"type\\": \\"plain_text\\",
                                                        \\"text\\": \\"🔍 View Build\\"
                                                    },
                                                    \\"url\\": \\"${env.BUILD_URL}\\"
                                                }
                                            ]
                                        }
                                    ]
                                 }" ^
                                 "https://slack.com/api/chat.postMessage"
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished."
            
            // Final verification
            bat """
                echo "=== FINAL VERIFICATION ==="
                echo "Workspace content:"
                dir /b
                echo "Report file details:"
                if exist "${env.REPORT_NAME}" (
                    for %%i in ("${env.REPORT_NAME}") do echo "File: %%i, Size: %%~zi bytes"
                ) else (
                    echo "❌ REPORT FILE NOT FOUND!"
                )
            """
        }
        
        failure {
            script {
                withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                    slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: 'danger',
                        message: "❌ Femverse build #${env.BUILD_NUMBER} failed! Check Jenkins for details.",
                        token: SLACK_TOKEN
                    )
                }
            }
        }
    }
}
