# Passo a passo do Lesmódromo

Siga na ordem, porque cada parte usa algo que a anterior gerou. Vale abrir um bloco de notas pra ir guardando:

```
MONGODB_URI=
RABBITMQ_URL=
Usuário do Docker Hub=
Token do Docker Hub=
URL do Render (API_URL)=
Deploy Hook do Render=
VERCEL_TOKEN=
VERCEL_ORG_ID=
VERCEL_PROJECT_ID=
URL da Vercel=
```

Contas necessárias, todas gratuitas: GitHub, MongoDB Atlas, CloudAMQP, Docker Hub, Render e Vercel. No computador: Java 21, Maven (ou a IDE com Maven embutido) e Node.js 22.

---

## 1. MongoDB Atlas

1. Em cloud.mongodb.com, crie um cluster no plano gratuito (Free / M0).
2. Em **Database Access**, crie um usuário com senha. Evite `@`, `/` e `:` na senha, porque quebram a URL.
3. Em **Network Access**, clique em **Add IP Address → Allow access from anywhere** (`0.0.0.0/0`). O Render gratuito não tem IP fixo.
4. No cluster, clique em **Connect → Drivers** e copie a connection string.
5. Troque `<db_password>` pela senha e coloque `lesmodromo` logo depois do `.net/`:

```
mongodb+srv://vini:minhasenha@cluster0.abcde.mongodb.net/lesmodromo?retryWrites=true&w=majority
```

Esse é o **MONGODB_URI**. O Atlas roda em replica set, que é o que permite a transação usada no ranking. Um MongoDB local simples não suportaria.

## 2. RabbitMQ na CloudAMQP

1. Em cloudamqp.com, clique em **Create New Instance**.
2. Nome `lesmodromo`, plano gratuito **Little Lemur**, região nos Estados Unidos.
3. Abra a instância e copie a **AMQP URL** (começa com `amqps://`).

Esse é o **RABBITMQ_URL**. Você não precisa criar nada no RabbitMQ: o exchange, as filas e os bindings são criados pelo Spring quando a aplicação sobe.

## 3. Testar no seu computador

Faça isso antes do primeiro push. É o jeito mais rápido de achar qualquer problema.

**Backend**

1. Abra a pasta `backend` como projeto na IDE.
2. Copie o `.env.example` para `.env` (na própria pasta `backend`) e cole o MONGODB_URI e o RABBITMQ_URL.
3. Rode os testes (`mvn test` ou o botão de testes da IDE). Os mesmos testes rodam na pipeline.
4. Rode a classe `LesmodromoApplication`.

No log deve aparecer `[mongo] conectado ao Atlas` e `[rabbit] conectado à CloudAMQP`. Confira em http://localhost:8080/health.

**Frontend**, em outro terminal:

```bash
cd frontend
cp .env.example .env     # já vem com http://localhost:8080
npm install
npm start
```

Faça uma aposta. No quadro "Bastidores", os cinco passos devem acender um por um.

## 4. Docker Hub: criar o token

1. Crie a conta em hub.docker.com e anote seu **nome de usuário**.
2. Vá em **Account settings → Personal access tokens → Generate new token**.
3. Em **Description**, escreva `github-actions-lesmodromo`. Em **Access permissions**, escolha **Read & Write**.
4. Copie o token na hora, porque ele só aparece uma vez.

O repositório `lesmodromo-api` é criado sozinho no primeiro push da imagem. Ele precisa ficar **público**, senão o Render não consegue baixar a imagem.

## 5. GitHub e primeira execução da pipeline

Crie um repositório (ex: `lesmodromo`) e, na pasta raiz do projeto:

```bash
git init
git add .
git commit -m "Lesmódromo: primeira versão"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/lesmodromo.git
git push -u origin main
```

Os `.env` não sobem porque estão no `.gitignore`.

Agora vá em **Settings → Secrets and variables → Actions → New repository secret** e crie os dois primeiros secrets:

| Nome | Valor |
|---|---|
| `DOCKERHUB_USERNAME` | seu usuário do Docker Hub |
| `DOCKERHUB_TOKEN` | o token `github-actions-lesmodromo` |

Na aba **Actions**, abra **Pipeline Lesmódromo** e clique em **Run workflow**.

As etapas 1 (testes) e 2 (imagem) devem passar. A etapa 3 vai falhar, porque o Render ainda não existe, e isso é esperado. Confira no Docker Hub se apareceu o repositório `lesmodromo-api` com as tags `latest` e a do commit.

## 6. Backend no Render

1. Em render.com, clique em **New → Web Service** e escolha **Existing Image**.
2. Image URL: `docker.io/SEU_USUARIO/lesmodromo-api:latest`.
3. Preencha:
   - **Name:** `lesmodromo-api` (vira parte da URL)
   - **Instance Type:** Free
   - **Environment Variables:** `MONGODB_URI` e `RABBITMQ_URL`
   - **Health Check Path** (em Advanced): `/api/info`
