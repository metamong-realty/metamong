export function formatPrice(price: number): string {
  const eok = Math.floor(price / 10000);
  const remainder = price % 10000;

  if (eok === 0) return `${remainder}만`;
  if (remainder === 0) return `${eok}억`;

  const cheonman = Math.floor(remainder / 1000);
  if (cheonman > 0) return `${eok}억 ${cheonman}천`;

  return `${eok}억 ${remainder}만`;
}

export function formatSize(area: number, pyeong: number): string {
  return `${area}㎡ (${pyeong}평)`;
}
