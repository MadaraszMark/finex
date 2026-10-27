import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, ArrowRight, Send } from 'lucide-react'
import { AnimatePresence, motion } from 'motion/react'
import { useCallback, useRef, useState, type FormEvent } from 'react'
import { FormProvider, useForm, useWatch, type Resolver } from 'react-hook-form'
import { useSearchParams } from 'react-router'
import { toast } from 'sonner'
import { getErrorMessage } from '../../../api/client'
import type { AccountResponse, BeneficiaryResponse, TransferResponse } from '../../../api/types'
import Alert from '../../../components/ui/Alert'
import Button from '../../../components/ui/Button'
import { Card, CardHeader } from '../../../components/ui/Card'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { useBankSettings } from '../../../hooks/useBankSettings'
import { formatMoney, parseAmount } from '../../../lib/format'
import { newIdempotencyKey } from '../../../lib/idempotency'
import { normalizeIban } from '../../../lib/iban'
import { useCurrentUser } from '../../../session/useCurrentUser'
import { useAccounts } from '../../accounts/queries'
import { useBeneficiaries, useCreateBeneficiary } from '../../beneficiaries/queries'
import { useCategories } from '../../transactions/queries'
import AccountOption from '../components/AccountOption'
import AmountStep from '../components/AmountStep'
import RecipientStep, { type RecipientMode } from '../components/RecipientStep'
import Stepper from '../components/Stepper'
import TransferOverview from '../components/TransferOverview'
import TransferReceipt from '../components/TransferReceipt'
import TransferSkeleton from '../components/TransferSkeleton'
import { useTransfer } from '../queries'
import { transferSchema, type TransferForm } from '../schemas'

const steps = ['Honnan', 'Kinek', 'Mennyit', 'Áttekintés'] as const
const stepFields: (keyof TransferForm)[][] = [['fromAccountId'], ['toAccountNumber', 'partnerName'], ['amount', 'message'], []]
// Ennyi ideig nem indítható az utalás az áttekintés után (dupla kattintás ellen)
const reviewGuardMs = 600

// Utalás lépésről lépésre; a címsorból előre kitölthető (?from=3&to=HU…&name=…&amount=…)
export default function TransferPage() {
  const accountsQuery = useAccounts()
  const beneficiariesQuery = useBeneficiaries()
  const [receipt, setReceipt] = useState<TransferResponse | null>(null)
  const [runId, setRunId] = useState(0)
  const [, setSearchParams] = useSearchParams()

  const activeAccounts = (accountsQuery.data ?? []).filter((account) => account.status === 'ACTIVE')

  function startNewTransfer() {
    setSearchParams(new URLSearchParams(), { replace: true })
    setReceipt(null)
    setRunId((current) => current + 1)
  }

  return (
    <>
      <PageHeader title="Utalás" description="Pénzküldés pár lépésben – FineX-es címzettnek azonnal megérkezik." />

      {accountsQuery.isPending || beneficiariesQuery.isPending ? (
        <TransferSkeleton />
      ) : accountsQuery.isError || beneficiariesQuery.isError ? (
        <Card>
          <QueryError
            message={getErrorMessage(accountsQuery.error ?? beneficiariesQuery.error)}
            retrying={accountsQuery.isFetching || beneficiariesQuery.isFetching}
            onRetry={() => {
              void accountsQuery.refetch()
              void beneficiariesQuery.refetch()
            }}
          />
        </Card>
      ) : receipt ? (
        <TransferReceipt receipt={receipt} onNewTransfer={startNewTransfer} />
      ) : activeAccounts.length === 0 ? (
        <Card className="p-6">
          <Alert tone="info">Nincs aktív számlád, amelyről utalni lehetne.</Alert>
        </Card>
      ) : (
        <TransferWizard key={runId} accounts={activeAccounts} beneficiaries={beneficiariesQuery.data ?? []} onDone={setReceipt} />
      )}
    </>
  )
}

interface TransferWizardProps {
  accounts: AccountResponse[]
  beneficiaries: BeneficiaryResponse[]
  onDone: (receipt: TransferResponse) => void
}

