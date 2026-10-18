import React from 'react';
import { SearchIcon, MenuIcon, PlusIcon } from './Icons';

export default function Header({
  activeTab,
  searchQuery,
  setSearchQuery,
  setMobileOpen,
  onOpenQuickIssue,
  onOpenAddBook,
}) {
  const getTabTitle = () => {
    switch (activeTab) {
      case 'books':
        return {
          title: 'Book Catalogue & Inventory',
          subtitle: 'Search, filter, and inspect textbook stacks across departments',
        };
      case 'members':
        return {
          title: 'Registered Members Directory',
          subtitle: 'Manage active student, faculty, and research scholar accounts',
        };
      case 'circulation':
        return {
          title: 'Book Issue & Return Desk',
          subtitle: 'Process book loans, check-ins, and circulation records',
        };
      case 'fines':
        return {
          title: 'Overdue Dues & Fine Collections',
          subtitle: 'Track late return fees, overdue notices, and receipt history',
        };
      case 'dashboard':
      default:
        return {
          title: 'Library Operations Dashboard',
          subtitle: 'Real-time overview of college book loans, inventory, and activity',
        };
    }
  };

  const { title, subtitle } = getTabTitle();

  return (
    <header className="top-header">
      <div className="header-left">
        {/* Mobile Hamburger Button */}
        <button
          type="button"
          id="mobile-menu-toggle"
          className="hamburger-btn"
          onClick={() => setMobileOpen(true)}
          aria-label="Open navigation drawer"
        >
          <MenuIcon size={22} />
        </button>

        {/* Dynamic Titles */}
        <div className="header-title-box">
          <h1 className="header-page-title">{title}</h1>
          <p className="header-page-subtitle">{subtitle}</p>
        </div>
      </div>

      <div className="header-right">
        {/* Search Field */}
        <div className="search-wrapper">
          <SearchIcon size={17} className="search-icon" />
          <input
            type="text"
            id="global-search-input"
            className="search-input"
            placeholder="Search books, authors, ISBN, or student roll #..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            aria-label="Search library records"
          />
          {searchQuery && (
            <button
              type="button"
              className="search-clear-btn"
              onClick={() => setSearchQuery('')}
              aria-label="Clear search query"
            >
              ×
            </button>
          )}
        </div>

        {/* Header Action Buttons */}
        <div className="header-actions">
          <button
            type="button"
            id="quick-issue-btn"
            className="btn btn-primary"
            onClick={onOpenQuickIssue}
            title="Fast checkout demo workflow"
          >
            <PlusIcon size={16} />
            <span>Issue Book</span>
          </button>

          <button
            type="button"
            id="quick-add-book-btn"
            className="btn btn-secondary"
            onClick={onOpenAddBook}
            title="Add new textbook demo workflow"
          >
            <PlusIcon size={16} />
            <span className="btn-hide-mobile">Add Book</span>
          </button>

          {/* Librarian Profile Pill */}
          <div className="librarian-profile-pill">
            <div className="librarian-avatar">SJ</div>
            <div className="librarian-info">
              <span className="librarian-name">Prof. S. Jenkins</span>
              <span className="librarian-role">Chief Librarian</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
