import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { cores, fontes } from '../tema';

// Mostra, passo a passo, a aposta atravessando a mensageria. Cada passo acende quando
// o backend confirma que aquele consumidor do RabbitMQ terminou a parte dele.
export default function Bastidores({ corrida, falhaDaMensageria }) {
  const passos = montarPassos(corrida, falhaDaMensageria);

  return (
    <View style={estilos.lista}>
      {passos.map((passo, i) => (
        <View key={passo.titulo} style={estilos.passo}>
          <View style={estilos.trilho}>
            <Marcador estado={passo.estado} numero={i + 1} />
            {i < passos.length - 1 && <View style={[estilos.fio, passo.estado === 'feito' && estilos.fioFeito]} />}
          </View>
          <View style={estilos.textos}>
            <Text style={[estilos.titulo, passo.estado === 'pendente' && estilos.tituloPendente]}>{passo.titulo}</Text>
            <Text style={estilos.detalhe}>
              {passo.detalhe.map((parte, j) =>
                parte.codigo ? (
                  <Text key={j} style={estilos.codigo}>
                    {parte.codigo}
                  </Text>
                ) : (
                  <Text key={j}>{parte}</Text>
                )
              )}
            </Text>
          </View>
        </View>
      ))}
      {falhaDaMensageria && (
        <Text style={estilos.aviso}>
          O RabbitMQ não respondeu nem com retry, então a API rodou o juiz e o ranking direto (fallback).
        </Text>
      )}
    </View>
  );
}

function Marcador({ estado, numero }) {
  if (estado === 'andando') {
    return (
      <View style={[estilos.marcador, estilos.marcadorAndando]}>
        <ActivityIndicator size="small" color={cores.musgo} />
      </View>
    );
  }
  return (
    <View style={[estilos.marcador, estado === 'feito' && estilos.marcadorFeito]}>
      <Text style={[estilos.marcadorTexto, estado === 'feito' && estilos.marcadorTextoFeito]}>
        {estado === 'feito' ? '✓' : numero}
      </Text>
    </View>
  );
}

function ordinal(n) {
  return `${n}ª`;
}

function montarPassos(corrida, falhaDaMensageria) {
  const status = corrida?.status;
  const correndo = status === 'CORRENDO' || status === 'FINALIZADA';
  const finalizada = status === 'FINALIZADA';

  const estado = (feito, andando) => (feito ? 'feito' : andando ? 'andando' : 'pendente');

  let detalheClima = ['O juiz busca o clima na Open-Meteo e publica ', { codigo: 'corrida.finalizada' }, '.'];
  if (finalizada && corrida.climaFallback) {
    detalheClima = ['A API de clima não respondeu nem com retry: valeu o clima padrão (fallback).'];
  } else if (finalizada && corrida.tentativasClima > 1) {
    detalheClima = [
      `A API de clima falhou ${corrida.tentativasClima - 1}x e o retry buscou de novo. Respondeu na ${ordinal(corrida.tentativasClima)} tentativa.`,
    ];
  } else if (finalizada) {
    detalheClima = ['A API de clima respondeu de primeira e o juiz publicou ', { codigo: 'corrida.finalizada' }, '.'];
  }

  return [
    {
      titulo: 'Aposta registrada',
      estado: estado(Boolean(corrida), false),
      detalhe: ['A API salvou a pule no MongoDB e publicou ', { codigo: 'corrida.largada' }, '.'],
    },
    {
      titulo: 'Juiz pegou a mensagem',
      estado: estado(correndo, Boolean(corrida) && !correndo),
      detalhe: ['O consumidor da fila ', { codigo: 'lesmodromo.juiz' }, ' começou o páreo.'],
    },
    {
      titulo: 'Clima consultado, páreo julgado',
      estado: estado(finalizada, status === 'CORRENDO'),
      detalhe: detalheClima,
    },
    {
      titulo: 'Ranking atualizado',
      estado: estado(Boolean(corrida?.rankingAtualizado), finalizada),
      detalhe: ['A fila ', { codigo: 'lesmodromo.ranking' }, ' somou seus pontos numa transação.'],
    },
    {
      titulo: 'Narrador anunciou',
      estado: estado(Boolean(corrida?.narrada), finalizada && !falhaDaMensageria),
      detalhe: [
        'A fila ',
        { codigo: 'lesmodromo.narrador' },
        ' recebeu o mesmo evento e mandou a frase para a Rádio Lesma.',
      ],
    },
  ];
}

const estilos = StyleSheet.create({
  lista: { gap: 0 },
  passo: { flexDirection: 'row', gap: 14 },
  trilho: { alignItems: 'center', width: 30 },
  marcador: {
    width: 30,
    height: 30,
    borderRadius: 15,
    borderWidth: 2,
    borderColor: cores.linha,
    backgroundColor: cores.papel,
    alignItems: 'center',
    justifyContent: 'center',
  },
  marcadorAndando: { borderColor: cores.folha },
  marcadorFeito: { backgroundColor: cores.folha, borderColor: cores.folha },
  marcadorTexto: { fontFamily: fontes.forte, fontSize: 13, color: cores.tintaSuave },
  marcadorTextoFeito: { color: '#FFFFFF' },
  fio: { width: 2, flex: 1, minHeight: 14, backgroundColor: cores.linha, marginVertical: 2 },
  fioFeito: { backgroundColor: cores.folha },
  textos: { flex: 1, paddingBottom: 16, paddingTop: 4 },
  titulo: { fontFamily: fontes.forte, fontSize: 16, color: cores.tinta },
  tituloPendente: { color: cores.tintaSuave },
  detalhe: { fontFamily: fontes.texto, fontSize: 14, lineHeight: 20, color: cores.tintaSuave, marginTop: 2 },
  codigo: {
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
    fontSize: 13,
    color: cores.musgo,
    backgroundColor: '#DCE8D5',
  },
  aviso: {
    fontFamily: fontes.media,
    fontSize: 14,
    lineHeight: 20,
    color: cores.erro,
    marginTop: 4,
  },
});
