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
        sh 'mvn clean package'
        sh 'mvn test'
      }
    }

    stage('Deploy') {
      steps {
        script {
          docker.image('chriscamicas/awscli-awsebcli').inside {
            withAWS(credentials: 'aws-credentials') {
              stage('prepare environment') {
                echo "Executing: eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform '64bit Amazon Linux 2 v3.5.9 running Docker' --region ${AWS_REGION}"
                sh "eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform '64bit Amazon Linux 2 v3.5.9 running Docker' --region ${AWS_REGION}"
                echo "Executing: eb create ${AWS_EB_ENV_NAME} --cname-prefix ${AWS_EB_ENV_NAME} --instance-type t2.micro --platform 'Docker Running on 64bit Amazon Linux 2'"
                sh "eb create ${AWS_EB_ENV_NAME}"
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
