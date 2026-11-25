import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { chamarApi } from '../api';
import { cores, fontes } from '../tema';

// Entrar ou criar conta. Quem guarda a sessão é o cookie HttpOnly que o servidor manda:
// aqui não salvamos nada no navegador, só avisamos o App de quem entrou.
export default function Acesso({ onEntrou }) {
  const [modo, setModo] = useState('entrar');
  const [nome, setNome] = useState('');
  const [senha, setSenha] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  const criando = modo === 'criar';
  const podeEnviar = nome.trim() && senha && !enviando;

  async function enviar() {
    if (!podeEnviar) return;
    setErro('');
    setEnviando(true);
    try {
      const usuario = await chamarApi(criando ? '/api/auth/cadastro' : '/api/auth/login', {
        metodo: 'POST',
        corpo: { nome, senha },
        tentativas: 2,
      });
      setSenha('');
      onEntrou(usuario);
    } catch (e) {
      setErro(e.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <View style={estilos.caixa}>
      <View style={estilos.abas} accessibilityRole="tablist">
        {[
          ['entrar', 'Entrar'],
          ['criar', 'Criar conta'],
        ].map(([valor, rotulo]) => (
          <Pressable
            key={valor}
            onPress={() => {
              setModo(valor);
              setErro('');
            }}
            style={[estilos.aba, modo === valor && estilos.abaAtiva]}
            accessibilityRole="tab"
            accessibilityState={{ selected: modo === valor }}
          >
            <Text style={[estilos.abaTexto, modo === valor && estilos.abaTextoAtivo]}>{rotulo}</Text>
          </Pressable>
        ))}
      </View>

      <Text style={estilos.explica}>
        {criando
          ? 'Escolha um nome (é ele que aparece no ranking) e uma senha com pelo menos 6 caracteres.'
          : 'Entre na sua conta para apostar. Seus pontos ficam guardados no ranking.'}
      </Text>

      <View style={estilos.campos}>
        <TextInput
          style={estilos.campo}
          placeholder="Nome"
          placeholderTextColor="#8A9A8E"
          value={nome}
          onChangeText={setNome}
          autoCapitalize="none"
          autoComplete="username"
          maxLength={20}
        />
        <TextInput
          style={estilos.campo}
          placeholder="Senha"
          placeholderTextColor="#8A9A8E"
          value={senha}
          onChangeText={setSenha}
          secureTextEntry
          autoComplete={criando ? 'new-password' : 'current-password'}
          onSubmitEditing={enviar}
        />
        <Pressable
          onPress={enviar}
          disabled={!podeEnviar}
          style={({ hovered }) => [estilos.botao, hovered && podeEnviar && estilos.botaoSobre, !podeEnviar && estilos.botaoDesligado]}
        >
          <Text style={[estilos.botaoTexto, !podeEnviar && estilos.botaoTextoDesligado]}>
            {enviando ? 'Aguarde...' : criando ? 'Criar conta e entrar' : 'Entrar'}
          </Text>
        </Pressable>
      </View>
      {!!erro && <Text style={estilos.erro}>{erro}</Text>}
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: { backgroundColor: cores.marcaSuave, borderRadius: 14, padding: 16, gap: 12 },
  abas: { flexDirection: 'row', alignSelf: 'flex-start', backgroundColor: '#FFFFFF', borderRadius: 999, padding: 3 },
  aba: { paddingVertical: 7, paddingHorizontal: 16, borderRadius: 999 },
  abaAtiva: { backgroundColor: cores.musgo },
  abaTexto: { fontFamily: fontes.forte, fontSize: 14, color: cores.tintaSuave },
  abaTextoAtivo: { color: '#FFFFFF' },
  explica: { fontFamily: fontes.texto, fontSize: 14, lineHeight: 20, color: cores.tintaSuave },
  campos: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  campo: {
    flexGrow: 1,
    flexBasis: 200,
    fontFamily: fontes.texto,
    fontSize: 16,
    color: cores.tinta,
    borderWidth: 1.5,
    borderColor: cores.linha,
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 14,
    backgroundColor: '#FFFFFF',
    outlineColor: cores.folha,
  },
  botao: {
    flexGrow: 1,
    flexBasis: 160,
    backgroundColor: cores.musgo,
    borderRadius: 12,
    paddingVertical: 13,
    paddingHorizontal: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoSobre: { backgroundColor: cores.musgoClaro },
  botaoDesligado: { backgroundColor: '#C9D6C8' },
  botaoTexto: { fontFamily: fontes.extra, fontSize: 15, color: '#FFFFFF' },
  botaoTextoDesligado: { color: '#F4F7F2' },
  erro: { fontFamily: fontes.media, fontSize: 14, color: cores.erro },
});
