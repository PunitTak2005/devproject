pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {

        stage('Git Checkout') {
            steps {
                git(
                    branch: 'main',
                    credentialsId: 'git-cred',
                    url: 'https://github.com/PunitTak2005/devproject.git'
                )
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('File System Scan') {
            steps {
                sh '''
                    trivy fs \
                    --format table \
                    -o trivy-fs-report.html \
                    .
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                        $SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectName=DevOpsProject \
                        -Dsonar.projectKey=DevOpsProject \
                        -Dsonar.java.binaries=.
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    waitForQualityGate(
                        abortPipeline: true,
                        credentialsId: 'sonar-token'
                    )
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Build & Tag Docker Image') {
            steps {
                script {
                    withDockerRegistry(
                        credentialsId: 'docker-cred'
                    ) {
                        sh """
                            docker build -t ${JOB_NAME}:${BUILD_ID} .

                            docker tag ${JOB_NAME}:${BUILD_ID} \
                                jacksneel/${JOB_NAME}:${BUILD_ID}

                            docker tag ${JOB_NAME}:${BUILD_ID} \
                                jacksneel/${JOB_NAME}:latest
                        """
                    }
                }
            }
        }

        stage('Docker Image Scan') {
            steps {
                sh """
                    trivy image \
                    --format table \
                    -o trivy-image-report.html \
                    jacksneel/${JOB_NAME}:${BUILD_ID}
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withDockerRegistry(
                        credentialsId: 'docker-cred'
                    ) {
                        sh """
                            docker push jacksneel/${JOB_NAME}:${BUILD_ID}
                            docker push jacksneel/${JOB_NAME}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy To Kubernetes') {
            steps {
                withKubeConfig(
                    credentialsId: 'k8-cred',
                    namespace: 'webapps',
                    serverUrl: 'https://172.31.40.204:6443'
                ) {
                    sh '''
                        kubectl apply -f deployment-service.yaml
                    '''
                }
            }
        }
    }

    post {
        always {
            emailext(
                subject: "Jenkins Build #${BUILD_NUMBER} - ${JOB_NAME}",
                body: """
                    <p>Build Status: ${currentBuild.currentResult}</p>
                    <p>
                        Check the build details:
                        <a href="${BUILD_URL}">${BUILD_URL}</a>
                    </p>
                """,
                to: 'sunielmahla@gmail.com',
                replyTo: 'sunielmahla@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}
