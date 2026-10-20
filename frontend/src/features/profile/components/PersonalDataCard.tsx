import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, Phone, UserRound } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage, getFieldErrors } from '../../../api/client'
import type { UserResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import TextField from '../../../components/ui/TextField'
import { useUpdateProfile } from '../queries'
import { profileSchema, type ProfileForm } from '../schemas'

const fields = ['lastName', 'firstName', 'email', 'phone'] as const

// Személyes adatok módosítása; a Mentés gomb csak változtatás után aktív
export default function PersonalDataCard({ user }: { user: UserResponse }) {
  const updateProfile = useUpdateProfile()

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isDirty },
  } = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { lastName: user.lastName, firstName: user.firstName, email: user.email, phone: user.phone ?? '' },
  })

  function onSubmit(values: ProfileForm) {
    updateProfile.mutate(
      { ...values, phone: values.phone || undefined },
      {
        onSuccess: (updated) => {
          reset({ lastName: updated.lastName, firstName: updated.firstName, email: updated.email, phone: updated.phone ?? '' })
          toast.success('Mentettük az adataidat')
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

        {/* A gomb a kártya aljára kerül, így a szomszédos kártya gombjával egy vonalban áll */}
        <div className="mt-auto flex justify-end">
          <Button type="submit" loading={updateProfile.isPending} disabled={!isDirty}>
            Mentés
          </Button>
        </div>
      </form>
    </Card>
  )
}
