# Lesmódromo

Uma corrida de lesmas em que o clima de verdade decide quem ganha. Você escolhe a cidade, aposta numa lesma e o backend consulta o tempo naquela cidade agora:

| Lesma | Corre melhor com | Acessório |
|---|---|---|
| Gosminha | umidade | laço e gotinha de orvalho |
| Lesmalda | frio | gorro e cachecol |
| Dona Baba | vento | óculos de aviadora e lenço |
| Turbo Molenga | sorte (ignora o clima) | capacete de corrida |
| Brasinha | calor | boné e óculos escuros |
| Mestre Zen | calmaria (pouco vento) | faixa na testa |
| Garoa | chuva | guarda-chuva |
| Barão Babão | ar seco | cartola, monóculo e bigode |

Se estiver chovendo, todas ganham bônus. Vitória vale 10 pontos no ranking, participação vale 1.

| Parte | Tecnologia | Onde roda |
|---|---|---|
| Frontend | Expo (só web) | Vercel |
| Backend | Spring Boot 3, Java 21, Clean Architecture | Render (imagem do Docker Hub) |
| Banco | MongoDB | Atlas |
| Mensageria | RabbitMQ | CloudAMQP |
| API externa | Open-Meteo (clima, gratuita e sem chave) | |
| Pipeline | GitHub Actions | |

## Arquitetura do backend

O código Java é dividido em duas pastas, e a regra é uma só: **o `core` não conhece o Spring**.

```
backend/src/main/java/com/lesmodromo/
├── core/                          Java puro. Nenhum import de Spring, Mongo ou RabbitMQ.
│   ├── domain/                    Corrida, Lesma, Cidade, Clima, Pista (regra da corrida), Narrador...
│   ├── usecase/                   LargarCorrida, ProcessarCorrida, AtualizarRanking, NarrarCorrida...
│   ├── gateway/                   Interfaces (portas): CorridaGateway, ClimaGateway, EventoGateway,
│   │                              RankingGateway, NarracaoGateway, Transacao
│   └── exception/                 Erros de negócio
└── framework/                     Tudo que depende do Spring. Implementa as portas do core.
    ├── LesmodromoApplication      Classe main
    ├── config/                    UseCaseConfig (monta os casos de uso), Mongo, CORS, verificação na subida
    ├── controller/                REST + DTOs + tratamento de erros
    ├── persistence/               MongoDB: documentos, repositório, gateways, transação
    ├── messaging/                 RabbitMQ: exchange, filas, produtor e os 3 consumidores
    ├── client/                    API de clima (Open-Meteo)
    └── resilience/                RetryExecutor: o retry do projeto inteiro, num lugar só
```

Os casos de uso recebem interfaces no construtor e não sabem quem está do outro lado. O `UseCaseConfig` é o único ponto onde as duas camadas se encontram: ali o Spring entrega a cada caso de uso as implementações de `framework`.

Por isso os testes do core rodam com gateways falsos em memória, sem subir banco, fila nem Spring.

## Mensageria (publish/subscribe)

```
POST /api/corridas
   │  LargarCorrida salva a aposta (NA_LARGADA) e publica:
   ▼
exchange lesmodromo.eventos (topic)
   ├── corrida.largada ─────► fila lesmodromo.juiz ─────► JuizListener
   │                              ProcessarCorrida: CORRENDO → busca clima → FINALIZADA
   │                              e publica corrida.finalizada
   │
   └── corrida.finalizada ──┬► fila lesmodromo.ranking ──► RankingListener   (soma pontos, numa transação)
                            └► fila lesmodromo.narrador ─► NarradorListener  (frase da Rádio Lesma)

Mensagem que falha 3 vezes → exchange lesmodromo.eventos.falhas → fila lesmodromo.falhas
```

Antes da aposta, o front chama `GET /api/cidades/{cidade}/clima` (caso de uso `PreverPareo`), que mostra o clima de agora e a vantagem de cada lesma.

O mesmo evento `corrida.finalizada` chega em duas filas, e cada consumidor faz a sua parte sem conhecer o outro. O front acompanha tudo pelo `GET /api/corridas/{id}`: o status muda de `NA_LARGADA` para `CORRENDO` e depois para `FINALIZADA`, e os campos `rankingAtualizado` e `narrada` viram `true` quando cada consumidor termina. O quadro "Bastidores" da tela mostra isso ao vivo.

## Retry, fallback e transação

Todo retry usa a mesma classe, `framework/resilience/RetryExecutor`. Ela tenta, espera e tenta de novo, com a espera dobrando a cada vez e um teto de 15 s.

