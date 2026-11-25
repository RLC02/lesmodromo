import { useEffect, useRef, useState } from 'react';
import { AccessibilityInfo, Animated, Easing, StyleSheet, Text, View } from 'react-native';
import Lesma from './Lesma';
import { cores, coresDasLesmas, fontes } from '../tema';

const LARGURA_LESMA = 66;
const LARGURA_CHEGADA = 14;

function useMovimentoReduzido() {
  const [reduzido, setReduzido] = useState(false);
  useEffect(() => {
    AccessibilityInfo.isReduceMotionEnabled?.().then(setReduzido).catch(() => {});
  }, []);
  return reduzido;
}

// A pista: quatro raias de terra molhada. Cada lesma deixa um rastro de gosma por onde passa.
export default function Pista({ lesmas, resultado, idDaLargada, onVencedoraChegou, compacta, children }) {
  const [largura, setLargura] = useState(0);
  const posicoes = useRef({}).current;
  const balanco = useRef(new Animated.Value(0)).current;
  const movimentoReduzido = useMovimentoReduzido();

  lesmas.forEach((l) => {
    if (!posicoes[l.codigo]) posicoes[l.codigo] = new Animated.Value(0);
  });

  // Sem resultado: todo mundo volta para a largada
  useEffect(() => {
    if (!resultado) Object.values(posicoes).forEach((p) => p.setValue(0));
  }, [resultado]);

  // Quem fez mais pontos chega primeiro. A diferença é exagerada para dar emoção.
  useEffect(() => {
    if (!resultado || !idDaLargada) return undefined;

    const melhor = resultado[0].pontuacao;
    const corrida = Animated.parallel(
      resultado.map((r) =>
        Animated.timing(posicoes[r.lesma], {
          toValue: 1,
          duration: movimentoReduzido ? 500 : Math.min(3200 * (melhor / r.pontuacao) ** 2, 7000),
          easing: Easing.inOut(Easing.quad),
          useNativeDriver: false,
        })
      )
    );

    const rebolado = Animated.loop(
      Animated.sequence([
        Animated.timing(balanco, { toValue: 1, duration: 280, useNativeDriver: false }),
        Animated.timing(balanco, { toValue: 0, duration: 280, useNativeDriver: false }),
      ])
    );

    corrida.start(() => rebolado.stop());
    if (!movimentoReduzido) rebolado.start();
    const aviso = setTimeout(() => onVencedoraChegou?.(), movimentoReduzido ? 550 : 3300);

    return () => {
      clearTimeout(aviso);
      rebolado.stop();
    };
  }, [idDaLargada]);

  const percurso = Math.max(largura - LARGURA_LESMA - LARGURA_CHEGADA, 0);

  return (
    <View style={estilos.moldura}>
      {children}

      <View style={estilos.gramado} />
      {lesmas.map((l, i) => (
        <View key={l.codigo} style={[estilos.raia, i > 0 && estilos.divisaoDeRaia]}>
          <View style={[estilos.placa, compacta && estilos.placaCompacta]}>
            <View style={[estilos.numero, { backgroundColor: coresDasLesmas[l.codigo] }]}>
              <Text style={estilos.numeroTexto}>{l.numero}</Text>
            </View>
            {!compacta && (
              <Text style={estilos.nomeNaRaia} numberOfLines={1}>
                {l.nome}
              </Text>
            )}
          </View>

          <View style={estilos.chao} onLayout={i === 0 ? (e) => setLargura(e.nativeEvent.layout.width) : undefined}>
            <Animated.View
              style={[
                estilos.gosma,
                {
                  width: posicoes[l.codigo].interpolate({
                    inputRange: [0, 1],
                    outputRange: [LARGURA_LESMA * 0.3, percurso + LARGURA_LESMA * 0.3],
                  }),
                },
              ]}
            >
              <View style={estilos.brilhoDaGosma} />
            </Animated.View>

            <Animated.View
              style={[
                estilos.lesma,
                {
                  transform: [
                    { translateX: posicoes[l.codigo].interpolate({ inputRange: [0, 1], outputRange: [0, percurso] }) },
                    { scaleX: balanco.interpolate({ inputRange: [0, 1], outputRange: [1, 1.07] }) },
                  ],
                },
              ]}
              accessibilityLabel={`${l.nome}, raia ${l.numero}`}
            >
              <Lesma codigo={l.codigo} largura={LARGURA_LESMA} />
            </Animated.View>

            <View style={estilos.chegada}>
              {Array.from({ length: 8 }).map((_, linha) => (
                <View key={linha} style={estilos.linhaXadrez}>
                  <View style={[estilos.casa, linha % 2 === 0 && estilos.casaEscura]} />
                  <View style={[estilos.casa, linha % 2 === 1 && estilos.casaEscura]} />
                </View>
              ))}
            </View>
          </View>
        </View>
      ))}
      <View style={estilos.gramado} />
    </View>
  );
}

const estilos = StyleSheet.create({
  moldura: {
    backgroundColor: cores.musgo,
    borderRadius: 20,
    padding: 20,
  },
  gramado: {
    height: 10,
    backgroundColor: cores.grama,
    borderRadius: 4,
  },
  raia: {
    flexDirection: 'row',
    alignItems: 'stretch',
    height: 54,
    backgroundColor: cores.terra,
  },
  divisaoDeRaia: {
    borderTopWidth: 2,
    borderTopColor: cores.terraEscura,
  },
  placa: {
    width: 170,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 10,
    backgroundColor: cores.terraEscura,
  },
  placaCompacta: { width: 40, paddingHorizontal: 6 },
  numero: {
    width: 26,
    height: 26,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
  },
  numeroTexto: { color: '#FFFFFF', fontFamily: fontes.extra, fontSize: 14 },
  nomeNaRaia: { flex: 1, color: '#EFE6D2', fontFamily: fontes.media, fontSize: 14 },
  chao: { flex: 1, justifyContent: 'flex-end', overflow: 'hidden' },
  gosma: {
    position: 'absolute',
    left: 0,
    bottom: 4,
    height: 8,
    borderTopRightRadius: 6,
    borderBottomRightRadius: 6,
    backgroundColor: cores.gosma,
    opacity: 0.7,
  },
  brilhoDaGosma: {
    height: 2,
    marginTop: 2,
    marginRight: 8,
    backgroundColor: '#FFFFFF',
    opacity: 0.8,
    borderRadius: 1,
  },
  lesma: { position: 'absolute', left: 0, bottom: 2 },
  chegada: { position: 'absolute', right: 0, top: 0, bottom: 0, width: LARGURA_CHEGADA, justifyContent: 'space-between' },
  linhaXadrez: { flexDirection: 'row', flex: 1 },
  casa: { flex: 1, backgroundColor: '#F4F1E6' },
  casaEscura: { backgroundColor: cores.tinta },
});
