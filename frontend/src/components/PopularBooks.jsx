import React from 'react';
import { BookmarkIcon } from './Icons';

export default function PopularBooks({ popularBooks, onInspectBook }) {
  return (
    <div className="card popular-books-card">
      <div className="card-header-flex">
        <div className="card-title-group">
          <h2 className="card-heading">Top Circulated Books</h2>
          <span className="card-subheading">Most requested textbooks this semester</span>
        </div>
        <span className="popular-badge">High Demand</span>
      </div>

      <div className="popular-books-list">
        {popularBooks.map((book, index) => {
          const availabilityPct = Math.round((book.availableCopies / book.totalCopies) * 100);
          const isLowStock = book.availableCopies <= 1;

          return (
            <div key={book.id} className="popular-book-item">
              <div className="popular-rank-badge">{index + 1}</div>

              <div className="popular-book-content">
                <div className="popular-title-row">
                  <h4 className="popular-book-title">{book.title}</h4>
                  <span className={`stock-pill ${isLowStock ? 'stock-low' : 'stock-ok'}`}>
                    {book.availableCopies} / {book.totalCopies} Available
                  </span>
                </div>

                <p className="popular-book-author">{book.author} • {book.edition}</p>

                <div className="popular-meta-row">
                  <span className="dept-tag">{book.department}</span>
                  <span className="shelf-location-tag">📍 {book.shelfLocation}</span>
                  <span className="borrows-count">🔄 {book.borrowCount} checkouts</span>
                </div>

                <div className="availability-bar-track">
                  <div
                    className={`availability-bar-fill ${isLowStock ? 'fill-low' : 'fill-ok'}`}
                    style={{ width: `${Math.max(availabilityPct, 12)}%` }}
                  />
                </div>
              </div>

              <div className="popular-action-col">
                <button
                  type="button"
                  className="quick-reserve-btn"
                  onClick={() => onInspectBook(book)}
                  title="View book details or hold copy"
                >
                  <BookmarkIcon size={14} />
                  <span>Inspect</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>

      <div className="card-footer-note">
        <span className="data-disclaimer-dot"></span>
        <span>Based on 30-day circulating volume (Sample College Data)</span>
      </div>
    </div>
  );
}
