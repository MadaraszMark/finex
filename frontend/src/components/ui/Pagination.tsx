import { ChevronLeft, ChevronRight } from 'lucide-react'
import Button from './Button'

interface PaginationProps {
  // 0-tól számozott oldal (a Spring Data lapozásával egyezően)
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

// Lapozó a táblázatok alá: előző / következő, köztük az aktuális oldal
export default function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) {
    return null
  }

  return (
    <nav aria-label="Lapozás" className="flex items-center justify-between gap-3">
      <Button variant="outline" size="sm" leftIcon={<ChevronLeft />} disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
        Előző
      </Button>
      <span className="text-sm text-muted-foreground">
        {page + 1}. oldal / {totalPages}
      </span>
      <Button variant="outline" size="sm" rightIcon={<ChevronRight />} disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
        Következő
      </Button>
    </nav>
  )
}
