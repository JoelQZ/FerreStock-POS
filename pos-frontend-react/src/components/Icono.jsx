const TRAZOS = {
  salir: (
    <>
      <path d="M14 4h5a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-5" />
      <path d="m9 8-4 4 4 4M5 12h11" />
    </>
  ),
  buscar: (
    <>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4.5 4.5" />
    </>
  ),
  mas: <path d="M12 5v14M5 12h14" />,
  alerta: <path d="M12 3 2 20h20zM12 10v4M12 17v.5" />,
  check: <path d="m5 12 4.5 4.5L19 7" />,
  cerrar: <path d="M6 6l12 12M18 6 6 18" />,
  carrito: (
    <>
      <path d="M3 4h2.5l2.2 10.5a1.5 1.5 0 0 0 1.5 1.2h8.3a1.5 1.5 0 0 0 1.4-1.1L21 8H6.3" />
      <circle cx="10" cy="19.5" r="1.3" />
      <circle cx="17" cy="19.5" r="1.3" />
    </>
  ),
  billete: (
    <>
      <path d="M3 7h18v10H3z" />
      <circle cx="12" cy="12" r="2.5" />
      <path d="M6 10v4M18 10v4" />
    </>
  ),
  recibo: <path d="M6 3h12v18l-3-2-3 2-3-2-3 2zM9 8h6M9 12h6M9 16h3" />,
  menos: <path d="M5 12h14" />,
  tuerca: (
    <>
      <path d="M12 2.5 20.5 7.25v9.5L12 21.5 3.5 16.75v-9.5z" />
      <circle cx="12" cy="12" r="3.5" />
    </>
  ),
}

export default function Icono({ nombre, tamano = 18 }) {
  return (
    <svg
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {TRAZOS[nombre]}
    </svg>
  )
}
