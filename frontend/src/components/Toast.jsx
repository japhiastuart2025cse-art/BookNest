import React from 'react';
import { CheckCircleIcon, InfoIcon, CloseIcon } from './Icons';

export default function Toast({ toast, onClose }) {
  if (!toast) return null;

  return (
    <div className="toast-notification" role="status" aria-live="polite">
      <div className="toast-icon-box">
        {toast.type === 'info' ? <InfoIcon size={18} /> : <CheckCircleIcon size={18} />}
      </div>
      <div className="toast-content">
        <span className="toast-title">{toast.title || 'Demo Action'}</span>
        <span className="toast-message">{toast.message}</span>
      </div>
      <button
        type="button"
        className="toast-close-btn"
        onClick={onClose}
        aria-label="Close notification"
      >
        <CloseIcon size={14} />
      </button>
    </div>
  );
}
