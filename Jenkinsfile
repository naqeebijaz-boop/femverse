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
                    bat 'mvn clean test -DsuiteXmlFile=testng.xml -Dsurefire.suiteXmlFiles=testng.xml'
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

                    def testResults = junit 'target/surefire-reports/*.xml'
                    env.TOTAL_TESTS   = testResults.totalCount.toString()
                    env.FAILED_TESTS  = testResults.failCount.toString()
                    env.SKIPPED_TESTS = testResults.skipCount.toString()
                    env.SUCCESS_TESTS = (testResults.totalCount - testResults.failCount - testResults.skipCount).toString()

                    if (fileExists('summary.txt')) {
                        def summary = readFile('summary.txt').trim().split('\n')
                        summary.each { line ->
                            def parts = line.split('=')
                            if (parts.size() == 2) {
                                def key = parts[0].trim()
                                def val = parts[1].trim()
                                if (key == "TOTAL")   { env.TOTAL_TESTS = val }
                                if (key == "PASSED")  { env.SUCCESS_TESTS = val }
                                if (key == "FAILED")  { env.FAILED_TESTS = val }
                                if (key == "SKIPPED") { env.SKIPPED_TESTS = val }
                            }
                        }
                        echo "✅ Loaded test summary from summary.txt"
                    } else {
                        echo "⚠️ summary.txt not found, using default JUnit results"
                    }
                }
            }
        }

        stage('Upload Report to Slack') {
            steps {
                script {
                    if (fileExists(env.REPORT_NAME)) {
                        withCredentials([string(credentialsId: env.SLACK_CREDENTIALS_ID, variable: 'SLACK_TOKEN')]) {
                            def slackMessage = [
                                channel: env.SLACK_CHANNEL,
                                text: "📊 Femverse Test Report - Build #${env.BUILD_NUMBER}",
                                blocks: [
                                    [
                                        type: "section",
                                        text: [
                                            type: "mrkdwn",
                                            text: "*📊 Femverse Test Report - Build #${env.BUILD_NUMBER}*\nThe test report has been generated and is available for download."
                                        ]
                                    ],
                                    [
                                        type: "section",
                                        text: [
                                            type: "mrkdwn",
                                            text: "*📋 Test Results:*\n• Tests Run: ${env.TOTAL_TESTS}\n• ✅ Passes: ${env.SUCCESS_TESTS}\n• ❌ Failures: ${env.FAILED_TESTS}\n• ⏭️ Skipped: ${env.SKIPPED_TESTS}"
                                        ]
                                    ],
                                    [
                                        type: "actions",
                                        elements: [
                                            [
                                                type: "button",
                                                text: [
                                                    type: "plain_text",
                                                    text: "📥 Download Report"
                                                ],
                                                url: "${env.BUILD_URL}artifact/${env.REPORT_NAME}",
                                                action_id: "download_report"
                                            ]
                                        ]
                                    ]
                                ]
                            ]

                            writeFile file: 'slack_message.json', text: groovy.json.JsonOutput.toJson(slackMessage)

                            bat """curl -X POST ^
                                -H "Authorization: Bearer %SLACK_TOKEN%" ^
                                -H "Content-type: application/json" ^
                                --data-binary "@slack_message.json" ^
                                "https://slack.com/api/chat.postMessage"
                            """

                            bat 'del slack_message.json'
                            echo "✅ Slack notification sent with report download link"
                        }
                    } else {
                        echo "⚠️ Report not found for upload"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "✅ Pipeline finished!"
            echo "📄 Report available at: ${env.BUILD_URL}artifact/${env.REPORT_NAME}"
        }
    }
}
