def call(String username = 'a', String password ='a' , String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		docusername = "${username}"
	        docpassword = "${password}"
	        registry = "${registryin}" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
	agent none
	
	stages {
		stage("POLL SCM"){
      agent{label 'docker'}
			steps {
				 checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])
			}
		}	
					
		stage('BUILD IMAGE') {
       agent{label 'docker'}
			 steps { 
				 
				 sh 'docker build -t "$registry:$dockerTag" .'
					 
				 
				 
			} 
		}
					
		stage('PUSH HUB') { 
       agent{label 'docker'}
			 steps { 
				 echo "$docpassword"
				 sh 'docker login --username="${docusername}" --password="${docpassword}" '
				 sh ' sleep 5'
				 sh 'docker push $registry:$dockerTag'	
				 
			} 
		}
					
		stage('DEPLOY IMAGE') {
      agent{label 'kubernetes'}
			steps {
				sh 'kubectl set image deployment/webapp-deployment nodejs="$registry:$dockerTag" --record'
			}
		}
	}
			  
}

}
