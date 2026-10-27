import { useFormContext } from 'react-hook-form'
import type { AccountResponse, CategoryResponse } from '../../../api/types'
import AmountField from '../../../components/ui/AmountField'
import Select from '../../../components/ui/Select'
import TextField from '../../../components/ui/TextField'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { formatMoney } from '../../../lib/format'
import { quickAmounts } from '../../accounts/schemas'
import type { TransferForm } from '../schemas'

const amountInputFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 })

// 3. lépés: összeg és közlemény (a kategória nem kötelező)
export default function AmountStep({ source, categories }: { source: AccountResponse | undefined; categories: CategoryResponse[] }) {
  const {
    register,
    setValue,
    formState: { errors },
  } = useFormContext<TransferForm>()
  const currency = source?.currency ?? 'HUF'
  const { data: settings } = useBankSettings()

  return (
    <div className="space-y-5">
      <div>
        <AmountField
          label="Összeg"
          currency={currency}
          placeholder={currency === 'HUF' ? '25 000' : '100'}
          hint={source ? `Elérhető egyenleg: ${formatMoney(source.balance, currency)}` : undefined}
          error={errors.amount?.message}
          {...register('amount')}
        />
        <div className="mt-3 flex flex-wrap gap-2">
          {(quickAmounts[currency] ?? []).map((amount) => (
            <button
              key={amount}
              type="button"
              onClick={() => setValue('amount', amountInputFormatter.format(amount), { shouldValidate: true, shouldDirty: true })}
              className="rounded-full border border-border bg-card px-3 py-1 text-sm font-semibold text-muted-foreground transition hover:border-brand-300 hover:text-foreground focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
            >
              {formatMoney(amount, currency)}
            </button>
          ))}
        </div>
      </div>

      <TextField label="Közlemény (nem kötelező)" placeholder="pl. Albérlet – október" maxLength={255} error={errors.message?.message} {...register('message')} />

      <Select label="Kategória (nem kötelező)" {...register('categoryId')}>
        <option value="">Nincs megadva</option>
        {categories.map((category) => (
          <option key={category.id} value={category.id}>
            {category.name}
          </option>
        ))}
      </Select>

      {settings && (
        <p className="text-sm text-muted-foreground">Egy számláról naponta legfeljebb {formatMoney(settings.transferDailyLimit, currency)} utalható.</p>
      )}
    </div>
  )
}
