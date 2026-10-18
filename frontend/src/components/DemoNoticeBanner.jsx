import React, { useState } from 'react';
import { InfoIcon, CloseIcon } from './Icons';

export default function DemoNoticeBanner() {
  const [dismissed, setDismissed] = useState(false);

  if (dismissed) return null;

  return (
    <aside className="demo-notice-banner" role="region" aria-label="Demo environment notice">
      <div className="banner-icon-col">
        <InfoIcon size={20} className="banner-icon" />
      </div>

      <div className="banner-content">
        <div className="banner-headline">
          <strong>Interactive Demo Mode:</strong> Realistic sample data loaded for preview.
        </div>
        <p className="banner-description">
          This frontend dashboard demonstrates BookNest's interface, workflows, and responsive layout.
          Changes you make are kept in active memory. Full persistence with the Java Spring Boot + MySQL backend will be linked in upcoming milestones.
        </p>
      </div>

      <div className="banner-actions">
        <span className="banner-badge">Frontend v1.0.0</span>
        <button
          type="button"
          className="banner-close-btn"
          onClick={() => setDismissed(true)}
          aria-label="Dismiss banner"
        >
          <CloseIcon size={16} />
        </button>
      </div>
    </aside>
  );
}
