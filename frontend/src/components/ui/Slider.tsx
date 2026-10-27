import type { ComponentProps, CSSProperties } from 'react'
import { cn } from '../../lib/cn'

interface SliderProps extends Omit<ComponentProps<'input'>, 'type' | 'value' | 'onChange' | 'min' | 'max' | 'step'> {
  value: number
  min: number
  max: number
  step: number
  onValueChange: (value: number) => void
  // A felolvasó ezt mondja a nyers szám helyett (pl. "200 000 Ft")
  valueText?: string
}

// Natív range csúszka; a kitöltést a --slider-fill adja, a kinézet az index.css-ben van
export default function Slider({ value, min, max, step, onValueChange, valueText, className, style, ...props }: SliderProps) {
  const clamped = Math.min(Math.max(value, min), max)
  const percent = max > min ? ((clamped - min) / (max - min)) * 100 : 0

  return (
    <input
      type="range"
      min={min}
      max={max}
      step={step}
      value={clamped}
      onChange={(event) => onValueChange(Number(event.target.value))}
      aria-valuetext={valueText}
      style={{ ...style, '--slider-fill': `${percent}%` } as CSSProperties}
      className={cn('finex-range w-full', className)}
      {...props}
    />
  )
}
