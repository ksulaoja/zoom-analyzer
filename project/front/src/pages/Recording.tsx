import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import "./recording.css";

function Recording() {
  const [recordingMetadata, setRecordingMetadata] = useState({
    id: "",
    meetingId: "",
    meetingPw: "",
    startTime: "",
    recordingLength: null,
    userEmail: "",
    token: ""
  });
  const [recordingStatusData, setRecordingStatusData] = useState([{
    id: null,
    logLevel: "",
    message: "",
    time: "",
    recordingId: null
  }]);
  const [latestStatus, setLatestStatus] = useState("UNKNOWN");
  const requestOptions = {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  };
  const { id, token } = useParams();

  useEffect(() => {
    fetch(`http://localhost:8080/log/recorder/${id}`, requestOptions)
    .then(response => response.json())
    .then(data => setRecordingStatusData(data));

    fetch(`http://localhost:8080/recordings/${id}`, requestOptions)
    .then(response => response.json())
    .then(data => setRecordingMetadata(data));

    fetch(`http://localhost:8080/log/recorder/${id}?level=STATUS`, requestOptions)
    .then(response => response.json())
    .then(data => {
      if (data.length != 0) {
        setLatestStatus(data[data.length - 1].message)
      }
    });
  }, [])

  const analyse = (): void => {
    fetch(`http://localhost:8080/recordings/analyze/${id}?token=${recordingMetadata.token}`, requestOptions)
    .then(response => response.json())
    .then(data => console.log(data));
  }


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
        <div className='header'>
          <h2>Meeting data</h2>
          <h3>Status: {latestStatus}</h3>

        </div>
        <table>
          <tbody>
            {Object.entries(recordingMetadata).map(([key, value]) => {
              if (key !== "token") {
                return (
                  <tr key={key}>
                    <td>{key}</td>
                    <td>{value}</td>
                  </tr>
                )
                }
              }
            )}
          </tbody>
        </table>
        <div className='buttonWrapper'>
          <a className={`downloadLink ${!["ENDED", "FAILED"].includes(latestStatus) && "downloadLink_disabled"}`} href={`http://localhost:8080/recordings/download/${id}`} target="_blank">Download video</a>
          <button onClick={analyse}>Analyse</button>
        </div>
      </div>

      <div className='table-container'>
        <h2>Log</h2>
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