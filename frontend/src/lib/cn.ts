import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

// Osztálynevek feltételes összefűzése; ütköző Tailwind-osztálynál a később megadott nyer
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs))
}
