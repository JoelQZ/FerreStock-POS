import { Outlet } from 'react-router-dom'
import Cabecera from '../components/Cabecera'

export default function LayoutPos() {
  return (
    <div className="pos">
      <Cabecera />
      <Outlet />
    </div>
  )
}
