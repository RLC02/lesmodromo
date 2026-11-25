import Svg, { Circle, G, Path } from 'react-native-svg';

// Ícone de cada especialidade (o tipo de clima que a lesma gosta)
export default function Icone({ especialidade, cor = '#17251C', tamanho = 16 }) {
  return (
    <Svg width={tamanho} height={tamanho} viewBox="0 0 24 24">
      <G stroke={cor} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" fill="none">
        {desenho(especialidade)}
      </G>
    </Svg>
  );
}

function desenho(especialidade) {
  switch (especialidade) {
    case 'Umidade':
      return <Path d="M12 3 C12 3 5 11 5 15 a7 7 0 0 0 14 0 C19 11 12 3 12 3 Z" />;
    case 'Frio':
      return (
        <>
          <Path d="M12 2 V22 M3.4 7 L20.6 17 M3.4 17 L20.6 7" />
          <Path d="M9 4 L12 6.5 L15 4 M9 20 L12 17.5 L15 20" />
        </>
      );
    case 'Vento':
      return (
        <>
          <Path d="M3 8 H14 a3 3 0 1 0 -3 -3" />
          <Path d="M3 12 H18 a3 3 0 1 1 -3 3" />
          <Path d="M3 16 H9" />
        </>
      );
    case 'Sorte':
      return (
        <>
          <Circle cx={12} cy={7.5} r={3.3} />
          <Circle cx={16.5} cy={12} r={3.3} />
          <Circle cx={7.5} cy={12} r={3.3} />
          <Circle cx={12} cy={16.5} r={3.3} />
          <Path d="M13.5 19 C14.5 21 16 22 18 22" />
        </>
      );
    case 'Calor':
      return (
        <>
          <Circle cx={12} cy={12} r={4} />
          <Path d="M12 2 V4 M12 20 V22 M2 12 H4 M20 12 H22 M4.9 4.9 L6.3 6.3 M17.7 17.7 L19.1 19.1 M4.9 19.1 L6.3 17.7 M17.7 6.3 L19.1 4.9" />
        </>
      );
    case 'Calmaria':
      return <Path d="M3 9 H21 M5 13 H19 M8 17 H16" />;
    case 'Chuva':
      return (
        <>
          <Path d="M7 15 a4 4 0 0 1 0.6 -8 a5.5 5.5 0 0 1 10.4 1.6 a3.5 3.5 0 0 1 -0.5 6.4 Z" />
          <Path d="M9 18 L8 21 M13 18 L12 21 M17 18 L16 21" />
        </>
      );
    case 'Ar seco':
      return (
        <>
          <Path d="M10 21 V6 a2 2 0 0 1 4 0 V21" />
          <Path d="M10 13 H8 a2 2 0 0 1 -2 -2 V9 M14 11 H16 a2 2 0 0 0 2 -2 V7" />
          <Path d="M5 21 H19" />
        </>
      );
    default:
      return <Circle cx={12} cy={12} r={6} />;
  }
}
