import { Children, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  View,
  useWindowDimensions,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { useFonts } from 'expo-font';
import { API_URL, chamarApi, esperar, novoIdDaAposta } from './src/api';
import { clarear, cores, coresDasLesmas, dicasDasCidades, escurecer, fontes } from './src/tema';
import Pista from './src/componentes/Pista';
import Lesma from './src/componentes/Lesma';
import Icone from './src/componentes/Icone';
import Podio from './src/componentes/Podio';
import Bastidores from './src/componentes/Bastidores';
import Acesso from './src/componentes/Acesso';

export default function App() {
  // Fontes ficam em assets/fonts (licença OFL). Bagel Fat One no logo, Bricolage Grotesque no resto.
  const [fontesProntas] = useFonts({
    BagelFatOne_400Regular: require('./assets/fonts/BagelFatOne_400Regular.ttf'),
    BricolageGrotesque_400Regular: require('./assets/fonts/BricolageGrotesque_400Regular.ttf'),
    BricolageGrotesque_500Medium: require('./assets/fonts/BricolageGrotesque_500Medium.ttf'),
    BricolageGrotesque_700Bold: require('./assets/fonts/BricolageGrotesque_700Bold.ttf'),
    BricolageGrotesque_800ExtraBold: require('./assets/fonts/BricolageGrotesque_800ExtraBold.ttf'),
  });
  const { width } = useWindowDimensions();
  const largo = width >= 960;
  const estreito = width < 560;

  const [servidor, setServidor] = useState({ estado: 'acordando', texto: 'Acordando o servidor' });
  const [info, setInfo] = useState(null);
  const [ranking, setRanking] = useState(null);
  const [narracoes, setNarracoes] = useState([]);
  const [previsoes, setPrevisoes] = useState({});

  // undefined = ainda verificando, null = ninguém logado. Vem do servidor, não de localStorage.
  const [usuario, setUsuario] = useState(undefined);
  const [cidade, setCidade] = useState('SAO_PAULO');
  const [lesma, setLesma] = useState(null);
  const [modoCaos, setModoCaos] = useState(false);

  const [corrida, setCorrida] = useState(null);
  const [falhaDaMensageria, setFalhaDaMensageria] = useState(false);
  const [idDaLargada, setIdDaLargada] = useState(null);
  const [chegou, setChegou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');
  const largadaAtual = useRef(0);
  const rolagem = useRef(null);
  const posicaoDaPista = useRef(0);

  // Ao abrir: busca lesmas e cidades. Se o Render estiver dormindo, o retry espera ele acordar.
  useEffect(() => {
    chamarApi('/api/info', {
      tentativas: 6,
      aoTentar: (t, total) =>
        setServidor({
          estado: 'acordando',
          texto: t === 1 ? 'Acordando o servidor' : `Servidor acordando (tentativa ${t} de ${total})`,
        }),
    })
      .then((dados) => {
        setInfo(dados);
        setServidor({ estado: 'online', texto: 'Servidor no ar' });
        carregarPainel();
        verificarLogin();
      })
      .catch((e) =>
        setServidor({
          estado: 'erro',
          texto: 'Servidor fora do ar',
          detalhe: API_URL
            ? `Não consegui falar com ${API_URL} (${e.message}). Confira se a API está no ar e recarregue a página.`
            : 'O front não encontrou a URL da API. Crie o arquivo frontend/.env com EXPO_PUBLIC_API_URL=http://localhost:8080 e reinicie com npx expo start --web -c.',
        })
      );
  }, []);

  // Clima de agora da cidade escolhida e a vantagem de cada lesma
  useEffect(() => {
    if (!info) return;
    const atual = previsoes[cidade];
    if (atual && (atual.carregando || Date.now() - atual.quando < 5 * 60 * 1000)) return;

    setPrevisoes((p) => ({ ...p, [cidade]: { carregando: true, quando: Date.now() } }));
    chamarApi(`/api/cidades/${cidade}/clima`, { tentativas: 2 })
      .then((dados) => setPrevisoes((p) => ({ ...p, [cidade]: { dados, quando: Date.now() } })))
      .catch(() => setPrevisoes((p) => ({ ...p, [cidade]: { erro: true, quando: 0 } })));
  }, [cidade, info]);

  // O cookie é HttpOnly: o front não consegue lê-lo. Então pergunta ao servidor quem está logado.
  function verificarLogin() {
    chamarApi('/api/auth/eu', { tentativas: 2 })
      .then(setUsuario)
      .catch(() => setUsuario(null));
  }

  async function sair() {
    await chamarApi('/api/auth/logout', { metodo: 'POST', tentativas: 2 }).catch(() => {});
    setUsuario(null);
  }

  function carregarPainel() {
    chamarApi('/api/ranking', { tentativas: 3 }).then(setRanking).catch(() => {});
    chamarApi('/api/narracoes', { tentativas: 3 }).then(setNarracoes).catch(() => {});
  }

  async function apostar() {
    const minhaLargada = ++largadaAtual.current;
    setErro('');
    setCorrida(null);
    setIdDaLargada(null);
    setChegou(false);
    setFalhaDaMensageria(false);
    setEnviando(true);
    rolagem.current?.scrollTo({ y: Math.max(posicaoDaPista.current - 16, 0), animated: true });

    try {
      const pule = await chamarApi('/api/corridas', {
        metodo: 'POST',
        corpo: { lesma, cidade, modoCaos, idCliente: novoIdDaAposta() }, // o nome vem do login (cookie)
      });
      // Se já voltou finalizada, é porque o RabbitMQ estava fora e a API fez o fallback
      const semMensageria = pule.status === 'FINALIZADA';
      setFalhaDaMensageria(semMensageria);
      setCorrida(pule);
      await acompanhar(pule, minhaLargada, semMensageria);
    } catch (e) {
      if (e.status === 401) {
        setUsuario(null);
        setErro('Sua sessão terminou. Entre de novo para apostar.');
      } else {
        setErro(e.message);
      }
    } finally {
      if (largadaAtual.current === minhaLargada) setEnviando(false);
    }
  }

  // A corrida acontece nos consumidores do RabbitMQ. O front pergunta o status até todos terminarem.
  async function acompanhar(pule, minhaLargada, semMensageria) {
    let atual = pule;
    for (let volta = 0; volta < 45; volta++) {
      if (largadaAtual.current !== minhaLargada) return; // o jogador já largou outra corrida

      if (atual.status === 'FINALIZADA') setIdDaLargada(atual.id);
      const tudoPronto =
        atual.status === 'FINALIZADA' && atual.rankingAtualizado && (atual.narrada || semMensageria);
      if (tudoPronto) break;

      await esperar(700);
      atual = await chamarApi(`/api/corridas/${pule.id}`, { tentativas: 3 });
      if (largadaAtual.current === minhaLargada) setCorrida(atual);
    }
    carregarPainel();
  }

  if (!fontesProntas) return <View style={estilos.tela} />;

  const lesmaEscolhida = info?.lesmas.find((l) => l.codigo === lesma);
  const nomeDaCidade = info?.cidades.find((c) => c.codigo === cidade)?.nome ?? '';
  const finalizada = corrida?.status === 'FINALIZADA';
  const vencedora = info?.lesmas.find((l) => l.codigo === corrida?.vencedora);
  const podeApostar = servidor.estado === 'online' && usuario && lesma && !enviando;
  const posicaoDaSua = corrida?.resultado?.find((r) => r.lesma === corrida.lesmaEscolhida)?.posicao;
  const previsao = previsoes[cidade];
  const vantagens = previsao?.dados?.vantagens ?? [];
  const vantagemDe = (codigo) => vantagens.find((v) => v.lesma === codigo);
  const maiorVantagem = Math.max(1, ...vantagens.map((v) => v.bonus));
  const climaDaPista = finalizada ? corrida.clima : previsao?.dados?.clima;

  let situacaoDaPista = 'Aguardando apostas';
  if (corrida?.status === 'NA_LARGADA') situacaoDaPista = 'Aposta registrada, esperando o juiz';
  if (corrida?.status === 'CORRENDO') situacaoDaPista = 'O juiz está consultando o clima';
  if (finalizada && !chegou) situacaoDaPista = 'Valendo!';
  if (finalizada && chegou) situacaoDaPista = `${vencedora?.nome} venceu`;

  return (
    <ScrollView ref={rolagem} style={estilos.tela} contentContainerStyle={estilos.conteudo}>
      <StatusBar style="dark" />

      <View style={estilos.barraTopo}>
        <Text style={[estilos.logo, estreito && estilos.logoPequeno]} accessibilityRole="header">
          Lesmódromo
        </Text>
        <View style={estilos.ladoDireito}>
          {usuario && (
            <View style={estilos.conta}>
              <Text style={estilos.contaTexto}>
                Olá, <Text style={estilos.contaNome}>{usuario.nome}</Text>
              </Text>
              <Pressable onPress={sair} style={({ hovered }) => [estilos.sair, hovered && estilos.sairSobre]}>
                <Text style={estilos.sairTexto}>Sair</Text>
              </Pressable>
            </View>
          )}
          <View style={[estilos.servidor, estilos[`servidor_${servidor.estado}`]]}>
            {servidor.estado === 'acordando' ? (
              <ActivityIndicator size="small" color={cores.tinta} />
            ) : (
              <View style={[estilos.luz, servidor.estado === 'erro' && { backgroundColor: cores.erro }]} />
            )}
            <Text style={estilos.servidorTexto}>{servidor.texto}</Text>
          </View>
        </View>
      </View>

      <View style={estilos.intro}>
        <Text style={[estilos.manchete, estreito && estilos.manchetePequena]}>
          Aposte numa lesma. O clima de agora decide quem ganha.
        </Text>
        <Text style={estilos.chamada}>
          Lesmas são muito lentas. Na hora da largada, o servidor consulta o clima real da cidade
          escolhida e as lesmas correm de acordo.
        </Text>
      </View>

      {servidor.estado === 'erro' && (
        <View style={estilos.avisoErro}>
          <Text style={estilos.avisoErroTexto}>{servidor.detalhe}</Text>
        </View>
      )}

      {info && (
        <>
          <View style={[estilos.painelAposta, estreito && estilos.painelApostaEstreito]}>
            <Secao numero={1} titulo="Escolha a cidade do páreo" ajuda="O clima de agora nessa cidade vai decidir a corrida.">
              <Grade colunas={largo ? 4 : 2} espaco={12}>
                {info.cidades.map((c) => {
                  const ativa = cidade === c.codigo;
                  return (
                    <Pressable
                      key={c.codigo}
                      onPress={() => setCidade(c.codigo)}
                      style={({ hovered }) => [estilos.cartaoCidade, hovered && estilos.sobre, ativa && estilos.cidadeAtiva]}
                      accessibilityRole="radio"
                      accessibilityState={{ checked: ativa }}
                    >
                      <View style={estilos.linhaTitulo}>
                        <Text style={estilos.nomeCidade}>{c.nome}</Text>
                        {ativa && <Marca cor={cores.musgo} />}
                      </View>
                      <Text style={estilos.dicaCidade}>{dicasDasCidades[c.codigo]}</Text>
                    </Pressable>
                  );
                })}
              </Grade>
              <ClimaAgora nomeCidade={nomeDaCidade} previsao={previsao} largo={largo} />
            </Secao>

            <Secao
              numero={2}
              titulo="Escolha sua lesma"
              ajuda="A barrinha mostra a vantagem de cada lesma com o clima de agora. Ajuda, mas não garante: toda corrida ainda tem uma parte de sorte."
            >
              <Grade colunas={largo ? 4 : 2} espaco={12}>
                {info.lesmas.map((l) => {
                  const ativa = lesma === l.codigo;
                  const cor = coresDasLesmas[l.codigo];
                  return (
                    <Pressable
                      key={l.codigo}
                      onPress={() => setLesma(l.codigo)}
                      style={({ hovered }) => [
                        estilos.cartaoLesma,
                        hovered && estilos.sobre,
                        ativa && { borderColor: cor, backgroundColor: clarear(cor, 0.93) },
                      ]}
                      accessibilityRole="radio"
                      accessibilityState={{ checked: ativa }}
                      accessibilityLabel={`${l.nome}, corre melhor com ${l.especialidade}`}
                    >
                      <View style={[estilos.palco, { backgroundColor: clarear(cor, 0.86) }]}>
                        <Text style={[estilos.numeroLesma, { color: escurecer(cor, 0.25) }]}>{l.numero}</Text>
                        {vantagemDe(l.codigo)?.posicao === 1 && (
                          <View style={[estilos.selo, estilos.seloFavorita]}>
                            <Text style={estilos.seloTexto}>Favorita</Text>
                          </View>
                        )}
                        {[2, 3].includes(vantagemDe(l.codigo)?.posicao) && (
                          <View style={[estilos.selo, estilos.seloBoaChance]}>
                            <Text style={[estilos.seloTexto, { color: cores.musgo }]}>Boa chance</Text>
                          </View>
                        )}
                        <Lesma codigo={l.codigo} largura={estreito ? 118 : 150} />
                      </View>
                      <View style={estilos.linhaNome}>
                        <Text style={estilos.nomeLesma}>{l.nome}</Text>
                        {ativa && <Marca cor={cor} />}
                      </View>
                      <View style={[estilos.especialidade, { backgroundColor: clarear(cor, 0.85) }]}>
                        <View style={{ flexShrink: 0 }}>
                          <Icone especialidade={l.especialidade} cor={escurecer(cor, 0.35)} tamanho={14} />
                        </View>
                        <Text style={[estilos.especialidadeTexto, { color: escurecer(cor, 0.35) }]}>{l.especialidade}</Text>
                      </View>
                      <Text style={estilos.descricaoLesma}>{l.descricao}</Text>
                      {vantagemDe(l.codigo) && (
                        <View style={estilos.vantagem}>
                          <View style={estilos.vantagemTopo}>
                            <Text style={estilos.vantagemRotulo}>{estreito ? 'Vantagem' : 'Vantagem agora'}</Text>
                            <Text style={estilos.vantagemValor}>
                              +{decimal(vantagemDe(l.codigo).bonus)}
                              {l.codigo === 'TURBO_MOLENGA' ? ' na média' : ''}
                            </Text>
                          </View>
                          <View style={estilos.vantagemFundo}>
                            <View
                              style={[
                                estilos.vantagemBarra,
                                {
                                  width: `${Math.max(4, (vantagemDe(l.codigo).bonus / maiorVantagem) * 100)}%`,
                                  backgroundColor: cor,
                                },
                              ]}
                            />
                          </View>
                        </View>
                      )}
                    </Pressable>
                  );
                })}
              </Grade>
            </Secao>

            <Secao numero={3} titulo={usuario ? 'Confirme e largue' : 'Entre e largue'}>
              <Text style={estilos.resumo}>
                {lesmaEscolhida ? (
                  <>
                    Você vai apostar na <Text style={estilos.resumoForte}>{lesmaEscolhida.nome}</Text> no páreo de{' '}
                    <Text style={estilos.resumoForte}>{nomeDaCidade}</Text>.
                  </>
                ) : (
                  'Escolha uma lesma no passo 2 para continuar.'
                )}
              </Text>

              {usuario === null && <Acesso onEntrou={setUsuario} />}

              <View style={[estilos.linhaAposta, !largo && estilos.linhaApostaEmPe]}>
                {usuario && (
                  <View style={[estilos.apostador, largo && { flex: 1 }]}>
                    <Text style={estilos.apostadorTexto}>
                      Apostando como <Text style={estilos.resumoForte}>{usuario.nome}</Text>
                    </Text>
                  </View>
                )}
                <Pressable
                  onPress={apostar}
                  disabled={!podeApostar}
                  style={({ pressed, hovered }) => [
                    estilos.botao,
                    hovered && podeApostar && estilos.botaoSobre,
                    !podeApostar && estilos.botaoDesligado,
                    pressed && estilos.botaoApertado,
                  ]}
                >
                  <Text style={[estilos.botaoTexto, !podeApostar && estilos.botaoTextoDesligado]}>
                    {enviando ? 'Páreo em andamento...' : 'Largar as lesmas'}
                  </Text>
                </Pressable>
              </View>

              <View style={estilos.caos}>
                <Switch
                  value={modoCaos}
                  onValueChange={setModoCaos}
                  trackColor={{ false: cores.linha, true: cores.gemaEscura }}
                  thumbColor="#FFFFFF"
                  activeThumbColor="#FFFFFF"
                />
                <Text style={estilos.caosTexto}>
                  <Text style={estilos.caosTitulo}>Modo caos: </Text>a API de clima falha 2 vezes de propósito, pra
                  você ver o retry trabalhando.
                </Text>
              </View>
              {!!erro && <Text style={estilos.erro}>{erro}</Text>}
            </Secao>
          </View>

          <View onLayout={(e) => (posicaoDaPista.current = e.nativeEvent.layout.y)}>
            <Pista
              lesmas={info.lesmas}
              resultado={finalizada ? corrida.resultado : null}
              idDaLargada={idDaLargada}
              onVencedoraChegou={() => setChegou(true)}
              compacta={estreito}
            >
              <View style={estilos.faixa}>
                <Text style={estilos.faixaTitulo}>Páreo em {corrida?.cidade ?? nomeDaCidade}</Text>
                <Text style={estilos.faixaSituacao}>{situacaoDaPista}</Text>
              </View>
              {climaDaPista && (
                <View style={estilos.climaNaPista}>
                  <Text style={estilos.climaItem}>{Math.round(climaDaPista.temperatura)} °C</Text>
                  <Text style={estilos.climaItem}>{Math.round(climaDaPista.umidade)}% de umidade</Text>
                  <Text style={estilos.climaItem}>vento de {Math.round(climaDaPista.vento)} km/h</Text>
                  <Text style={estilos.climaItem}>{climaDaPista.chuva > 0 ? 'chovendo' : 'sem chuva'}</Text>
                  {(finalizada ? corrida.climaFallback : previsao?.dados?.climaFallback) && (
                    <Text style={[estilos.climaItem, estilos.climaPadrao]}>clima padrão</Text>
                  )}
                </View>
              )}
            </Pista>
          </View>

          {finalizada && chegou && (
            <View style={estilos.cartao}>
              <Text style={estilos.resultadoTitulo}>
                {vencedora?.nome} venceu em {corrida.cidade}
              </Text>
              <Text style={[estilos.resultadoSub, corrida.venceu && estilos.resultadoSubVenceu]}>
                {corrida.venceu
                  ? `Você acertou, ${corrida.jogador}! +${corrida.pontosGanhos} pontos no ranking.`
                  : `Sua lesma chegou em ${posicaoDaSua}º. Você ganha +${corrida.pontosGanhos} ponto por participar.`}
              </Text>
              <Podio corrida={corrida} compacto={estreito} />
            </View>
          )}

          <View style={[estilos.colunas, largo && estilos.colunasLado]}>
            <View style={[estilos.cartao, largo && estilos.coluna]}>
              <Text style={estilos.titulo}>Bastidores</Text>
              <Text style={[estilos.ajuda, { marginBottom: 16 }]}>
                O caminho da sua aposta pela mensageria, atualizado ao vivo.
              </Text>
              <Bastidores corrida={corrida} falhaDaMensageria={falhaDaMensageria} />
            </View>

            <View style={[estilos.cartao, estilos.radioLesma, largo && estilos.coluna]}>
              <Text style={[estilos.titulo, { color: '#F1F6EC' }]}>Rádio Lesma</Text>
              <Text style={[estilos.ajuda, { color: '#A9C2A8', marginBottom: 12 }]}>
                O narrador conta como foi cada páreo assim que ele termina.
              </Text>
              {!narracoes.length ? (
                <Text style={[estilos.vazio, { color: '#A9C2A8' }]}>A rádio entra no ar depois do primeiro páreo.</Text>
              ) : (
                narracoes.slice(0, 4).map((n) => (
                  <View key={`${n.idCorrida}-${n.criadaEm}`} style={estilos.narracao}>
                    <Text style={estilos.narracaoQuando}>{haQuanto(n.criadaEm)}</Text>
                    <Text style={estilos.narracaoTexto}>{n.texto}</Text>
                  </View>
                ))
              )}
            </View>
          </View>

          <View style={estilos.cartao}>
            <Text style={estilos.titulo}>Ranking</Text>
            <View style={[estilos.colunasRanking, largo && estilos.colunasLado]}>
              <View style={largo && estilos.coluna}>
                <Text style={estilos.subtitulo}>Apostadores</Text>
                {!ranking?.jogadores?.length ? (
                  <Text style={estilos.vazio}>Ninguém apostou ainda. A primeira aposta é sua.</Text>
                ) : (
                  ranking.jogadores.map((j, i) => (
                    <View key={j.nome} style={estilos.linhaRanking}>
                      <Text style={estilos.posicao}>{i + 1}º</Text>
                      <View style={{ flex: 1 }}>
                        <Text style={estilos.rankingNome}>{j.nome}</Text>
                        <Text style={estilos.rankingDetalhe}>
                          {j.vitorias} {j.vitorias === 1 ? 'vitória' : 'vitórias'} em {j.corridas}{' '}
                          {j.corridas === 1 ? 'páreo' : 'páreos'}
                        </Text>
                      </View>
                      <Text style={estilos.rankingPontos}>{j.pontos} pts</Text>
                    </View>
                  ))
                )}
              </View>

              <View style={[{ gap: 8 }, largo && estilos.coluna]}>
                <Text style={estilos.subtitulo}>Vitórias por lesma</Text>
                {!ranking?.lesmas?.length ? (
                  <Text style={[estilos.vazio, { marginTop: 0 }]}>Nenhuma lesma venceu ainda.</Text>
                ) : (
                  ranking.lesmas.map((l) => {
                    const maximo = ranking.lesmas[0].vitorias || 1;
                    return (
                      <View key={l.lesma} style={estilos.linhaBarra}>
                        <Lesma codigo={l.lesma} largura={42} />
                        <Text style={estilos.barraNome} numberOfLines={1}>
                          {l.nome}
                        </Text>
                        <View style={estilos.barraFundo}>
                          <View
                            style={[
                              estilos.barra,
                              { width: `${(l.vitorias / maximo) * 100}%`, backgroundColor: coresDasLesmas[l.lesma] },
                            ]}
                          />
                        </View>
                        <Text style={estilos.barraValor}>{l.vitorias}</Text>
                      </View>
                    );
                  })
                )}
              </View>
            </View>
          </View>
        </>
      )}
    </ScrollView>
  );
}

// Clima de agora na cidade escolhida, com o palpite de quem sai na frente
function ClimaAgora({ nomeCidade, previsao, largo }) {
  if (!previsao) return null;

  if (previsao.carregando) {
    return (
      <View style={[estilos.climaAgora, estilos.climaAgoraLinha]}>
        <ActivityIndicator size="small" color={cores.musgo} />
        <Text style={estilos.ajuda}>Consultando o clima de agora em {nomeCidade}...</Text>
      </View>
    );
  }

  if (previsao.erro || !previsao.dados) {
    return (
      <View style={estilos.climaAgora}>
        <Text style={estilos.ajuda}>Não deu pra consultar o clima agora. Você ainda pode apostar normalmente.</Text>
      </View>
    );
  }

  const { clima, climaFallback, vantagens } = previsao.dados;
  const favoritas = vantagens.slice(0, 3).map((v) => v.nome);

  return (
    <View style={estilos.climaAgora}>
      <View style={estilos.climaAgoraTopo}>
        <Text style={estilos.climaAgoraTitulo}>Agora em {nomeCidade}</Text>
        {climaFallback && <Text style={estilos.climaAgoraAviso}>A API de clima não respondeu: valendo o clima padrão</Text>}
      </View>
      <Grade colunas={largo ? 4 : 2} espaco={10}>
        <Medida especialidade="Calor" valor={`${Math.round(clima.temperatura)} °C`} rotulo="temperatura" />
        <Medida especialidade="Umidade" valor={`${Math.round(clima.umidade)}%`} rotulo="umidade do ar" />
        <Medida especialidade="Vento" valor={`${Math.round(clima.vento)} km/h`} rotulo="vento" />
        <Medida
          especialidade="Chuva"
          valor={clima.chuva > 0 ? `${decimal(clima.chuva)} mm` : 'sem chuva'}
          rotulo="chuva agora"
        />
      </Grade>
      <Text style={estilos.palpite}>
        Com esse tempo, saem na frente: <Text style={estilos.palpiteForte}>{favoritas[0]}</Text>,{' '}
        <Text style={estilos.palpiteForte}>{favoritas[1]}</Text> e{' '}
        <Text style={estilos.palpiteForte}>{favoritas[2]}</Text>.
      </Text>
    </View>
  );
}

function Medida({ especialidade, valor, rotulo }) {
  return (
    <View style={estilos.medida}>
      <View style={{ flexShrink: 0 }}>
        <Icone especialidade={especialidade} cor={cores.musgo} tamanho={20} />
      </View>
      <View style={{ flexShrink: 1 }}>
        <Text style={estilos.medidaValor}>{valor}</Text>
        <Text style={estilos.medidaRotulo}>{rotulo}</Text>
      </View>
    </View>
  );
}

// Bloco numerado de um passo da aposta
function Secao({ numero, titulo, ajuda, children }) {
  return (
    <View style={estilos.secao}>
      <View style={estilos.secaoTopo}>
        <View style={estilos.secaoNumero}>
          <Text style={estilos.secaoNumeroTexto}>{numero}</Text>
        </View>
        <View style={{ flex: 1 }}>
          <Text style={estilos.secaoTitulo}>{titulo}</Text>
          {!!ajuda && <Text style={estilos.ajuda}>{ajuda}</Text>}
        </View>
      </View>
      {children}
    </View>
  );
}

// Grade com colunas de largura igual (todos os cartões da linha ficam do mesmo tamanho)
function Grade({ colunas, espaco, children }) {
  const [largura, setLargura] = useState(0);
  const itens = Children.toArray(children);
  const larguraItem = largura ? Math.floor((largura - espaco * (colunas - 1)) / colunas) : 0;

  return (
    <View style={[estilos.grade, { gap: espaco }]} onLayout={(e) => setLargura(e.nativeEvent.layout.width)}>
      {largura > 0 &&
        itens.map((item, i) => (
          <View key={i} style={{ width: larguraItem }}>
            {item}
          </View>
        ))}
    </View>
  );
}

// Selo de "escolhido"
function Marca({ cor }) {
  return (
    <View style={[estilos.marca, { backgroundColor: cor }]}>
      <Text style={estilos.marcaTexto}>✓</Text>
    </View>
  );
}

const decimal = (n) => n.toFixed(1).replace('.', ',');

function haQuanto(iso) {
  const segundos = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000);
  if (segundos < 60) return 'agora';
  if (segundos < 3600) return `há ${Math.floor(segundos / 60)} min`;
  if (segundos < 86400) return `há ${Math.floor(segundos / 3600)} h`;
  return `há ${Math.floor(segundos / 86400)} d`;
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { padding: 24, paddingBottom: 64, width: '100%', maxWidth: 1080, alignSelf: 'center', gap: 24 },

  barraTopo: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 12,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: cores.linha,
  },
  logo: { fontFamily: fontes.logo, fontSize: 34, lineHeight: 40, color: cores.musgo },
  logoPequeno: { fontSize: 28, lineHeight: 34 },
  ladoDireito: { flexDirection: 'row', flexWrap: 'wrap', alignItems: 'center', gap: 10 },
  conta: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  contaTexto: { fontFamily: fontes.texto, fontSize: 15, color: cores.tintaSuave },
  contaNome: { fontFamily: fontes.forte, color: cores.tinta },
  sair: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999, borderWidth: 1.5, borderColor: cores.linha },
  sairSobre: { borderColor: '#B9CBB3' },
  sairTexto: { fontFamily: fontes.forte, fontSize: 13, color: cores.tinta },
  apostador: {
    justifyContent: 'center',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    backgroundColor: cores.marcaSuave,
  },
  apostadorTexto: { fontFamily: fontes.texto, fontSize: 16, color: cores.tintaSuave },
  servidor: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingVertical: 7, paddingHorizontal: 14, borderRadius: 999 },
  servidor_acordando: { backgroundColor: '#F6E7B4' },
  servidor_online: { backgroundColor: '#D6EBD9' },
  servidor_erro: { backgroundColor: '#F2D4CC' },
  luz: { width: 8, height: 8, borderRadius: 4, backgroundColor: cores.folha },
  servidorTexto: { fontFamily: fontes.media, fontSize: 14, color: cores.tinta, flexShrink: 1 },

  intro: { gap: 10, paddingTop: 8 },
  manchete: { fontFamily: fontes.extra, fontSize: 40, lineHeight: 46, color: cores.tinta, maxWidth: 760, letterSpacing: -0.5 },
  manchetePequena: { fontSize: 30, lineHeight: 36 },
  chamada: { fontFamily: fontes.texto, fontSize: 17, lineHeight: 26, color: cores.tintaSuave, maxWidth: 640 },
  avisoErro: { backgroundColor: '#F7E3DD', borderRadius: 14, padding: 16, borderWidth: 1, borderColor: '#E9C3B8' },
  avisoErroTexto: { fontFamily: fontes.media, fontSize: 15, lineHeight: 22, color: cores.erro },

  painelAposta: {
    backgroundColor: cores.papel,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: cores.linha,
    padding: 28,
    gap: 36,
    shadowColor: '#1E4A34',
    shadowOpacity: 0.08,
    shadowRadius: 24,
    shadowOffset: { width: 0, height: 8 },
  },
  painelApostaEstreito: { padding: 18, gap: 28 },
  secao: { gap: 16 },
  secaoTopo: { flexDirection: 'row', alignItems: 'flex-start', gap: 14 },
  secaoNumero: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: cores.musgo,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
  },
  secaoNumeroTexto: { fontFamily: fontes.extra, fontSize: 16, color: '#FFFFFF' },
  secaoTitulo: { fontFamily: fontes.extra, fontSize: 22, lineHeight: 30, color: cores.tinta },
  ajuda: { fontFamily: fontes.texto, fontSize: 15, lineHeight: 21, color: cores.tintaSuave, marginTop: 2 },
  grade: { flexDirection: 'row', flexWrap: 'wrap' },
  sobre: { borderColor: '#B9CBB3' },

  cartaoCidade: {
    flex: 1,
    padding: 16,
    borderRadius: 14,
    borderWidth: 2,
    borderColor: cores.linha,
    backgroundColor: cores.papel,
    gap: 4,
  },
  cidadeAtiva: { borderColor: cores.musgo, backgroundColor: cores.marcaSuave },
  linhaTitulo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 8 },
  nomeCidade: { fontFamily: fontes.forte, fontSize: 17, color: cores.tinta },
  dicaCidade: { fontFamily: fontes.texto, fontSize: 14, lineHeight: 19, color: cores.tintaSuave },

  cartaoLesma: {
    flex: 1,
    padding: 10,
    paddingBottom: 14,
    borderRadius: 16,
    borderWidth: 2,
    borderColor: cores.linha,
    backgroundColor: cores.papel,
  },
  palco: { borderRadius: 10, alignItems: 'center', justifyContent: 'flex-end', paddingTop: 22, paddingBottom: 4 },
  numeroLesma: { position: 'absolute', top: 6, left: 10, fontFamily: fontes.extra, fontSize: 15 },
  selo: { position: 'absolute', top: 7, right: 8, paddingVertical: 3, paddingHorizontal: 9, borderRadius: 999 },
  seloFavorita: { backgroundColor: cores.gema },
  seloBoaChance: { backgroundColor: '#FFFFFF' },
  seloTexto: { fontFamily: fontes.extra, fontSize: 12, color: cores.tinta },
  linhaNome: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 8, marginTop: 12, marginHorizontal: 4 },
  nomeLesma: { flexShrink: 1, fontFamily: fontes.extra, fontSize: 18, color: cores.tinta },
  vantagem: { marginTop: 12, marginHorizontal: 4, gap: 5 },
  vantagemTopo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'baseline', gap: 6 },
  vantagemRotulo: { fontFamily: fontes.media, fontSize: 13, color: cores.tintaSuave },
  vantagemValor: { fontFamily: fontes.extra, fontSize: 13, color: cores.tinta },
  vantagemFundo: { height: 8, borderRadius: 4, backgroundColor: '#E5ECE0', overflow: 'hidden' },
  vantagemBarra: { height: 8, borderRadius: 4 },

  climaAgora: { backgroundColor: cores.marcaSuave, borderRadius: 14, padding: 16, gap: 12 },
  climaAgoraLinha: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  climaAgoraTopo: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'baseline', gap: 6 },
  climaAgoraTitulo: { fontFamily: fontes.extra, fontSize: 17, color: cores.tinta },
  climaAgoraAviso: { fontFamily: fontes.media, fontSize: 13, color: cores.erro },
  medida: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    backgroundColor: cores.papel,
    borderRadius: 12,
    paddingVertical: 10,
    paddingHorizontal: 12,
  },
  medidaValor: { fontFamily: fontes.extra, fontSize: 18, color: cores.tinta },
  medidaRotulo: { fontFamily: fontes.texto, fontSize: 13, color: cores.tintaSuave },
  palpite: { fontFamily: fontes.texto, fontSize: 15, lineHeight: 22, color: cores.tinta },
  palpiteForte: { fontFamily: fontes.forte },
  especialidade: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    gap: 6,
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 999,
    marginTop: 6,
    marginHorizontal: 4,
  },
  especialidadeTexto: { fontFamily: fontes.forte, fontSize: 13 },
  descricaoLesma: { fontFamily: fontes.texto, fontSize: 14, lineHeight: 20, color: cores.tintaSuave, marginTop: 8, marginHorizontal: 4 },
  marca: { width: 22, height: 22, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  marcaTexto: { color: '#FFFFFF', fontSize: 13, fontFamily: fontes.extra },

  resumo: { fontFamily: fontes.texto, fontSize: 16, lineHeight: 24, color: cores.tintaSuave },
  resumoForte: { fontFamily: fontes.forte, color: cores.tinta },
  linhaAposta: { flexDirection: 'row', gap: 12, alignItems: 'stretch' },
  linhaApostaEmPe: { flexDirection: 'column' },
  botao: {
    backgroundColor: cores.gema,
    borderRadius: 12,
    paddingVertical: 15,
    paddingHorizontal: 28,
    alignItems: 'center',
    justifyContent: 'center',
    borderBottomWidth: 4,
    borderBottomColor: cores.gemaEscura,
  },
  botaoSobre: { backgroundColor: '#F7CF5A' },
  botaoDesligado: { backgroundColor: '#ECE7D3', borderBottomColor: '#DCD5BC' },
  botaoApertado: { transform: [{ translateY: 2 }], borderBottomWidth: 2 },
  botaoTexto: { fontFamily: fontes.extra, fontSize: 17, color: cores.tinta },
  botaoTextoDesligado: { color: '#8D8A7C' },
  caos: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  caosTitulo: { fontFamily: fontes.forte, color: cores.tinta },
  caosTexto: { flex: 1, fontFamily: fontes.texto, fontSize: 14, lineHeight: 20, color: cores.tintaSuave },
  erro: { fontFamily: fontes.media, fontSize: 14, color: cores.erro },

  faixa: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    gap: 8,
    marginBottom: 12,
  },
  faixaTitulo: { fontFamily: fontes.extra, fontSize: 22, color: '#F1F6EC' },
  faixaSituacao: { fontFamily: fontes.media, fontSize: 15, color: cores.gema },
  climaNaPista: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginBottom: 12 },
  climaItem: {
    fontFamily: fontes.media,
    fontSize: 13,
    color: '#E3EEDC',
    backgroundColor: cores.musgoClaro,
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 999,
  },
  climaPadrao: { backgroundColor: '#6B4A2E' },

  cartao: { backgroundColor: cores.papel, borderRadius: 20, padding: 24, borderWidth: 1, borderColor: cores.linha },
  titulo: { fontFamily: fontes.extra, fontSize: 24, color: cores.tinta },
  subtitulo: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta, marginBottom: 4 },
  resultadoTitulo: { fontFamily: fontes.extra, fontSize: 28, lineHeight: 34, color: cores.tinta },
  resultadoSub: { fontFamily: fontes.texto, fontSize: 16, lineHeight: 24, color: cores.tintaSuave, marginTop: 4, marginBottom: 12 },
  resultadoSubVenceu: { color: cores.musgo, fontFamily: fontes.forte },

  colunas: { gap: 24 },
  colunasLado: { flexDirection: 'row', alignItems: 'stretch' },
  coluna: { flex: 1, flexBasis: 0 },
  colunasRanking: { gap: 28, marginTop: 14 },

  linhaRanking: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: cores.linha,
  },
  posicao: { width: 30, fontFamily: fontes.extra, fontSize: 16, color: cores.tintaSuave },
  rankingNome: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta },
  rankingDetalhe: { fontFamily: fontes.texto, fontSize: 13, color: cores.tintaSuave },
  rankingPontos: { fontFamily: fontes.extra, fontSize: 18, color: cores.musgo },
  linhaBarra: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  barraNome: { width: 104, fontFamily: fontes.media, fontSize: 14, color: cores.tinta },
  barraFundo: { flex: 1, height: 12, borderRadius: 6, backgroundColor: '#E5ECE0', overflow: 'hidden' },
  barra: { height: 12, borderRadius: 6 },
  barraValor: { width: 24, textAlign: 'right', fontFamily: fontes.forte, fontSize: 14, color: cores.tinta },
  vazio: { fontFamily: fontes.texto, fontSize: 15, color: cores.tintaSuave, marginTop: 8 },

  radioLesma: { backgroundColor: cores.musgo, borderColor: cores.musgo },
  narracao: { paddingVertical: 12, borderTopWidth: 1, borderTopColor: cores.musgoClaro, gap: 4 },
  narracaoQuando: { fontFamily: fontes.media, fontSize: 13, color: cores.gema },
  narracaoTexto: { fontFamily: fontes.texto, fontSize: 16, lineHeight: 24, color: '#EAF2E4' },
});
