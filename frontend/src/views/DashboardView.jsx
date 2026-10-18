import React from 'react';
import StatCard from '../components/StatCard';
import RecentActivity from '../components/RecentActivity';
import PopularBooks from '../components/PopularBooks';
import { DEPARTMENT_STATS } from '../data/mockData';
import { PlusIcon, BookIcon, IssueReturnIcon, OverdueIcon } from '../components/Icons';

export default function DashboardView({
  stats,
  recentActivities,
  popularBooks,
  onNavigateTab,
  onQuickIssue,
  onAddBook,
  onQuickReturnDemo,
  onInspectBook,
  searchQuery,
}) {
  return (
    <div className="dashboard-content-flow">
      {/* 4 Summary Cards */}
      <section className="stats-grid" aria-label="Library key metrics">
        {stats.map((stat) => (
          <StatCard
            key={stat.id}
            stat={stat}
            onClick={() => {
              if (stat.id === 'total-books') onNavigateTab('books');
              else if (stat.id === 'registered-members') onNavigateTab('members');
              else if (stat.id === 'books-issued') onNavigateTab('circulation');
              else if (stat.id === 'overdue-books') onNavigateTab('fines');
            }}
          />
        ))}
      </section>

      {/* Quick Action Bar for Library Staff */}
      <section className="quick-actions-bar" aria-label="Quick operations">
        <div className="quick-actions-title-group">
          <span className="quick-action-label">Quick Desk Shortcuts</span>
          <span className="quick-action-sub">Frequently used workflows for circulation librarians</span>
        </div>

        <div className="quick-action-buttons">
          <button
            type="button"
            className="action-pill-btn"
            onClick={onQuickIssue}
          >
            <IssueReturnIcon size={16} />
            <span>Issue New Book</span>
          </button>

          <button
            type="button"
            className="action-pill-btn"
            onClick={onAddBook}
          >
            <PlusIcon size={16} />
            <span>Catalogue New Arrival</span>
          </button>

          <button
            type="button"
            className="action-pill-btn btn-warning-soft"
            onClick={() => onNavigateTab('fines')}
          >
            <OverdueIcon size={16} />
            <span>Review Overdue Dues (32)</span>
          </button>

          <button
            type="button"
            className="action-pill-btn"
            onClick={() => onNavigateTab('books')}
          >
            <BookIcon size={16} />
            <span>Browse All Stacks</span>
          </button>
        </div>
      </section>

      {/* Main Two-Column Layout: Recent Activity (left/wide) & Popular Books (right) */}
      <div className="dashboard-main-grid">
        <div className="grid-left-col">
          <RecentActivity
            activities={recentActivities}
            onQuickReturnDemo={onQuickReturnDemo}
            searchQuery={searchQuery}
          />
        </div>

        <div className="grid-right-col">
          <PopularBooks
            popularBooks={popularBooks}
            onInspectBook={onInspectBook}
          />

          {/* Department Distribution Mini-Card */}
          <div className="card department-distribution-card">
            <div className="card-header-flex">
              <div>
                <h3 className="card-heading text-sm">Department Circulation</h3>
                <span className="card-subheading">Active book usage by engineering school</span>
              </div>
            </div>

            <div className="dept-distribution-list">
              {DEPARTMENT_STATS.map((dept) => (
                <div key={dept.name} className="dept-stat-row">
                  <div className="dept-name-row">
                    <span className="dept-name">{dept.name}</span>
                    <span className="dept-counts">{dept.borrowed} on loan / {dept.books}</span>
                  </div>
                  <div className="dept-progress-track">
                    <div
                      className="dept-progress-fill"
                      style={{ width: `${dept.percentage}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>

            <div className="card-footer-note">
              <span className="data-disclaimer-dot"></span>
              <span>Updated from mock circulation ledger</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
