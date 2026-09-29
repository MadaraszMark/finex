import { Eye, EyeOff } from 'lucide-react'
import { useState } from 'react'
import TextField, { type TextFieldProps } from './TextField'

// Jelszómező a koncepcióképek "szem" ikonjával: megmutatja vagy elrejti a beírt jelszót
export default function PasswordField(props: Omit<TextFieldProps, 'type' | 'rightSlot'>) {
  const [visible, setVisible] = useState(false)

  return (
    <TextField
      {...props}
      type={visible ? 'text' : 'password'}
      rightSlot={
        <button
          type="button"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? 'Jelszó elrejtése' : 'Jelszó megjelenítése'}
          aria-pressed={visible}
          className="flex size-10 items-center justify-center rounded-xl text-muted-foreground transition hover:text-primary focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
        >
          {visible ? <EyeOff className="size-5" /> : <Eye className="size-5" />}
        </button>
      }
    />
  )
}
