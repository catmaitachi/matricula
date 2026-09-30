import type { LucideIcon, LucideProps } from 'lucide-react'

/** Ícone da lucide no estilo do projeto: traço reto (pontas quadradas, cantos vivos) e decorativo por padrão. */
export function Icone({ de: Desenho, ...resto }: { de: LucideIcon } & LucideProps) {
  return <Desenho size={22} strokeWidth={2.25} strokeLinecap="square" strokeLinejoin="miter" aria-hidden="true" {...resto} />
}
