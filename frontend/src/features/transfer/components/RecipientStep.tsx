import { Check, Search, UsersRound } from 'lucide-react'
import { useState } from 'react'
import { Controller, useFormContext, useWatch } from 'react-hook-form'
import type { AccountResponse, BeneficiaryResponse } from '../../../api/types'
import ChipGroup from '../../../components/ui/ChipGroup'
import EmptyState from '../../../components/ui/EmptyState'
import IbanField from '../../../components/ui/IbanField'
import Switch from '../../../components/ui/Switch'
import TextField from '../../../components/ui/TextField'
import { cn } from '../../../lib/cn'
import { formatShortIban, nameInitials } from '../../../lib/format'
import { isValidIban, normalizeIban } from '../../../lib/iban'
import type { TransferForm } from '../schemas'
import AccountOption from './AccountOption'

export type RecipientMode = 'beneficiary' | 'own' | 'new'

const modeOptions: { value: RecipientMode; label: string }[] = [
  { value: 'beneficiary', label: 'Kedvezményezettek' },
  { value: 'own', label: 'Saját számla' },
  { value: 'new', label: 'Új címzett' },
]

// Kereséshez: kisbetűs, szóköz nélküli alak (így a "hu15 1177" is megtalálja a számlaszámot)
function searchable(value: string): string {
  return value.toLowerCase().replace(/\s/g, '')
}

interface RecipientStepProps {
  mode: RecipientMode
  onModeChange: (mode: RecipientMode) => void
  beneficiaries: BeneficiaryResponse[]
  // A forrásszámlával azonos pénznemű, aktív saját számlák (átvezetés)
  ownAccounts: AccountResponse[]
  ownerName: string
  // A megadott számlaszám már kedvezményezett vagy saját számla: nincs mit elmenteni
  knownTarget: boolean
}

// 2. lépés: kinek utalsz? Kedvezményezett a listából, saját másik számla, vagy új címzett IBAN-nal
export default function RecipientStep({ mode, onModeChange, beneficiaries, ownAccounts, ownerName, knownTarget }: RecipientStepProps) {
  const {
    register,
    control,
    setValue,
    formState: { errors },
  } = useFormContext<TransferForm>()
  const toAccountNumber = useWatch({ control, name: 'toAccountNumber' })
  const [query, setQuery] = useState('')
  const selectedIban = normalizeIban(toAccountNumber ?? '')

  function choose(accountNumber: string, name: string) {
    setValue('toAccountNumber', accountNumber, { shouldValidate: true, shouldDirty: true })
    setValue('partnerName', name, { shouldValidate: true, shouldDirty: true })
    setValue('saveBeneficiary', false)
  }

  const filtered = beneficiaries.filter((beneficiary) => searchable(`${beneficiary.name}${beneficiary.accountNumber}`).includes(searchable(query)))
  const missingSelection = mode !== 'new' && (errors.toAccountNumber || errors.partnerName)

  return (
    <div className="space-y-5">
      <ChipGroup label="Címzett forrása" value={mode} options={modeOptions} onChange={onModeChange} />

      {mode === 'beneficiary' &&
        (beneficiaries.length === 0 ? (
          <EmptyState icon={<UsersRound />} title="Még nincs kedvezményezetted" description="Add meg az új címzett adatait; az utalás után el is mentheted." className="py-8" />
        ) : (
          <>
            {beneficiaries.length > 4 && (
              <TextField label="Keresés" type="search" placeholder="Név vagy számlaszám" leftIcon={<Search />} value={query} onChange={(event) => setQuery(event.target.value)} />
            )}
            <div role="radiogroup" aria-label="Kedvezményezettek" className="space-y-2">
              {filtered.map((beneficiary) => {
                const selected = selectedIban === beneficiary.accountNumber
                return (
                  <button
                    key={beneficiary.id}
                    type="button"
                    role="radio"
                    aria-checked={selected}
                    onClick={() => choose(beneficiary.accountNumber, beneficiary.name)}
                    className={cn(
                      'flex w-full items-center gap-4 rounded-2xl border p-3.5 text-left transition duration-200',
                      'focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none',
                      selected ? 'border-brand-400 bg-brand-50 dark:border-brand-500/60 dark:bg-brand-950/60' : 'border-border bg-card/70 hover:border-brand-300',
                    )}
                  >
                    <span aria-hidden className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-brand-gradient text-sm font-bold text-white">
                      {nameInitials(beneficiary.name)}
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate font-bold text-foreground">{beneficiary.name}</span>
                      <span className="block font-mono text-sm tracking-wide text-muted-foreground">{formatShortIban(beneficiary.accountNumber)}</span>
                    </span>
                    {beneficiary.internal && (
                      <span className="shrink-0 rounded-full bg-brand-100 px-2.5 py-0.5 text-xs font-bold text-brand-700 dark:bg-brand-950 dark:text-brand-300">FineX</span>
                    )}
                    <span
                      aria-hidden
                      className={cn('flex size-6 shrink-0 items-center justify-center rounded-full border-2', selected ? 'border-transparent bg-brand-gradient text-white' : 'border-border')}
                    >
                      {selected && <Check className="size-3.5" />}
                    </span>
                  </button>
                )
              })}
              {filtered.length === 0 && <p className="py-4 text-center text-sm text-muted-foreground">Nincs ilyen kedvezményezett.</p>}
            </div>
          </>
        ))}

      {mode === 'own' &&
        (ownAccounts.length === 0 ? (
          <p className="rounded-2xl bg-muted/70 p-4 text-sm text-muted-foreground">Nincs másik, azonos pénznemű aktív számlád, amelyre átvezethetnél.</p>
        ) : (
          <div role="radiogroup" aria-label="Saját számlák" className="space-y-2">
            {ownAccounts.map((account) => (
              <AccountOption
                key={account.id}
                account={account}
                selected={selectedIban === account.accountNumber}
                onSelect={() => choose(account.accountNumber, ownerName)}
                showBalance={false}
              />
            ))}
          </div>
        ))}

      {mode === 'new' && (
        <>
          <IbanField label="Számlaszám (IBAN)" valid={isValidIban(toAccountNumber ?? '')} error={errors.toAccountNumber?.message} {...register('toAccountNumber')} />
          <TextField label="A címzett neve" placeholder="pl. Tóth Gábor" maxLength={150} error={errors.partnerName?.message} {...register('partnerName')} />
          {!knownTarget && (
            <Controller
              control={control}
              name="saveBeneficiary"
              render={({ field }) => (
                <Switch
                  label="Mentés kedvezményezettként"
                  description="Legközelebb egy érintéssel választhatod ki."
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
          )}
        </>
      )}

      {missingSelection && (
        <p role="alert" className="text-sm font-medium text-danger">
          {selectedIban ? (errors.toAccountNumber?.message ?? errors.partnerName?.message) : 'Válassz címzettet a listából.'}
        </p>
      )}
    </div>
  )
}
