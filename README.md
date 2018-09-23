# GP Bot
###### 1.1.0
\
Cansado de **gastar seu tempo** lançando o [TQI-GP](https://helpdesk.tqi.com.br/sso/login.action) **na mão todos os dias**? 

Ou pior, na **correria**, com o **prazo** quase acabando e cheio de **cobranças**?

Deixe o **GP Bot** fazer todo o trabalho **automaticamente** e em instantes!

* faça o lançamento das **horas do dia** ou **de todos os dias do mês de uma só vez**!
* informe **dias com horas diferentes de 8** se houver: ganhe rapidez com precisão!
* não quer que ele lance algum dia em específico? Sem problema: escolha os **dias que você quer tratar manualmente** e ajuste você mesmo na página do TQI-GP

E o melhor: levando **menos de 1 minuto!** :O

## Requisitos
- Linux 64-bit;
- Google Chrome entre (e incluindo) as versões 61 e 69;
- Java Runtime Environment 8 ou superior.

## Faça o download

Baixe a última versao [aqui](release/gp-bot.zip?raw=true) e descompacte o arquivo em um diretório de sua preferência.

## Executando o GP Bot

Há duas maneiras de informar os dados de lançamento ao bot:

- via **console**, informando manualmente os parâmetros de execução. Use esta opção se quiser preencher com calma, passo a passo.
- via **linha de comando**. Para quem quer mais **rapidez**! Use também esta opção se necessitar de um **comando único** que possa ser configurado numa **ferramenta de agendamento** como o **cron** ou de **automatização de execução** como o **Jenkins**.

Em comum, as duas opções são iniciadas com a execução do arquivo `gp-bot.jar`:

1. para preenchimento dos dados no console: 
`java -jar gp-bot.jar`

2. para execução com parâmetros via linha de comando: 
`java -jar gp-bot.jar [parametros]`

No caso da execução via **linha de comando** (veja detalhes abaixo), o console pedirá que o usuário complete somente as **informações obrigatórias** que não tiverem sido preenchidas. São **obrigatórios** para o lançamento do TQI-GP:

- usuário (TQI);
- senha;
- aplicativo;
- atividade.

Caso tenham sido **todos** informados via linha de comando o GP Bot iniciará **imediatamente** o processo de lançamento.

## Lançamento pelo console 
Pelo console, informe em sequência:
- **Usuário**: seu username TQI. Exemplo: _jose.silva_
- **Senha**: sua senha (não se preocupe, ela será ofuscada).
- **Nome do aplicativo**: Aplicativo/projeto em que você está trabalhando. Exemplo: _Rede Acme Fase 3_
- **Atividade**: Será exibida a lista de atividades disponíveis para lançamento no TQI-GP. Informe um código numérico da lista ou se preferir o nome exato da atividade. Exemplo: _1_ ou _Codificação_ para lançar a atividade de codificação.
- **Lançar somente hoje (opcional):** Digite _s_ ou _sim_ se quiser que seja lançado somente o dia corrente, com 8 horas. Caso seja informado, as opções complementares abaixo deixarão de ser exibidas.
- **Mês (opcional)**: o mês (numeral) em que serão feitos os lançamentos. Se não informado, o **GP Bot** **assumirá o mês atual**. Exemplo: _3_ (Março)
- **Dias a serem ignorados (opcional)**: dias, separados por vírgulas, para os quais o **GP Bot não efetuará lançamentos**. Exemplo: _3, 5, 17_
- **Dias com horas específicas (opcional)**: dias para os quais o **GP Bot** deverá **lançar horas diferentes de 8**. Separados por vírgulas, a quantidade de horas deverá ser informada entre parênteses no **formato fracional ou horário**. Exemplo: _3(7), 5(7.5), 17(6:35)_  - sete horas no dia 3, sete horas e meia no dia 5 e seis horas e trinca e cinco minutos no dia 17)
   
## Lançamento com parâmetros via linha de comando
Pela linha de comando, complete os parâmetros passados ao executável:

`java -jar gp-bot [parametros]`

com as opções abaixo, em qualquer ordem:

- `--usuario`, para seu usuário;
- `--senha`, para sua senha;
- `--aplicativo`, para o nome do aplicativo;
- `--atividade`, para a atividade desempenhada;
- `--hoje`, para efetuar o lançamento somente do dia corrente;
- `--mes`, para o mês desejado;
- `--skipdays`, para os dias a serem ignorados;
- `--customdays`, para os dias com horas específicas.

O formato dos dados é o mesmo descrito na seção de lançamento via console. 

Para parâmetros de lista de valores ou com mais de uma palavra separada por espaços utilize **aspas** no preenchimento. Exemplo: `java -jar gp-bot --aplicativo='Uma app Fantástica'`

### Exemplos de execução com parâmetros via linha de comando

###### 1. Lançamento de todo o mês com atividade de codificação, execução imediata (todos os parâmetros obrigatórios informados):
\
`java -jar gp-bot.jar --usuario=jose.silva --senha=foobar123 --aplicativo=AppCliente --atividade=1`

###### 2. Lançamento do dia atual com atividade de codificação, execução imediata:
\
`java -jar gp-bot.jar --usuario=jose.silva --senha=foobar123 --aplicativo=AppCliente --atividade=1 --hoje`

###### 3. Lançamento do dia atual com atividade de codificação, console aberto para preenchimento manual da senha (parâmetro obrigatório não informado):
\
`java -jar gp-bot.jar --usuario=jose.silva --aplicativo=AppCliente --atividade=1 --hoje`

###### 3. Lançamento de todo o mês de Março exceto os dias  3, 5 e 17, com os dias 25 e 14 customizados com 7h e 7h35min respectivamente, com atividade de codificação e execução imediata:
\
`java -jar gp-bot.jar --usuario=jose.silva --senha=foobar123 --aplicativo='Rede Acme Fase 3' --atividade='Codificação' --mes=3 --skipdays='3, 5, 17' --customdays='25(7), 14(7:35)'`