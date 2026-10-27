import type { ComponentProps, ReactNode } from 'react'
import { cn } from '../../lib/cn'

// Admin táblázatok: keskeny képernyőn vízszintesen görgethetők

export function Table({ caption, children, minWidth = '44rem' }: { caption: string; children: ReactNode; minWidth?: string }) {
  return (
    <div className="-mx-5 overflow-x-auto px-5 sm:-mx-6 sm:px-6">
      <table className="w-full text-sm" style={{ minWidth }}>
        <caption className="sr-only">{caption}</caption>
        {children}
      </table>
    </div>
  )
}

export function Th({ className, ...props }: ComponentProps<'th'>) {
  return <th scope="col" className={cn('border-b border-border py-3 pr-4 text-left text-xs font-semibold tracking-wide text-muted-foreground uppercase last:pr-0', className)} {...props} />
}

export function Td({ className, ...props }: ComponentProps<'td'>) {
  return <td className={cn('py-3 pr-4 align-middle last:pr-0', className)} {...props} />
}

export function Tr({ className, ...props }: ComponentProps<'tr'>) {
  return <tr className={cn('border-b border-border transition-colors last:border-0 hover:bg-muted/50', className)} {...props} />
}