4. Clique em **Deploy** e espere. O Spring leva perto de 1 minuto pra subir no plano gratuito.
5. Copie a URL do topo (ex: `https://lesmodromo-api.onrender.com`). Esse é o **API_URL**, sem a barra no final.
6. Abra `https://SUA-URL.onrender.com/health`. Tem que aparecer `mongo: conectado` e `rabbit: conectado`.
7. Em **Settings**, procure **Deploy Hook** e copie a URL. Esse é o **RENDER_DEPLOY_HOOK_URL**, e deve ser tratado como senha.

Imagem no Docker Hub não dispara deploy sozinha no Render. Quem avisa é a pipeline, chamando o Deploy Hook com o nome da imagem nova.

## 7. Vercel: criar o token e o projeto

**Token**

1. Na Vercel, clique na sua foto → **Account Settings → Tokens**.
2. Em **Token Name**, escreva `github-actions-lesmodromo`. Escolha sua conta no scope e uma validade (90 dias cobre o semestre).
3. Clique em **Create** e copie o valor na hora. Esse é o **VERCEL_TOKEN**.

**Projeto**, no terminal, dentro de `frontend`:

```bash
npx vercel login
npx vercel link
```

Responda assim:

- Set up and link? **yes**
- Scope: sua conta
- Link to existing project? **no**
- Nome: `lesmodromo`
- Diretório: `./`
- Se perguntar se quer mudar configurações detectadas: **no** (o `vercel.json` já cuida disso)
- Se perguntar se quer conectar o repositório Git: **no** (quem publica é a pipeline)

Abra o arquivo `frontend/.vercel/project.json` e copie o `orgId` (**VERCEL_ORG_ID**) e o `projectId` (**VERCEL_PROJECT_ID**). Se a Vercel conectar o Git mesmo assim, desconecte em **Settings → Git → Disconnect**.

## 8. Os outros secrets

No GitHub, crie mais cinco secrets, totalizando sete:

| Nome | Valor |
|---|---|
| `API_URL` | URL do Render |
| `RENDER_DEPLOY_HOOK_URL` | Deploy Hook do Render |
| `VERCEL_TOKEN` | token `github-actions-lesmodromo` da Vercel |
| `VERCEL_ORG_ID` | `orgId` do project.json |
| `VERCEL_PROJECT_ID` | `projectId` do project.json |

O `API_URL` é o que liga o frontend ao backend: a pipeline cria o `.env` do front com ele na hora do build, então a URL nunca fica escrita no código.

A URL acaba visível no JavaScript do navegador, como toda URL de API pública. As credenciais do Mongo e do RabbitMQ ficam só no Render e nunca chegam ao frontend.

## 9. Rodar a pipeline completa

Em **Actions → Pipeline Lesmódromo → Run workflow**. As quatro etapas, em sequência:

1. **Testes do backend:** `mvn test`.
2. **Imagem no Docker Hub:** build com o Dockerfile e push com as tags `latest` e a do commit.
3. **Deploy do backend no Render:** chama o Deploy Hook com a imagem do commit e pergunta o `/health` a cada 15 s até a versão nova responder.
4. **Deploy do frontend na Vercel:** cria o `.env` com a URL da API, faz o build do Expo web e publica com o token.

No final da etapa 4, o log mostra a URL da Vercel. Abra e jogue.

Não precisa configurar CORS pra produção: o front chama `/api` no próprio domínio da Vercel, e a Vercel repassa pro Render. A pipeline monta esse repasse com o secret `API_URL`.

---

## Roteiro da apresentação

**1. O jogo no link da Vercel.** Uns 2 minutos antes, abra o link uma vez, porque o Render gratuito dorme depois de 15 minutos parado. Faça uma aposta e mostre o quadro "Bastidores" acendendo passo a passo enquanto as lesmas correm.

**2. A mensageria.** Com o desenho do README aberto, explique o pub/sub:

- A API publica `corrida.largada` no exchange `lesmodromo.eventos`.
- O juiz consome, faz a corrida e publica `corrida.finalizada`.
- Esse mesmo evento chega em duas filas: o ranking soma os pontos e o narrador manda a frase pra Rádio Lesma.

Depois abra o painel da CloudAMQP (**RabbitMQ Manager**) e mostre o exchange em **Exchanges** e as quatro filas em **Queues** recebendo mensagens.

**3. A arquitetura limpa.** Abra `core` e mostre que não existe nenhum import de Spring. Os casos de uso dependem de interfaces (`gateway`), e quem as implementa é a `framework`. Abra o `UseCaseConfig`, que é o único lugar onde as duas camadas se encontram. Mostre também que a classe `main` está dentro de `framework`.

