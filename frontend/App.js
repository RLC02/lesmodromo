import { useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
  useWindowDimensions,
} from "react-native";
import { StatusBar } from "expo-status-bar";
import { useFonts } from "expo-font";
import { API_URL, chamarApi, esperar, novoIdDaAposta } from "./src/api";
import { cores, coresDasLesmas, fontes } from "./src/tema";
import Pista from "./src/componentes/Pista";
import Lesma from "./src/componentes/Lesma";
import Bastidores from "./src/componentes/Bastidores";

export default function App() {
  // Fontes ficam em assets/fonts (licença OFL). Bagel Fat One no logo, Bricolage Grotesque no resto.
  const [fontesProntas] = useFonts({
    BagelFatOne_400Regular: require("./assets/fonts/BagelFatOne_400Regular.ttf"),
    BricolageGrotesque_400Regular: require("./assets/fonts/BricolageGrotesque_400Regular.ttf"),
    BricolageGrotesque_500Medium: require("./assets/fonts/BricolageGrotesque_500Medium.ttf"),
    BricolageGrotesque_700Bold: require("./assets/fonts/BricolageGrotesque_700Bold.ttf"),
    BricolageGrotesque_800ExtraBold: require("./assets/fonts/BricolageGrotesque_800ExtraBold.ttf"),
  });
  const { width } = useWindowDimensions();
  const largo = width >= 860;
  const estreito = width < 520;

  const [servidor, setServidor] = useState({
    estado: "acordando",
    texto: "Acordando o servidor",
  });
  const [info, setInfo] = useState(null);
  const [ranking, setRanking] = useState(null);
  const [narracoes, setNarracoes] = useState([]);

  const [jogador, setJogador] = useState("");
  const [cidade, setCidade] = useState("SAO_PAULO");
  const [lesma, setLesma] = useState(null);
  const [modoCaos, setModoCaos] = useState(false);

  const [corrida, setCorrida] = useState(null);
  const [falhaDaMensageria, setFalhaDaMensageria] = useState(false);
  const [idDaLargada, setIdDaLargada] = useState(null);
  const [chegou, setChegou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");
  const largadaAtual = useRef(0);
  const rolagem = useRef(null);

  // Ao abrir: busca lesmas e cidades. Se o Render estiver dormindo, o retry espera ele acordar.
  useEffect(() => {
    chamarApi("/api/info", {
      tentativas: 6,
      aoTentar: (t, total) =>
        setServidor({
          estado: "acordando",
          texto:
            t === 1
              ? "Acordando o servidor"
              : `Servidor acordando (tentativa ${t} de ${total})`,
        }),
    })
      .then((dados) => {
        setInfo(dados);
        setServidor({ estado: "online", texto: "Servidor no ar" });
        carregarPainel();
      })
      .catch((e) =>
        setServidor({
          estado: "erro",
          texto: "Servidor fora do ar",
          detalhe: API_URL
            ? `Não consegui falar com ${API_URL} (${e.message}). Confira se a API está no ar e recarregue a página.`
            : "O front não encontrou a URL da API. Crie o arquivo frontend/.env com EXPO_PUBLIC_API_URL=http://localhost:8080 e reinicie com npx expo start --web -c.",
        }),
      );
  }, []);

  function carregarPainel() {
    chamarApi("/api/ranking", { tentativas: 3 })
      .then(setRanking)
      .catch(() => {});
    chamarApi("/api/narracoes", { tentativas: 3 })
      .then(setNarracoes)
      .catch(() => {});
  }

  async function apostar() {
    const minhaLargada = ++largadaAtual.current;
    setErro("");
    setCorrida(null);
    setIdDaLargada(null);
    setChegou(false);
    setFalhaDaMensageria(false);
    setEnviando(true);
    rolagem.current?.scrollTo({ y: 0, animated: true }); // a corrida acontece lá em cima, na pista

    try {
      const pule = await chamarApi("/api/corridas", {
        metodo: "POST",
        corpo: {
          jogador,
          lesma,
          cidade,
          modoCaos,
          idCliente: novoIdDaAposta(),
        },
      });
      // Se já voltou finalizada, é porque o RabbitMQ estava fora e a API fez o fallback
      const semMensageria = pule.status === "FINALIZADA";
      setFalhaDaMensageria(semMensageria);
      setCorrida(pule);
      await acompanhar(pule, minhaLargada, semMensageria);
    } catch (e) {
      setErro(e.message);
    } finally {
      if (largadaAtual.current === minhaLargada) setEnviando(false);
    }
  }

  // A corrida acontece nos consumidores do RabbitMQ. O front pergunta o status até todos terminarem.
  async function acompanhar(pule, minhaLargada, semMensageria) {
    let atual = pule;
    for (let volta = 0; volta < 45; volta++) {
      if (largadaAtual.current !== minhaLargada) return; // o jogador já largou outra corrida

      if (atual.status === "FINALIZADA") setIdDaLargada(atual.id);
      const tudoPronto =
        atual.status === "FINALIZADA" &&
        atual.rankingAtualizado &&
        (atual.narrada || semMensageria);
      if (tudoPronto) break;

      await esperar(700);
      atual = await chamarApi(`/api/corridas/${pule.id}`, { tentativas: 3 });
      if (largadaAtual.current === minhaLargada) setCorrida(atual);
    }
    carregarPainel();
  }

  if (!fontesProntas) return <View style={estilos.tela} />;

  const nomeDaCidade =
    info?.cidades.find((c) => c.codigo === cidade)?.nome ?? "";
  const finalizada = corrida?.status === "FINALIZADA";
  const vencedora = info?.lesmas.find((l) => l.codigo === corrida?.vencedora);
  const podeApostar =
    servidor.estado === "online" && jogador.trim() && lesma && !enviando;

  let situacaoDaPista = "Aguardando apostas";
  if (corrida?.status === "NA_LARGADA")
    situacaoDaPista = "Pule registrada, esperando o juiz";
  if (corrida?.status === "CORRENDO")
    situacaoDaPista = "O juiz está consultando o clima";
  if (finalizada && !chegou) situacaoDaPista = "Valendo!";
  if (finalizada && chegou) situacaoDaPista = `${vencedora?.nome} venceu`;

  return (
    <ScrollView
      ref={rolagem}
      style={estilos.tela}
      contentContainerStyle={estilos.conteudo}
    >
      <StatusBar style="dark" />

      <View style={estilos.topo}>
        <View style={estilos.linhaDoTopo}>
          <Text
            style={[estilos.logo, estreito && estilos.logoPequeno]}
            accessibilityRole="header"
          >
            Lesmódromo
          </Text>
          <View
            style={[estilos.servidor, estilos[`servidor_${servidor.estado}`]]}
          >
            {servidor.estado === "acordando" ? (
              <ActivityIndicator size="small" color={cores.tinta} />
            ) : (
              <View
                style={[
                  estilos.luz,
                  servidor.estado === "erro" && { backgroundColor: cores.erro },
                ]}
              />
            )}
            <Text style={estilos.servidorTexto}>{servidor.texto}</Text>
          </View>
        </View>
        <Text style={estilos.chamada}>
          Corrida de lesmas decidida pelo clima de agora. Escolha a cidade,
          aposte numa lesma e torça pro tempo ajudar.
        </Text>
      </View>

      {servidor.estado === "erro" && (
        <View style={estilos.avisoErro}>
          <Text style={estilos.avisoErroTexto}>{servidor.detalhe}</Text>
        </View>
      )}

      {info && (
        <>
          <Pista
            lesmas={info.lesmas}
            resultado={finalizada ? corrida.resultado : null}
            idDaLargada={idDaLargada}
            onVencedoraChegou={() => setChegou(true)}
            compacta={estreito}
          >
            <View style={estilos.faixa}>
              <Text style={estilos.faixaTitulo}>
                Páreo em {corrida?.cidade ?? nomeDaCidade}
              </Text>
              <Text style={estilos.faixaSituacao}>{situacaoDaPista}</Text>
            </View>
            {finalizada && corrida.clima && (
              <View style={estilos.climaNaPista}>
                <Text style={estilos.climaItem}>
                  {Math.round(corrida.clima.temperatura)} °C
                </Text>
                <Text style={estilos.climaItem}>
                  {Math.round(corrida.clima.umidade)}% de umidade
                </Text>
                <Text style={estilos.climaItem}>
                  vento de {Math.round(corrida.clima.vento)} km/h
                </Text>
                <Text style={estilos.climaItem}>
                  {corrida.clima.chuva > 0 ? "chovendo" : "sem chuva"}
                </Text>
                {corrida.climaFallback && (
                  <Text style={[estilos.climaItem, estilos.climaPadrao]}>
                    clima padrão
                  </Text>
                )}
              </View>
            )}
          </Pista>

          {finalizada && chegou && (
            <View
              style={[
                estilos.resultado,
                { borderColor: coresDasLesmas[corrida.vencedora] },
              ]}
            >
              <View
                style={[
                  estilos.resultadoTopo,
                  !largo && {
                    flexDirection: "column",
                    alignItems: "flex-start",
                  },
                ]}
              >
                <Lesma cor={coresDasLesmas[corrida.vencedora]} largura={88} />
                <View style={{ flex: 1 }}>
                  <Text style={estilos.resultadoTitulo}>
                    {vencedora?.nome} venceu em {corrida.cidade}
                  </Text>
                  <Text style={estilos.resultadoSub}>
                    {corrida.venceu
                      ? `Você apostou certo, ${corrida.jogador}. +${corrida.pontosGanhos} pontos.`
                      : `Sua lesma chegou em ${corrida.resultado.find((r) => r.lesma === corrida.lesmaEscolhida)?.posicao}º. +${corrida.pontosGanhos} ponto pela participação.`}
                  </Text>
                </View>
              </View>
              {corrida.resultado.map((r) => (
                <View key={r.lesma} style={estilos.linhaResultado}>
                  <Text style={estilos.posicao}>{r.posicao}º</Text>
                  <View
                    style={[
                      estilos.bolinha,
                      { backgroundColor: coresDasLesmas[r.lesma] },
                    ]}
                  />
                  <Text
                    style={[
                      estilos.resultadoNome,
                      r.lesma === corrida.lesmaEscolhida && estilos.suaLesma,
                    ]}
                  >
                    {r.nome}
                    {r.lesma === corrida.lesmaEscolhida ? " (sua)" : ""}
                  </Text>
                  <Text style={estilos.pontos}>{decimal(r.pontuacao)}</Text>
                  {!estreito && (
                    <Text style={estilos.bonus}>
                      +{decimal(r.bonus)} do clima
                    </Text>
                  )}
                </View>
              ))}
            </View>
          )}

          <View style={[estilos.colunas, largo && estilos.colunasLado]}>
            <View style={largo && estilos.coluna}>
              <View style={[estilos.pule, largo && { flex: 1 }]}>
                <Text style={estilos.titulo}>Sua pule</Text>
                <Text style={estilos.ajuda}>
                  Pule é o bilhete de aposta do turfe.
                </Text>

                <Text style={estilos.rotulo}>Seu nome</Text>
                <TextInput
                  style={estilos.campo}
                  placeholder="Como você quer aparecer no ranking"
                  placeholderTextColor="#8A9A8E"
                  value={jogador}
                  onChangeText={setJogador}
                  maxLength={20}
                />

                <Text style={estilos.rotulo}>Cidade do páreo</Text>
                <View style={estilos.chips}>
                  {info.cidades.map((c) => (
                    <Pressable
                      key={c.codigo}
                      onPress={() => setCidade(c.codigo)}
                      style={[
                        estilos.chip,
                        cidade === c.codigo && estilos.chipAtivo,
                      ]}
                      accessibilityRole="radio"
                      accessibilityState={{ checked: cidade === c.codigo }}
                    >
                      <Text
                        style={[
                          estilos.chipTexto,
                          cidade === c.codigo && estilos.chipTextoAtivo,
                        ]}
                      >
                        {c.nome}
                      </Text>
                    </Pressable>
                  ))}
                </View>

                <Text style={estilos.rotulo}>Lesma</Text>
                <View style={{ gap: 8 }}>
                  {info.lesmas.map((l) => {
                    const escolhida = lesma === l.codigo;
                    const cor = coresDasLesmas[l.codigo];
                    return (
                      <Pressable
                        key={l.codigo}
                        onPress={() => setLesma(l.codigo)}
                        style={[
                          estilos.opcaoLesma,
                          escolhida && {
                            borderColor: cor,
                            backgroundColor: `${cor}18`,
                          },
                        ]}
                        accessibilityRole="radio"
                        accessibilityState={{ checked: escolhida }}
                      >
                        <Lesma cor={cor} largura={48} />
                        <View style={{ flex: 1 }}>
                          <Text style={estilos.opcaoNome}>
                            {l.nome}{" "}
                            <Text
                              style={[estilos.especialidade, { color: cor }]}
                            >
                              {l.especialidade}
                            </Text>
                          </Text>
                          <Text style={estilos.opcaoDescricao}>
                            {l.descricao}
                          </Text>
                        </View>
                        <View
                          style={[
                            estilos.radio,
                            escolhida && { borderColor: cor },
                          ]}
                        >
                          {escolhida && (
                            <View
                              style={[
                                estilos.radioMiolo,
                                { backgroundColor: cor },
                              ]}
                            />
                          )}
                        </View>
                      </Pressable>
                    );
                  })}
                </View>

                <View style={estilos.picote}>
                  <View style={[estilos.furo, estilos.furoEsquerdo]} />
                  <View style={estilos.tracejado} />
                  <View style={[estilos.furo, estilos.furoDireito]} />
                </View>

                <View style={estilos.caos}>
                  <View style={{ flex: 1 }}>
                    <Text style={estilos.caosTitulo}>Modo caos</Text>
                    <Text style={estilos.caosTexto}>
                      A API de clima falha 2 vezes de propósito, pra mostrar o
                      retry.
                    </Text>
                  </View>
                  <Switch
                    value={modoCaos}
                    onValueChange={setModoCaos}
                    trackColor={{ false: cores.linha, true: cores.gemaEscura }}
                    thumbColor="#FFFFFF"
                    activeThumbColor="#FFFFFF"
                  />
                </View>

                <Pressable
                  onPress={apostar}
                  disabled={!podeApostar}
                  style={({ pressed }) => [
                    estilos.botao,
                    !podeApostar && estilos.botaoDesligado,
                    pressed && estilos.botaoApertado,
                  ]}
                >
                  <Text style={estilos.botaoTexto}>
                    {enviando ? "Páreo em andamento" : "Apostar e largar"}
                  </Text>
                </Pressable>
                {!podeApostar && !enviando && servidor.estado === "online" && (
                  <Text style={estilos.dica}>
                    {!jogador.trim()
                      ? "Escreva seu nome para apostar."
                      : !lesma
                        ? "Escolha uma lesma."
                        : ""}
                  </Text>
                )}
                {!!erro && <Text style={estilos.erro}>{erro}</Text>}
              </View>
            </View>

            <View style={[estilos.pilha, largo && estilos.coluna]}>
              <View style={estilos.cartao}>
                <Text style={estilos.titulo}>Bastidores</Text>
                <Text style={[estilos.ajuda, { marginBottom: 16 }]}>
                  O caminho da sua aposta pela mensageria, atualizado ao vivo.
                </Text>
                <Bastidores
                  corrida={corrida}
                  falhaDaMensageria={falhaDaMensageria}
                />
              </View>

              <View
                style={[
                  estilos.cartao,
                  estilos.radioLesma,
                  largo && { flex: 1 },
                ]}
              >
                <Text style={[estilos.titulo, { color: "#F1F6EC" }]}>
                  Rádio Lesma
                </Text>
                <Text
                  style={[
                    estilos.ajuda,
                    { color: "#A9C2A8", marginBottom: 12 },
                  ]}
                >
                  Frases do consumidor narrador, que ouve o mesmo evento que o
                  ranking.
                </Text>
                {!narracoes.length ? (
                  <Text style={[estilos.vazio, { color: "#A9C2A8" }]}>
                    A rádio entra no ar depois do primeiro páreo.
                  </Text>
                ) : (
                  narracoes.slice(0, 3).map((n) => (
                    <View
                      key={`${n.idCorrida}-${n.criadaEm}`}
                      style={estilos.narracao}
                    >
                      <Text style={estilos.narracaoQuando}>
                        {haQuanto(n.criadaEm)}
                      </Text>
                      <Text style={estilos.narracaoTexto}>{n.texto}</Text>
                    </View>
                  ))
                )}
              </View>
            </View>
          </View>

          <View style={estilos.cartao}>
            <Text style={estilos.titulo}>Ranking</Text>
            <View
              style={[estilos.colunasRanking, largo && estilos.colunasLado]}
            >
              <View style={largo && estilos.coluna}>
                <Text style={estilos.subtitulo}>Apostadores</Text>
                {!ranking?.jogadores?.length ? (
                  <Text style={estilos.vazio}>
                    Ninguém apostou ainda. A primeira pule é sua.
                  </Text>
                ) : (
                  ranking.jogadores.map((j, i) => (
                    <View key={j.nome} style={estilos.linhaRanking}>
                      <Text style={estilos.posicao}>{i + 1}º</Text>
                      <View style={{ flex: 1 }}>
                        <Text style={estilos.rankingNome}>{j.nome}</Text>
                        <Text style={estilos.rankingDetalhe}>
                          {j.vitorias}{" "}
                          {j.vitorias === 1 ? "vitória" : "vitórias"} em{" "}
                          {j.corridas} {j.corridas === 1 ? "páreo" : "páreos"}
                        </Text>
                      </View>
                      <Text style={estilos.rankingPontos}>{j.pontos} pts</Text>
                    </View>
                  ))
                )}
              </View>

              <View style={[{ gap: 10 }, largo && estilos.coluna]}>
                <Text style={estilos.subtitulo}>Vitórias por lesma</Text>
                {!ranking?.lesmas?.length ? (
                  <Text style={[estilos.vazio, { marginTop: 0 }]}>
                    Nenhuma lesma venceu ainda.
                  </Text>
                ) : (
                  ranking.lesmas.map((l) => {
                    const maximo = ranking.lesmas[0].vitorias || 1;
                    return (
                      <View key={l.lesma} style={estilos.linhaBarra}>
                        <Text style={estilos.barraNome}>{l.nome}</Text>
                        <View style={estilos.barraFundo}>
                          <View
                            style={[
                              estilos.barra,
                              {
                                width: `${(l.vitorias / maximo) * 100}%`,
                                backgroundColor: coresDasLesmas[l.lesma],
                              },
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

const decimal = (n) => n.toFixed(1).replace(".", ",");

function haQuanto(iso) {
  const segundos = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000);
  if (segundos < 60) return "agora";
  if (segundos < 3600) return `há ${Math.floor(segundos / 60)} min`;
  if (segundos < 86400) return `há ${Math.floor(segundos / 3600)} h`;
  return `há ${Math.floor(segundos / 86400)} d`;
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: {
    padding: 24,
    paddingBottom: 56,
    width: "100%",
    maxWidth: 1080,
    alignSelf: "center",
    gap: 24,
  },

  topo: { gap: 6, paddingTop: 12 },
  linhaDoTopo: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 12,
  },
  logo: {
    fontFamily: fontes.logo,
    fontSize: 64,
    lineHeight: 72,
    color: cores.musgo,
  },
  logoPequeno: { fontSize: 46, lineHeight: 54 },
  chamada: {
    fontFamily: fontes.texto,
    fontSize: 17,
    lineHeight: 26,
    color: cores.tintaSuave,
    maxWidth: 560,
  },
  avisoErro: {
    backgroundColor: "#F7E3DD",
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: "#E9C3B8",
  },
  avisoErroTexto: {
    fontFamily: fontes.media,
    fontSize: 15,
    lineHeight: 22,
    color: cores.erro,
  },
  servidor: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 999,
  },
  servidor_acordando: { backgroundColor: "#F6E7B4" },
  servidor_online: { backgroundColor: "#D3E6C9" },
  servidor_erro: { backgroundColor: "#F2D4CC" },
  luz: { width: 9, height: 9, borderRadius: 5, backgroundColor: cores.folha },
  servidorTexto: {
    fontFamily: fontes.media,
    fontSize: 14,
    color: cores.tinta,
    flexShrink: 1,
  },

  faixa: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-between",
    alignItems: "baseline",
    gap: 8,
    marginBottom: 12,
  },
  faixaTitulo: { fontFamily: fontes.extra, fontSize: 22, color: "#F1F6EC" },
  faixaSituacao: { fontFamily: fontes.media, fontSize: 15, color: cores.gema },
  climaNaPista: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 6,
    marginBottom: 12,
  },
  climaItem: {
    fontFamily: fontes.media,
    fontSize: 13,
    color: "#E3EEDC",
    backgroundColor: cores.musgoClaro,
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 999,
  },
  climaPadrao: { backgroundColor: "#6B4A2E" },

  resultado: {
    backgroundColor: cores.papel,
    borderRadius: 20,
    borderWidth: 3,
    padding: 24,
    gap: 4,
  },
  resultadoTopo: {
    flexDirection: "row",
    alignItems: "center",
    gap: 16,
    marginBottom: 10,
  },
  resultadoTitulo: {
    fontFamily: fontes.extra,
    fontSize: 28,
    lineHeight: 34,
    color: cores.tinta,
  },
  resultadoSub: {
    fontFamily: fontes.texto,
    fontSize: 16,
    lineHeight: 24,
    color: cores.tintaSuave,
    marginTop: 2,
  },
  linhaResultado: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    paddingVertical: 8,
    borderTopWidth: 1,
    borderTopColor: cores.linha,
  },
  posicao: {
    width: 30,
    fontFamily: fontes.extra,
    fontSize: 16,
    color: cores.tintaSuave,
  },
  bolinha: { width: 12, height: 12, borderRadius: 6 },
  resultadoNome: {
    flex: 1,
    fontFamily: fontes.media,
    fontSize: 16,
    color: cores.tinta,
  },
  suaLesma: { fontFamily: fontes.extra },
  pontos: {
    fontFamily: fontes.forte,
    fontSize: 16,
    color: cores.tinta,
    minWidth: 44,
    textAlign: "right",
  },
  bonus: {
    fontFamily: fontes.texto,
    fontSize: 13,
    color: cores.tintaSuave,
    minWidth: 104,
    textAlign: "right",
  },

  colunas: { gap: 24 },
  colunasLado: { flexDirection: "row", alignItems: "stretch" },
  coluna: { flex: 1, flexBasis: 0 },
  pilha: { gap: 24 },
  colunasRanking: { gap: 28, marginTop: 14 },
  cartao: {
    backgroundColor: cores.papel,
    borderRadius: 20,
    padding: 24,
    borderWidth: 1,
    borderColor: cores.linha,
  },
  titulo: { fontFamily: fontes.extra, fontSize: 24, color: cores.tinta },
  subtitulo: {
    fontFamily: fontes.forte,
    fontSize: 16,
    color: cores.tinta,
    marginBottom: 2,
  },
  ajuda: {
    fontFamily: fontes.texto,
    fontSize: 14,
    lineHeight: 20,
    color: cores.tintaSuave,
    marginTop: 2,
  },

  pule: {
    backgroundColor: cores.papel,
    borderRadius: 20,
    padding: 24,
    borderWidth: 1,
    borderColor: cores.linha,
    overflow: "hidden",
  },
  rotulo: {
    fontFamily: fontes.forte,
    fontSize: 15,
    color: cores.tinta,
    marginTop: 18,
    marginBottom: 8,
  },
  campo: {
    fontFamily: fontes.texto,
    fontSize: 16,
    color: cores.tinta,
    borderWidth: 1.5,
    borderColor: cores.linha,
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 14,
    backgroundColor: "#FFFFFF",
  },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  chip: {
    paddingVertical: 9,
    paddingHorizontal: 16,
    borderRadius: 999,
    borderWidth: 1.5,
    borderColor: cores.linha,
    backgroundColor: "#FFFFFF",
  },
  chipAtivo: { backgroundColor: cores.musgo, borderColor: cores.musgo },
  chipTexto: { fontFamily: fontes.media, fontSize: 15, color: cores.tinta },
  chipTextoAtivo: { color: "#FFFFFF" },
  opcaoLesma: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    padding: 12,
    borderRadius: 14,
    borderWidth: 2,
    borderColor: cores.linha,
    backgroundColor: "#FFFFFF",
  },
  opcaoNome: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta },
  especialidade: { fontFamily: fontes.media, fontSize: 14 },
  opcaoDescricao: {
    fontFamily: fontes.texto,
    fontSize: 14,
    lineHeight: 19,
    color: cores.tintaSuave,
    marginTop: 1,
  },
  radio: {
    width: 22,
    height: 22,
    borderRadius: 11,
    borderWidth: 2,
    borderColor: cores.linha,
    alignItems: "center",
    justifyContent: "center",
  },
  radioMiolo: { width: 10, height: 10, borderRadius: 5 },

  picote: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 24,
    marginHorizontal: -24,
  },
  furo: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: cores.fundo,
    borderWidth: 1,
    borderColor: cores.linha,
  },
  furoEsquerdo: { marginLeft: -11 },
  furoDireito: { marginRight: -11 },
  tracejado: {
    flex: 1,
    borderTopWidth: 2,
    borderStyle: "dashed",
    borderColor: cores.linha,
    marginHorizontal: 6,
  },

  caos: { flexDirection: "row", alignItems: "center", gap: 12, marginTop: 18 },
  caosTitulo: { fontFamily: fontes.forte, fontSize: 15, color: cores.tinta },
  caosTexto: {
    fontFamily: fontes.texto,
    fontSize: 14,
    lineHeight: 19,
    color: cores.tintaSuave,
    marginTop: 1,
  },

  botao: {
    backgroundColor: cores.gema,
    borderRadius: 14,
    paddingVertical: 16,
    alignItems: "center",
    marginTop: 18,
    borderBottomWidth: 4,
    borderBottomColor: cores.gemaEscura,
  },
  botaoDesligado: { backgroundColor: "#E9E3C9", borderBottomColor: "#D8D1B3" },
  botaoApertado: { transform: [{ translateY: 2 }], borderBottomWidth: 2 },
  botaoTexto: { fontFamily: fontes.extra, fontSize: 18, color: cores.tinta },
  dica: {
    fontFamily: fontes.texto,
    fontSize: 14,
    color: cores.tintaSuave,
    marginTop: 8,
    textAlign: "center",
  },
  erro: {
    fontFamily: fontes.media,
    fontSize: 14,
    color: cores.erro,
    marginTop: 10,
  },

  linhaRanking: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: cores.linha,
  },
  rankingNome: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta },
  rankingDetalhe: {
    fontFamily: fontes.texto,
    fontSize: 13,
    color: cores.tintaSuave,
  },
  rankingPontos: { fontFamily: fontes.extra, fontSize: 18, color: cores.musgo },
  linhaBarra: { flexDirection: "row", alignItems: "center", gap: 10 },
  barraNome: {
    width: 110,
    fontFamily: fontes.media,
    fontSize: 14,
    color: cores.tinta,
  },
  barraFundo: {
    flex: 1,
    height: 12,
    borderRadius: 6,
    backgroundColor: "#E1EADB",
    overflow: "hidden",
  },
  barra: { height: 12, borderRadius: 6 },
  barraValor: {
    width: 24,
    textAlign: "right",
    fontFamily: fontes.forte,
    fontSize: 14,
    color: cores.tinta,
  },
  vazio: {
    fontFamily: fontes.texto,
    fontSize: 15,
    color: cores.tintaSuave,
    marginTop: 12,
  },

  radioLesma: { backgroundColor: cores.musgo, borderColor: cores.musgo },
  narracao: {
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: cores.musgoClaro,
    gap: 4,
  },
  narracaoQuando: { fontFamily: fontes.media, fontSize: 13, color: cores.gema },
  narracaoTexto: {
    fontFamily: fontes.texto,
    fontSize: 16,
    lineHeight: 24,
    color: "#EAF2E4",
  },
});