// Kezdőállapot a címsorból: kitöltött mezők és az első hiányos lépés
function initialState(params: URLSearchParams, accounts: AccountResponse[], beneficiaries: BeneficiaryResponse[]) {
  const from = accounts.find((account) => String(account.id) === params.get('from')) ?? accounts[0]
  const to = normalizeIban(params.get('to') ?? '')
  const name = params.get('name') ?? ''
  const amount = parseAmount(params.get('amount') ?? '')

  const values: TransferForm = {
    fromAccountId: String(from.id),
    toAccountNumber: to,
    partnerName: name,
    amount: amount !== undefined ? new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 2 }).format(amount) : '',
    message: '',
    categoryId: '',
    saveBeneficiary: false,
  }

  const mode: RecipientMode = !to
    ? beneficiaries.length > 0
      ? 'beneficiary'
      : 'new'
    : accounts.some((account) => account.accountNumber === to)
      ? 'own'
      : beneficiaries.some((beneficiary) => beneficiary.accountNumber === to)
        ? 'beneficiary'
        : 'new'

  const step = params.has('from') && to && name ? 2 : to && name ? 0 : params.has('from') ? 1 : 0
  return { values, mode, step }
}

function TransferWizard({ accounts, beneficiaries, onDone }: TransferWizardProps) {
  const [searchParams] = useSearchParams()
  const { data: user } = useCurrentUser()
  const { data: categories = [] } = useCategories()
  const transferMutation = useTransfer()
  const createBeneficiary = useCreateBeneficiary()

  const [initial] = useState(() => initialState(searchParams, accounts, beneficiaries))
  const [step, setStep] = useState(initial.step)
  const [direction, setDirection] = useState(1)
  const [mode, setMode] = useState<RecipientMode>(initial.mode)
  const reviewShownAt = useRef(0)
  // Áttekintésenként egy kulcs: az újraküldéseket a szerver legfeljebb egyszer könyveli le
  const idempotencyKey = useRef('')

  // A séma a forrásszámla pénzneméhez és a backend napi limitjéhez igazodik
  const { data: settings } = useBankSettings()
  const dailyLimit = settings?.transferDailyLimit
  const resolver = useCallback<Resolver<TransferForm>>(
    (values, context, options) => {
      const currency = accounts.find((account) => String(account.id) === values.fromAccountId)?.currency ?? 'HUF'
      return zodResolver(transferSchema(currency, dailyLimit))(values, context, options)
    },
    [accounts, dailyLimit],
  )

  const form = useForm<TransferForm>({ resolver, defaultValues: initial.values, mode: 'onTouched' })
  const values = useWatch({ control: form.control }) as TransferForm

  const source = accounts.find((account) => String(account.id) === values.fromAccountId)
  const targetIban = normalizeIban(values.toAccountNumber ?? '')
  const ownTarget = accounts.find((account) => account.accountNumber === targetIban)
  const beneficiaryTarget = beneficiaries.find((beneficiary) => beneficiary.accountNumber === targetIban)
  const internal = ownTarget ? true : beneficiaryTarget?.internal
  const categoryName = categories.find((category) => String(category.id) === values.categoryId)?.name
  const ownerName = user ? `${user.lastName} ${user.firstName}` : ''

  function goTo(nextStep: number) {
    setDirection(nextStep > step ? 1 : -1)
    setStep(nextStep)
    if (nextStep === steps.length - 1) {
      reviewShownAt.current = Date.now()
      idempotencyKey.current = newIdempotencyKey()
    }
  }

  // Továbblépés előtt a lépés mezői, a saját számla és a fedezet is ellenőrződik
  async function next() {
    const valid = await form.trigger(stepFields[step])
    if (!valid || !source) {
      return
    }
    if (step === 1 && targetIban === source.accountNumber) {
      form.setError('toAccountNumber', { message: 'Nem utalhatsz ugyanarra a számlára.' })
      return
    }
    if (step === 2 && (parseAmount(values.amount) ?? 0) > source.balance) {
      form.setError('amount', { message: `Nincs elég fedezet: a számla egyenlege ${formatMoney(source.balance, source.currency)}.` })
      return
    }
    goTo(step + 1)
  }

  function submit(formValues: TransferForm) {
    transferMutation.mutate(
      {
        request: {
          fromAccountId: Number(formValues.fromAccountId),
          toAccountNumber: normalizeIban(formValues.toAccountNumber),
          partnerName: formValues.partnerName,
          amount: parseAmount(formValues.amount) ?? 0,
          message: formValues.message || undefined,
          categoryIds: formValues.categoryId ? [Number(formValues.categoryId)] : undefined,
        },
        idempotencyKey: idempotencyKey.current,
      },
      {
        onSuccess: async (response) => {
          if (formValues.saveBeneficiary && !ownTarget && !beneficiaryTarget) {
            try {
              await createBeneficiary.mutateAsync({ name: formValues.partnerName, accountNumber: normalizeIban(formValues.toAccountNumber) })
              toast.success('Elmentettük kedvezményezettként', { description: formValues.partnerName })
            } catch (error) {
              toast.error(getErrorMessage(error))
            }
          }
          onDone(response)
        },
      },
    )
  }

  // A Tovább és az Utalás gomb egy helyen van: a túl korai beküldést (dupla kattintás) elvetjük
  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (step < steps.length - 1) {
      void next()
    } else if (Date.now() - reviewShownAt.current >= reviewGuardMs) {
      void form.handleSubmit(submit)()
    }
  }

  return (
    <FormProvider {...form}>
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]">
        <Card className="flex flex-col p-5 sm:p-8">
          <Stepper steps={steps} current={step} />

          <form noValidate onSubmit={handleSubmit} className="mt-8 flex flex-1 flex-col">
            <AnimatePresence mode="wait" initial={false} custom={direction}>
              <motion.div
                key={step}
                custom={direction}
                initial={{ opacity: 0, x: 24 * direction }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -24 * direction }}
                transition={{ duration: 0.22, ease: 'easeOut' }}
              >
                {step === 0 && (
                  <fieldset>
                    <legend className="text-lg font-bold text-foreground">Melyik számládról utalsz?</legend>
                    <div role="radiogroup" aria-label="Forrásszámla" className="mt-4 space-y-2">
                      {accounts.map((account) => (
                        <AccountOption
                          key={account.id}
                          account={account}
                          selected={values.fromAccountId === String(account.id)}
                          onSelect={() => form.setValue('fromAccountId', String(account.id), { shouldValidate: true })}
                        />
                      ))}
                    </div>
                    {form.formState.errors.fromAccountId && <p className="mt-2 text-sm text-danger">{form.formState.errors.fromAccountId.message}</p>}
                  </fieldset>
                )}

                {step === 1 && (
                  <fieldset>
                    <legend className="mb-4 text-lg font-bold text-foreground">Kinek utalsz?</legend>
                    <RecipientStep
                      mode={mode}
                      onModeChange={setMode}
                      beneficiaries={beneficiaries}
                      ownAccounts={accounts.filter((account) => account.id !== source?.id && account.currency === source?.currency)}
                      ownerName={ownerName}
                      knownTarget={ownTarget !== undefined || beneficiaryTarget !== undefined}
                    />
                  </fieldset>
                )}

                {step === 2 && (
                  <fieldset>
                    <legend className="mb-4 text-lg font-bold text-foreground">Mennyit utalsz?</legend>
                    <AmountStep source={source} categories={categories} />
                  </fieldset>
                )}

                {step === 3 && (
                  <section aria-labelledby="transfer-review">
                    <h2 id="transfer-review" className="mb-4 text-lg font-bold text-foreground">
                      Minden stimmel?
                    </h2>
                    <TransferOverview
                      source={source}
                      toAccountNumber={values.toAccountNumber}
                      partnerName={values.partnerName}
                      amount={values.amount}
                      message={values.message}
                      categoryName={categoryName}
                      internal={internal}
                    />
                    {transferMutation.isError && (
                      <Alert tone="danger" className="mt-4">
                        {getErrorMessage(transferMutation.error)}
                      </Alert>
                    )}
                  </section>
                )}
              </motion.div>
            </AnimatePresence>

            <div className="mt-auto pt-8">
              <div className="flex flex-col-reverse gap-3 border-t border-border pt-6 sm:flex-row sm:justify-between">
                <Button variant="ghost" leftIcon={<ArrowLeft />} disabled={step === 0 || transferMutation.isPending} onClick={() => goTo(step - 1)}>
                  Vissza
                </Button>
                {step < steps.length - 1 ? (
                  <Button type="submit" rightIcon={<ArrowRight />}>
                    Tovább
                  </Button>
                ) : (
                  <Button type="submit" leftIcon={<Send />} loading={transferMutation.isPending}>
                    Utalás indítása
                  </Button>
                )}
              </div>
            </div>
          </form>
        </Card>

        <Card className="hidden p-6 lg:block">
          <CardHeader title="Összefoglaló" />
          <div className="mt-4">
            <TransferOverview
              compact
              source={source}
              toAccountNumber={values.toAccountNumber}
              partnerName={values.partnerName}
              amount={values.amount}
              message={values.message}
              categoryName={categoryName}
              internal={internal}
            />
          </div>
        </Card>
      </div>
    </FormProvider>
  )
}
