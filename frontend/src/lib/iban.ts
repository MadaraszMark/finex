// IBAN-számlaszámok ellenőrzése a backend IbanUtils szabályai szerint (ISO 13616, mod-97 ellenőrzőszám),
// hogy a hibás számlaszám már gépelés közben kiderüljön. A végső ellenőrzés mindig a backendé.

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

  // Az ország és az ellenőrzőszám a végére kerül, a betűk számokká alakulnak (A = 10 … Z = 35),
  // és a kapott (nagyon hosszú) szám 97-tel osztva 1 maradékot kell adjon. Számjegyenként számolunk, így nem kell BigInt.
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
