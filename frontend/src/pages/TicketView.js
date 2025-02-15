import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";

function TicketView() {
  const { id } = useParams();
  const [ticket, setTicket] = useState(null);

  useEffect(() => {
    axios.get(`http://localhost:8080/api/tickets/${id}`).then((response) => {
      setTicket(response.data);
    });
  }, [id]);

  return (
    <div>
      {ticket ? (
        <div>
          <h2>{ticket.title}</h2>
          <p>{ticket.description}</p>
          <p>Status: {ticket.status}</p>
        </div>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
}

export default TicketView;