**4. A pipeline.** Aba Actions, última execução, as quatro etapas ligadas.

**5. Docker Hub e Render.** As tags da imagem no Docker Hub, o painel do Render e o `/health` mostrando a versão (o commit), o Mongo e o RabbitMQ.

**6. Tokens e secrets.** Mostre só os nomes, nunca os valores:

- na Vercel, o token `github-actions-lesmodromo`;
- no Docker Hub, o token de mesmo nome;
- no GitHub, a lista dos sete secrets.

Depois mostre o código lendo variáveis de ambiente: `frontend/src/api.js` e `backend/src/main/resources/application.yml`.

**7. O login com cookie.** Crie uma conta no site e aperte F12. Mostre três coisas:

- Em **Application → Cookies**, o cookie `lesmodromo_sessao`, com HttpOnly marcado e SameSite Lax.
- Em **Application → Local Storage**, que não tem nada.
- No **Console**, que `document.cookie` não mostra o token. É o HttpOnly funcionando.

Recarregue a página e mostre que você continua logado, porque o front pergunta em `/api/auth/eu`. Depois clique em Sair e mostre que o cookie sumiu. No Atlas, mostre as coleções `usuarios` (senha só em hash BCrypt) e `sessoes` (só o hash do token).

**8. O retry.** Detalhado abaixo.

---

## "Fiz o retry aqui, aqui, aqui e aqui"

Toda a lógica está numa classe só: `framework/resilience/RetryExecutor`. Ela executa a ação. Se der erro, espera e tenta de novo, e a espera dobra a cada vez (500 ms, 1 s, 2 s...) até no máximo 15 s. Esse é o backoff exponencial: dá tempo do serviço se recuperar sem ficar martelando ele. Esgotadas as tentativas, ela lança `RetryEsgotadoException`, e quem chamou decide o fallback.

Como a lógica está num lugar só, todos os pontos críticos usam a mesma classe, sem código de retry repetido.

### Aqui 1: consumo da API de clima

**Onde:** `framework/client/ClimaGatewayOpenMeteo`, com até 4 tentativas e timeout de 5 s cada. O fallback fica em `core/usecase/ConsultarClima`: se as tentativas acabarem, usa `Clima.PADRAO`. A corrida e a previsão do clima (o painel "Agora em…") usam esse mesmo caso de uso, então a regra não fica repetida.

**No que ajudou:** API externa a gente não controla. Ela pode demorar, devolver erro 500 ou cair por alguns segundos. Sem retry, qualquer soluço da Open-Meteo travava a corrida. Com retry, a falha passageira nem aparece. E se a API cair de vez, o fallback deixa o jogo continuar.

**Como mostrar:** ligue o **Modo caos** e aposte. No quadro Bastidores aparece "A API de clima falhou 2x e o retry buscou de novo. Respondeu na 3ª tentativa". Nos logs do Render:

```
[retry] API de clima (Manaus): tentativa 1/4 falhou (falha simulada (modo caos)). Tentando de novo em 500 ms...
[retry] API de clima (Manaus): tentativa 2/4 falhou (falha simulada (modo caos)). Tentando de novo em 1000 ms...
[retry] API de clima (Manaus): deu certo na tentativa 3
```

### Aqui 2: MongoDB Atlas

**Onde:** em dois lugares.

- `framework/persistence/CorridaGatewayMongo.salvar`: cada gravação da corrida tem 3 tentativas.
- `framework/config/VerificadorDeConexoes`: quando a aplicação sobe, faz um ping no Atlas com até 10 tentativas, por quase 2 minutos.

**No que ajudou:** o banco está em outra rede. Na subida do servidor, ou numa troca de primário do cluster, a conexão pode falhar por alguns segundos. Sem retry, a aposta se perdia ou o servidor subia sem banco. Com retry, ele insiste até o Atlas responder.

### Aqui 3: RabbitMQ na CloudAMQP

**Onde:** em três lugares.

- **Publicar:** `framework/messaging/EventoGatewayRabbit`, com 3 tentativas. Se acabarem, entra o fallback no core: `LargarCorrida` roda o juiz direto e `ProcessarCorrida` atualiza o ranking direto.
- **Consumir:** `application.yml`, em `spring.rabbitmq.listener.simple.retry`. Aqui o retry é o do próprio Spring AMQP: se um consumidor lançar erro, ele tenta mais 2 vezes. Na 3ª falha, a mensagem vai para a dead letter, a fila `lesmodromo.falhas`, em vez de ficar travando a fila.
- **Subida:** `VerificadorDeConexoes` confirma a conexão com retry, como no Mongo.

