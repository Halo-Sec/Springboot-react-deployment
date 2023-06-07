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
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build and Test') {
      steps {
        sh 'mvn clean package'
        sh 'mvn test'
      }
    }

    stage('Deploy') {
      steps {
        withAWS(credentials: 'aws-credentials') {
          sh "eb init ${AWS_EB_APP_NAME} --keyname 'Spring' --platform 'Docker Running on 64bit Amazon Linux 2' --region ${AWS_REGION}"
          sh "eb create ${AWS_EB_ENV_NAME} --cname-prefix ${AWS_EB_ENV_NAME} --instance-type t2.micro --platform 'Docker Running on 64bit Amazon Linux 2'"
          sh "eb deploy --label 'version ${BUILD_NUMBER}'"
          sh 'eb status'
        }
      }
    }
  }
}
