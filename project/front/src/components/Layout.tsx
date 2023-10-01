import React from 'react';
import { Outlet } from "react-router-dom";
import './layout.css';

function Layout({ children }) {
  return (
    <div className='container'>
      <Outlet />
    </div>
  );
}

export default Layout;