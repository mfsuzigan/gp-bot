# GP Bot
Cansado de ter que preencher o [TQI-GP](https://helpdesk.tqi.com.br/sso/login.action) **na correria**? 

Deixe o **GP Bot** fazer todo o trabalho chato **automaticamente** e em instantes!

* Faça o lançamento de **todos os dias do mês com 8 horas de trabalho** de uma vez só
* Informe **dias com horas diferentes de 8**, se houver
* Escolha os **dias que você quiser tratar manualmente** e ajuste você mesmo depois

E o melhor: tudo isso em **menos de 2 minutos!** :O

## Requisitos
- Linux 64-bit;
- Google Chrome entre (e incluindo) as versões 61 e 67;
- Java Runtime Environment 8 ou superior.

## Executando o GP Bot

1. Faça o download da última versao [aqui](release/gp-bot.zip?raw=true)
2. Descompacte o arquivo em um diretório, navegue até ele e execute no console: `java -jar gp-bot.jar`
3. Informe:
    - **Usuário**: seu username TQI. Exemplo: _jose.silva_
    - **Senha**: sua senha
    - **Nome do aplicativo**: Aplicativo/projeto em que você está trabalhando. Exemplo: _Rede Acme Fase 3_
    - **Atividade**: atividade desepenhada por você, escolhida da **lista disponível no TQI-GP**. Consulte os valores e preencha de forma exata, incluindo maiúsculas. Exemplo: _Codificação_
    - **Mês (opcional)**: o mês (numeral) em que serão feitos os lançamentos. Se não informado, o **GP Bot** **assumirá o mês atual**. Exemplo: _3_ (Março)
    - **Dias a serem ignorados (opcional)**: dias, separados por vírgulas, para os quais o **GP Bot não efetuará lançamentos**. Exemplo: _3, 5, 17_
    - **Dias com horas específicas (opcional)**: dias para os quais o **GP Bot** deverá **lançar horas diferentes de 8**. Separados por vírgulas, a quantidade de horas deverá ser informada entre parênteses. Exemplo: _3(7), 5(7.5), 17(6)_ (sete horas no dia 3, sete horas e meia no dia 5 e seis horas no dia 17)
   

## Parâmetros via linha de comando
Caso queira, você pode informar via linha de comando os parâmetros:
- `--usuario`, para seu usuário;
- `--senha`, para sua senha;
- `--aplicativo`, para o nome do aplicativo;
- `--atividade`, para a atividade desempenhada;
- `--mes`, para o mês;
- `--skipdays`, para os dias a serem ignorados;
- `--customdays`, para os dias com horas específicas;

Exemplo de execução com todos os parâmetros via linha de comando:

`java -jar gp-bot.jar --usuario=jose.silva --senha=foobar123 --aplicativo='Rede Acme Fase 3' --atividade='Codificação' --mes=3 --skipdays='3, 5, 17' --customdays='3(7), 5(7.5), 17(6)'`

Caso não sejam informados ao **GP Bot** os parâmetros obrigatórios (usuário, senha, aplicativo e atividade) via linha de comando, o passo-a-passo de preenchimento no console será exibido.