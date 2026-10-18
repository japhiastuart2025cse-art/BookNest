import React, { useState } from 'react';
import { PlusIcon, CheckCircleIcon, RefreshIcon, AlertCircleIcon } from '../components/Icons';

export default function CirculationView({
  circulation = [],
  loading = false,
  error = null,
  onRefresh,
  onOpenQuickIssue,
  onReturnBook,
  searchQuery = '',
}) {
  const [statusFilter, setStatusFilter] = useState('All');
  const [actionLoadingId, setActionLoadingId] = useState(null);

  const filteredLoans = circulation.filter((item) => {
    const isReturned = item.status === 'RETURNED' || Boolean(item.returnDate);
    const isOverdue = (item.daysOverdue > 0 || item.status === 'Overdue') && !isReturned;

    if (statusFilter === 'Active' && isReturned) return false;
    if (statusFilter === 'Overdue' && (!isOverdue || isReturned)) return false;
    if (statusFilter === 'Returned' && !isReturned) return false;

    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    const title = (item.bookTitle || '').toLowerCase();
    const student = (item.memberName || item.studentName || '').toLowerCase();
    const id = String(item.issueId || item.id || '').toLowerCase();
    const email = (item.memberEmail || item.rollNumber || '').toLowerCase();

    return title.includes(q) || student.includes(q) || id.includes(q) || email.includes(q);
  });

  const activeCount = circulation.filter((c) => c.status === 'ISSUED' && !c.returnDate).length;
  const overdueCount = circulation.filter((c) => (c.daysOverdue > 0 || c.status === 'Overdue') && c.status !== 'RETURNED').length;
  const returnedCount = circulation.filter((c) => c.status === 'RETURNED' || Boolean(c.returnDate)).length;

  const handleReturnClick = async (loan) => {
    const loanId = loan.issueId || loan.id;
    try {
      setActionLoadingId(loanId);
      await onReturnBook(loan);
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="tab-view-container">
      {error && (
        <div className="form-error-alert" role="alert" style={{ marginBottom: '16px' }}>
          <AlertCircleIcon size={18} />
          <span>{error}</span>
          {onRefresh && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={onRefresh} style={{ marginLeft: 'auto' }}>
              Retry
            </button>
          )}
        </div>
      )}

      <div className="view-header-bar">
        <div className="filter-pill-group">
          <button
            type="button"
            className={`filter-btn ${statusFilter === 'All' ? 'active' : ''}`}
            onClick={() => setStatusFilter('All')}
          >
            All Records ({circulation.length})
          </button>
          <button
            type="button"
            className={`filter-btn ${statusFilter === 'Active' ? 'active' : ''}`}
            onClick={() => setStatusFilter('Active')}
          >
            Active Loans ({activeCount})
          </button>
          <button
            type="button"
            className={`filter-btn ${statusFilter === 'Overdue' ? 'active' : ''}`}
            onClick={() => setStatusFilter('Overdue')}
          >
            Overdue Loans ({overdueCount})
          </button>
          <button
            type="button"
            className={`filter-btn ${statusFilter === 'Returned' ? 'active' : ''}`}
            onClick={() => setStatusFilter('Returned')}
          >
            Returned ({returnedCount})
          </button>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          {onRefresh && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onRefresh}
              title="Refresh ledger from MySQL"
              disabled={loading}
            >
              <RefreshIcon size={16} />
              <span>Refresh</span>
            </button>
          )}

          <button
            type="button"
            className="btn btn-primary"
            onClick={onOpenQuickIssue}
          >
            <PlusIcon size={16} />
            <span>Issue Book</span>
          </button>
        </div>
      </div>

      <div className="card">
        <div className="card-header-flex">
          <div>
            <h2 className="card-heading">Circulation Ledger</h2>
            <span className="card-subheading">Live loans and check-ins managed in MySQL database</span>
          </div>
          <span className="stock-pill stock-ok">Active On Loan: {activeCount} Books</span>
        </div>

        <div className="activity-table-wrapper">
          <table className="library-table">
            <thead>
              <tr>
                <th scope="col">Loan ID</th>
                <th scope="col">Book Title</th>
                <th scope="col">Borrower / Member</th>
                <th scope="col">Issue Date</th>
                <th scope="col">Due Date</th>
                <th scope="col">Return Date</th>
                <th scope="col">Status</th>
                <th scope="col">Fine</th>
                <th scope="col" className="text-right">Desk Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="9" className="text-center py-6 text-muted">
                    <span className="btn-spinner" style={{ display: 'inline-block', marginRight: '8px' }}></span>
                    Loading circulation records from Spring Boot...
                  </td>
                </tr>
              ) : filteredLoans.length === 0 ? (
                <tr>
                  <td colSpan="9" className="text-center py-6 text-muted">
                    {searchQuery
                      ? 'No circulation records match your search criteria.'
                      : 'No circulation records recorded yet. Click "Issue Book" to issue a book to a registered member.'}
                  </td>
                </tr>
              ) : (
                filteredLoans.map((loan) => {
                  const id = loan.issueId || loan.id;
                  const isReturned = loan.status === 'RETURNED' || Boolean(loan.returnDate);
                  const isOverdue = (loan.daysOverdue > 0 || loan.status === 'Overdue') && !isReturned;
                  const borrower = loan.memberName || loan.studentName || `Member #${loan.memberId}`;
                  const borrowerSub = loan.memberEmail || loan.rollNumber || loan.memberDepartment || `ID #${loan.memberId}`;
                  const issueDate = loan.issueDate || loan.issuedDate || '—';
                  const dueDate = loan.dueDate || '—';
                  const returnDate = loan.returnDate || '—';
                  const fine = loan.fineAmount !== undefined ? loan.fineAmount : 0;
                  const isActing = actionLoadingId === id;

                  return (
                    <tr key={id}>
                      <td>
                        <span className="member-id-pill mono">#{id}</span>
                      </td>
                      <td>
                        <div className="book-cell">
                          <span className="book-title-main">{loan.bookTitle}</span>
                          {loan.bookAuthor && (
                            <span className="book-isbn-sub">By {loan.bookAuthor}</span>
                          )}
                        </div>
                      </td>
                      <td>
                        <div className="member-cell">
                          <span className="member-name">{borrower}</span>
                          <span className="member-id-pill mono">{borrowerSub}</span>
                        </div>
                      </td>
                      <td>
                        <span className="text-slate text-sm">{issueDate}</span>
                      </td>
                      <td>
                        <div className="due-date-block">
                          <span className={`text-sm font-semibold ${isOverdue ? 'text-red' : 'text-slate'}`}>
                            {dueDate}
                          </span>
                          {isOverdue && (
                            <span className="overdue-tag">
                              {loan.daysOverdue} days late
                            </span>
                          )}
                        </div>
                      </td>
                      <td>
                        <span className="text-slate text-sm">{returnDate}</span>
                      </td>
                      <td>
                        <span
                          className={`status-badge ${
                            isReturned
                              ? 'badge-green'
                              : isOverdue
                              ? 'badge-red'
                              : 'badge-blue'
                          }`}
                        >
                          {isReturned ? 'Returned' : isOverdue ? 'Overdue' : 'On Loan'}
                        </span>
                      </td>
                      <td>
                        {fine > 0 ? (
                          <span className="fine-amount font-semibold text-red">₹{fine}</span>
                        ) : (
                          <span className="text-muted text-xs">₹0.00</span>
                        )}
                      </td>
                      <td className="text-right">
                        {!isReturned ? (
                          <button
                            type="button"
                            className="table-action-btn"
                            onClick={() => handleReturnClick(loan)}
                            disabled={isActing}
                            title="Return this book and restock available inventory"
                          >
                            {isActing ? (
                              <span className="btn-spinner"></span>
                            ) : (
                              <>
                                <CheckCircleIcon size={14} />
                                <span>Check In</span>
                              </>
                            )}
                          </button>
                        ) : (
                          <span className="text-green text-xs font-semibold">✓ Completed</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        <div className="card-footer-note">
          <span className="data-disclaimer-dot" style={{ background: 'var(--brand-primary, #2563EB)' }}></span>
          <span>Records stored in MySQL <code>circulation</code> table. Returns automatically restock available book copies.</span>
        </div>
      </div>
    </div>
  );
}
