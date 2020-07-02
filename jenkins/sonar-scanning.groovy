def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
  containerTemplate(name: 'sonarqube', image: 'sonarsource/sonar-scanner-cli:latest', command: 'cat', ttyEnabled: true),
]) {
  node(label) {
    def repo = checkout scm
    def gitCommit = repo.GIT_COMMIT
    def gitBranch = repo.GIT_BRANCH
    def shortGitCommit = "${gitCommit[0..10]}"
    def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)
	  
    stage('Code Scanning') {
	withCredentials([
                  usernamePassword(credentialsId: 'SONARQUBE_AUTHENTICATION',
                  usernameVariable: 'SONARQUBE_HOST',
                  passwordVariable: 'SONARQUBE_TOKEN')
                ]) {
	      container('sonarqube') {
		sh "echo $SONARQUBE_HOST"
		sh "printenv"
	      }
	  }
    }
  }
}
