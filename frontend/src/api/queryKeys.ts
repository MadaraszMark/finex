// A React Query gyorsítótár kulcsai egy helyen: így egy módosítás után pontosan tudjuk, mit kell újratölteni
// (pl. utalás után a számlákat és a tranzakciókat)
export const queryKeys = {
  me: ['users', 'me'] as const,
  cards: ['cards'] as const,
  unreadNotifications: ['notifications', 'unread-count'] as const,
}
