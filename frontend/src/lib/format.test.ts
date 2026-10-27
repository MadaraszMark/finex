import { describe, expect, it } from 'vitest'
import { formatMoney, formatSignedMoney, formatTabTitle, parseAmount, SHY, signedAmount } from './format'

// Ezen múlik, hogy pontosan a beírt összeg menjen el az utalásban
describe('parseAmount', () => {
  it('a szóközt ezres elválasztónak, a vesszőt tizedesjelnek veszi', () => {
    expect(parseAmount('25 000')).toBe(25000)
    expect(parseAmount('12 500,50')).toBe(12500.5)
    expect(parseAmount('120.25')).toBe(120.25)
    expect(parseAmount('7')).toBe(7)
  })

  it('legfeljebb két tizedesjegyet enged', () => {
    expect(parseAmount('10,555')).toBeUndefined()
  })

  it('elutasítja a negatív, az üres és a nem szám bemenetet', () => {
    expect(parseAmount('-5')).toBeUndefined()
    expect(parseAmount('')).toBeUndefined()
    expect(parseAmount('1e6')).toBeUndefined()
    expect(parseAmount('abc')).toBeUndefined()
    expect(parseAmount('1,2,3')).toBeUndefined()
  })
})

describe('pénzösszegek megjelenítése', () => {
  it('a bejövő tétel pozitív, a kimenő negatív előjelet kap', () => {
    expect(signedAmount('TRANSFER_IN', 100)).toBe(100)
    expect(signedAmount('OUTCOME', 100)).toBe(-100)
    expect(formatSignedMoney(1500, 'HUF').startsWith('+')).toBe(true)
  })

  it('forintnál "Ft", devizánál a devizanem jelenik meg', () => {
    expect(formatMoney(1500, 'HUF')).toContain('Ft')
    expect(formatMoney(12.5, 'EUR')).toMatch(/12,50/)
  })
})

describe('böngészőfül címe', () => {
  it('olvasatlan értesítésnél elöl a darabszám, 99 fölött rövidítve', () => {
    expect(formatTabTitle('Főoldal', 0)).toBe('Főoldal · FineX')
    expect(formatTabTitle('Főoldal', 3)).toBe('(3) Főoldal · FineX')
    expect(formatTabTitle('Főoldal', 150)).toBe('(99+) Főoldal · FineX')
    expect(formatTabTitle(undefined, 0)).toBe('FineX')
  })

  it('a feltételes elválasztójel nem kerül a címbe', () => {
    expect(formatTabTitle(`Megtaka${SHY}rítások`, 0)).toBe('Megtakarítások · FineX')
  })
})
