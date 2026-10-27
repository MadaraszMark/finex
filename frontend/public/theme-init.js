// A téma még a React előtt beáll; külön fájlban, hogy a CSP (script-src 'self') engedje
;(function () {
  var preference = null
  try {
    preference = localStorage.getItem('finex.theme')
  } catch (e) {}
  var dark = preference === 'dark' || (preference !== 'light' && window.matchMedia('(prefers-color-scheme: dark)').matches)
  document.documentElement.classList.toggle('dark', dark)
  document.documentElement.style.colorScheme = dark ? 'dark' : 'light'
})()
