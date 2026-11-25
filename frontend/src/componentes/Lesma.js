import Svg, { Circle, Defs, Ellipse, G, Line, LinearGradient, Path, RadialGradient, Rect, Stop } from 'react-native-svg';
import { clarear, coresDasLesmas, escurecer } from '../tema';

const CORPO = '#EBDDB6';
const CORPO_ESCURO = '#CDB98A';
const CONTORNO = '#8C7852';
const TINTA = '#1E2A22';

// Lesma desenhada em SVG, virada para a direita, cada uma com o seu acessório.
export default function Lesma({ codigo, largura = 120 }) {
  const cor = coresDasLesmas[codigo] ?? '#7A8B7E';
  const altura = (largura * 80) / 120;
  const olhosFechados = codigo === 'MESTRE_ZEN';

  return (
    <Svg width={largura} height={altura} viewBox="0 0 120 80">
      <Defs>
        <LinearGradient id="lesma-corpo" x1="0" y1="0" x2="0" y2="1">
          <Stop offset="0" stopColor="#F4E9CB" />
          <Stop offset="1" stopColor={CORPO_ESCURO} />
        </LinearGradient>
        <RadialGradient id={`lesma-concha-${codigo}`} cx="38%" cy="30%" r="75%">
          <Stop offset="0" stopColor={clarear(cor, 0.5)} />
          <Stop offset="0.55" stopColor={cor} />
          <Stop offset="1" stopColor={escurecer(cor, 0.28)} />
        </RadialGradient>
      </Defs>

      {/* sombra no chão */}
      <Ellipse cx={58} cy={76.5} rx={46} ry={2.8} fill="#000" opacity={0.12} />

      <AcessorioAtras codigo={codigo} />

      {/* corpo */}
      <Path
        d="M6 72 C6 66 14 63 24 63 L80 63 C85 63 87 60 88 55 L89 50 C89.5 40 95.5 33 103 33 C111 33 115.5 40 114.5 48 C113.8 56 109 61 107 66 C105 71 100 74 92 74 L12 74 C8 74 6 73 6 72 Z"
        fill="url(#lesma-corpo)"
        stroke={CONTORNO}
        strokeWidth={1.6}
      />
      <Path d="M16 71 L88 71" stroke="#FFFFFF" strokeOpacity={0.45} strokeWidth={1.4} strokeLinecap="round" />
      <Ellipse cx={94} cy={62} rx={1.8} ry={1.1} fill={CORPO_ESCURO} />
      <Ellipse cx={99} cy={67} rx={1.4} ry={0.9} fill={CORPO_ESCURO} />

      {/* concha */}
      <Circle cx={50} cy={40} r={25} fill={`url(#lesma-concha-${codigo})`} stroke={escurecer(cor, 0.38)} strokeWidth={1.6} />
      <Path
        d="M51 41 m-2.5 0 a2.5 2.5 0 1 1 5 0 a6 6 0 1 1 -12 0 a9.5 9.5 0 1 1 19 0 a13 13 0 1 1 -26 0 a16.5 16.5 0 1 1 33 0"
        stroke="#FFFFFF"
        strokeOpacity={0.6}
        strokeWidth={2.2}
        fill="none"
        strokeLinecap="round"
      />
      <Ellipse cx={40} cy={25} rx={8} ry={3.4} fill="#FFFFFF" opacity={0.38} rotation={-28} origin="40, 25" />
      <Circle cx={33} cy={33} r={1.5} fill="#FFFFFF" opacity={0.5} />

      <AcessorioNaConcha codigo={codigo} />

      {/* antenas */}
      <Line x1={99} y1={36} x2={95} y2={14} stroke={CONTORNO} strokeWidth={3.4} strokeLinecap="round" />
      <Line x1={107} y1={36} x2={111} y2={16} stroke={CONTORNO} strokeWidth={3.4} strokeLinecap="round" />
      <Line x1={99} y1={36} x2={95} y2={14} stroke={CORPO} strokeWidth={1.4} strokeLinecap="round" />
      <Line x1={107} y1={36} x2={111} y2={16} stroke={CORPO} strokeWidth={1.4} strokeLinecap="round" />

      <AcessorioNaCabeca codigo={codigo} cor={cor} />

      {/* olhos */}
      {olhosFechados ? (
        <>
          <Circle cx={95} cy={12} r={5.2} fill={CORPO} stroke={CONTORNO} strokeWidth={1.2} />
          <Circle cx={111} cy={14} r={5.2} fill={CORPO} stroke={CONTORNO} strokeWidth={1.2} />
          <Path d="M91.2 12.6 Q95 15.6 98.8 12.6" stroke={TINTA} strokeWidth={1.6} fill="none" strokeLinecap="round" />
          <Path d="M107.2 14.6 Q111 17.6 114.8 14.6" stroke={TINTA} strokeWidth={1.6} fill="none" strokeLinecap="round" />
        </>
      ) : (
        <>
          <Circle cx={95} cy={12} r={5.2} fill="#FFFFFF" stroke={TINTA} strokeWidth={1.1} />
          <Circle cx={111} cy={14} r={5.2} fill="#FFFFFF" stroke={TINTA} strokeWidth={1.1} />
          <Circle cx={96.6} cy={12.6} r={2.6} fill={TINTA} />
          <Circle cx={112.6} cy={14.6} r={2.6} fill={TINTA} />
          <Circle cx={97.4} cy={11.5} r={0.9} fill="#FFFFFF" />
          <Circle cx={113.4} cy={13.5} r={0.9} fill="#FFFFFF" />
        </>
      )}

      {/* rosto */}
      <Ellipse cx={109.5} cy={48.5} rx={3.2} ry={2} fill="#F29C9C" opacity={0.55} />
      <Path d="M101.5 54 Q105.5 57.5 109.5 53.5" stroke="#5A4A30" strokeWidth={1.5} fill="none" strokeLinecap="round" />

      <AcessorioNoRosto codigo={codigo} />
    </Svg>
  );
}

