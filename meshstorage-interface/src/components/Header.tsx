export default function Header() {
  return (
    <nav className="navbar navbar-light bg-white shadow-sm">
      <div className="container d-flex align-items-center">
        <span className="material-icons me-2">menu</span>
        <h5 className="mb-0 fw-bold">MeshStorage</h5>
        <div className="ms-auto">
          <span className="material-icons">account_circle</span>
        </div>
      </div>
    </nav>
  );
}
