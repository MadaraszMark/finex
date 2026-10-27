import { describe, expect, it } from 'vitest'
import { isValidIban, normalizeIban } from './iban'

// Ugyanazt kell mondania, mint a backend IbanUtils-ának (a demóadatok számlaszámaival)
describe('isValidIban', () => {
  it('elfogadja az érvényes magyar IBAN-t, szóközökkel és kisbetűvel is', () => {
    expect(isValidIban('HU15117730161111101800000001')).toBe(true)
    expect(isValidIban('hu66 1091 8001 0000 0489 0024 0017')).toBe(true)
  })

  it('elutasítja a rossz ellenőrzőszámot (egy elgépelt számjegyet)', () => {
    expect(isValidIban('HU15117730161111101800000002')).toBe(false)
    expect(isValidIban('HU16117730161111101800000001')).toBe(false)
  })

  it('elutasítja a rossz hosszúságú magyar számlaszámot és a hibás formátumot', () => {
    expect(isValidIban('HU1511773016111110180000000')).toBe(false)
    expect(isValidIban('HU00 1234')).toBe(false)
    expect(isValidIban('')).toBe(false)
    expect(isValidIban('123456789012345678')).toBe(false)
  })

  it('külföldi IBAN-t is ellenőriz (német példa)', () => {
    expect(isValidIban('DE89 3704 0044 0532 0130 00')).toBe(true)
    expect(isValidIban('DE89 3704 0044 0532 0130 01')).toBe(false)
  })
})

describe('normalizeIban', () => {
  it('eltávolítja a szóközöket és nagybetűssé alakít', () => {
    expect(normalizeIban(' hu15 1177 3016 1111 1018 0000 0001 ')).toBe('HU15117730161111101800000001')
  })
})
