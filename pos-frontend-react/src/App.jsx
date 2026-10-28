import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthProvider'
import LayoutPos from './layouts/LayoutPos'
import CierreCaja from './pages/CierreCaja'
import Login from './pages/Login'
import MisVentas from './pages/MisVentas'
import NuevaVenta from './pages/NuevaVenta'
import RutaPrivada from './routes/RutaPrivada'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<RutaPrivada />}>
            <Route element={<LayoutPos />}>
              <Route index element={<Navigate to="/nueva-venta" replace />} />
              <Route path="nueva-venta" element={<NuevaVenta />} />
              <Route path="cierre-caja" element={<CierreCaja />} />
              <Route path="mis-ventas" element={<MisVentas />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
