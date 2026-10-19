pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        DOCKER_IMAGE = 'punittak2005cse/javaproject'
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
                sh '''
                    echo "Java version:"
                    java -version
                    mvn compile
                '''
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
                            docker build -t ${DOCKER_IMAGE}:${BUILD_ID} .

                            docker tag ${DOCKER_IMAGE}:${BUILD_ID} \
                                ${DOCKER_IMAGE}:${BUILD_ID}

                            docker tag ${DOCKER_IMAGE}:${BUILD_ID} \
                                ${DOCKER_IMAGE}:latest
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
                    ${DOCKER_IMAGE}:${BUILD_ID}
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
                            docker push ${DOCKER_IMAGE}:${BUILD_ID}
                            docker push ${DOCKER_IMAGE}:latest
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
                to: 'punittak2005@gmail.com',
                replyTo: 'punittak2005@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}
