// A URL da API NÃO fica escrita no código.
// Ela vem da variável EXPO_PUBLIC_API_URL:
//  - no seu PC: arquivo .env (copie do .env.example)
//  - no deploy: a pipeline cria o .env a partir do secret API_URL do GitHub
export const API_URL = process.env.EXPO_PUBLIC_API_URL;

export const esperar = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// RETRY no frontend: o Render gratuito "dorme" depois de 15 min sem uso
// e leva perto de 1 minuto para acordar. Sem retry, a primeira chamada falha
// e o jogador vê erro. Com retry, o app espera o servidor acordar sozinho.
export async function chamarApi(caminho, { metodo = 'GET', corpo, tentativas = 4, aoTentar } = {}) {
  if (!API_URL) throw new Error('EXPO_PUBLIC_API_URL não configurada');

  for (let tentativa = 1; tentativa <= tentativas; tentativa++) {
    aoTentar?.(tentativa, tentativas);

    try {
      const resposta = await fetch(`${API_URL}${caminho}`, {
        method: metodo,
        // Só manda Content-Type quando tem corpo: GET simples não precisa de preflight de CORS
        headers: corpo ? { 'Content-Type': 'application/json' } : undefined,
        body: corpo ? JSON.stringify(corpo) : undefined,
        signal: AbortSignal.timeout(15000),
      });

      // 5xx = problema no servidor, vale tentar de novo
      if (resposta.status >= 500) throw new Error(`servidor respondeu ${resposta.status}`);

      const dados = await resposta.json();

      // 4xx = erro de quem chamou (ex: nome vazio). Repetir não adianta.
      if (!resposta.ok) {
        const erro = new Error(dados.erro || 'Pedido inválido');
        erro.semRetry = true;
        throw erro;
      }

      return dados;
    } catch (erro) {
      if (erro.semRetry || tentativa === tentativas) throw erro;

      const espera = Math.min(2000 * 2 ** (tentativa - 1), 8000); // 2 s, 4 s, 8 s, 8 s...
      console.warn(`[retry] ${caminho}: tentativa ${tentativa}/${tentativas} falhou (${erro.message}). Nova tentativa em ${espera} ms`);
      await esperar(espera);
    }
  }
}

// Id gerado no navegador para cada aposta. Se o retry mandar a mesma aposta duas vezes,
// o backend reconhece o id e não cria uma corrida duplicada.
export const novoIdDaAposta = () => `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
