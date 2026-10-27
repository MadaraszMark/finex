import { describe, expect, it } from 'vitest'
import { newPasswordField, passwordByteLength, registerSchema } from './schemas'

const validRegistration = {
  lastName: 'Kovács',
  firstName: 'Anna',
  email: 'anna@example.com',
  phone: '',
  password: 'Titkos123',
  passwordConfirm: 'Titkos123',
}

describe('új jelszó szabályai', () => {
  it('betűt és számot is megkövetel, legalább 8 karakter', () => {
    expect(newPasswordField.safeParse('Titkos123').success).toBe(true)
    expect(newPasswordField.safeParse('csakbetu').success).toBe(false)
    expect(newPasswordField.safeParse('12345678').success).toBe(false)
    expect(newPasswordField.safeParse('Rov1d').success).toBe(false)
  })

  // BCrypt-korlát 72 bájt; az "ő" 2 bájt, így 40 db (80 bájt) már túl hosszú
  it('legfeljebb 72 bájtos jelszót enged (az ékezetes betű 2 bájt)', () => {
    expect(passwordByteLength('ő')).toBe(2)
    expect(newPasswordField.safeParse('Aa1' + 'ő'.repeat(34)).success).toBe(true)
    expect(newPasswordField.safeParse('Aa1' + 'ő'.repeat(40)).success).toBe(false)
  })
})

describe('regisztráció', () => {
  it('elfogadja az érvényes adatokat', () => {
    expect(registerSchema.safeParse(validRegistration).success).toBe(true)
  })

  it('a két jelszónak egyeznie kell', () => {
    const result = registerSchema.safeParse({ ...validRegistration, passwordConfirm: 'Masik123' })
    expect(result.success).toBe(false)
  })

  it('a nevek legfeljebb 50 karakteresek (a kártyára is ráférnek)', () => {
    expect(registerSchema.safeParse({ ...validRegistration, lastName: 'A'.repeat(50) }).success).toBe(true)
    expect(registerSchema.safeParse({ ...validRegistration, lastName: 'A'.repeat(51) }).success).toBe(false)
  })
})