// Coisas desenhadas atrás do corpo (rastro de velocidade, vento, gotas)
function AcessorioAtras({ codigo }) {
  switch (codigo) {
    case 'DONA_BABA':
      return (
        <G stroke="#9BB7C9" strokeWidth={1.8} strokeLinecap="round">
          <Path d="M2 30 H16" />
          <Path d="M6 38 H20" />
          <Path d="M0 46 H12" />
        </G>
      );
    case 'TURBO_MOLENGA':
      return (
        <G stroke="#BBA7F5" strokeWidth={2} strokeLinecap="round">
          <Path d="M1 54 H12" />
          <Path d="M3 61 H16" />
          <Path d="M0 67 H8" />
        </G>
      );
    case 'GAROA':
      return (
        <G fill="#8FC3EA">
          <Path d="M10 24 c0 0 -2.5 3.4 -2.5 5 a2.5 2.5 0 0 0 5 0 c0 -1.6 -2.5 -5 -2.5 -5 Z" />
          <Path d="M17 40 c0 0 -2 2.8 -2 4.2 a2 2 0 0 0 4 0 c0 -1.4 -2 -4.2 -2 -4.2 Z" />
          <Path d="M90 6 c0 0 -2 2.8 -2 4.2 a2 2 0 0 0 4 0 c0 -1.4 -2 -4.2 -2 -4.2 Z" />
        </G>
      );
    default:
      return null;
  }
}

