import React from 'react';
import {
  BookIcon,
  UsersIcon,
  IssueReturnIcon,
  FinesIcon,
  DashboardIcon,
  CloseIcon,
  GraduationCapIcon,
} from './Icons';

export default function Sidebar({
  activeTab,
  setActiveTab,
  mobileOpen,
  setMobileOpen,
  overdueCount = 32,
}) {
  const navItems = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      icon: DashboardIcon,
      badge: null,
      description: 'Overview & metrics',
    },
    {
      id: 'books',
      label: 'Books',
      icon: BookIcon,
      badge: '14.8k',
      description: 'Catalogue & inventory',
    },
    {
      id: 'members',
      label: 'Members',
      icon: UsersIcon,
      badge: '2.1k',
      description: 'Students & Faculty',
    },
    {
      id: 'circulation',
      label: 'Issue & Return',
      icon: IssueReturnIcon,
      badge: '546',
      description: 'Circulation desk',
    },
    {
      id: 'fines',
      label: 'Fines',
      icon: FinesIcon,
      badge: `${overdueCount}`,
      badgeVariant: 'warning',
      description: 'Penalties & dues',
    },
  ];

  const handleNavClick = (id) => {
    setActiveTab(id);
    if (mobileOpen) {
      setMobileOpen(false);
    }
  };

  return (
    <>
      {/* Mobile Backdrop Overlay */}
      {mobileOpen && (
        <div
          className="sidebar-backdrop"
          onClick={() => setMobileOpen(false)}
          aria-hidden="true"
        />
      )}

      <aside className={`sidebar ${mobileOpen ? 'open' : ''}`}>
        {/* Brand / Logo Section */}
        <div className="sidebar-brand">
          <div className="brand-logo-container">
            <div className="brand-icon-box">
              <BookIcon size={24} className="brand-icon" />
            </div>
            <div className="brand-text-group">
              <div className="brand-title-row">
                <span className="brand-title">BookNest</span>
                <span className="brand-badge">LMS</span>
              </div>
              <span className="brand-subtitle">College Library System</span>
            </div>
          </div>

          {/* Mobile close button */}
          <button
            type="button"
            className="mobile-close-btn"
            onClick={() => setMobileOpen(false)}
            aria-label="Close navigation menu"
          >
            <CloseIcon size={20} />
          </button>
        </div>

        {/* College Campus Info Pill */}
        <div className="campus-badge-box">
          <GraduationCapIcon size={16} className="campus-icon" />
          <div className="campus-text">
            <span className="campus-name">Central University Library</span>
            <span className="campus-session">Academic Year 2026-27</span>
          </div>
        </div>

        {/* Navigation Menu */}
        <div className="sidebar-nav-container">
          <div className="sidebar-section-title">Navigation</div>
          <nav className="sidebar-nav" aria-label="Main Navigation">
            {navItems.map((item) => {
              const IconComponent = item.icon;
              const isActive = activeTab === item.id;

              return (
                <button
                  key={item.id}
                  type="button"
                  id={`nav-item-${item.id}`}
                  className={`nav-link ${isActive ? 'active' : ''}`}
                  onClick={() => handleNavClick(item.id)}
                  aria-current={isActive ? 'page' : undefined}
                >
                  <span className="nav-icon-wrap">
                    <IconComponent size={19} />
                  </span>
                  <div className="nav-label-wrap">
                    <span className="nav-label">{item.label}</span>
                    <span className="nav-description">{item.description}</span>
                  </div>
                  {item.badge && (
                    <span
                      className={`nav-pill ${
                        item.badgeVariant === 'warning' ? 'pill-warning' : ''
                      }`}
                    >
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Sidebar Footer / Desk Hours Widget */}
        <div className="sidebar-footer">
          <div className="desk-status-card">
            <div className="desk-status-header">
              <span className="status-indicator online"></span>
              <span className="status-label">Circulation Desk Open</span>
            </div>
            <p className="desk-hours">Mon - Sat: 8:00 AM - 8:00 PM</p>
            <div className="demo-tag">
              <span>Frontend Demo • Spring Boot Ready</span>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
