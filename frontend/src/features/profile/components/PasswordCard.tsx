import { zodResolver } from '@hookform/resolvers/zod'
import { KeyRound, Lock } from 'lucide-react'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import PasswordField from '../../../components/ui/PasswordField'
import PasswordChecklist from '../../auth/components/PasswordChecklist'
import { useChangePassword } from '../queries'
import { passwordSchema, type PasswordForm } from '../schemas'

// Jelszócsere: a jelenlegi jelszó és kétszer az új, a követelmények élőben pipálódnak ki
export default function PasswordCard() {
  const changePassword = useChangePassword()

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PasswordForm>({
    resolver: zodResolver(passwordSchema),
    mode: 'onTouched',
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  })
  const newPassword = useWatch({ control, name: 'newPassword' })

  function onSubmit(values: PasswordForm) {
    changePassword.mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      {
        onSuccess: () => {
          reset()
          toast.success('Megváltoztattad a jelszavad', { description: 'Legközelebb már az újjal lépj be.' })
        },
      },
    )
  }

  return (
    <Card className="flex flex-col p-6 sm:p-7">
      <CardHeader title="Jelszócsere" description="Rendszeresen cseréld, és ne használd máshol ugyanezt a jelszót." />
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="mt-6 flex flex-1 flex-col space-y-5">
        <PasswordField label="Jelenlegi jelszó" autoComplete="current-password" leftIcon={<Lock />} error={errors.currentPassword?.message} {...register('currentPassword')} />
        <div>
          <PasswordField label="Új jelszó" autoComplete="new-password" leftIcon={<KeyRound />} error={errors.newPassword?.message} {...register('newPassword')} />
          <PasswordChecklist password={newPassword} />
        </div>
        <PasswordField label="Új jelszó még egyszer" autoComplete="new-password" leftIcon={<KeyRound />} error={errors.confirmPassword?.message} {...register('confirmPassword')} />

        {changePassword.isError && <Alert tone="danger">{getErrorMessage(changePassword.error)}</Alert>}

        {/* A gomb a kártya aljára kerül, így a szomszédos kártya gombjával egy vonalban áll */}
        <div className="mt-auto flex justify-end">
          <Button type="submit" loading={changePassword.isPending}>
            Jelszó megváltoztatása
          </Button>
        </div>
      </form>
    </Card>
  )
}
