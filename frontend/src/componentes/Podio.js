import { StyleSheet, Text, View } from 'react-native';
import Lesma from './Lesma';
import { cores, coresDasLesmas, fontes } from '../tema';

const decimal = (n) => n.toFixed(1).replace('.', ',');

// Pódio com as três primeiras e a lista do resto da corrida
export default function Podio({ corrida, compacto }) {
  const [primeira, segunda, terceira, ...resto] = corrida.resultado;
  const ordemNoPodio = [segunda, primeira, terceira].filter(Boolean);
  const altura = { 1: compacto ? 56 : 76, 2: compacto ? 40 : 54, 3: compacto ? 28 : 38 };
  const tamanho = { 1: compacto ? 96 : 150, 2: compacto ? 74 : 116, 3: compacto ? 74 : 116 };

  return (
    <View>
      <View style={estilos.podio}>
        {ordemNoPodio.map((c) => {
          const sua = c.lesma === corrida.lesmaEscolhida;
          return (
            <View key={c.lesma} style={estilos.coluna}>
              <Lesma codigo={c.lesma} largura={tamanho[c.posicao]} />
              <Text style={[estilos.nome, sua && estilos.sua]} numberOfLines={1}>
                {c.nome}
              </Text>
              <Text style={estilos.pontos}>
                {decimal(c.pontuacao)} pts{sua ? ' · sua' : ''}
              </Text>
              <View style={[estilos.degrau, { height: altura[c.posicao], backgroundColor: coresDasLesmas[c.lesma] }]}>
                <Text style={estilos.lugar}>{c.posicao}º</Text>
              </View>
            </View>
          );
        })}
      </View>

      <View style={estilos.lista}>
        {resto.map((c) => {
          const sua = c.lesma === corrida.lesmaEscolhida;
          return (
            <View key={c.lesma} style={[estilos.linha, sua && estilos.linhaSua]}>
              <Text style={estilos.posicao}>{c.posicao}º</Text>
              <View style={[estilos.bolinha, { backgroundColor: coresDasLesmas[c.lesma] }]} />
              <Text style={[estilos.nomeLista, sua && estilos.sua]} numberOfLines={1}>
                {c.nome}
                {sua ? ' (sua)' : ''}
              </Text>
              <Text style={estilos.pontosLista}>{decimal(c.pontuacao)} pts</Text>
              {!compacto && <Text style={estilos.bonus}>+{decimal(c.bonus)} do clima</Text>}
            </View>
          );
        })}
      </View>
    </View>
  );
}

const estilos = StyleSheet.create({
  podio: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'flex-end',
    gap: 12,
    paddingTop: 8,
    borderBottomWidth: 2,
    borderBottomColor: cores.linha,
  },
  coluna: { flex: 1, maxWidth: 220, alignItems: 'center' },
  nome: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta, marginTop: 4, textAlign: 'center' },
  sua: { color: cores.musgo, fontFamily: fontes.extra },
  pontos: { fontFamily: fontes.texto, fontSize: 13, color: cores.tintaSuave, marginBottom: 8 },
  degrau: {
    alignSelf: 'stretch',
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  lugar: { fontFamily: fontes.extra, fontSize: 20, color: '#FFFFFF' },
  lista: { marginTop: 8 },
  linha: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 9,
    paddingHorizontal: 8,
    borderBottomWidth: 1,
    borderBottomColor: cores.linha,
  },
  linhaSua: { backgroundColor: cores.marcaSuave, borderRadius: 8 },
  posicao: { width: 28, fontFamily: fontes.extra, fontSize: 15, color: cores.tintaSuave },
  bolinha: { width: 10, height: 10, borderRadius: 5 },
  nomeLista: { flex: 1, fontFamily: fontes.media, fontSize: 15, color: cores.tinta },
  pontosLista: { fontFamily: fontes.forte, fontSize: 15, color: cores.tinta, minWidth: 64, textAlign: 'right' },
  bonus: { fontFamily: fontes.texto, fontSize: 13, color: cores.tintaSuave, minWidth: 104, textAlign: 'right' },
});
