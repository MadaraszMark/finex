import { zodResolver } from '@hookform/resolvers/zod'
import { CreditCard, Landmark } from 'lucide-react'
import { Controller, useForm } from 'react-hook-form'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { Currency } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import Dialog, { DialogActions } from '../../../components/ui/Dialog'
import SegmentedControl from '../../../components/ui/SegmentedControl'
import TextField from '../../../components/ui/TextField'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { currencies, currencyNames } from '../../../lib/format'
import { useOpenAccount } from '../queries'
import { openAccountSchema, type OpenAccountForm } from '../schemas'

const suggestedNames: Record<Currency, string> = {
  HUF: 'Forint számla',
  EUR: 'Euró számla',
  USD: 'Dollár számla',
}

interface OpenAccountDialogProps {
  open: boolean
  onClose: () => void
  openAccountCount: number
}

// Új számla nyitása: név és pénznem (a backend bankkártyát is ad hozzá)
export default function OpenAccountDialog({ open, onClose, openAccountCount }: OpenAccountDialogProps) {
  const navigate = useNavigate()
  const openAccount = useOpenAccount()
  // A nyitható számlák száma a backend beállítása (GET /settings)
  const { data: settings } = useBankSettings()
  const limitReached = settings !== undefined && openAccountCount >= settings.maxAccountsPerUser

  const {
    register,
    control,
    handleSubmit,
    setValue,
    reset,
    formState: { errors, dirtyFields },
  } = useForm<OpenAccountForm>({
    resolver: zodResolver(openAccountSchema),
    defaultValues: { name: suggestedNames.EUR, currency: 'EUR' },
  })

  function close() {
    onClose()
    reset()
    openAccount.reset()
  }

  function onSubmit(values: OpenAccountForm) {
    openAccount.mutate(values, {
      onSuccess: (account) => {
        close()
        toast.success('Megnyitottuk az új számládat', {
          description: `${account.name} – bankkártyát is kaptál hozzá.`,
          action: { label: 'Megnézem', onClick: () => navigate(`/accounts/${account.id}`) },
        })
      },
    })
  }

  return (
    <Dialog icon={<Landmark />} open={open} onClose={close} title="Új számla nyitása" description="Forint-, euró- vagy dollárszámla, azonnal használható bankkártyával.">
      <form noValidate onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div>
          <p className="mb-1.5 text-sm font-medium text-foreground">Pénznem</p>
          <Controller
            control={control}
            name="currency"
            render={({ field }) => (
              <SegmentedControl
                label="Pénznem"
                value={field.value}
                options={currencies.map((currency) => ({ value: currency, label: currencyNames[currency] }))}
                onChange={(currency) => {
                  field.onChange(currency)
                  // Amíg a felhasználó nem írt saját nevet, a javasolt név követi a pénznemet
                  if (!dirtyFields.name) {
                    setValue('name', suggestedNames[currency])
                  }
                }}
              />
            )}
          />
        </div>

        <TextField label="A számla neve" placeholder="pl. Nyaralás, Közös kassza" maxLength={100} error={errors.name?.message} {...register('name')} />

        <p className="flex items-start gap-2.5 rounded-2xl bg-muted/70 px-4 py-3 text-sm text-muted-foreground">
          <CreditCard aria-hidden className="mt-0.5 size-4 shrink-0 text-primary" />
          A számlához automatikusan bankkártyát is nyitunk, a napi limitet a Kártyák oldalon állíthatod.
        </p>

        {limitReached && settings && (
          <Alert tone="info">Legfeljebb {settings.maxAccountsPerUser} nyitott számlád lehet. Új számlához előbb zárj le egy nulla egyenlegűt.</Alert>
        )}
        {openAccount.isError && <Alert tone="danger">{getErrorMessage(openAccount.error)}</Alert>}

        <DialogActions>
          <Button variant="ghost" onClick={close}>
            Mégse
          </Button>
          <Button type="submit" loading={openAccount.isPending} disabled={limitReached}>
            Számla megnyitása
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
