import { ChevronDown, Tags } from 'lucide-react'
import { motion } from 'motion/react'
import { useState } from 'react'
import type { CategorySpendingResponse } from '../../../api/types'
import { Card, CardHeader } from '../../../components/ui/Card'
import EmptyState from '../../../components/ui/EmptyState'
import Button from '../../../components/ui/Button'
import ButtonLink from '../../../components/ui/ButtonLink'
import { cn } from '../../../lib/cn'
import { formatMoney } from '../../../lib/format'
import { transactionIcon } from '../../transactions/categoryIcons'
import { CategoryRowsSkeleton } from './StatisticsSkeleton'

const percentFormatter = new Intl.NumberFormat('hu-HU', { maximumFractionDigits: 0 })

// Ennyi kategória látszik alapból (a többi egy gombbal nyitható le), így a kártya a mellette álló listával egy magas
const VISIBLE_COUNT = 6

interface CategorySpendingProps {
  data: CategorySpendingResponse[] | undefined
  currency: string
  periodLabel: string
  refreshing?: boolean
}

// Költés kategóriánként rangsorolt sávokkal: egy sorozat, ezért egy szín; a sáv hossza az összeg, mellette az
// összeg és a részarány felirattal (így nincs szükség buborékra, és felolvasóval is minden érték elérhető).
// Kördiagram helyett, mert 13 kategóriánál a hasonló méretű cikkek nem összehasonlíthatók.
export default function CategorySpending({ data, currency, periodLabel, refreshing = false }: CategorySpendingProps) {
  const total = data?.reduce((sum, item) => sum + item.totalAmount, 0) ?? 0
  const max = data?.[0]?.totalAmount ?? 0
  const [expanded, setExpanded] = useState(false)
  const visible = expanded ? data : data?.slice(0, VISIBLE_COUNT)

  return (
    <Card className="flex flex-col p-5 sm:p-6">
      {/* A leírás egy sor (csak az időszak), mint a szomszédos kártyáé, így a két lista sorai egy vonalban kezdődnek */}
      <CardHeader title="Költés kategóriánként" description={periodLabel} />

      <div className={cn('mt-5 transition-opacity duration-300', refreshing && 'opacity-50')}>
        {!data ? (
          <CategoryRowsSkeleton count={VISIBLE_COUNT} />
        ) : data.length === 0 ? (
          <EmptyState
            icon={<Tags />}
            title="Nincs kategorizált kiadás"
            description="Ebben az időszakban nincs kategóriába sorolt kiadásod. A tételek adatlapján tudod őket kategorizálni."
            action={
              <ButtonLink to="/transactions" variant="outline">
                Tranzakciók
              </ButtonLink>
            }
            className="py-8"
          />
        ) : (
          <ol className="space-y-4">
            {visible?.map((item) => {
              const share = total > 0 ? (item.totalAmount / total) * 100 : 0
              return (
                <li key={item.categoryId}>
                  <div className="flex items-center gap-3">
                    <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-brand-100 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
                      {transactionIcon('OUTCOME', item.categoryIcon, 'size-5')}
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block truncate font-semibold text-foreground">{item.categoryName}</span>
                      <span className="block text-xs text-muted-foreground">{item.transactionCount} tétel</span>
                    </span>
                    <span className="shrink-0 text-right">
                      <span className="block font-bold text-foreground tabular-nums">{formatMoney(item.totalAmount, currency)}</span>
                      <span className="block text-xs text-muted-foreground tabular-nums">{percentFormatter.format(share)}%</span>
                    </span>
                  </div>
                  <div aria-hidden className="mt-2 ml-13 h-2 overflow-hidden rounded-full bg-muted">
                    <motion.div
                      className="h-full rounded-r-[4px] bg-chart-outcome"
                      initial={{ width: 0 }}
                      animate={{ width: `${max > 0 ? (item.totalAmount / max) * 100 : 0}%` }}
                      transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
                    />
                  </div>
                </li>
              )
            })}
          </ol>
        )}
      </div>

      {data && data.length > 0 && (
        <div className="mt-auto flex flex-wrap items-center justify-between gap-3 pt-5">
          {data.length > VISIBLE_COUNT ? (
            <Button
              variant="ghost"
              size="sm"
              aria-expanded={expanded}
              rightIcon={<ChevronDown className={cn('transition-transform', expanded && 'rotate-180')} />}
              onClick={() => setExpanded((open) => !open)}
            >
              {expanded ? 'Kevesebb' : `Mind a ${data.length} kategória`}
            </Button>
          ) : (
            <span />
          )}
          <p className="text-sm text-muted-foreground">
            Összesen: <span className="font-bold text-foreground tabular-nums">{formatMoney(total, currency)}</span>
          </p>
        </div>
      )}
    </Card>
  )
}
