import Svg, { Circle, Ellipse, Line, Path } from 'react-native-svg';
import { cores } from '../tema';

// Lesma desenhada em SVG, virada para a direita. A concha leva a cor da "seda" dela.
export default function Lesma({ cor, largura = 64 }) {
  const altura = (largura * 40) / 64;

  return (
    <Svg width={largura} height={altura} viewBox="0 0 64 40">
      {/* corpo */}
      <Path
        d="M3 35 Q3 29 12 29 L45 29 Q52 29 54 22 Q56 15 60 16 Q64 18 62 27 Q59 37 46 37 L9 37 Q3 37 3 35 Z"
        fill={cores.corpoLesma}
        stroke={cores.contornoLesma}
        strokeWidth={1.4}
      />
      {/* antenas */}
      <Line x1={55} y1={18} x2={55.5} y2={6} stroke={cores.contornoLesma} strokeWidth={1.6} strokeLinecap="round" />
      <Line x1={59} y1={18} x2={62} y2={8} stroke={cores.contornoLesma} strokeWidth={1.6} strokeLinecap="round" />
      <Circle cx={55.5} cy={5.5} r={2.4} fill={cores.tinta} />
      <Circle cx={62} cy={7.5} r={2.4} fill={cores.tinta} />
      <Circle cx={56.2} cy={4.8} r={0.8} fill="#FFFFFF" />
      <Circle cx={62.7} cy={6.8} r={0.8} fill="#FFFFFF" />
      {/* sorriso */}
      <Path d="M57 25 Q59 27 61 25" stroke={cores.contornoLesma} strokeWidth={1.2} fill="none" strokeLinecap="round" />
      {/* concha */}
      <Circle cx={28} cy={19} r={15} fill={cor} stroke={cores.tinta} strokeOpacity={0.35} strokeWidth={1.4} />
      <Path
        d="M28 19 m-2.5 0 a2.5 2.5 0 1 1 5 0 a5.5 5.5 0 1 1 -11 0 a8.5 8.5 0 1 1 17 0 a11.5 11.5 0 1 1 -23 0"
        stroke="#FFFFFF"
        strokeOpacity={0.75}
        strokeWidth={1.8}
        fill="none"
        strokeLinecap="round"
      />
      {/* brilho da concha */}
      <Ellipse cx={22} cy={10} rx={4} ry={2} fill="#FFFFFF" opacity={0.35} />
    </Svg>
  );
}
