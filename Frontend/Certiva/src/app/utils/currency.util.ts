/** Moneda por defecto del sistema Certiva (Colombia). */
export const MONEDA_CODIGO = 'COP' as const;
export const MONEDA_ETIQUETA = 'COP';

export function formatearPrecioCop(
  precio: number | string | null | undefined,
  gratuito?: boolean | null
): string {
  if (gratuito || precio == null || precio === '' || Number(precio) === 0) {
    return 'Gratuito';
  }
  const valor = Number(precio);
  if (!Number.isFinite(valor) || valor < 0) {
    return 'Gratuito';
  }
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: MONEDA_CODIGO,
    maximumFractionDigits: 0,
  }).format(valor);
}

export function etiquetaPrecioFormulario(
  precio: number | string | null | undefined
): string {
  const valor = Number(precio);
  if (!Number.isFinite(valor) || valor <= 0) {
    return '0 = evento gratuito';
  }
  return `Valor: ${formatearPrecioCop(valor)}`;
}
