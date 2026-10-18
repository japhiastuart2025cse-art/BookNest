import React, { useState } from 'react';
import { CloseIcon, BookIcon, IssueReturnIcon, FinesIcon, CheckCircleIcon, AlertCircleIcon, EditIcon, TrashIcon } from './Icons';

export default function ActionModal({
  modalType,
  modalData,
  books = [],
  members = [],
  onClose,
  onSubmitIssue,
  onSubmitAddBook,
  onSubmitEditBook,
  onSubmitDeleteBook,
  onSubmitCollectFine,
  onSubmitAddMember,
  onSwitchToEdit,
  onSwitchToDelete,
}) {
  // State for Quick Issue form (connected to Spring Boot POST /api/circulation/issue)
  const [selectedBookId, setSelectedBookId] = useState(() => {
    const presetBook = modalData?.bookId ?? modalData?.id ?? books[0]?.id ?? '';
    return presetBook ? String(presetBook) : '';
  });
  const [selectedMemberId, setSelectedMemberId] = useState(() => {
    const presetMember = modalData?.memberId ?? members[0]?.id ?? '';
    return presetMember ? String(presetMember) : '';
  });
  const [issueLoanDays, setIssueLoanDays] = useState('14');
  const [issueLoading, setIssueLoading] = useState(false);
  const [issueError, setIssueError] = useState(null);

  // State for Collect Fine form (connected to Spring Boot POST /api/fines/{id}/pay)
  const [finePaymentNote, setFinePaymentNote] = useState('Paid in cash at circulation desk');
  const [collectFineLoading, setCollectFineLoading] = useState(false);
  const [collectFineError, setCollectFineError] = useState(null);

  // State for Add Member form (connected to Spring Boot POST /api/members)
  const [memberForm, setMemberForm] = useState({
    name: '',
    email: '',
    phone: '',
    department: '',
    role: 'Student',
  });
  const [addMemberLoading, setAddMemberLoading] = useState(false);
  const [addMemberError, setAddMemberError] = useState(null);

  // State for Add Book form (connected to Spring Boot POST /api/books)

  const [bookForm, setBookForm] = useState({
    id: '',
    title: '',
    author: '',
    totalCopies: 5,
    availableCopies: '',
  });
  const [addBookLoading, setAddBookLoading] = useState(false);
  const [addBookError, setAddBookError] = useState(null);

  // State for Edit Book form (connected to Spring Boot PUT /api/books/{id})
  const [editForm, setEditForm] = useState(() => ({
    id: modalData?.id ?? '',
    title: modalData?.title ?? '',
    author: modalData?.author ?? '',
    totalCopies: modalData?.totalCopies ?? '',
    availableCopies: modalData?.availableCopies ?? '',
  }));
  const [editBookLoading, setEditBookLoading] = useState(false);
  const [editBookError, setEditBookError] = useState(null);

  // State for Delete Book confirmation
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  if (!modalType) return null;

  const handleIssueSubmit = async (e) => {
    e.preventDefault();
    setIssueError(null);

    const bId = parseInt(selectedBookId, 10);
    const mId = parseInt(selectedMemberId, 10);
    const days = parseInt(issueLoanDays, 10);

    if (isNaN(bId) || bId <= 0) {
      setIssueError('Please select a valid book from the catalogue.');
      return;
    }
    if (isNaN(mId) || mId <= 0) {
      setIssueError('Please select a valid registered member.');
      return;
    }

    try {
      setIssueLoading(true);
      await onSubmitIssue({
        bookId: bId,
        memberId: mId,
        loanDays: isNaN(days) || days <= 0 ? 14 : days,
      });
    } catch (err) {
      setIssueError(err.message || 'Failed to issue book.');
    } finally {
      setIssueLoading(false);
    }
  };

  const handleCollectFineSubmit = async (e) => {
    e.preventDefault();
    setCollectFineError(null);
    try {
      setCollectFineLoading(true);
      const fineId = modalData.fineId ?? modalData.id;
      await onSubmitCollectFine(fineId, finePaymentNote);
    } catch (err) {
      setCollectFineError(err.message || 'Failed to process fine payment.');
    } finally {
      setCollectFineLoading(false);
    }
  };

  const handleMemberSubmit = async (e) => {
    e.preventDefault();
    setAddMemberError(null);

    if (!memberForm.name || !memberForm.name.trim()) {
      setAddMemberError('Member name cannot be blank.');
      return;
    }
    if (!memberForm.email || !memberForm.email.trim()) {
      setAddMemberError('Member email cannot be blank.');
      return;
    }

    try {
      setAddMemberLoading(true);
      await onSubmitAddMember({
        name: memberForm.name.trim(),
        email: memberForm.email.trim(),
        phone: memberForm.phone ? memberForm.phone.trim() : null,
        department: memberForm.department ? memberForm.department.trim() : null,
        role: memberForm.role || 'Student',
        status: 'Active',
      });
      setMemberForm({ name: '', email: '', phone: '', department: '', role: 'Student' });
    } catch (err) {
      setAddMemberError(err.message || 'Failed to register member.');
    } finally {
      setAddMemberLoading(false);
    }
  };

  const handleBookSubmit = async (e) => {

    e.preventDefault();
    setAddBookError(null);

    const idNum = parseInt(bookForm.id, 10);
    const copiesNum = parseInt(bookForm.totalCopies, 10);

    if (isNaN(idNum) || idNum <= 0) {
      setAddBookError('Book ID must be a positive integer (e.g. 103).');
      return;
    }
    if (!bookForm.title || !bookForm.title.trim()) {
      setAddBookError('Book title cannot be blank.');
      return;
    }
    if (!bookForm.author || !bookForm.author.trim()) {
      setAddBookError('Book author cannot be blank.');
      return;
    }
    if (isNaN(copiesNum) || copiesNum <= 0) {
      setAddBookError('Total copies must be greater than zero.');
      return;
    }

    let availNum = copiesNum;
    if (bookForm.availableCopies !== undefined && bookForm.availableCopies !== '') {
      availNum = parseInt(bookForm.availableCopies, 10);
      if (isNaN(availNum) || availNum <= 0) {
        setAddBookError('Available copies must be greater than zero.');
        return;
      }
      if (availNum > copiesNum) {
        setAddBookError('Available copies cannot exceed total copies.');
        return;
      }
    }

    try {
      setAddBookLoading(true);
      await onSubmitAddBook({
        id: idNum,
        title: bookForm.title.trim(),
        author: bookForm.author.trim(),
        totalCopies: copiesNum,
        availableCopies: availNum,
      });
      setBookForm({ id: '', title: '', author: '', totalCopies: 5, availableCopies: '' });
    } catch (err) {
      setAddBookError(err.message || 'Failed to save book to backend.');
    } finally {
      setAddBookLoading(false);
    }
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditBookError(null);

    const copiesNum = parseInt(editForm.totalCopies, 10);
    const availNum = parseInt(editForm.availableCopies, 10);

    if (!editForm.title || !editForm.title.trim()) {
      setEditBookError('Book title cannot be blank.');
      return;
    }
    if (!editForm.author || !editForm.author.trim()) {
      setEditBookError('Book author cannot be blank.');
      return;
    }
    if (isNaN(copiesNum) || copiesNum <= 0) {
      setEditBookError('Total copies count must be greater than zero.');
      return;
    }
    if (isNaN(availNum) || availNum < 0) {
      setEditBookError('Available copies cannot be negative.');
      return;
    }
    if (availNum > copiesNum) {
      setEditBookError('Available copies cannot exceed total copies.');
      return;
    }

    try {
      setEditBookLoading(true);
      await onSubmitEditBook(modalData.id, {
        id: modalData.id,
        title: editForm.title.trim(),
        author: editForm.author.trim(),
        totalCopies: copiesNum,
        availableCopies: availNum,
      });
    } catch (err) {
      setEditBookError(err.message || 'Failed to update book.');
    } finally {
      setEditBookLoading(false);
    }
  };

  const handleDeleteSubmit = async (e) => {
    e.preventDefault();
    setDeleteError(null);
    try {
      setDeleteLoading(true);
      await onSubmitDeleteBook(modalData.id);
    } catch (err) {
      setDeleteError(err.message || 'Failed to delete book.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title-flex">
            {modalType === 'quick-issue' && <IssueReturnIcon size={22} className="modal-header-icon" />}
            {modalType === 'add-book' && <BookIcon size={22} className="modal-header-icon" />}
            {modalType === 'edit-book' && <EditIcon size={22} className="modal-header-icon" />}
            {modalType === 'delete-book' && <TrashIcon size={22} className="modal-header-icon" style={{ color: 'var(--rose-500)' }} />}
            {modalType === 'collect-fine' && <FinesIcon size={22} className="modal-header-icon" />}
            {modalType === 'inspect-book' && <BookIcon size={22} className="modal-header-icon" />}
            {modalType === 'add-member' && <IssueReturnIcon size={22} className="modal-header-icon" />}

            <div>
              <h3 className="modal-title">
                {modalType === 'quick-issue' && 'Issue Book to Student'}
                {modalType === 'add-book' && 'Add New Book to Inventory'}
                {modalType === 'edit-book' && 'Edit Book Details'}
                {modalType === 'delete-book' && 'Delete Book from Catalogue'}
                {modalType === 'collect-fine' && 'Process Fine Payment'}
                {modalType === 'inspect-book' && 'Textbook Information'}
                {modalType === 'add-member' && 'Register New Library Member'}
              </h3>
              <p className="modal-subtitle">
                {modalType === 'quick-issue' && 'Checkout registered book copy to an enrolled member'}
                {modalType === 'add-book' && 'Enter bibliographic data for campus stacks'}
                {modalType === 'edit-book' && 'Update bibliographic and stack inventory in MySQL'}
                {modalType === 'delete-book' && 'Confirm permanent removal from MySQL database'}
                {modalType === 'collect-fine' && 'Process overdue fine receipt in MySQL'}
                {modalType === 'inspect-book' && 'Live stack location and availability status'}
                {modalType === 'add-member' && 'Enroll a student or faculty account into MySQL'}
              </p>
            </div>
          </div>


          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            aria-label="Close dialog"
          >
            <CloseIcon size={20} />
          </button>
        </div>

        {/* Modal Body */}
        <div className="modal-body">
          {modalType === 'quick-issue' && (
            <form onSubmit={handleIssueSubmit} className="modal-form">
              {issueError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{issueError}</span>
                </div>
              )}

              <div className="form-group">
                <label htmlFor="issue-book-select" className="form-label">
                  Select Book to Issue <span className="text-rose-500">*</span>
                </label>
                <select
                  id="issue-book-select"
                  className="form-input"
                  value={selectedBookId}
                  onChange={(e) => setSelectedBookId(e.target.value)}
                  required
                >
                  <option value="">-- Choose Book from Catalogue --</option>
                  {books.map((b) => (
                    <option key={b.id} value={b.id} disabled={b.availableCopies <= 0}>
                      Book #{b.id}: {b.title} ({b.availableCopies} of {b.totalCopies} available) {b.availableCopies <= 0 ? '[OUT OF STOCK]' : ''}
                    </option>
                  ))}
                </select>
                <span className="form-field-hint">
                  Only books with at least 1 copy available can be issued.
                </span>
              </div>

              <div className="form-group">
                <label htmlFor="issue-member-select" className="form-label">
                  Select Registered Member <span className="text-rose-500">*</span>
                </label>
                {members && members.length > 0 ? (
                  <select
                    id="issue-member-select"
                    className="form-input"
                    value={selectedMemberId}
                    onChange={(e) => setSelectedMemberId(e.target.value)}
                    required
                  >
                    <option value="">-- Choose Member --</option>
                    {members.map((m) => (
                      <option key={m.id} value={m.id}>
                        Member #{m.id}: {m.name} ({m.role || 'Member'} - {m.department || m.email})
                      </option>
                    ))}
                  </select>
                ) : (
                  <input
                    id="issue-member-select"
                    type="number"
                    min="1"
                    className="form-input mono"
                    placeholder="Enter Member ID (e.g. 1)"
                    value={selectedMemberId}
                    onChange={(e) => setSelectedMemberId(e.target.value)}
                    required
                  />
                )}
                <span className="form-field-hint">
                  Borrower must be a registered library patron in active standing.
                </span>
              </div>

              <div className="form-group">
                <label htmlFor="issue-days" className="form-label">Loan Duration</label>
                <select
                  id="issue-days"
                  className="form-input"
                  value={issueLoanDays}
                  onChange={(e) => setIssueLoanDays(e.target.value)}
                >
                  <option value="14">14 Days (Standard Student Loan)</option>
                  <option value="30">30 Days (Extended Loan)</option>
                  <option value="60">60 Days (Faculty Privilege)</option>
                </select>
              </div>

              <div className="live-api-badge-box">
                <span className="live-pulse-dot"></span>
                <span>Target: <code>POST http://localhost:8080/api/circulation/issue</code> (Persisted in MySQL)</span>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={issueLoading}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={issueLoading}>
                  {issueLoading ? (
                    <span>Issuing Book...</span>
                  ) : (
                    <>
                      <CheckCircleIcon size={16} />
                      <span>Confirm Issue</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {modalType === 'add-member' && (
            <form onSubmit={handleMemberSubmit} className="modal-form">
              {addMemberError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{addMemberError}</span>
                </div>
              )}

              <div className="form-group">
                <label htmlFor="member-name" className="form-label">
                  Full Name <span className="text-rose-500">*</span>
                </label>
                <input
                  id="member-name"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Aarav Sharma"
                  value={memberForm.name}
                  onChange={(e) => setMemberForm({ ...memberForm, name: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="member-email" className="form-label">
                  Email Address <span className="text-rose-500">*</span>
                </label>
                <input
                  id="member-email"
                  type="email"
                  className="form-input"
                  placeholder="e.g. aarav@college.edu"
                  value={memberForm.email}
                  onChange={(e) => setMemberForm({ ...memberForm, email: e.target.value })}
                  required
                />
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor="member-phone" className="form-label">Phone Number</label>
                  <input
                    id="member-phone"
                    type="text"
                    className="form-input"
                    placeholder="e.g. 9876543210"
                    value={memberForm.phone}
                    onChange={(e) => setMemberForm({ ...memberForm, phone: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="member-dept" className="form-label">Department</label>
                  <input
                    id="member-dept"
                    type="text"
                    className="form-input"
                    placeholder="e.g. Computer Science"
                    value={memberForm.department}
                    onChange={(e) => setMemberForm({ ...memberForm, department: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="member-role" className="form-label">Member Role</label>
                <select
                  id="member-role"
                  className="form-input"
                  value={memberForm.role}
                  onChange={(e) => setMemberForm({ ...memberForm, role: e.target.value })}
                >
                  <option value="Student">Student</option>
                  <option value="Faculty">Faculty & Staff</option>
                </select>
              </div>

              <div className="live-api-badge-box">
                <span className="live-pulse-dot"></span>
                <span>Target: <code>POST http://localhost:8080/api/members</code> (Persisted in MySQL)</span>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={addMemberLoading}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={addMemberLoading}>
                  {addMemberLoading ? (
                    <span>Registering Member...</span>
                  ) : (
                    <>
                      <CheckCircleIcon size={16} />
                      <span>Register Member</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {modalType === 'add-book' && (
            <form onSubmit={handleBookSubmit} className="modal-form">

              {addBookError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{addBookError}</span>
                </div>
              )}

              <div className="form-group">
                <label htmlFor="book-id" className="form-label">
                  Book ID (Unique Number) <span className="text-rose-500">*</span>
                </label>
                <input
                  id="book-id"
                  type="number"
                  min="1"
                  step="1"
                  className="form-input mono"
                  placeholder="e.g. 103"
                  value={bookForm.id}
                  onChange={(e) => setBookForm({ ...bookForm, id: e.target.value })}
                  required
                />
                <span className="form-field-hint">Primary key in MySQL books table. Duplicate IDs will be rejected.</span>
              </div>

              <div className="form-group">
                <label htmlFor="book-title" className="form-label">
                  Book Title <span className="text-rose-500">*</span>
                </label>
                <input
                  id="book-title"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Database System Concepts"
                  value={bookForm.title}
                  onChange={(e) => setBookForm({ ...bookForm, title: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="book-author" className="form-label">
                  Author(s) <span className="text-rose-500">*</span>
                </label>
                <input
                  id="book-author"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Abraham Silberschatz, Henry F. Korth"
                  value={bookForm.author}
                  onChange={(e) => setBookForm({ ...bookForm, author: e.target.value })}
                  required
                />
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor="book-copies" className="form-label">
                    Total Copies <span className="text-rose-500">*</span>
                  </label>
                  <input
                    id="book-copies"
                    type="number"
                    min="1"
                    className="form-input"
                    placeholder="e.g. 5"
                    value={bookForm.totalCopies}
                    onChange={(e) => setBookForm({ ...bookForm, totalCopies: e.target.value })}
                    required
                  />
                  <span className="form-field-hint">Total holding inventory</span>
                </div>

                <div className="form-group">
                  <label htmlFor="book-avail-copies" className="form-label">
                    Available Copies <span className="text-muted">(Optional)</span>
                  </label>
                  <input
                    id="book-avail-copies"
                    type="number"
                    min="1"
                    className="form-input"
                    placeholder="Defaults to total"
                    value={bookForm.availableCopies}
                    onChange={(e) => setBookForm({ ...bookForm, availableCopies: e.target.value })}
                  />
                  <span className="form-field-hint">Initial shelf availability</span>
                </div>
              </div>

              <div className="live-api-badge-box">
                <span className="live-pulse-dot"></span>
                <span>Target: <code>POST http://localhost:8080/api/books</code> (Persisted in MySQL)</span>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={addBookLoading}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={addBookLoading}>
                  {addBookLoading ? (
                    <span>Saving to Database...</span>
                  ) : (
                    <>
                      <CheckCircleIcon size={16} />
                      <span>Add Book to Catalog</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {modalType === 'edit-book' && (
            <form onSubmit={handleEditSubmit} className="modal-form">
              {editBookError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{editBookError}</span>
                </div>
              )}

              <div className="form-group">
                <label htmlFor="edit-book-id" className="form-label">
                  Book ID (MySQL Primary Key)
                </label>
                <input
                  id="edit-book-id"
                  type="text"
                  className="form-input mono"
                  value={`#${editForm.id}`}
                  disabled
                  readOnly
                />
                <span className="form-field-hint">Book ID is immutable and cannot be changed.</span>
              </div>

              <div className="form-group">
                <label htmlFor="edit-book-title" className="form-label">
                  Book Title <span className="text-rose-500">*</span>
                </label>
                <input
                  id="edit-book-title"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Database System Concepts"
                  value={editForm.title}
                  onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
                  disabled={editBookLoading}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="edit-book-author" className="form-label">
                  Author(s) <span className="text-rose-500">*</span>
                </label>
                <input
                  id="edit-book-author"
                  type="text"
                  className="form-input"
                  placeholder="e.g. Abraham Silberschatz, Henry F. Korth"
                  value={editForm.author}
                  onChange={(e) => setEditForm({ ...editForm, author: e.target.value })}
                  disabled={editBookLoading}
                  required
                />
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor="edit-book-copies" className="form-label">
                    Total Copies <span className="text-rose-500">*</span>
                  </label>
                  <input
                    id="edit-book-copies"
                    type="number"
                    min="1"
                    className="form-input"
                    placeholder="e.g. 5"
                    value={editForm.totalCopies}
                    onChange={(e) => setEditForm({ ...editForm, totalCopies: e.target.value })}
                    disabled={editBookLoading}
                    required
                  />
                  <span className="form-field-hint">Total holdings inventory</span>
                </div>

                <div className="form-group">
                  <label htmlFor="edit-book-avail-copies" className="form-label">
                    Available Copies <span className="text-rose-500">*</span>
                  </label>
                  <input
                    id="edit-book-avail-copies"
                    type="number"
                    min="0"
                    max={editForm.totalCopies || undefined}
                    className="form-input"
                    placeholder="e.g. 3"
                    value={editForm.availableCopies}
                    onChange={(e) => setEditForm({ ...editForm, availableCopies: e.target.value })}
                    disabled={editBookLoading}
                    required
                  />
                  <span className="form-field-hint">Must not exceed total copies</span>
                </div>
              </div>

              <div className="live-api-badge-box">
                <span className="live-pulse-dot"></span>
                <span>Target: <code>PUT http://localhost:8080/api/books/{editForm.id}</code> (Persisted in MySQL)</span>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={editBookLoading}>
                  Cancel
                </button>
                <button type="submit" id="save-edit-book-btn" className="btn btn-primary" disabled={editBookLoading}>
                  {editBookLoading ? (
                    <span>Saving Changes...</span>
                  ) : (
                    <>
                      <CheckCircleIcon size={16} />
                      <span>Save Changes</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          )}

          {modalType === 'inspect-book' && modalData && (
            <div className="inspect-details">
              <div className="inspect-header-card">
                <div className="inspect-book-id-badge">Book #{modalData.id}</div>
                <h4 className="inspect-book-title">{modalData.title}</h4>
                <p className="inspect-book-author">By {modalData.author}</p>
                <div className="inspect-badges">
                  <span className={`status-badge ${
                    modalData.availableCopies === 0 ? 'badge-red' : modalData.availableCopies <= 2 ? 'badge-amber' : 'badge-green'
                  }`}>
                    {modalData.availableCopies === 0 ? 'Out of Stock' : modalData.availableCopies <= 2 ? 'Limited Copies' : 'Available'}
                  </span>
                </div>
              </div>

              <div className="inspect-grid">
                <div className="inspect-item">
                  <span className="inspect-label">Database ID</span>
                  <span className="inspect-val mono font-semibold">#{modalData.id}</span>
                </div>
                <div className="inspect-item">
                  <span className="inspect-label">Available Copies</span>
                  <span className="inspect-val font-semibold text-blue">
                    {modalData.availableCopies} of {modalData.totalCopies} copies
                  </span>
                </div>
                <div className="inspect-item">
                  <span className="inspect-label">Total Inventory</span>
                  <span className="inspect-val font-semibold">{modalData.totalCopies} Copies</span>
                </div>
                <div className="inspect-item">
                  <span className="inspect-label">Storage Backend</span>
                  <span className="inspect-val text-green font-semibold">MySQL books table</span>
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                  Close
                </button>
                {onSwitchToDelete && (
                  <button
                    type="button"
                    className="btn btn-danger-ghost"
                    onClick={() => onSwitchToDelete(modalData)}
                    title="Delete this book"
                  >
                    <TrashIcon size={16} />
                    <span>Delete</span>
                  </button>
                )}
                {onSwitchToEdit && (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => onSwitchToEdit(modalData)}
                  >
                    <EditIcon size={16} />
                    <span>Edit Book</span>
                  </button>
                )}
              </div>
            </div>
          )}

          {modalType === 'delete-book' && modalData && (
            <div className="inspect-details">
              {deleteError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{deleteError}</span>
                </div>
              )}

              <div className="delete-confirm-card">
                <div className="delete-badge">Book #{modalData.id}</div>
                <h4 className="inspect-book-title">{modalData.title}</h4>
                <p className="inspect-book-author">By {modalData.author}</p>
                <div className="inspect-grid" style={{ marginTop: '12px' }}>
                  <div className="inspect-item">
                    <span className="inspect-label">Available Copies</span>
                    <span className="inspect-val font-semibold">{modalData.availableCopies}</span>
                  </div>
                  <div className="inspect-item">
                    <span className="inspect-label">Total Inventory</span>
                    <span className="inspect-val font-semibold">{modalData.totalCopies}</span>
                  </div>
                </div>
              </div>

              <div className="delete-warning-banner" role="alert">
                <AlertCircleIcon size={20} className="warning-icon" />
                <div className="warning-content">
                  <strong>Are you sure you want to delete this book?</strong>
                  <p>This action cannot be undone. The book record will be permanently deleted from MySQL.</p>
                </div>
              </div>

              <div className="live-api-badge-box">
                <span className="live-pulse-dot" style={{ background: 'var(--rose-500)' }}></span>
                <span>Target: <code>DELETE http://localhost:8080/api/books/{modalData.id}</code> (MySQL)</span>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={onClose}
                  disabled={deleteLoading}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  id="confirm-delete-book-btn"
                  className="btn btn-danger"
                  onClick={handleDeleteSubmit}
                  disabled={deleteLoading}
                >
                  {deleteLoading ? (
                    <>
                      <span className="btn-spinner"></span>
                      <span>Deleting Book...</span>
                    </>
                  ) : (
                    <>
                      <TrashIcon size={16} />
                      <span>Delete Book</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}

          {modalType === 'collect-fine' && modalData && (
            <form onSubmit={handleCollectFineSubmit} className="modal-form">
              {collectFineError && (
                <div className="form-error-alert" role="alert">
                  <AlertCircleIcon size={16} />
                  <span>{collectFineError}</span>
                </div>
              )}

              <div className="inspect-details">
                <div className="fine-payment-card">
                  <div className="fine-amount-display">
                    <span className="currency-sign">₹</span>
                    <span className="fine-digits">{Number(modalData.amount ?? modalData.totalFine ?? 0).toFixed(2)}</span>
                  </div>
                  <p className="fine-subtext">
                    Overdue fine for {modalData.daysOverdue} days (₹5.00 / day rate)
                  </p>
                </div>

                <div className="inspect-grid">
                  <div className="inspect-item">
                    <span className="inspect-label">Borrower</span>
                    <span className="inspect-val font-semibold">
                      {modalData.memberName || modalData.studentName}
                    </span>
                  </div>
                  <div className="inspect-item">
                    <span className="inspect-label">Fine ID</span>
                    <span className="inspect-val mono">
                      {(modalData.fineId ?? modalData.id) < 0 ? 'Active Overdue' : `#FINE-${modalData.fineId ?? modalData.id}`}
                    </span>
                  </div>
                  <div className="inspect-item full-span">
                    <span className="inspect-label">Book Title</span>
                    <span className="inspect-val">{modalData.bookTitle}</span>
                  </div>
                </div>

                <div className="form-group" style={{ marginTop: '16px' }}>
                  <label htmlFor="fine-payment-note" className="form-label">Payment Receipt Note</label>
                  <input
                    id="fine-payment-note"
                    type="text"
                    className="form-input"
                    placeholder="e.g. Paid in cash at circulation desk"
                    value={finePaymentNote}
                    onChange={(e) => setFinePaymentNote(e.target.value)}
                  />
                  <span className="form-field-hint">Recorded in MySQL fines table as payment note.</span>
                </div>

                <div className="live-api-badge-box">
                  <span className="live-pulse-dot" style={{ background: 'var(--amber-500)' }}></span>
                  <span>Target: <code>POST http://localhost:8080/api/fines/{"{id}"}/pay</code> (Persisted in MySQL)</span>
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose} disabled={collectFineLoading}>
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary btn-success"
                    disabled={collectFineLoading}
                  >
                    {collectFineLoading ? (
                      <span>Recording Payment...</span>
                    ) : (
                      <>
                        <CheckCircleIcon size={16} />
                        <span>Collect ₹{Number(modalData.amount ?? modalData.totalFine ?? 0).toFixed(2)} & Clear Due</span>
                      </>
                    )}
                  </button>
                </div>
              </div>
            </form>
          )}

        </div>
      </div>
    </div>
  );
}
