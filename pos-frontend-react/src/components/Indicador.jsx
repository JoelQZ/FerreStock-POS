export default function Indicador({ etiqueta, valor }) {
  return (
    <div className="indicador">
      <span>{etiqueta}</span>
      <strong>{valor}</strong>
    </div>
  )
}
