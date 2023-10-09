import React from 'react';
import { Link, Outlet } from "react-router-dom";
import './layout.css';

function Layout({ children }) {
  return (
    <>
      <div className='navbar'>
        <Link className='navbarItem' to="/">Home</Link>
        <Link className='navbarItem' to="/recordings">Recordings</Link>
      </div>
    <div className='container'>
      <Outlet />
    </div>
    </>
  );
}

export default Layout;