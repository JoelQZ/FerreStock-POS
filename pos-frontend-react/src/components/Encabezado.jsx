export default function Encabezado({ kicker, titulo, subtitulo, children }) {
  return (
    <section className="franja">
      <div>
        <p className="franja__kicker">{kicker}</p>
        <h1 className="franja__titulo">{titulo}</h1>
        {subtitulo && <p className="franja__subtitulo">{subtitulo}</p>}
      </div>
      {children && <div className="franja__indicadores">{children}</div>}
    </section>
  )
}
