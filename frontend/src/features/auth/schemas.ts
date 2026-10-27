import { z } from 'zod'

// Az űrlapok szabályai a backend validációját követik (LoginRequest, CreateUserRequest)

export const loginSchema = z.object({
  email: z.email('Érvényes e-mail címet adj meg.'),
  password: z.string().min(1, 'Add meg a jelszavad.'),
})

export type LoginForm = z.infer<typeof loginSchema>

// A jelszó követelményei (a backend mintája szerint); a regisztráció élőben mutatja őket
export const passwordRules = [
  { id: 'length', label: 'Legalább 8 karakter', test: (value: string) => value.length >= 8 },
  { id: 'letter', label: 'Tartalmaz betűt', test: (value: string) => /[A-Za-z]/.test(value) },
  { id: 'digit', label: 'Tartalmaz számot', test: (value: string) => /\d/.test(value) },
] as const

// A regisztráció és a profil oldal közös mezőszabályai
export const personalDataFields = {
  lastName: z.string().trim().min(1, 'Add meg a vezetékneved.').max(50, 'Legfeljebb 50 karakter lehet.'),
  firstName: z.string().trim().min(1, 'Add meg a keresztneved.').max(50, 'Legfeljebb 50 karakter lehet.'),
  email: z.email('Érvényes e-mail címet adj meg.').max(255, 'Legfeljebb 255 karakter lehet.'),
  phone: z
    .string()
    .trim()
    .max(30, 'Legfeljebb 30 karakter lehet.')
    .regex(/^[+\d\s()-]*$/, 'Csak számjegyet, szóközt és + ( ) - jelet tartalmazhat.'),
}

// A BCrypt csak 72 bájtot használ (a backend a hosszabbat elutasítja); ékezetes betű 2 bájt
const MAX_PASSWORD_BYTES = 72

export function passwordByteLength(value: string): number {
  return new TextEncoder().encode(value).length
}

export const newPasswordField = z
  .string()
  .min(8, 'A jelszó legalább 8 karakter legyen.')
  .max(100, 'A jelszó legfeljebb 100 karakter lehet.')
  .regex(/[A-Za-z]/, 'A jelszóban betűnek is lennie kell.')
  .regex(/\d/, 'A jelszóban számnak is lennie kell.')
  .refine((value) => passwordByteLength(value) <= MAX_PASSWORD_BYTES, 'A jelszó túl hosszú (legfeljebb 72 bájt; az ékezetes betűk duplán számítanak).')

export const registerSchema = z
  .object({
    ...personalDataFields,
    password: newPasswordField,
    passwordConfirm: z.string().min(1, 'Írd be újra a jelszót.'),
  })
  .refine((values) => values.password === values.passwordConfirm, {
    path: ['passwordConfirm'],
    message: 'A két jelszó nem egyezik.',
  })

export type RegisterForm = z.infer<typeof registerSchema>

// A backend mezőhibái (violations) közül az ezen az űrlapon lévők
const registerFields = ['lastName', 'firstName', 'email', 'phone', 'password'] as const

export type RegisterField = (typeof registerFields)[number]

export function isRegisterField(field: string): field is RegisterField {
  return (registerFields as readonly string[]).includes(field)
}
