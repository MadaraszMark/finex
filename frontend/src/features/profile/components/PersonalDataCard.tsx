import { zodResolver } from '@hookform/resolvers/zod'
import { Lock, Mail, Phone, UserRound } from 'lucide-react'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage, getFieldErrors } from '../../../api/client'
import type { UserResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import PasswordField from '../../../components/ui/PasswordField'
import TextField from '../../../components/ui/TextField'
import { useUpdateProfile } from '../queries'
import { profileSchema, type ProfileForm } from '../schemas'

const fields = ['lastName', 'firstName', 'email', 'phone', 'currentPassword'] as const

// E-mail-cseréhez a jelszó is kell, hogy ellopott munkamenettel ne legyen átvehető a fiók
export default function PersonalDataCard({ user }: { user: UserResponse }) {
  const updateProfile = useUpdateProfile()

  const {
    register,
    control,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isDirty },
  } = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { lastName: user.lastName, firstName: user.firstName, email: user.email, phone: user.phone ?? '', currentPassword: '' },
  })
  const email = useWatch({ control, name: 'email' })
  const emailChanged = email.trim().toLowerCase() !== user.email.toLowerCase()

  function onSubmit(values: ProfileForm) {
    if (emailChanged && !values.currentPassword) {
      setError('currentPassword', { message: 'Az e-mail cím módosításához add meg a jelenlegi jelszavad.' })
      return
    }

    updateProfile.mutate(
      { ...values, phone: values.phone || undefined, currentPassword: emailChanged ? values.currentPassword : undefined },
      {
        onSuccess: (updated) => {
          reset({ lastName: updated.lastName, firstName: updated.firstName, email: updated.email, phone: updated.phone ?? '', currentPassword: '' })
          toast.success('Mentettük az adataidat', emailChanged ? { description: 'Mostantól az új e-mail címeddel lépj be.' } : undefined)
        },
        onError: (error) => {
          Object.entries(getFieldErrors(error)).forEach(([field, message]) => {
            if ((fields as readonly string[]).includes(field)) {
              setError(field as (typeof fields)[number], { message })
            }
          })
        },
      },
    )
  }

  return (
    <Card className="flex flex-col p-6 sm:p-7">
      <CardHeader title="Személyes adatok" description="A neved a bankkártyádon és az utalásaidon is így jelenik meg." />
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="mt-6 flex flex-1 flex-col space-y-5">
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <TextField label="Vezetéknév" autoComplete="family-name" leftIcon={<UserRound />} error={errors.lastName?.message} {...register('lastName')} />
          <TextField label="Keresztnév" autoComplete="given-name" error={errors.firstName?.message} {...register('firstName')} />
        </div>
        <TextField label="E-mail cím" type="email" autoComplete="email" leftIcon={<Mail />} error={errors.email?.message} {...register('email')} />
        {emailChanged && (
          <PasswordField
            label="Jelenlegi jelszó (az e-mail cím módosításához)"
            autoComplete="current-password"
            leftIcon={<Lock />}
            error={errors.currentPassword?.message}
            {...register('currentPassword')}
          />
        )}
        <TextField
          label="Telefonszám (nem kötelező)"
          type="tel"
          autoComplete="tel"
          placeholder="+36 30 123 4567"
          leftIcon={<Phone />}
          error={errors.phone?.message}
          {...register('phone')}
        />

        {updateProfile.isError && <Alert tone="danger">{getErrorMessage(updateProfile.error)}</Alert>}

        <div className="mt-auto flex justify-end">
          <Button type="submit" loading={updateProfile.isPending} disabled={!isDirty}>
            Mentés
          </Button>
        </div>
      </form>
    </Card>
  )
}
