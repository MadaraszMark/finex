import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

// Osztálynevek összefűzése feltételekkel; ütköző Tailwind-osztályoknál a később megadott nyer
// (pl. cn('px-4', 'px-6') → 'px-6'), így a komponensek stílusa kívülről felülírható
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs))
}
