export function parseQuantityMl(
  raw: string | number | null | undefined,
): number | null | 'invalid' {
  if (raw == null || raw === '') {
    return null;
  }
  const quantityMl = typeof raw === 'number' ? raw : Number(String(raw).trim());
  if (!Number.isInteger(quantityMl) || quantityMl < 1) {
    return 'invalid';
  }
  return quantityMl;
}