// Coisas que ficam em cima da concha
function AcessorioNaConcha({ codigo }) {
  switch (codigo) {
    case 'GOSMINHA':
      // gotinha de orvalho
      return <Path d="M76 14 C76 14 72.5 19 72.5 21.5 a3.5 3.5 0 0 0 7 0 C79.5 19 76 14 76 14 Z" fill="#8FD3F4" stroke="#5BAFD6" strokeWidth={1} />;
    case 'TURBO_MOLENGA':
      // raio de velocidade
      return <Path d="M53 21 L44 40 L51 40 L46 57 L61 34 L53 34 L58 21 Z" fill="#F5C542" stroke="#C99A1E" strokeWidth={1.2} strokeLinejoin="round" />;
    case 'MESTRE_ZEN':
      // brotinho de bambu
      return (
        <G>
          <Path d="M50 16 L50 9" stroke="#4E9A55" strokeWidth={1.6} strokeLinecap="round" />
          <Path d="M50 10 C45 5 47 1 53 1 C53 6 52 9 50 10 Z" fill="#7BC47F" stroke="#4E9A55" strokeWidth={1} />
        </G>
      );
    case 'GAROA':
      // guarda-chuva
      return (
        <G>
          <Line x1={52} y1={14} x2={52} y2={22} stroke="#6B4F3A" strokeWidth={2} strokeLinecap="round" />
          <Path
            d="M20 16 C24 2 80 2 84 16 C80 13 74 13 71 16 C67 13 61 13 58 16 C54 13 48 13 45 16 C41 13 35 13 32 16 C28 13 23 13 20 16 Z"
            fill="#3B82C4"
            stroke="#2A64A0"
            strokeWidth={1.4}
            strokeLinejoin="round"
          />
          <Path d="M52 3.5 L45 16 M52 3.5 L58 16 M52 3.5 L32 16 M52 3.5 L71 16" stroke="#2A64A0" strokeWidth={0.8} strokeOpacity={0.6} />
          <Circle cx={52} cy={2.6} r={1.6} fill="#2A64A0" />
        </G>
      );
    case 'BARAO':
      // cartola inclinada
      return (
        <G rotation={-8} origin="50, 16">
          <Path d="M41 16 L42 1.5 L58 1.5 L59 16 Z" fill="#2E2E36" stroke="#1A1A1F" strokeWidth={1} />
          <Rect x={41.6} y={11} width={16.8} height={3.2} fill="#C9A227" />
          <Ellipse cx={50} cy={1.6} rx={8} ry={1.4} fill="#3A3A44" />
          <Ellipse cx={50} cy={16} rx={13} ry={2.8} fill="#24242A" />
        </G>
      );
    default:
      return null;
  }
}

// Chapéus, gorros e faixas na cabeça (desenhados depois das antenas, que "saem" de cima deles)
function AcessorioNaCabeca({ codigo, cor }) {
  switch (codigo) {
    case 'GOSMINHA':
      // laço no topo da cabeça
      return (
        <G>
          <Path d="M103 34 C98 29 95 31 96 35 C95 39 98 40 103 34 Z" fill="#F06BA8" stroke="#C2457F" strokeWidth={1.1} />
          <Path d="M103 34 C108 29 111 31 110 35 C111 39 108 40 103 34 Z" fill="#F06BA8" stroke="#C2457F" strokeWidth={1.1} />
          <Circle cx={103} cy={34} r={2.2} fill="#E04D93" />
        </G>
      );
    case 'LESMALDA':
      // cachecol no pescoço e gorro com pompom
      return (
        <G>
          <Path d="M86.5 55 C93 59.5 103 59.5 111 55 L111.8 60.6 C103 65.4 93 65.4 86 60.8 Z" fill="#E25555" stroke="#B53A3A" strokeWidth={1} />
          <Path d="M91 57.6 L91.8 62.8 M97 58.8 L97.4 64 M103 58.6 L103.4 63.8" stroke="#FFFFFF" strokeWidth={1.4} strokeOpacity={0.85} />
          <Path d="M89 60.4 L84.4 72 L90.8 72.4 L93.6 61.6 Z" fill="#E25555" stroke="#B53A3A" strokeWidth={1} />
          <Path d="M85.2 72 L84.6 74.4 M87.6 72.2 L87.2 74.6 M90 72.3 L89.8 74.7" stroke="#B53A3A" strokeWidth={1} />
          <Path d="M90.5 40 C90.5 26 115.5 26 115.5 40 Z" fill="#E25555" stroke="#B53A3A" strokeWidth={1.1} />
          <Path d="M93.5 33.6 H112.5" stroke="#FFFFFF" strokeWidth={1.6} strokeOpacity={0.8} />
          <Rect x={89.5} y={38} width={27} height={5.5} rx={2.75} fill="#F4F1EA" stroke="#CFC8B8" strokeWidth={1} />
          <Circle cx={103} cy={25.6} r={4.2} fill="#F4F1EA" stroke="#CFC8B8" strokeWidth={1} />
        </G>
      );
    case 'DONA_BABA':
      // lenço amarrado no pescoço, voando com o vento
      return (
        <G>
          <Path d="M88 57.4 C80 52 72 55 63 48.6 C67 57.4 76 60.6 88 62 Z" fill="#F2C14E" stroke="#C99A26" strokeWidth={1.1} />
          <Path d="M86.5 55 C93 59.5 103 59.5 111 55 L111.8 60.6 C103 65.4 93 65.4 86 60.8 Z" fill="#F2C14E" stroke="#C99A26" strokeWidth={1} />
          <Circle cx={89} cy={59.6} r={2.4} fill="#E3AE33" stroke="#C99A26" strokeWidth={0.8} />
        </G>
      );
    case 'TURBO_MOLENGA':
      // capacete de corrida
      return (
        <G>
          <Path d="M89.5 44 C88.5 31 96 26 103 26 C110 26 116.5 31 115.5 44 Z" fill="#FFFFFF" stroke="#8C8CA0" strokeWidth={1.2} />
          <Path d="M102.6 26.2 C103.8 32 103.8 38 102.8 44" stroke={cor} strokeWidth={3.6} />
          <Path d="M108.4 35.6 L116 36.2 L115.6 42.4 L108.4 42.4 Z" fill="#2B2A36" opacity={0.85} />
          <Path d="M89.6 44 H115.4" stroke="#8C8CA0" strokeWidth={1.4} strokeLinecap="round" />
        </G>
      );
    case 'BRASINHA':
      // boné com aba pra frente
      return (
        <G>
          <Path d="M90.5 39.5 C90.5 28.5 113.5 28 114 38.5 Z" fill="#E5484D" stroke="#B83236" strokeWidth={1.1} />
          <Path d="M110 37 C115 35.6 119 37.5 119.6 40.5 C116 40.8 111.5 40.6 108.6 39.8 Z" fill="#C73A3F" stroke="#9E2B30" strokeWidth={1} />
          <Circle cx={102} cy={29.4} r={1.5} fill="#B83236" />
        </G>
      );
    case 'MESTRE_ZEN':
      // faixa na testa com as pontas pra trás
      return (
        <G>
          <Path d="M91 38.5 C85 36 81 38.6 76.5 35.6 L79 41 C83.6 43 87.4 42 91 41.6 Z" fill="#0F8A8A" />
          <Path d="M90 37 C97 33.5 109 33.5 115 37 L115.2 41.4 C109 38 97 38 90 41.4 Z" fill="#13A3A3" stroke="#0F8A8A" strokeWidth={1} />
        </G>
      );
    default:
      return null;
  }
}

