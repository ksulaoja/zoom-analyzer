import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ZoomForm from './components/ZoomForm.tsx';
import Layout from './components/Layout.tsx';
import Recording from './pages/Recording.tsx';

function App() {
  return (
    <BrowserRouter>
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<ZoomForm />} />
        <Route path="recordings" element={<ZoomForm />} />
        <Route path="recordings/recording/:id" element={<Recording />} />
      </Route>
    </Routes>
  </BrowserRouter>

  );
}

export default App;
