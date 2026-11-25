// Paleta de jardim depois da chuva: musgo, folha, terra molhada e o brilho da gosma.
export const cores = {
  fundo: '#F2F5EF',
  papel: '#FFFFFF',
  tinta: '#17251C',
  tintaSuave: '#5A6A5F',
  linha: '#DCE4D6',
  musgo: '#1E4A34',
  musgoClaro: '#2C6148',
  marcaSuave: '#E6F0E8',
  folha: '#3F9A5B',
  grama: '#4C8A46',
  terra: '#6A4E3A',
  terraEscura: '#563F2F',
  gosma: '#CFF3A6',
  gema: '#F5C542',
  gemaEscura: '#D9A92C',
  erro: '#B4432F',
};

// Cor da concha de cada lesma (a "seda" dela, como as cores dos jóqueis no turfe)
export const coresDasLesmas = {
  GOSMINHA: '#2F9E68',
  LESMALDA: '#3A78D0',
  DONA_BABA: '#E8833A',
  TURBO_MOLENGA: '#8B5CF6',
  BRASINHA: '#E5484D',
  MESTRE_ZEN: '#13A3A3',
  GAROA: '#E5B21B',
  BARAO: '#A23B72',
};

// Dica rápida de cada cidade, pra ajudar a escolher a lesma
export const dicasDasCidades = {
  SAO_PAULO: 'Terra da garoa, com tempo que muda rápido.',
  MANAUS: 'Quente e muito úmida o ano todo.',
  PORTO_ALEGRE: 'Costuma ser a mais fria das quatro.',
  FORTALEZA: 'Calor com vento constante do mar.',
};

export const fontes = {
  logo: 'BagelFatOne_400Regular',
  texto: 'BricolageGrotesque_400Regular',
  media: 'BricolageGrotesque_500Medium',
  forte: 'BricolageGrotesque_700Bold',
  extra: 'BricolageGrotesque_800ExtraBold',
};

// Mistura uma cor com branco (clarear) ou preto (escurecer). fator de 0 a 1.
function misturar(hex, alvo, fator) {
  const n = parseInt(hex.slice(1), 16);
  const canais = [(n >> 16) & 255, (n >> 8) & 255, n & 255];
  const [r, g, b] = canais.map((c) => Math.round(c + (alvo - c) * fator));
  return `#${((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1)}`;
}

export const clarear = (hex, fator) => misturar(hex, 255, fator);
export const escurecer = (hex, fator) => misturar(hex, 0, fator);
