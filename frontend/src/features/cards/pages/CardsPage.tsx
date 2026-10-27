import { CreditCard } from 'lucide-react'
import { useSearchParams } from 'react-router'
import { getErrorMessage } from '../../../api/client'
import { Card } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import PageHeader from '../../../components/ui/PageHeader'
import QueryError from '../../../components/ui/QueryError'
import { useAccounts } from '../../accounts/queries'
import CardPicker from '../components/CardPicker'
import CardsSkeleton from '../components/CardsSkeleton'
import CardSettings from '../components/CardSettings'
import { useCards } from '../queries'

// Kártyák; a kiválasztott kártya a címsorban van (?card=7), így ide lehet linkelni
export default function CardsPage() {
  const cardsQuery = useCards()
  const accountsQuery = useAccounts()
  const [searchParams, setSearchParams] = useSearchParams()

  const cards = cardsQuery.data ?? []
  const accounts = accountsQuery.data ?? []
  const requestedId = Number(searchParams.get('card'))
  const selected = cards.find((card) => card.id === requestedId) ?? cards.find((card) => card.status !== 'CANCELLED') ?? cards[0]

  function select(id: number) {
    setSearchParams({ card: String(id) }, { replace: true })
  }

  return (
    <>
      <PageHeader title="Kártyák" description="Tiltás, napi limit és online fizetés – egy helyen." />

      {cardsQuery.isPending || accountsQuery.isPending ? (
        <CardsSkeleton />
      ) : cardsQuery.isError || accountsQuery.isError ? (
        <Card>
          <QueryError
            message={getErrorMessage(cardsQuery.error ?? accountsQuery.error)}
            retrying={cardsQuery.isFetching || accountsQuery.isFetching}
            onRetry={() => {
              void cardsQuery.refetch()
              void accountsQuery.refetch()
            }}
          />
        </Card>
      ) : !selected ? (
        <Card>
          <EmptyState icon={<CreditCard />} title="Még nincs bankkártyád" description="Minden számládhoz automatikusan kártya jár, nyiss egy számlát." />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,22rem)_minmax(0,1fr)] lg:items-start">
          <div className="lg:sticky lg:top-28">
            <CardPicker cards={cards} accounts={accounts} selectedId={selected.id} onSelect={select} />
          </div>
          <CardSettings key={selected.id} card={selected} account={accounts.find((account) => account.id === selected.accountId)} />
        </div>
      )}
    </>
  )
}
