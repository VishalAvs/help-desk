import { Link } from "react-router-dom";

function Home() {
  return (
    <div>
      <h1>Help Desk System</h1>
      <Link to="/tickets">View Tickets</Link>
    </div>
  );
}

export default Home;
