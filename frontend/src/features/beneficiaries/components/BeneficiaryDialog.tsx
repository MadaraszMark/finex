import { UserRoundPen, UserRoundPlus } from 'lucide-react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { BeneficiaryResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import IbanField from '../../../components/ui/IbanField'
import TextField from '../../../components/ui/TextField'
import { formatIban } from '../../../lib/format'
import { isValidIban, normalizeIban } from '../../../lib/iban'
import { useCreateBeneficiary, useUpdateBeneficiary } from '../queries'
import { beneficiarySchema, type BeneficiaryForm } from '../schemas'

interface BeneficiaryDialogProps {
  open: boolean
  onClose: () => void
  // Megadva szerkesztés, egyébként új kedvezményezett (a szülő kulcsolja, így mindig friss értékekkel nyílik)
  beneficiary?: BeneficiaryResponse
}

// Kedvezményezett felvétele vagy módosítása: név, számlaszám (élő IBAN-ellenőrzéssel) és megjegyzés
export default function BeneficiaryDialog({ open, onClose, beneficiary }: BeneficiaryDialogProps) {
  const createBeneficiary = useCreateBeneficiary()
  const updateBeneficiary = useUpdateBeneficiary()
  const mutation = beneficiary ? updateBeneficiary : createBeneficiary

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BeneficiaryForm>({
    resolver: zodResolver(beneficiarySchema),
    defaultValues: {
      name: beneficiary?.name ?? '',
      accountNumber: beneficiary ? formatIban(beneficiary.accountNumber) : '',
      note: beneficiary?.note ?? '',
    },
  })
  const accountNumber = useWatch({ control, name: 'accountNumber' })

  function close() {
    onClose()
    reset()
    createBeneficiary.reset()
    updateBeneficiary.reset()
  }

  function onSubmit(values: BeneficiaryForm) {
    const request = { name: values.name, accountNumber: normalizeIban(values.accountNumber), note: values.note || undefined }
    const options = {
      onSuccess: () => {
        close()
        toast.success(beneficiary ? 'Mentettük a módosítást' : 'Új kedvezményezett', { description: values.name })
      },
    }
    if (beneficiary) {
      updateBeneficiary.mutate({ id: beneficiary.id, request }, options)
    } else {
      createBeneficiary.mutate(request, options)
    }
  }

  return (
    <Dialog icon={beneficiary ? <UserRoundPen /> : <UserRoundPlus />}
      open={open}
      onClose={close}
      title={beneficiary ? 'Kedvezményezett szerkesztése' : 'Új kedvezményezett'}
      description="A gyakori címzettjeidnek így nem kell mindig beírni a számlaszámát."
    >
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <TextField label="Név" placeholder="pl. Tóth Gábor" maxLength={150} error={errors.name?.message} {...register('name')} />
        <IbanField label="Számlaszám (IBAN)" valid={isValidIban(accountNumber ?? '')} error={errors.accountNumber?.message} {...register('accountNumber')} />
        <TextField label="Megjegyzés (nem kötelező)" placeholder="pl. albérlet" maxLength={255} error={errors.note?.message} {...register('note')} />

        {mutation.isError && <Alert tone="danger">{getErrorMessage(mutation.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            {beneficiary ? 'Mentés' : 'Hozzáadás'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
