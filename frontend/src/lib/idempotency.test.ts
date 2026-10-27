import { describe, expect, it } from 'vitest'
import { newIdempotencyKey } from './idempotency'

describe('newIdempotencyKey', () => {
  it('minden hívásra új, a backend által elfogadott formátumú kulcsot ad', () => {
    const keys = new Set(Array.from({ length: 50 }, () => newIdempotencyKey()))

    expect(keys.size).toBe(50)
    for (const key of keys) {
      expect(key).toMatch(/^[A-Za-z0-9_-]{8,100}$/)
    }
  })
})
