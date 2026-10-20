import { Check, Copy } from 'lucide-react'
import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import type { IconButtonTone } from './buttonStyles'
import IconButton from './IconButton'

interface CopyButtonProps {
  value: string
  label: string
  successMessage: string
  tone?: IconButtonTone
  className?: string
}

// Vágólapra másolás (pl. számlaszám): sikerkor egy pillanatra pipára vált az ikon
export default function CopyButton({ value, label, successMessage, tone, className }: CopyButtonProps) {
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    if (!copied) {
      return
    }
    const timer = window.setTimeout(() => setCopied(false), 1600)
    return () => window.clearTimeout(timer)
  }, [copied])

  async function copy() {
    try {
      await navigator.clipboard.writeText(value)
      setCopied(true)
      toast.success(successMessage)
    } catch {
      toast.error('Nem sikerült a vágólapra másolni.')
    }
  }

  return (
    <IconButton label={label} tone={tone} onClick={copy} className={className}>
      {copied ? <Check /> : <Copy />}
    </IconButton>
  )
}
