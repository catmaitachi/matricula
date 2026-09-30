// Aplica o tema guardado antes da primeira pintura (evita piscar). Só uma preferência visual: nunca sessão nem token.
try {
  var t = localStorage.getItem('matricula.tema')
  if (t === 'claro' || t === 'escuro') document.documentElement.dataset.tema = t
} catch {
  /* sem armazenamento: vale o tema do sistema */
}
