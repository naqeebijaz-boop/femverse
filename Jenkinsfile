pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        SLACK_CHANNEL = '#femverse'
        REPORT_NAME = 'Femverse_API_Report.docx'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/naqeebijaz-boop/femverse.git'
            }
        }

        stage('Debug: Check Project Structure') {
            steps {
                bat """
                    echo === PROJECT STRUCTURE ANALYSIS ===
                    echo Workspace: ${env.WORKSPACE}
                    echo Current directory:
                    cd
                    echo Java source files:
                    dir src/test/java/com/product/femverse/femverse /b
                    echo testng.xml content:
                    type testng.xml
                """
            }
        }

        stage('Clean and Compile') {
            steps {
                bat """
                    echo Cleaning and compiling...
                    mvn clean test-compile
                    echo Compiled classes:
                    dir target/test-classes/com/product/femverse/femverse /b
                """
            }
        }

        stage('Run Tests with Debug') {
            steps {
                bat """
                    echo Running tests with detailed output...
                    mvn test -DsuiteXmlFile=testng.xml -Dsurefire.suiteXmlFiles=testng.xml -X
                """
            }
        }

        stage('Check Results') {
            steps {
                bat """
                    echo === TEST EXECUTION RESULTS ===
                    echo Surefire reports:
                    if exist target/surefire-reports (
                        dir target/surefire-reports /b
                        echo Test results:
                        type target\\surefire-reports\\*.txt 2>nul || echo No text reports
                    )
                    
                    echo TestNG reports:
                    if exist test-output (
                        dir test-output /b
                    )
                    
                    echo Checking for DOCX report:
                    if exist "${env.REPORT_NAME}" (
                        echo DOCX report found!
                        for %%i in ("${env.REPORT_NAME}") do echo Size: %%~zi bytes
                    ) else (
                        echo No DOCX report generated
                    )
                """
            }
        }

        stage('Generate Report if Missing') {
            steps {
                script {
                    if (!fileExists(env.REPORT_NAME)) {
                        echo "Creating manual test report since no tests ran..."
                        bat """
                            echo FEMVERSE API TEST REPORT > "${env.REPORT_NAME}"
                            echo ========================= >> "${env.REPORT_NAME}"
                            echo Build Number: ${env.BUILD_NUMBER} >> "${env.REPORT_NAME}"
                            echo Execution Date: %DATE% %TIME% >> "${env.REPORT_NAME}"
                            echo Status: No tests executed >> "${env.REPORT_NAME}"
                            echo >> "${env.REPORT_NAME}"
                            echo Possible issues: >> "${env.REPORT_NAME}"
                            echo 1. Test classes not found by TestNG >> "${env.REPORT_NAME}"
                            echo 2. Incorrect package structure in testng.xml >> "${env.REPORT_NAME}"
                            echo 3. Test compilation failed >> "${env.REPORT_NAME}"
                        """
                    }
                }
            }
        }

        stage('Archive Results') {
            steps {
                archiveArtifacts artifacts: "**/*.docx, **/surefire-reports/*, **/test-output/*", fingerprint: true
                junit 'target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            echo "=== FINAL STATUS ==="
            bat """
                echo Workspace content:
                dir /b
                echo Report file:
                if exist "${env.REPORT_NAME}" (
                    for %%i in ("${env.REPORT_NAME}") do echo "File: %%i, Size: %%~zi bytes"
                )
            """
        }
    }
}
