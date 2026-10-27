// IBAN-ellenőrzés a backend IbanUtils szabályai szerint (mod-97); a végső ellenőrzés a backendé

// Szóközök nélküli, nagybetűs alak: "hu15 1177 3016 ..." → "HU1511773016..."
export function normalizeIban(value: string): string {
  return value.replace(/\s/g, '').toUpperCase()
}

export function isValidIban(value: string): boolean {
  const iban = normalizeIban(value)
  if (!/^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$/.test(iban)) {
    return false
  }
  if (iban.startsWith('HU') && iban.length !== 28) {
    return false
  }

  // Mod-97: ország és ellenőrzőszám a végére, betűk számokká; számjegyenként, BigInt nélkül
  const rearranged = iban.slice(4) + iban.slice(0, 4)
  let remainder = 0
  for (const character of rearranged) {
    const digits = /[A-Z]/.test(character) ? String(character.charCodeAt(0) - 55) : character
    for (const digit of digits) {
      remainder = (remainder * 10 + Number(digit)) % 97
    }
  }
  return remainder === 1
}
