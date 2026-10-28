export default function TituloTarjeta({ titulo, detalle }) {
  return (
    <div className="tarjeta__cabecera">
      <h2>{titulo}</h2>
      {detalle && <span>{detalle}</span>}
    </div>
  )
}
