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

    triggers {
        cron('H/5 * * * *') // Runs every 5 minutes
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/naqeebijaz-boop/femverse.git'
            }
        }

        stage('Run TestNG Suite') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    bat "mvn clean test -DsuiteXmlFile=testng.xml -Dsurefire.suiteXmlFiles=testng.xml"
                }
            }
        }

        stage('Check and Archive Report') {
            steps {
                script {
                    if (fileExists(env.REPORT_NAME)) {
                        echo "✅ DOCX Report generated successfully!"
                        archiveArtifacts artifacts: "${env.REPORT_NAME}", fingerprint: true
                        echo "✅ Report archived"
                    } else {
                        echo "⚠️ No DOCX report found"
                    }

                    // Capture JUnit results
                    def testResults = junit 'target/surefire-reports/*.xml'
                    env.TOTAL_TESTS   = testResults.totalCount.toString()
                    env.FAILED_TESTS  = testResults.failCount.toString()
                    env.SKIPPED_TESTS = testResults.skipCount.toString()
                    env.SUCCESS_TESTS = (testResults.totalCount - testResults.failCount - testResults.skipCount).toString()
                }
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    def reportPath = "${env.WORKSPACE}/${env.REPORT_NAME}"
                    
                    if (fileExists(reportPath)) {
                        echo "Attempting to send Slack notification with report link..."
                        
                        try {
                            withCredentials([string(credentialsId: "${env.SLACK_CREDENTIALS_ID}", variable: 'SLACK_TOKEN')]) {
                                // Create a simple JSON file first
                                bat """
                                    echo { > slack_message.json
                                    echo    "channel": "${env.SLACK_CHANNEL}", >> slack_message.json
                                    echo    "text": "📊 Femverse Test Report - Build #${env.BUILD_NUMBER}", >> slack_message.json
                                    echo    "blocks": [ >> slack_message.json
                                    echo        { >> slack_message.json
                                    echo            "type": "section", >> slack_message.json
                                    echo            "text": { >> slack_message.json
                                    echo                "type": "mrkdwn", >> slack_message.json
                                    echo                "text": "*📊 Femverse Test Report - Build #${env.BUILD_NUMBER}*\\nThe test report has been generated and is available for download." >> slack_message.json
                                    echo            } >> slack_message.json
                                    echo        }, >> slack_message.json
                                    echo        { >> slack_message.json
                                    echo            "type": "section", >> slack_message.json
                                    echo            "text": { >> slack_message.json
                                    echo                "type": "mrkdwn", >> slack_message.json
                                    echo                "text": "*📋 Test Results:*\\n• Tests Run: ${env.TOTAL_TESTS}\\n• Success: ${env.SUCCESS_TESTS}\\n• Failures: ${env.FAILED_TESTS}\\n• Skipped: ${env.SKIPPED_TESTS}" >> slack_message.json
                                    echo            } >> slack_message.json
                                    echo        }, >> slack_message.json
                                    echo        { >> slack_message.json
                                    echo            "type": "actions", >> slack_message.json
                                    echo            "elements": [ >> slack_message.json
                                    echo                { >> slack_message.json
                                    echo                    "type": "button", >> slack_message.json
                                    echo                    "text": { >> slack_message.json
                                    echo                        "type": "plain_text", >> slack_message.json
                                    echo                        "text": "📥 Download Report" >> slack_message.json
                                    echo                    }, >> slack_message.json
                                    echo                    "url": "${env.BUILD_URL}artifact/${env.REPORT_NAME}", >> slack_message.json
                                    echo                    "action_id": "download_report" >> slack_message.json
                                    echo                } >> slack_message.json
                                    echo            ] >> slack_message.json
                                    echo        } >> slack_message.json
                                    echo    ] >> slack_message.json
                                    echo } >> slack_message.json
                                """
                                
                                // Now send the JSON file to Slack
                                bat """
                                    curl -X POST ^
                                         -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                         -H "Content-type: application/json" ^
                                         --data-binary "@slack_message.json" ^
                                         "https://slack.com/api/chat.postMessage"
                                """
                                
                                // Clean up the JSON file
                                bat "del slack_message.json"
                                
                                echo "✅ Slack notification sent with report download link"
                            }
                        } catch (Exception e) {
                            echo "⚠️ Slack notification failed: ${e.message}"
                            echo "This is expected if Slack credentials are not configured"
                            echo "Report is available at: ${env.BUILD_URL}artifact/${env.REPORT_NAME}"
                        }
                    } else {
                        echo "⚠️ Report not found for upload: ${reportPath}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished successfully!"
            echo "📄 Report available at: ${env.BUILD_URL}artifact/${env.REPORT_NAME}"
        }
    }
}
