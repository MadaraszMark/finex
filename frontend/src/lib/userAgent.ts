// Eszközleírás a User-Agentből a belépési naplóhoz: "Chrome · Windows"

export function describeUserAgent(userAgent: string | null): string {
  if (!userAgent) {
    return 'Ismeretlen eszköz'
  }

  const browser = /Edg\//.test(userAgent)
    ? 'Edge'
    : /OPR\//.test(userAgent)
      ? 'Opera'
      : /Chrome\//.test(userAgent)
        ? 'Chrome'
        : /Firefox\//.test(userAgent)
          ? 'Firefox'
          : /Safari\//.test(userAgent)
            ? 'Safari'
            : /curl|Postman|Java|okhttp|Apache-HttpClient/i.test(userAgent)
              ? 'API-kliens'
              : 'Böngésző'

  const os = /Windows/.test(userAgent)
    ? 'Windows'
    : /Android/.test(userAgent)
      ? 'Android'
      : /iPhone|iPad/.test(userAgent)
        ? 'iOS'
        : /Mac OS X/.test(userAgent)
          ? 'macOS'
          : /Linux/.test(userAgent)
            ? 'Linux'
            : undefined

  return os ? `${browser} · ${os}` : browser
}

// A saját gépről érkező belépés (::1, 127.x) címe a naplóban "helyi gép"
export function describeIpAddress(ipAddress: string): string {
  return ipAddress === '0:0:0:0:0:0:0:1' || ipAddress === '::1' || ipAddress.startsWith('127.') ? 'helyi gép' : ipAddress
}

export function isMobileUserAgent(userAgent: string | null): boolean {
  return /Android|iPhone|iPad|Mobile/.test(userAgent ?? '')
}
