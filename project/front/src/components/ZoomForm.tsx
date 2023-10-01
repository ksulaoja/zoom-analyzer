import React, { useState } from 'react';
import './styles.css';

export const ZoomForm = () => {
  const [formData, setFormData] = useState({
    name: '',
    meetingId: '',
    meetingPassword: '',
    startTime: '',
    meetingLength: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Form Data:', formData);
  };

  return (
    <div className='form'>
      <h1>Zoom Analyzer</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Name:</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Meeting ID:</label>
          <input
            type="text"
            name="meetingId"
            value={formData.meetingId}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Meeting Password:</label>
          <input
            type="text"
            name="meetingPassword"
            value={formData.meetingPassword}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Starting Time:</label>
          <input
            type="datetime-local"
            name="startTime"
            value={formData.startTime}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Meeting Length (in minutes):</label>
          <input
            type="number"
            name="meetingLength"
            value={formData.meetingLength}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit">Send Data</button>
      </form>
    </div>
  );
}

export default ZoomForm;
