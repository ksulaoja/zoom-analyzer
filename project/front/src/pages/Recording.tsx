import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import "./recording.css";

function Recording() {
  const [recordingMetadata, setRecordingMetadata] = useState({
    id: "",
    meetingId: "",
    meetingPw: null,
    startTime: "",
    recordingLength: null,
    userEmail: ""
  });
  const [recordingStatusData, setRecordingStatusData] = useState([{
    id: null,
    logLevel: "",
    message: "",
    time: "",
    recordingId: null
  }]);
  const requestOptions = {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  };
  const { id } = useParams();

  useEffect(() => {
    fetch(`http://localhost:8080/log/recorder/${id}`, requestOptions)
    .then(response => response.json())
    .then(data => setRecordingStatusData(data));

    fetch(`http://localhost:8080/recordings/${id}`, requestOptions)
    .then(response => response.json())
    .then(data => setRecordingMetadata(data));
  }, [])

  const formatDateTime = (dateString: string): string => {
    const dateObject = new Date(dateString);

    const year = dateObject.getFullYear();
    const month = String(dateObject.getMonth() + 1).padStart(2, '0');
    const day = String(dateObject.getDate()).padStart(2, '0');
    const hours = String(dateObject.getHours()).padStart(2, '0');
    const minutes = String(dateObject.getMinutes()).padStart(2, '0');
    const seconds = String(dateObject.getSeconds()).padStart(2, '0');

    const formattedDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

    return formattedDate;
  }

  return (
    <div className='wrapper'>
      <div className='table-container'>
        <h2>Recording metadata</h2>
        <table>
          <tbody>
            {Object.entries(recordingMetadata).map(([key, value]) => (
              <tr key={key}>
                <td>{key}</td>
                <td>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <a className='downloadLink' href={`http://localhost:8080/recordings/download/${id}`} target="_blank">Download video</a>
      </div>

      <div className='table-container'>
        <h2>Recording statuses</h2>
        <table>
          <thead>
            <tr>
              <th>Id</th>
              <th>LogLevel</th>
              <th>Message</th>
              <th>Time</th>
            </tr>
          </thead>
          <tbody>
            {recordingStatusData.map((status, index) => (
              <tr key={index}>
                <td>{status.id}</td>
                <td>{status.logLevel}</td>
                <td>{status.message}</td>
                <td>{formatDateTime(status.time)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Recording;