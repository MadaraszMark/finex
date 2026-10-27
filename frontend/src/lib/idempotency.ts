// Idempotency-Key műveletenként: az újraküldés ugyanazzal megy, így csak egyszer könyvelődik
export function newIdempotencyKey(): string {
  if (typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  // A randomUUID csak https-en vagy localhoston érhető el; máshol 128 véletlen bit hexában
  const bytes = crypto.getRandomValues(new Uint8Array(16))
  return Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('')
}
