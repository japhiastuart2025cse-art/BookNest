import React from 'react';
import {
  BookIcon,
  UsersIcon,
  IssueReturnIcon,
  OverdueIcon,
  TrendingUpIcon,
} from './Icons';

export default function StatCard({ stat, onClick }) {
  const getIcon = (type) => {
    switch (type) {
      case 'book':
        return <BookIcon size={22} />;
      case 'users':
        return <UsersIcon size={22} />;
      case 'repeat':
        return <IssueReturnIcon size={22} />;
      case 'alert':
        return <OverdueIcon size={22} />;
      default:
        return <BookIcon size={22} />;
    }
  };

  return (
    <div
      className={`stat-card stat-${stat.id}`}
      onClick={onClick}
      role={onClick ? 'button' : 'region'}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={(e) => {
        if (onClick && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          onClick();
        }
      }}
    >
      <div className="stat-card-top">
        <div
          className="stat-icon-wrapper"
          style={{
            backgroundColor: stat.bgColor,
            color: stat.accentColor,
            borderColor: stat.borderColor,
          }}
        >
          {getIcon(stat.iconType)}
        </div>

        <div className={`stat-trend-pill trend-${stat.trend}`}>
          {stat.trend === 'up' && <TrendingUpIcon size={12} />}
          <span>{stat.change}</span>
        </div>
      </div>

      <div className="stat-card-body">
        <span className="stat-label">{stat.title}</span>
        <h3 className="stat-value">{stat.value}</h3>
        <p className="stat-subtext">{stat.subtext}</p>
      </div>

      <div className="stat-card-footer">
        <span className="stat-demo-note">Demo record count</span>
        <span className="stat-explore-arrow">View details →</span>
      </div>
    </div>
  );
}