// Óculos, monóculo e bigode (desenhados por cima dos olhos e do rosto)
function AcessorioNoRosto({ codigo }) {
  switch (codigo) {
    case 'DONA_BABA':
      // óculos de aviadora
      return (
        <G stroke="#7A5230" strokeWidth={2.2} fill="#AAD7F0" fillOpacity={0.35}>
          <Circle cx={95} cy={12} r={6.6} />
          <Circle cx={111} cy={14} r={6.6} />
          <Path d="M101.4 12.6 Q103 10.8 104.6 13.4" fill="none" />
          <Path d="M88.4 11.6 L84.6 12.8" fill="none" />
        </G>
      );
    case 'BRASINHA':
      // óculos escuros
      return (
        <G>
          <Rect x={89.4} y={8.6} width={11.4} height={7.2} rx={3} fill="#1F2328" />
          <Rect x={105.4} y={10.6} width={11.4} height={7.2} rx={3} fill="#1F2328" />
          <Path d="M100.8 11.6 Q103 10 105.4 13" stroke="#1F2328" strokeWidth={2} fill="none" />
          <Path d="M91.6 10.6 L94.4 10.6 M107.6 12.6 L110.4 12.6" stroke="#FFFFFF" strokeOpacity={0.7} strokeWidth={1.2} strokeLinecap="round" />
        </G>
      );
    case 'BARAO':
      // monóculo e bigode
      return (
        <G>
          <Circle cx={111} cy={14} r={6.4} fill="#DCEBF5" fillOpacity={0.3} stroke="#C9A227" strokeWidth={1.8} />
          <Path d="M115.6 18.6 C117.4 28 114.4 36 110.6 40" stroke="#C9A227" strokeWidth={1} fill="none" />
          <Path
            d="M98.5 51.2 C101.5 48.2 104.5 49.2 106 51.2 C107.5 49.2 110.5 48.2 113.5 51.2 C110.5 53.8 107.5 53.4 106 52.1 C104.5 53.4 101.5 53.8 98.5 51.2 Z"
            fill="#3B2A1E"
          />
        </G>
      );
    default:
      return null;
  }
}
