/**
 * This pipeline will build and deploy a Docker image with Kaniko
 * https://github.com/GoogleContainerTools/kaniko
 * without needing a Docker host
 *
 * You need to create a jenkins-docker-cfg secret with your docker config
 * as described in
 * https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-in-the-cluster-that-holds-your-authorization-token
 */

podTemplate(yaml: """
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug-539ddefcae3fd6b411a95982a830d987f4214251
    imagePullPolicy: Always
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
      - name: jenkins-docker-cfg
        mountPath: /kaniko/.docker
  volumes:
  - name: jenkins-docker-cfg
    projected:
      sources:
      - secret:
          name: regcred
          items:
            - key: .dockerconfigjson
              path: config.json
"""
  ) {

  node(POD_LABEL) {
    def repo = checkout scm
    def gitCommit = repo.GIT_COMMIT
    def gitBranch = repo.GIT_BRANCH
    def shortGitCommit = "${gitCommit[0..10]}"
    
    def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)
    stage('Build with Kaniko') {
      container('kaniko') {
        sh '/kaniko/executor -f ./dockerfile/Dockerfile -c `pwd` --insecure --skip-tls-verify --cache=true --destination=646315653071.dkr.ecr.ap-south-1.amazonaws.com/santabanta/usermanagement:latest'
      }
    }
  }
}