| Ponto crítico | Onde está o retry | Fallback se esgotar |
|---|---|---|
| API de clima | `client/ClimaGatewayOpenMeteo` (4 tentativas, timeout de 5 s) | `ConsultarClima` usa o clima padrão (vale pra corrida e pra previsão) |
| MongoDB Atlas | `persistence/CorridaGatewayMongo.salvar` (3) e `config/VerificadorDeConexoes` na subida (10) | erro 503, e o front tenta de novo |
| RabbitMQ: publicar | `messaging/EventoGatewayRabbit` (3) | `LargarCorrida` roda o juiz direto e `ProcessarCorrida` atualiza o ranking direto |
| RabbitMQ: consumir | `application.yml`, em `spring.rabbitmq.listener.simple.retry` (3) | a mensagem vai para a fila `lesmodromo.falhas` |
| Subir no Render | `.github/workflows/pipeline.yml`, etapa 3 (curl com retry + 40 checagens do `/health`) | a pipeline falha e o frontend não é publicado |
| Render acordando | `frontend/src/api.js` (até 6 tentativas) | a tela explica que o servidor não respondeu |

Retry sem duplicar:

- O front manda um `idCliente` em cada aposta, e um índice único no Mongo garante que um pedido repetido não vira duas corridas.
- O ranking só conta a corrida uma vez, por causa da marcação `rankingAtualizado`.

A transação fica em `AtualizarRanking`. Marcar a corrida como contada e somar os pontos acontecem juntos, pela porta `core/gateway/Transacao`, implementada em `framework/persistence/TransacaoMongo` com uma transação do MongoDB.

## Login com cookie (sem localStorage)

Pra apostar é preciso entrar na conta. A sessão fica só num cookie, e o navegador não guarda nada em localStorage nem em sessionStorage.

- **Cadastro e login:** `POST /api/auth/cadastro` e `POST /api/auth/login` conferem o nome e a senha. A senha é guardada só como hash BCrypt.
- **O cookie:** o servidor cria uma sessão e devolve o cookie `lesmodromo_sessao`. Ele é `HttpOnly` (o JavaScript não consegue ler, o que protege contra XSS), `SameSite=Lax` (não vai junto em POST vindo de outro site, o que protege contra CSRF) e `Secure` em produção.
- **O token:** é aleatório. No MongoDB fica só o hash dele (coleção `sessoes`), com um índice TTL que apaga a sessão sozinho quando ela vence (7 dias).
- **Quem está logado:** como o front não lê o cookie, ele pergunta em `GET /api/auth/eu`.
- **Sair:** `POST /api/auth/logout` apaga a sessão no banco e o cookie.
- **A aposta:** `POST /api/corridas` responde 401 sem sessão válida, e o nome do apostador vem do login.

No core ficam os casos de uso `CadastrarUsuario`, `FazerLogin`, `FazerLogout`, `IdentificarUsuario` e `AbrirSessao`, com as portas `UsuarioGateway`, `SessaoGateway` e `SenhaGateway`. Na framework ficam o MongoDB, o BCrypt (`security/SenhaBCrypt`) e o cookie (`security/CookieDeSessao`).

Em produção, o front chama `/api` no próprio domínio da Vercel, e a Vercel repassa pro Render (rewrite no `frontend/vercel.json`). Assim o cookie é do mesmo site do front e funciona em qualquer navegador. Cookie de outro domínio é bloqueado pelo Safari e pela aba anônima do Chrome. A URL do Render não fica no código: a pipeline grava ela no `vercel.json` na hora do build, a partir do secret `API_URL`.

## Pipeline

A cada push na `main`:

1. `mvn test`
2. Gera a imagem Docker do backend e publica no Docker Hub.
3. Manda o Render usar a imagem nova e espera o `/health` mostrar o commit novo.
4. Gera o Expo web com a URL da API vinda do secret e publica na Vercel com o token.

O passo a passo completo, com o roteiro da apresentação, está em [PASSO_A_PASSO.md](PASSO_A_PASSO.md).

## Rodar no seu computador

**Backend:** abra a pasta `backend` na IDE (IntelliJ, por exemplo). Copie o `.env.example` para `.env`, preencha `MONGODB_URI` e `RABBITMQ_URL` e rode a classe `LesmodromoApplication`. A API sobe em http://localhost:8080.

**Frontend:**

```bash
cd frontend
cp .env.example .env   # EXPO_PUBLIC_API_URL=http://localhost:8080 (o backend aceita o front em localhost:8081)
npm install
npm start              # abre no navegador
```
