import React, { useState } from 'react';
import { ClockIcon, CheckCircleIcon, OverdueIcon, IssueReturnIcon, FinesIcon } from './Icons';

export default function RecentActivity({ activities, onQuickReturnDemo, searchQuery = '' }) {
  const [filterType, setFilterType] = useState('all');

  const filteredActivities = activities.filter((item) => {
    const matchesFilter = filterType === 'all' || item.type === filterType;
    if (!matchesFilter) return false;

    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      item.studentName.toLowerCase().includes(q) ||
      item.memberId.toLowerCase().includes(q) ||
      item.bookTitle.toLowerCase().includes(q) ||
      item.isbn.toLowerCase().includes(q)
    );
  });

  const getActionIcon = (type) => {
    switch (type) {
      case 'issue':
        return <IssueReturnIcon size={14} className="act-icon text-blue" />;
      case 'return':
        return <CheckCircleIcon size={14} className="act-icon text-green" />;
      case 'overdue':
        return <OverdueIcon size={14} className="act-icon text-red" />;
      case 'fine':
        return <FinesIcon size={14} className="act-icon text-amber" />;
      default:
        return <ClockIcon size={14} className="act-icon text-slate" />;
    }
  };

  const getBadgeClass = (type) => {
    switch (type) {
      case 'issue':
        return 'badge-blue';
      case 'return':
        return 'badge-green';
      case 'overdue':
        return 'badge-red';
      case 'fine':
        return 'badge-amber';
      case 'renewal':
        return 'badge-purple';
      default:
        return 'badge-slate';
    }
  };

  return (
    <div className="card recent-activity-card">
      <div className="card-header-flex">
        <div className="card-title-group">
          <h2 className="card-heading">Recent Circulation Activity</h2>
          <span className="card-subheading">Live feed of book checkouts, check-ins, and renewals</span>
        </div>

        {/* Filter Pills */}
        <div className="filter-pill-group">
          <button
            type="button"
            className={`filter-btn ${filterType === 'all' ? 'active' : ''}`}
            onClick={() => setFilterType('all')}
          >
            All
          </button>
          <button
            type="button"
            className={`filter-btn ${filterType === 'issue' ? 'active' : ''}`}
            onClick={() => setFilterType('issue')}
          >
            Issues
          </button>
          <button
            type="button"
            className={`filter-btn ${filterType === 'return' ? 'active' : ''}`}
            onClick={() => setFilterType('return')}
          >
            Returns
          </button>
          <button
            type="button"
            className={`filter-btn ${filterType === 'overdue' ? 'active' : ''}`}
            onClick={() => setFilterType('overdue')}
          >
            Overdue
          </button>
        </div>
      </div>

      <div className="activity-list-container">
        {filteredActivities.length === 0 ? (
          <div className="empty-state">
            <p>No activity records match your current filter or search criteria.</p>
          </div>
        ) : (
          <div className="activity-table-wrapper">
            <table className="library-table activity-table">
              <thead>
                <tr>
                  <th scope="col">Student / Member</th>
                  <th scope="col">Action & Book</th>
                  <th scope="col">Due / Recorded</th>
                  <th scope="col">Status</th>
                  <th scope="col" className="text-right">Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredActivities.map((act) => (
                  <tr key={act.id} className="activity-row">
                    <td>
                      <div className="member-cell">
                        <span className="member-name">{act.studentName}</span>
                        <span className="member-id-pill">{act.memberId}</span>
                      </div>
                    </td>
                    <td>
                      <div className="book-cell">
                        <div className="book-action-line">
                          {getActionIcon(act.type)}
                          <span className="book-action-text">{act.action}</span>
                        </div>
                        <span className="book-title-sub">{act.bookTitle}</span>
                        <span className="book-isbn-sub">ISBN: {act.isbn}</span>
                      </div>
                    </td>
                    <td>
                      <div className="time-cell">
                        <span className="time-ago">{act.timestamp}</span>
                        <span className="due-date-sub">Due: {act.dueDate}</span>
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${getBadgeClass(act.type)}`}>
                        {act.badgeText}
                      </span>
                    </td>
                    <td className="text-right">
                      {act.type === 'issue' ? (
                        <button
                          type="button"
                          className="table-action-btn"
                          onClick={() => onQuickReturnDemo(act)}
                          title="Simulate check-in"
                        >
                          Check In
                        </button>
                      ) : (
                        <span className="text-muted text-xs">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card-footer-note">
        <span className="data-disclaimer-dot"></span>
        <span>Displaying realistic mock transaction feed. Real JDBC/MySQL logs will stream here.</span>
      </div>
    </div>
  );
}
