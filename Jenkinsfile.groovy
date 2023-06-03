pipeline {
  agent any

  environment {
    AWS_EB_APP_NAME = 'springbootapp'
    AWS_EB_ENV_NAME = 'springbootapp-env'
    AWS_REGION = 'us-east-1'
  }

  triggers {
    pollSCM('*/1 * * * *')
  }

  stages {
    stage('checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build and test') {
      steps {
        script {
          docker.image('maven').inside {
            stages{
              stage('Build') {
                steps {
                  sh 'mvn clean package'
                }
              }
              
              stage('Test') {
                steps {
                  sh 'mvn test'
                }
              }
            }
          }
        }
      }
    }

    stage('Get Container ID') {
      steps {
        script {
          def containerId = sh(script: 'docker ps -q --filter ancestor=maven:latest', returnStdout: true).trim()
          def jarFile = sh(script: "docker exec ${containerId} find . -type f -name 'react-and-spring-data-rest-0.0.1-SNAPSHOT.jar'", returnStdout: true).trim()
          sh "docker cp ${containerId}:${jarFile} ./"
        }
      }
    }

    stage('Deploy') {
      steps {
        script {
          docker.image('chriscamicas/awscli-awsebcli').inside {
            withAWS(credentials: 'aws-credentials') {
              stage('prepare environment') {
                sh "eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform 'Docker Running on 64bit Amazon Linux 2' --region ${AWS_REGION}"
                sh "eb create ${AWS_EB_ENV_NAME} --cname-prefix ${AWS_EB_ENV_NAME} --instance-type t2.micro --platform 'Docker Running on 64bit Amazon Linux 2'"
              }
            }
          }
        }
      }
    }
  }
}
