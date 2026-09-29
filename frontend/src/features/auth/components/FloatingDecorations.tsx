import { motion, useTransform } from 'motion/react'
import { DotGrid, Waves } from '../../../components/brand/Decorations'
import type { PointerParallax } from '../../../hooks/usePointerParallax'

// A koncepcióképek díszítőelemei (hullámok, pöttyrács, körök) különböző "mélységben":
// az egér mozgására eltérő mértékben és irányban mozdulnak el, ettől térhatású a háttér
export default function FloatingDecorations({ pointer }: { pointer: PointerParallax }) {
  const nearX = useTransform(pointer.x, (value) => value * -60)
  const nearY = useTransform(pointer.y, (value) => value * -60)
  const farX = useTransform(pointer.x, (value) => value * 30)
  const farY = useTransform(pointer.y, (value) => value * 30)

  return (
    <div aria-hidden className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      {/* A nagyobb elemek mobilon rálógnának a címre, ezért csak nagyobb képernyőn jelennek meg */}
      <motion.div style={{ x: nearX, y: nearY }} className="absolute top-24 right-[6%] hidden w-44 text-white/40 md:block">
        <Waves strokeWidth={11} className="motion-safe:animate-float" />
      </motion.div>
      <motion.div style={{ x: farX, y: farY }} className="absolute top-[22%] left-[4%] hidden w-14 text-white/55 md:block">
        <Waves />
      </motion.div>
      <motion.div style={{ x: farX, y: farY }} className="absolute right-[32%] bottom-[10%] w-24 text-white/40">
        <DotGrid />
      </motion.div>
      <motion.div style={{ x: nearX, y: nearY }} className="absolute top-[58%] left-[3%] w-20 text-white/35">
        <DotGrid rows={4} columns={4} />
      </motion.div>

      {/* Körök: egy nagy, lassan forgó szaggatott gyűrű és két áttetsző korong */}
      <motion.div style={{ x: farX, y: farY }} className="absolute -bottom-40 -left-40 size-[26rem]">
        <div className="size-full rounded-full border-2 border-dashed border-white/15 motion-safe:animate-spin-slow" />
      </motion.div>
      <motion.div style={{ x: nearX, y: nearY }} className="absolute top-[12%] left-[38%] hidden size-24 rounded-full bg-white/10 md:block" />
      <motion.div style={{ x: farX, y: farY }} className="absolute right-[12%] bottom-[18%] size-14 rounded-full bg-white/10" />
    </div>
  )
}
