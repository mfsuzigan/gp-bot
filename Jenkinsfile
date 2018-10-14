pipeline{
    agent any
    stages {
         stage('StartNotification'){
             steps {
                slackSend channel:"${params.canal}",  message: "Iniciando lançamento diario do TQI-GP..."
             }
        }
        stage('GPApp'){
            steps {
                withCredentials([string(credentialsId: "${senha}", variable: 'senha')]) {
         			script {
        				docker.node {
                            docker.script.sh(script: "docker run -i mfsuzigan/gp-bot --usuario=${params.usuario} --senha=${senha} --atividade=${params.atividade} --aplicativo=${params.aplicativo} --hoje", returnStdout: false)
        				}
    			    }
                }   
             }
        }
        stage('FinishNotification'){
            steps {
                slackSend channel: "${params.canal}", color: "good", message: "TQI-GP lançado com sucesso! Detalhes do build <${env.BUILD_URL}|aqui>."
            }
        }
    }
}