import { Navigate, Route, Routes } from 'react-router-dom';
import PositionsPage from '../pages/PositionsPage';

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/positions" replace />} />
      <Route path="/positions" element={<PositionsPage />} />
    </Routes>
  );
}
