import { z } from 'zod'
import { newPasswordField, personalDataFields } from '../auth/schemas'

// A backend szabályai (UpdateUserRequest, ChangePasswordRequest)

// A jelenlegi jelszó csak e-mail-cserénél kötelező (ezt az űrlap ellenőrzi, ott van a régi cím)
export const profileSchema = z.object({ ...personalDataFields, currentPassword: z.string().max(100) })

export type ProfileForm = z.infer<typeof profileSchema>

export const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Add meg a jelenlegi jelszavad.'),
    newPassword: newPasswordField,
    confirmPassword: z.string().min(1, 'Írd be újra az új jelszót.'),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    path: ['confirmPassword'],
    message: 'A két jelszó nem egyezik.',
  })
  .refine((values) => values.newPassword !== values.currentPassword, {
    path: ['newPassword'],
    message: 'Az új jelszó nem egyezhet meg a régivel.',
  })

export type PasswordForm = z.infer<typeof passwordSchema>
