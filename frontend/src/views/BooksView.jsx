import React, { useState } from 'react';
import {
  PlusIcon,
  BookmarkIcon,
  RefreshIcon,
  AlertCircleIcon,
  CheckCircleIcon,
  BookIcon,
  DatabaseIcon,
  CloseIcon,
  EditIcon,
  TrashIcon,
} from '../components/Icons';

export default function BooksView({
  books = [],
  loading = false,
  error = null,
  onRefresh,
  onInspectBook,
  onEditBook,
  onDeleteBook,
  searchQuery = '',
  onAddBookSubmit,
}) {
  const [statusFilter, setStatusFilter] = useState('All');
  const [showInlineForm, setShowInlineForm] = useState(false);

  // Inline form state
  const [formData, setFormData] = useState({
    id: '',
    title: '',
    author: '',
    totalCopies: 5,
    availableCopies: '',
  });
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [formSuccess, setFormSuccess] = useState(null);

  // Filter books based on search query and status
  const filteredBooks = books.filter((book) => {
    // Status filter
    const isOutOfStock = book.availableCopies === 0;
    const isLow = book.availableCopies <= 2 && !isOutOfStock;
    const isAvailable = book.availableCopies > 2;

    if (statusFilter === 'Available' && !isAvailable) return false;
    if (statusFilter === 'Limited' && !isLow) return false;
    if (statusFilter === 'Out of Stock' && !isOutOfStock) return false;

    // Search query filter
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase().trim();
    const idMatch = String(book.id).includes(q);
    const titleMatch = book.title && book.title.toLowerCase().includes(q);
    const authorMatch = book.author && book.author.toLowerCase().includes(q);
    return idMatch || titleMatch || authorMatch;
  });

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setFormSuccess(null);

    const idNum = parseInt(formData.id, 10);
    const copiesNum = parseInt(formData.totalCopies, 10);

    if (isNaN(idNum) || idNum <= 0) {
      setFormError('Book ID must be a positive number (e.g. 101).');
      return;
    }
    if (!formData.title || !formData.title.trim()) {
      setFormError('Book title cannot be blank.');
      return;
    }
    if (!formData.author || !formData.author.trim()) {
      setFormError('Book author cannot be blank.');
      return;
    }
    if (isNaN(copiesNum) || copiesNum <= 0) {
      setFormError('Total copies count must be greater than zero.');
      return;
    }

    let availNum = copiesNum;
    if (formData.availableCopies !== undefined && formData.availableCopies !== '') {
      availNum = parseInt(formData.availableCopies, 10);
      if (isNaN(availNum) || availNum <= 0) {
        setFormError('Available copies must be greater than zero.');
        return;
      }
      if (availNum > copiesNum) {
        setFormError('Available copies cannot exceed total copies.');
        return;
      }
    }

    try {
      setFormSubmitting(true);
      await onAddBookSubmit({
        id: idNum,
        title: formData.title.trim(),
        author: formData.author.trim(),
        totalCopies: copiesNum,
        availableCopies: availNum,
      });

      setFormSuccess(`Book #${idNum} "${formData.title.trim()}" added to MySQL database!`);
      setFormData({ id: '', title: '', author: '', totalCopies: 5, availableCopies: '' });

      // Auto clear success notice after 5 seconds
      setTimeout(() => setFormSuccess(null), 5000);
    } catch (err) {
      setFormError(err.message || 'Failed to add book. Please check backend response.');
    } finally {
      setFormSubmitting(false);
    }
  };

  const totalHoldingCopies = books.reduce((sum, b) => sum + (b.totalCopies || 0), 0);
  const totalAvailableCopies = books.reduce((sum, b) => sum + (b.availableCopies || 0), 0);

  return (
    <div className="tab-view-container">
      {/* Top Header Controls Bar */}
      <div className="view-header-bar">
        <div className="dept-filter-scroll">
          {['All', 'Available', 'Limited', 'Out of Stock'].map((status) => (
            <button
              key={status}
              type="button"
              className={`filter-btn ${statusFilter === status ? 'active' : ''}`}
              onClick={() => setStatusFilter(status)}
            >
              {status}
            </button>
          ))}
        </div>

        <div className="view-actions-cluster">
          <button
            type="button"
            className="btn btn-secondary btn-refresh"
            onClick={onRefresh}
            disabled={loading}
            title="Reload books from backend API"
          >
            <RefreshIcon size={16} className={loading ? 'spinning-icon' : ''} />
            <span>{loading ? 'Refreshing...' : 'Refresh'}</span>
          </button>

          <button
            type="button"
            id="toggle-add-book-form-btn"
            className={`btn ${showInlineForm ? 'btn-secondary' : 'btn-primary'}`}
            onClick={() => {
              setShowInlineForm(!showInlineForm);
              setFormError(null);
            }}
          >
            {showInlineForm ? (
              <>
                <CloseIcon size={16} />
                <span>Close Form</span>
              </>
            ) : (
              <>
                <PlusIcon size={16} />
                <span>Add New Book</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Inline Form to Add a Book */}
      {showInlineForm && (
        <div className="card inline-add-book-card" id="add-book-inline-section">
          <div className="card-header-flex">
            <div>
              <h3 className="card-heading">Add Book to MySQL Catalogue</h3>
              <p className="card-subheading">
                Sends JSON payload directly to Spring Boot endpoint <code>POST /api/books</code>
              </p>
            </div>
            <span className="live-api-tag">
              <span className="live-pulse-dot"></span>
              Live Backend API
            </span>
          </div>

          {formError && (
            <div className="form-error-alert" role="alert">
              <AlertCircleIcon size={18} />
              <div className="alert-text-wrap">
                <strong>API Validation Error:</strong> {formError}
              </div>
            </div>
          )}

          {formSuccess && (
            <div className="form-success-alert" role="alert">
              <CheckCircleIcon size={18} />
              <div className="alert-text-wrap">{formSuccess}</div>
            </div>
          )}

          <form onSubmit={handleFormSubmit} className="inline-book-form">
            <div className="form-grid-5">
              <div className="form-group">
                <label htmlFor="inline-book-id" className="form-label">
                  Book ID (Number) <span className="text-required">*</span>
                </label>
                <input
                  id="inline-book-id"
                  type="number"
                  min="1"
                  step="1"
                  className="form-input mono"
                  placeholder="e.g. 104"
                  value={formData.id}
                  onChange={(e) => setFormData({ ...formData, id: e.target.value })}
                  disabled={formSubmitting}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="inline-book-title" className="form-label">
                  Book Title <span className="text-required">*</span>
                </label>
                <input
                  id="inline-book-title"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Modern Operating Systems"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  disabled={formSubmitting}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="inline-book-author" className="form-label">
                  Author(s) <span className="text-required">*</span>
                </label>
                <input
                  id="inline-book-author"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Andrew S. Tanenbaum"
                  value={formData.author}
                  onChange={(e) => setFormData({ ...formData, author: e.target.value })}
                  disabled={formSubmitting}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="inline-book-copies" className="form-label">
                  Total Copies <span className="text-required">*</span>
                </label>
                <input
                  id="inline-book-copies"
                  type="number"
                  min="1"
                  className="form-input"
                  placeholder="e.g. 6"
                  value={formData.totalCopies}
                  onChange={(e) => setFormData({ ...formData, totalCopies: e.target.value })}
                  disabled={formSubmitting}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="inline-book-avail-copies" className="form-label">
                  Available Copies <span className="text-muted">(Optional)</span>
                </label>
                <input
                  id="inline-book-avail-copies"
                  type="number"
                  min="1"
                  className="form-input"
                  placeholder="Defaults to total"
                  value={formData.availableCopies}
                  onChange={(e) => setFormData({ ...formData, availableCopies: e.target.value })}
                  disabled={formSubmitting}
                />
              </div>
            </div>

            <div className="form-actions-row">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setFormData({ id: '', title: '', author: '', totalCopies: 5, availableCopies: '' });
                  setFormError(null);
                }}
                disabled={formSubmitting}
              >
                Clear
              </button>
              <button
                type="submit"
                id="submit-create-book-btn"
                className="btn btn-primary"
                disabled={formSubmitting}
              >
                {formSubmitting ? (
                  <>
                    <span className="btn-spinner"></span>
                    <span>Saving to Database...</span>
                  </>
                ) : (
                  <>
                    <PlusIcon size={16} />
                    <span>Create Book Record</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Book Catalog Table Card */}
      <div className="card">
        <div className="card-header-flex">
          <div>
            <h2 className="card-heading">Catalogue Stack Inventory</h2>
            <span className="card-subheading">
              Showing {filteredBooks.length} of {books.length} book titles in MySQL library holdings
            </span>
          </div>

          <div className="header-pills-group">
            <span className="stock-pill stock-ok" title="Total physical copies across all titles">
              Holdings: {totalHoldingCopies} Copies ({totalAvailableCopies} Available)
            </span>
            <span className="stock-pill stock-backend" title="Connected backend">
              <DatabaseIcon size={13} />
              <span>MySQL Live</span>
            </span>
          </div>
        </div>

        {/* Global Connection / API Error Banner */}
        {error && (
          <div className="api-error-card" role="alert">
            <div className="api-error-icon-box">
              <AlertCircleIcon size={24} />
            </div>
            <div className="api-error-content">
              <h4 className="api-error-title">Backend Connection Issue</h4>
              <p className="api-error-message">{error}</p>
              <div className="api-error-hint">
                Check that Spring Boot is running on <code>http://localhost:8080</code> with <code>$env:DB_PASSWORD</code> set.
              </div>
            </div>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={onRefresh}
            >
              Retry Connection
            </button>
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="api-loading-state" role="status">
            <div className="loading-spinner"></div>
            <h4 className="loading-title">Loading Catalogue from Database...</h4>
            <p className="loading-subtitle">
              Fetching records from <code>GET http://localhost:8080/api/books</code>
            </p>
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && filteredBooks.length === 0 && (
          <div className="api-empty-state">
            <div className="empty-icon-box">
              <BookIcon size={36} />
            </div>
            <h3 className="empty-state-title">
              {books.length === 0 ? 'No Books in Library Database' : 'No Books Match Search Filter'}
            </h3>
            <p className="empty-state-description">
              {books.length === 0
                ? 'The MySQL books table currently has no records. Add your first textbook using the form above.'
                : `No book titles, authors, or IDs matched "${searchQuery}". Try a different keyword.`}
            </p>
            {books.length === 0 && !showInlineForm && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setShowInlineForm(true)}
              >
                <PlusIcon size={16} />
                <span>Add First Book</span>
              </button>
            )}
          </div>
        )}

        {/* Data Table */}
        {!loading && !error && filteredBooks.length > 0 && (
          <div className="activity-table-wrapper">
            <table className="library-table" aria-label="Book Inventory">
              <thead>
                <tr>
                  <th scope="col" style={{ width: '90px' }}>Book ID</th>
                  <th scope="col">Book Title & Details</th>
                  <th scope="col">Author(s)</th>
                  <th scope="col">Copies (Available / Total)</th>
                  <th scope="col">Availability</th>
                  <th scope="col" className="text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredBooks.map((book) => {
                  const isOutOfStock = book.availableCopies === 0;
                  const isLow = book.availableCopies <= 2 && !isOutOfStock;

                  return (
                    <tr key={book.id}>
                      <td>
                        <span className="book-id-badge mono">#{book.id}</span>
                      </td>
                      <td>
                        <div className="book-cell">
                          <span className="book-title-main">{book.title}</span>
                          <span className="book-author-sub">ID: #{book.id}</span>
                        </div>
                      </td>
                      <td>
                        <span className="author-name-text">{book.author}</span>
                      </td>
                      <td>
                        <span className="copies-count font-semibold">
                          {book.availableCopies} <span className="copies-separator">/</span> {book.totalCopies}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`status-badge ${
                            isOutOfStock ? 'badge-red' : isLow ? 'badge-amber' : 'badge-green'
                          }`}
                        >
                          {isOutOfStock ? 'Out of Stock' : isLow ? 'Limited Copies' : 'Available'}
                        </span>
                      </td>
                      <td className="text-right">
                        <div className="table-actions-inline">
                          <button
                            type="button"
                            className="table-action-btn"
                            onClick={() => onInspectBook(book)}
                            title="Inspect book details"
                          >
                            <BookmarkIcon size={14} />
                            <span>Inspect</span>
                          </button>
                          <button
                            type="button"
                            className="table-action-btn btn-edit-book"
                            onClick={() => onEditBook && onEditBook(book)}
                            title={`Edit ${book.title}`}
                          >
                            <EditIcon size={14} />
                            <span>Edit</span>
                          </button>
                          <button
                            type="button"
                            className="table-action-btn btn-delete-book"
                            onClick={() => onDeleteBook && onDeleteBook(book)}
                            title={`Delete ${book.title}`}
                          >
                            <TrashIcon size={14} />
                            <span>Delete</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <div className="card-footer-note">
          <span className="data-live-dot"></span>
          <span>
            Connected to Spring Boot REST API: <code>/api/books</code> • Backed by MySQL <code>books</code> table with JdbcTemplate
          </span>
        </div>
      </div>
    </div>
  );
}