**No que ajudou:** conexão com fila na nuvem cai de vez em quando. Sem retry, uma queda perdia a aposta ou deixava o ranking desatualizado. Com retry e fallback, a aposta nunca se perde. Mesmo com o RabbitMQ fora do ar, a corrida acontece e os pontos entram.

**Como mostrar o 2 e o 3:** no Render, em **Environment**, crie `SIMULAR_FALHAS_NA_SUBIDA` com valor `true` e salve (o Render reinicia). Nos logs vai aparecer:

```
[retry] MongoDB Atlas: tentativa 1/10 falhou (falha simulada (modo caos)). Tentando de novo em 1000 ms...
[retry] MongoDB Atlas: deu certo na tentativa 2
[mongo] conectado ao Atlas
[retry] RabbitMQ (CloudAMQP): tentativa 1/10 falhou (falha simulada (modo caos)). Tentando de novo em 1000 ms...
[retry] RabbitMQ (CloudAMQP): deu certo na tentativa 2
[rabbit] conectado à CloudAMQP
```

Depois da apresentação, volte a variável para `false`.

### Aqui 4: subir no Render

**Onde:** `.github/workflows/pipeline.yml`, etapa 3. O Deploy Hook é chamado com `curl --retry 5`, e o passo "Espera a nova versão subir" pergunta o `/health` a cada 15 s, até 40 vezes, esperando aparecer o commit novo. O commit vai gravado dentro da imagem pela variável `APP_VERSAO`.

**No que ajudou:** o Render leva alguns minutos pra trocar a versão e não avisa quando termina. Sem esse retry, a pipeline publicaria o front com o backend velho no ar, ou ainda subindo. Com ele, o front só vai pra Vercel quando a versão nova responde. Se não responder em 10 minutos, a pipeline falha e não publica nada quebrado.

**Como mostrar:** abra esse passo no log do Actions. Aparecem as linhas "Tentativa X/40: versão nova ainda não respondeu" e no final "Backend no ar com a versão ...".

### Bônus: o front esperando o Render acordar

**Onde:** `frontend/src/api.js`, função `chamarApi`.

**No que ajudou:** quem abria o jogo com o servidor dormindo recebia erro. Agora a tela mostra "Servidor acordando (tentativa 2 de 6)" e carrega sozinha. Erros 4xx, como nome vazio, não são repetidos, porque tentar de novo não resolveria.

### Retry sem efeito colateral

Repetir uma ação pode duplicar coisa. O projeto se protege disso em dois pontos:

- Cada aposta leva um `idCliente` gerado no navegador, e um índice único no Mongo impede que um pedido repetido vire duas corridas.
- O ranking marca a corrida como contada (`rankingAtualizado`) **na mesma transação** em que soma os pontos (`AtualizarRanking` + `TransacaoMongo`). Se a mensagem chegar de novo, os pontos não entram duas vezes.

---

## Se algo der errado

- **A aplicação não sobe e reclama de `Could not resolve placeholder 'MONGODB_URI'`:** falta a variável de ambiente, no `.env` local ou no Environment do Render.
- **/health mostra `mongo: desconectado`:** confira o MONGODB_URI (senha certa, com `/lesmodromo` depois do `.net`) e o `0.0.0.0/0` no Network Access.
- **/health mostra `rabbit: desconectado`:** confira se copiou a AMQP URL inteira, começando com `amqps://`.
- **Erro `PRECONDITION_FAILED` sobre uma fila:** a fila já existia com outra configuração. Apague as filas `lesmodromo.*` no RabbitMQ Manager e reinicie a aplicação.
- **Etapa 2 falha no login:** DOCKERHUB_USERNAME ou DOCKERHUB_TOKEN errado, ou token sem permissão de escrita.
- **Render não consegue baixar a imagem:** o repositório no Docker Hub está privado. Deixe público.
- **Etapa 3 dá timeout:** veja os logs do Render. Se aparecer falta de memória, confira se o `ENTRYPOINT` do Dockerfile ficou com os ajustes de memória.
- **Etapa 4 falha com "Project not found":** VERCEL_ORG_ID ou VERCEL_PROJECT_ID errado.
- **No seu PC, o login não "pega" (volta a pedir pra entrar):** confira se o front está em http://localhost:8081 e o `frontend/.env` aponta pra http://localhost:8080. O backend só libera essas origens com cookie (`FRONTEND_URL` muda isso).
- **Na Vercel, `/api/...` dá 404 ou erro de proxy:** confira o secret `API_URL` (a URL do Render, sem barra no final). A pipeline grava ela no `vercel.json` e falha se não conseguir.
- **O jogo abre mas diz que o servidor não respondeu:** o secret API_URL está errado. Corrija e rode a pipeline de novo, porque a URL entra no build.
