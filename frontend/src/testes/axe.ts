import axe from 'axe-core'

/**
 * Falha se o axe achar problema de acessibilidade na parte da tela informada (rótulos, papéis ARIA, nomes de
 * controles, hierarquia de títulos, landmarks). O contraste não é testável em jsdom (não há layout nem cor real):
 * ele é garantido nos tokens do design (AA nos dois temas, conferido na fase 3).
 */
export async function semViolacoes(elemento: HTMLElement) {
  const resultado = await axe.run(elemento, {
    rules: { 'color-contrast': { enabled: false }, 'html-has-lang': { enabled: false }, region: { enabled: false } },
  })
  expect(resultado.violations.map((v) => `${v.id}: ${v.help} → ${v.nodes.map((n) => n.target.join(' ')).join(' | ')}`)).toEqual([])
}
