// Az alkalmazás hátterének halvány, lassan úszó lila fényei (a fejlécsáv alatt is legyen némi élet,
// de a tartalom olvashatóságát ne zavarja)
export default function AmbientBackground() {
  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 -z-20 overflow-hidden">
      <div className="absolute top-[35%] -right-40 size-[36rem] rounded-full bg-brand-300/20 blur-3xl will-change-transform motion-safe:animate-aurora-2 dark:bg-brand-700/15" />
      <div className="absolute -bottom-48 left-[15%] size-[32rem] rounded-full bg-fuchsia-300/15 blur-3xl will-change-transform motion-safe:animate-aurora-3 dark:bg-fuchsia-800/10" />
    </div>
  )
}
