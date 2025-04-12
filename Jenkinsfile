pipeline {
	agent {
		node {
			label 'spot-slaves'
		}
	}
	environment {
	    service = "neutrino"
        registry = "z21labs/neutrino"
        registryCredential = "dockerhub"
        version = "1.0.1"
	}
	stages {
		stage('Maven Build') {
		   steps {
			  sh 'mvn -B -DskipTests -Drevision=0.0.0-SNAPSHOT clean package'
			  echo 'maven build success'
		   }
	    }

	    stage("Docker Build") {
            steps {
                script {
                    BUILD_ARGS="--build-arg JAR_FILE=target/neutrino-0.0.0-SNAPSHOT.jar"
                    docker.withTool("docker") {
                        dockerImage = docker.build("$registry:$BUILD_NUMBER", "${BUILD_ARGS} .")
                    }
                }
            }
		  }

        stage("Push") {
            steps {
                script {
                    docker.withTool("docker") {
                        sh "bash ./ciBuild.sh"
                    }
                }
            }
        }

        stage("Code Coverage") {
            steps {
                jacoco buildOverBuild: true, changeBuildStatus: true, deltaLineCoverage: '1'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage("PushECR") {
            steps {
                script {
                    docker.withTool("docker") {
                        docker.withRegistry('https://342593766919.dkr.ecr.us-east-2.amazonaws.com', 'ecr:us-east-2:aws-ecr-cred') {
                            finalver = "${version}" + "." + env.BUILD_NUMBER
                            dockerImage.push(finalver)
                        }
                    }
                }
            }
        }

        stage("Analysis") {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    sh "mvn sonar:sonar"
                }
            }
        }

		stage("CleanUp") {
            steps {
                script {
                    docker.withTool("docker") {
                        sh("docker rmi $registry:$BUILD_NUMBER")
                    }
                }
            }
        }
	}

	post {
		failure {
			emailext attachLog: true, body: "Please find attached build logs for the job.\nMore information available at: ${env.BUILD_URL} .\n\nThis is an automated response. Please do not reply to it.", subject: "Jenkins job ${env.JOB_NAME} #${env.BUILD_NUMBER}: ${currentBuild.currentResult}", to: 'realtime-engg-team@observe.ai'
		}
		always {
            emailext attachLog: true, body: "Please find attached build logs for the job.\nMore information available at: ${env.BUILD_URL} .\n\nThis is an automated response. Please do not reply to it.", subject: "Jenkins job ${env.JOB_NAME} #${env.BUILD_NUMBER}: ${currentBuild.currentResult}", to: 'realtime-engg-team@observe.ai'
            cleanWs()
		}
	}
}
