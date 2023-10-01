import React, { useState } from 'react';

function LoginForm() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const handleLogin = () => {
    setIsLoggedIn(true);
  };

  if (isLoggedIn) {
    console.log("Form");
  }

  return (
    <div>

    </div>
  );
}

export default LoginForm;