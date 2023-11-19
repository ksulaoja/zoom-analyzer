import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import './styles.css';

export const ZoomForm = () => {
  const [formData, setFormData] = useState({
    meetingId: '',
    meetingPw: '',
    startTime: '',
    recordingLength: 5,
    userEmail: ''
  });
  const [showPw, setShowPw] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleMeetingIdChange = (e) => {
    handleChange(e);
    let isnum = /^\d+$/.test(e.target.value);
    setShowPw(isnum);
  }

  const handleSubmit = (e) => {
    e.preventDefault();

    let date = new Date(formData.startTime);
    let isoDate = date.toISOString();
    formData.startTime = isoDate;
    console.log('Form Data:', formData);

    const requestOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData)
    };

    fetch('http://localhost:8080/recordings', requestOptions)
    .then(response => response.json())
    .then(data => navigate(`/recordings/recording/${data.id}?token=${data.token}`));
  };

  return (
    <div className='form'>
      <h1>Zoom Analyzer</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Meeting ID / URL:</label>
          <input
            type="text"
            name="meetingId"
            value={formData.meetingId}
            onChange={handleMeetingIdChange}
            required
          />
        </div>
        {showPw && <div>
          <label>Meeting Password:</label>
          <input
            type="text"
            name="meetingPw"
            value={formData.meetingPw}
            onChange={handleChange}
            required
          />
        </div>}
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
          <p className='sliderValue'>{formData.recordingLength}</p>
          <input
            type="range"
            min={5}
            max={90}
            name="recordingLength"
            value={formData.recordingLength}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Email:</label>
          <input
            type="text"
            name="userEmail"
            value={formData.userEmail}
            onChange={handleChange}
            required
          />
        </div>
        <p>A link containing the status and the recording will be sent to your email. You will be notified via email if the recording fails.</p>
        <button type="submit">Send Data</button>
      </form>
    </div>
  );
}

export default ZoomForm;
