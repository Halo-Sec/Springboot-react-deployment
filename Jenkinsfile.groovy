pipeline {
  agent {
      label 'agent1'
    }

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
          docker.image('maven').withRun ('-d --rm=false') {
            stage('Build') {
              echo 'Executing: mvn clean package'
              sh 'mvn clean package'
            }

            stage('Test') {
              echo 'Executing: mvn test'
              sh 'mvn test'
            }
          }
        }
      }
    }

    stage('Get Container ID') {
        steps {
            script {
                sh '''
                  containerId=$(docker ps -q --filter ancestor=maven:latest | tr -d '\r\n')
                  jarFile=$(docker exec $containerId find / -type f -name 'react-and-spring-data-rest-0.0.1-SNAPSHOT.jar' | tr -d '\r\n')
                  echo "Container ID: $containerId"
                  echo "JAR file: $jarFile"
                  docker cp $containerId:$jarFile ./
                '''
            }
        }
        
    }

    stage('Deploy') {
      steps {
        script {
          docker.image('chriscamicas/awscli-awsebcli').inside {
            withAWS(credentials: 'aws-credentials') {
              stage('prepare environment') {
                echo "Executing: eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform 'Docker Running on 64bit Amazon Linux 2' --region ${AWS_REGION}"
                sh "eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform 'Docker Running on 64bit Amazon Linux 2' --region ${AWS_REGION}"
                echo "Executing: eb create ${AWS_EB_ENV_NAME} --cname-prefix ${AWS_EB_ENV_NAME} --instance-type t2.micro --platform 'Docker Running on 64bit Amazon Linux 2'"
                sh "eb create ${AWS_EB_ENV_NAME} --cname-prefix ${AWS_EB_ENV_NAME} --instance-type t2.micro --platform 'Docker Running on 64bit Amazon Linux 2'"
              }

              stage('deployment') {
                echo "Executing: eb deploy --label 'version ${BUILD_NUMBER}'"
                sh "eb deploy --label 'version ${BUILD_NUMBER}'"
                echo 'Executing: eb status'
                sh 'eb status'
              }
            }
          }
        }
      }
    }
  }
}
