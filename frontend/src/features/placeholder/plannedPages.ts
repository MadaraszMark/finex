// A még el nem készült oldalak tervezett tartalma (a frontend 2–5. része). Amikor egy oldal elkészül,
// a router a valódi oldalt tölti be helyette, és a bejegyzés innen törölhető.

export interface PlannedPage {
  part: number
  description: string
  features: string[]
}

export const plannedPages: Record<string, PlannedPage> = {
  '/accounts': {
    part: 2,
    description: 'Az összes számlád egy helyen, részletes kivonattal.',
    features: [
      'Számlák listája egyenleggel és devizanemmel',
      'Új forint-, euró- vagy dollárszámla nyitása',
      'Számlaadatlap egyenleg-grafikonnal',
      'Számlakivonat tetszőleges időszakra, futó egyenleggel',
      'Nulla egyenlegű számla lezárása',
    ],
  },
  '/cards': {
    part: 2,
    description: 'A bankkártyáid kezelése.',
    features: [
      'Élethű kártyakép maszkolt kártyaszámmal',
      'Tiltás és feloldás egy érintéssel',
      'Napi költési limit csúszkával, online fizetés kapcsolója',
      'Mai költés a limithez képest',
      'Demó kártyás fizetés',
    ],
  },
  '/transfer': {
    part: 3,
    description: 'Utalás lépésről lépésre.',
    features: [
      'Forrásszámla → címzett → összeg → áttekintés → nyugta',
      'Címzett a kedvezményezettek közül vagy IBAN-nal, gépelés közbeni ellenőrzéssel',
      'FineX-es címzettnél azonnali jóváírás',
      'Napi limit és fedezet ellenőrzése',
    ],
  },
  '/transactions': {
    part: 3,
    description: 'Minden pénzmozgásod kereshetően.',
    features: [
      'Szűrés időszakra, típusra, összegre, kategóriára és szövegre',
      'A szűrés a címsorban marad: megosztható, a vissza gombbal is működik',
      'Tétel részletei és kategorizálása',
    ],
  },
  '/beneficiaries': {
    part: 3,
    description: 'A gyakori címzettjeid.',
    features: ['Kedvezményezettek felvétele, módosítása és törlése', 'IBAN-ellenőrzés és FineX-es címzett jelölése', 'Utalás indítása egy érintéssel'],
  },
  '/standing-orders': {
    part: 3,
    description: 'Ismétlődő utalások (pl. albérlet, rezsi).',
    features: ['Heti vagy havi rendszeres átutalás', 'Következő esedékesség és utolsó teljesítés', 'Módosítás és megszüntetés'],
  },
  '/savings': {
    part: 4,
    description: 'Célok, amelyekre gyűjtesz.',
    features: [
      'Megtakarítási célok haladásjelzővel',
      'Befizetés és kivét a folyószámláról',
      'Havi kamatjóváírás és a mozgások listája',
      'Lezárás a teljes egyenleg kifizetésével',
    ],
  },
  '/statistics': {
    part: 4,
    description: 'Mire megy el a pénzed?',
    features: ['Havi bevétel és kiadás oszlopdiagramon', 'Költés kategóriánként kördiagramon', 'Időszak- és devizaválasztó'],
  },
  '/notifications': {
    part: 5,
    description: 'Minden fontos esemény egy helyen.',
    features: ['Beérkező utalások, biztonsági és ügyfélszolgálati értesítések', 'Olvasottnak jelölés egyenként vagy egyszerre'],
  },
  '/support': {
    part: 5,
    description: 'Kérdésed van? Írj nekünk.',
    features: ['Új ticket nyitása', 'Beszélgetés az ügyintézővel csevegő nézetben', 'A ticketek állapotának követése'],
  },
  '/profile': {
    part: 5,
    description: 'Személyes adatok és biztonság.',
    features: ['Személyes adatok módosítása', 'Jelszócsere', 'Belépési napló: eszköz, IP-cím, sikeres és sikertelen próbálkozások'],
  },
  '/admin': {
    part: 5,
    description: 'A bank működése egy pillantással.',
    features: ['Felhasználók, számlák, betétállomány és mai forgalom', 'Nyitott ticketek, sikertelen belépések', 'Ütemezett feladatok kézi indítása'],
  },
  '/admin/users': {
    part: 5,
    description: 'Ügyfelek keresése és kezelése.',
    features: ['Keresés névre vagy e-mail címre', 'Ügyfél teljes áttekintése (számlák, kártyák, megtakarítások)', 'Letiltás és szerepkör-módosítás'],
  },
  '/admin/accounts': {
    part: 5,
    description: 'Számlák állapotának kezelése.',
    features: ['Számlák szűrése állapot szerint', 'Befagyasztás, tiltás, feloldás, lezárás'],
  },
  '/admin/support-tickets': {
    part: 5,
    description: 'Ügyfélszolgálati ticketek.',
    features: ['Ticketek szűrése állapot szerint', 'Válasz az ügyfélnek, állapot módosítása'],
  },
  '/admin/login-logs': {
    part: 5,
    description: 'Biztonsági napló.',
    features: ['Belépések szűrése állapot, felhasználó, IP-cím és időszak szerint'],
  },
  '/admin/categories': {
    part: 5,
    description: 'Tranzakciókategóriák karbantartása.',
    features: ['Új kategória, átnevezés, ikon', 'Használatban lévő kategória nem törölhető'],
  },
}
