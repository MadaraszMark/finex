import { ArrowLeft } from 'lucide-react'
import ButtonLink from '../../../components/ui/ButtonLink'

// Vissza a nyitóképernyőre; 360 px alatt csak a nyíl látszik, hogy a fő gomb kiférjen
export default function BackToWelcomeLink() {
  return (
    <ButtonLink to="/welcome" variant="ghost" size="lg" aria-label="Vissza a nyitóképernyőre" className="shrink-0 px-3 min-[360px]:px-4">
      <ArrowLeft className="min-[360px]:hidden" />
      <span className="hidden min-[360px]:inline">Vissza</span>
    </ButtonLink>
  )
}
