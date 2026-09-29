// Az üveghatású kártyafelület (mint a belépő oldal űrlapja): a lila fejlécsávra "rácsúszva" finoman áttetszik.
// Külön szövegként is elérhető, így ugyanígy néz ki a Card, egy kattintható csempe (Link) és egy animált elem is.
// Animált elemnél az áttetszőséget magán a kártyán kell animálni, nem egy szülő elemen, különben az elmosás
// (backdrop-blur) az animáció végéig nem látszik.
export const cardSurface =
  'rounded-3xl border border-white/70 bg-card/90 shadow-card backdrop-blur-xl dark:border-white/10 dark:bg-card/80'
