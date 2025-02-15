import { useEffect, useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

function Tickets() {
  const [tickets, setTickets] = useState([]);

  useEffect(() => {
    axios.get("http://localhost:8080/api/tickets").then((response) => {
      setTickets(response.data);
    });
  }, []);

  return (
    <div>
      <h1>Tickets</h1>
      {tickets.map((ticket) => (
        <div key={ticket.id}>
          <Link to={`/tickets/${ticket.id}`}>{ticket.title}</Link>
        </div>
      ))}
    </div>
  );
}

export default Tickets;
