import React, { useState } from 'react';
import { CheckCircleIcon, RefreshIcon, AlertCircleIcon } from '../components/Icons';

export default function FinesView({
  fines = [],
  summary = null,
  loading = false,
  error = null,
  onRefresh,
  onOpenCollectFine,
  searchQuery = '',
}) {
  const [filterStatus, setFilterStatus] = useState('All');

  const filteredFines = fines.filter((fine) => {
    const isPaid = fine.status === 'PAID' || fine.status === 'Paid';
    const isPending = !isPaid;

    if (filterStatus === 'Pending' && !isPending) return false;
    if (filterStatus === 'Paid' && !isPaid) return false;

    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    const student = (fine.memberName || fine.studentName || '').toLowerCase();
    const roll = (fine.memberEmail || fine.rollNumber || '').toLowerCase();
    const book = (fine.bookTitle || '').toLowerCase();
    const id = String(fine.fineId || fine.id || '').toLowerCase();

    return student.includes(q) || roll.includes(q) || book.includes(q) || id.includes(q);
  });

  const pendingFines = fines.filter((f) => f.status === 'PENDING' || f.status === 'Pending');
  const paidFines = fines.filter((f) => f.status === 'PAID' || f.status === 'Paid');

  const pendingTotal = summary?.pendingTotal ?? pendingFines.reduce((sum, f) => sum + (Number(f.amount) || Number(f.totalFine) || 0), 0);
  const settledTotal = summary?.totalSettled ?? paidFines.reduce((sum, f) => sum + (Number(f.amount) || Number(f.totalFine) || 0), 0);
  const dailyRate = summary?.dailyRate ?? (fines[0]?.ratePerDay || 5.0);
  const defaultersCount = summary?.defaultersCount ?? pendingFines.length;

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

      {/* Mini Summary Strip for Fines */}
      <div className="fines-summary-strip">
        <div className="fine-metric-box">
          <span className="fine-metric-label">Pending Dues Total</span>
          <span className="fine-metric-val text-red">₹{pendingTotal.toFixed(2)}</span>
        </div>
        <div className="fine-metric-box">
          <span className="fine-metric-label">Daily Overdue Rate</span>
          <span className="fine-metric-val">₹{Number(dailyRate).toFixed(2)} / day</span>
        </div>
        <div className="fine-metric-box">
          <span className="fine-metric-label">Defaulters On Record</span>
          <span className="fine-metric-val text-amber">{defaultersCount} Members</span>
        </div>
        <div className="fine-metric-box">
          <span className="fine-metric-label">Total Fines Settled</span>
          <span className="fine-metric-val text-green">₹{settledTotal.toFixed(2)}</span>
        </div>
      </div>

      <div className="view-header-bar">
        <div className="filter-pill-group">
          <button
            type="button"
            className={`filter-btn ${filterStatus === 'All' ? 'active' : ''}`}
            onClick={() => setFilterStatus('All')}
          >
            All Records ({fines.length})
          </button>
          <button
            type="button"
            className={`filter-btn ${filterStatus === 'Pending' ? 'active' : ''}`}
            onClick={() => setFilterStatus('Pending')}
          >
            Pending ({pendingFines.length})
          </button>
          <button
            type="button"
            className={`filter-btn ${filterStatus === 'Paid' ? 'active' : ''}`}
            onClick={() => setFilterStatus('Paid')}
          >
            Settled / Paid ({paidFines.length})
          </button>
        </div>

        {onRefresh && (
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onRefresh}
            title="Refresh fines ledger from backend"
            disabled={loading}
          >
            <RefreshIcon size={16} />
            <span>Refresh</span>
          </button>
        )}
      </div>

      <div className="card">
        <div className="card-header-flex">
          <div>
            <h2 className="card-heading">Overdue Fine Ledger</h2>
            <span className="card-subheading">Standard college rate: ₹{dailyRate} per day for each day past due date</span>
          </div>
          <span className="stock-pill stock-ok">
            {pendingFines.length} Outstanding Accounts
          </span>
        </div>

        <div className="activity-table-wrapper">
          <table className="library-table">
            <thead>
              <tr>
                <th scope="col">Fine ID</th>
                <th scope="col">Borrower / Member</th>
                <th scope="col">Book Title</th>
                <th scope="col">Overdue Days</th>
                <th scope="col">Rate Calculation</th>
                <th scope="col">Total Fine</th>
                <th scope="col">Status</th>
                <th scope="col" className="text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-6 text-muted">
                    <span className="btn-spinner" style={{ display: 'inline-block', marginRight: '8px' }}></span>
                    Loading fine records from Spring Boot...
                  </td>
                </tr>
              ) : filteredFines.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center py-6 text-muted">
                    {searchQuery
                      ? 'No fine records match your filter criteria.'
                      : 'No overdue fines recorded. All books returned on time!'}
                  </td>
                </tr>
              ) : (
                filteredFines.map((fine) => {
                  const id = fine.fineId ?? fine.id;
                  const isPaid = fine.status === 'PAID' || fine.status === 'Paid';
                  const borrower = fine.memberName || fine.studentName || `Member #${fine.memberId}`;
                  const borrowerSub = fine.memberEmail || fine.rollNumber || `ID #${fine.memberId}`;
                  const amount = Number(fine.amount ?? fine.totalFine ?? 0);
                  const days = fine.daysOverdue ?? 0;
                  const rateStr = typeof fine.ratePerDay === 'number' ? `₹${fine.ratePerDay}` : (fine.ratePerDay || `₹${dailyRate}`);

                  return (
                    <tr key={id}>
                      <td>
                        <span className="member-id-pill mono">
                          {id < 0 ? `Unpersisted (#${-id})` : `#FINE-${id}`}
                        </span>
                      </td>
                      <td>
                        <div className="member-cell">
                          <span className="member-name">{borrower}</span>
                          <span className="member-id-pill mono">{borrowerSub}</span>
                        </div>
                      </td>
                      <td>
                        <span className="book-title-main">{fine.bookTitle}</span>
                      </td>
                      <td>
                        <span className="overdue-days-pill">
                          {days} Days Late
                        </span>
                      </td>
                      <td>
                        <span className="text-muted text-sm">
                          {days} × {rateStr}
                        </span>
                      </td>
                      <td>
                        <span className={`fine-amount font-semibold ${!isPaid ? 'text-red' : 'text-slate'}`}>
                          ₹{amount.toFixed(2)}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`status-badge ${
                            !isPaid ? 'badge-red' : 'badge-green'
                          }`}
                        >
                          {isPaid ? 'Paid' : 'Pending'}
                        </span>
                      </td>
                      <td className="text-right">
                        {!isPaid ? (
                          <button
                            type="button"
                            className="table-action-btn btn-collect-fine"
                            onClick={() => onOpenCollectFine(fine)}
                            title="Collect fine and record payment in database"
                          >
                            <CheckCircleIcon size={14} />
                            <span>Collect Fine</span>
                          </button>
                        ) : (
                          <span className="text-green text-xs font-semibold">
                            ✓ Cleared {fine.paidDate ? `(${fine.paidDate})` : ''}
                          </span>
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
          <span className="data-disclaimer-dot" style={{ background: 'var(--amber-500, #F59E0B)' }}></span>
          <span>Fine assessments and payments are persisted in the MySQL <code>fines</code> table.</span>
        </div>
      </div>
    </div>
  );
}
